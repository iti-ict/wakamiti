package iti.kukumo.server.infra;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.server.ExecutionCriteria;
import iti.kukumo.server.KukumoExecution;
import iti.kukumo.server.spi.ExecutionRepository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class FileBasedExecutionRepository implements ExecutionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedExecutionRepository.class);

    private static final Comparator<File> FILE_COMPARATOR = Comparator.comparing(File::lastModified);

    @Value("${kukumo.executions.path}")
    private String executionPath;


    @PostConstruct
    private void prepareFileSystem() {
        Path executionFolder = Path.of(executionPath);
        try {
            Files.createDirectories(executionFolder);
        } catch (IOException e) {
            LOGGER.error("Cannot create directory {} : {}", executionFolder, e.getMessage());
            LOGGER.debug("<caused by>",e);
        }
    }


    @Override
    public Optional<KukumoExecution> getExecution(String executionID) {
        return Optional
            .of(resultFile(executionID))
            .filter(Files::exists)
            .map(this::readFile);
    }


    @Override
    public boolean existsExecution(String executionID) {
        return Files.exists(resultFile(executionID));
    }

    @Override
    public List<KukumoExecution> getAllExecutions() {
        return findExecutions(x->true,0,Integer.MAX_VALUE);
    }

    @Override
    public List<String> getAllExecutionIDs() {
        return findExecutionIDs(x->true,0,Integer.MAX_VALUE);
    }

    @Override
    public List<KukumoExecution> getExecutions(ExecutionCriteria criteria) {
        return findExecutions(toPredicate(criteria),(criteria.getPage()-1)*criteria.getSize(),criteria.getSize());
    }

    @Override
    public List<String> getExecutionIDs(ExecutionCriteria criteria) {
        return findExecutionIDs(toPredicate(criteria),(criteria.getPage()-1)*criteria.getSize(),criteria.getSize());
    }


    @Override
    public void removeOldExecutions(int age) {
        var executionFolders = executionFiles()
            .filter(file -> lastModified(file).isBefore(LocalDateTime.now().minusDays(age)))
            .map(File::getParentFile)
            .collect(Collectors.toList());
        for (File executionFolder : executionFolders) {
            try {
                FileUtils.deleteDirectory(executionFolder);
            } catch (IOException e) {
                LOGGER.error("Error removing folder {} : {}", executionFolder, e.getMessage());
                LOGGER.debug("<caused by>",e);
            }
        }
    }



    private Predicate<File> toPredicate(ExecutionCriteria criteria) {
        Predicate<File> filter = x->true;
        if (criteria.getExecutionDate() != null) {
            filter = filter.and(file -> lastModified(file).toLocalDate().equals(criteria.getExecutionDate()));
        }
        if (criteria.getExecutionIntervalFrom() != null) {
            filter = filter.and(file -> lastModified(file).compareTo(criteria.getExecutionIntervalFrom()) >= 0);
        }
        if (criteria.getExecutionIntervalTo() != null) {
            filter = filter.and(file -> lastModified(file).compareTo(criteria.getExecutionIntervalTo()) <= 0);
        }
        return filter;
    }


    private Path resultFile(String executionID) {
        return Path.of(executionPath).resolve(executionID).resolve("kukumo.json");
    }


    private KukumoExecution readFile (Path file) {
        try {
            return new KukumoExecution(Kukumo.planSerializer().read(file));
        } catch (IOException e) {
            throw new KukumoException(e);
        }
    }



    private Stream<File> executionFiles() {
        try {
            return Files.walk(Path.of(executionPath))
                .map(Path::toFile)
                .filter(file -> file.getName().equals("kukumo.json"));
        } catch (IOException e) {
            throw new KukumoException(e);
        }
    }


    private Stream<File> filteredExecutionFiles(Predicate<File> predicate, int skip, int limit) {
        return executionFiles()
            .sorted(FILE_COMPARATOR)
            .skip(skip)
            .limit(limit);
    }


    
    private List<KukumoExecution> findExecutions(Predicate<File> predicate, int skip, int limit) {
        return filteredExecutionFiles(predicate,skip,limit)
            .map(File::toPath)
            .map(this::readFile)
            .collect(Collectors.toList());
    }


    private List<String> findExecutionIDs(Predicate<File> predicate, int skip, int limit) {
        return filteredExecutionFiles(predicate,skip,limit)
            .map(File::getParentFile)
            .map(File::getName)
            .collect(Collectors.toList());
    }



    private LocalDateTime lastModified(File file) {
        return Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
