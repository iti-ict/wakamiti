package iti.kukumo.server.infra.persistence;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.server.domain.model.ExecutionCriteria;
import iti.kukumo.server.domain.model.KukumoExecution;
import iti.kukumo.server.spi.ExecutionRepository;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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

@ApplicationScoped
public class FileBasedExecutionRepository implements ExecutionRepository {

    private static final String OUTPUT_FILE = "kukumo.json";
	private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedExecutionRepository.class);
    private static final Comparator<File> FILE_COMPARATOR = Comparator.comparing(File::lastModified).reversed();

    @ConfigProperty(name = "kukumo.executions.path")
    Optional<String> executionPath;


    @PostConstruct
    void prepareExecutionPath() throws IOException {
    	if (executionPath.isEmpty()) {
    		executionPath = Optional.of(Files.createTempDirectory("kukumo-executions").toString());
    	}
    	Path outputFilePath = Path.of(executionPath.orElseThrow());
    	if (Files.exists(outputFilePath)) {
    		Files.createDirectories(outputFilePath);
    	} else if (!Files.isDirectory(outputFilePath)) {
    		throw new IOException(outputFilePath+" is not a directory");
    	} else if (!Files.isWritable(outputFilePath)) {
    		throw new IOException(outputFilePath+" is not a writtable directory");
    	} else if (!Files.isReadable(outputFilePath)) {
    		throw new IOException(outputFilePath+" is not a redable directory");
    	}
    	LOGGER.info("Using {} as execution storage", executionPath);
    }


    @Override
    public void saveExecution(KukumoExecution execution) {
    	String executionID = execution.getData().getExecutionID();
    	Path file = resultFile(executionID);
    	writeFile(execution,file);
    	LOGGER.debug("Written file {}", file);
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
        var executionFolders = executionFolders()
                .filter(folder -> lastModified(folder).isBefore(LocalDateTime.now().minusDays(age)))
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


    @Override
    public Instant prepareExecution(String executionID) {
    	try {
    		var dir = Files.createDirectory(Path.of(executionPath.orElseThrow()).resolve(executionID));
    		return Files.getLastModifiedTime(dir).toInstant();
    	} catch (IOException e) {
            throw new KukumoException(e);
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
        return Path.of(executionPath.orElseThrow()).resolve(executionID).resolve(OUTPUT_FILE);
    }


    private KukumoExecution readFile (Path file) {
        try {
            return new KukumoExecution(Kukumo.planSerializer().read(file));
        } catch (IOException e) {
            throw new KukumoException(e);
        }
    }


    private Optional<KukumoExecution> readOutputFileInExecutionFolder (Path folder) {
        try {
            return Optional.of(
        		new KukumoExecution(Kukumo.planSerializer().read(folder.resolve(OUTPUT_FILE)))
    		);
        } catch (IOException e) {
            return Optional.empty();
        }
    }


    private void writeFile (KukumoExecution execution, Path file) {
        try {
        	Files.createDirectories(file.getParent());
            Kukumo.planSerializer().write(Files.newBufferedWriter(file),execution.getData());
        } catch (IOException e) {
            throw new KukumoException(e);
        }
    }


    private Stream<File> executionFolders() {
        try {
            return Files.walk(executionPath.map(Path::of).orElseThrow(), 1)
                .map(Path::toFile);
        } catch (IOException e) {
            throw new KukumoException(e);
        }
    }


    private Stream<File> filteredExecutionFolders(Predicate<File> predicate, int skip, int limit) {
        return executionFolders()
        		.filter(predicate)
                .sorted(FILE_COMPARATOR)
                .skip(skip)
                .limit(limit);
    }



    private List<KukumoExecution> findExecutions(Predicate<File> predicate, int skip, int limit) {
        return filteredExecutionFolders(predicate,skip,limit)
            .map(File::toPath)
            .map(this::readOutputFileInExecutionFolder)
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
    }


    private List<String> findExecutionIDs(Predicate<File> predicate, int skip, int limit) {
        return filteredExecutionFolders(predicate,skip,limit)
            .map(File::getName)
            .collect(Collectors.toList());
    }



    private LocalDateTime lastModified(File file) {
        return Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
