/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server.infra.persistence;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import es.iti.wakamiti.server.domain.model.ExecutionCriteria;
import es.iti.wakamiti.server.domain.model.WakamitiExecution;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.WakamitiException;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.*;

import es.iti.wakamiti.server.spi.ExecutionRepository;

@ApplicationScoped
public class FileBasedExecutionRepository implements ExecutionRepository {

    private static final String OUTPUT_FILE = "wakamiti.json";
	private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedExecutionRepository.class);

	private static final Comparator<File> FILE_COMPARATOR = Comparator
		.comparing(File::lastModified)
		.reversed();

    @ConfigProperty(name = "wakamiti.executions.path")
    Optional<String> executionPath;

    @PostConstruct
    void prepareExecutionPath() throws IOException {
    	if (executionPath.isEmpty()) {
    		executionPath = Optional.of(Files.createTempDirectory("wakamiti-executions").toString());
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
    public void saveExecution(WakamitiExecution execution) {
    	String executionID = execution.getData().getExecutionID();
    	String owner = execution.getOwner();
    	Path file = resultFile(owner, executionID);
    	writeFile(execution,file);
    	LOGGER.debug("Written file {}", file);
    }


    @Override
    public Optional<WakamitiExecution> getExecution(String owner, String executionID) {
        return Optional
            .of(resultFile(owner, executionID))
            .filter(Files::exists)
            .map(file -> readFile(file, owner));
    }


    @Override
    public boolean existsExecution(String owner, String executionID) {
        return Files.exists(resultFile(owner, executionID));
    }


    @Override
    public List<WakamitiExecution> getAllExecutions(String owner) {
        return findExecutions(owner,x->true,0,Integer.MAX_VALUE);
    }


    @Override
    public List<String> getAllExecutionIDs(String owner) {
        return findExecutionIDs(owner, x->true,0,Integer.MAX_VALUE);
    }


    @Override
    public List<WakamitiExecution> getExecutions(ExecutionCriteria criteria) {
        return findExecutions(
            criteria.getOwner(),
            toPredicate(criteria),
            (criteria.getPage()-1)*criteria.getSize(),
            criteria.getSize()
        );
    }


    @Override
    public List<String> getExecutionIDs(ExecutionCriteria criteria) {
        return findExecutionIDs(
            criteria.getOwner(),
            toPredicate(criteria),
            (criteria.getPage()-1)*criteria.getSize(),
            criteria.getSize()
        );
    }


    @Override
    public void removeOldExecutions(int age) {
        var executionRootFolder = executionPath.map(Path::of).orElseThrow();
        try (Stream<Path> allFiles = Files.walk(executionRootFolder)) {
            allFiles
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(file -> FileUtils.isFileOlder(file, LocalDateTime.now().minusDays(age)))
                .forEach(File::delete);
        } catch (IOException e) {
            LOGGER.error("Error removing old executions : {}",  e.getMessage());
            LOGGER.debug(e.toString(),e);
        }
        try (Stream<Path> allFiles = Files.walk(executionRootFolder)) {
            allFiles
                .map(Path::toFile)
                .filter(File::isDirectory)
                .filter(folder -> folder.list().length == 0)
                .forEach(File::delete);
        } catch (IOException e) {
            LOGGER.error("Error removing empty folders : {}",  e.getMessage());
            LOGGER.debug(e.toString(),e);
        }
    }


    @Override
    public Instant prepareExecution(String owner, String executionID) {
    	try {
    		var dir = Files.createDirectory(executionPath(owner).resolve(executionID));
    		return Files.getLastModifiedTime(dir).toInstant();
    	} catch (IOException e) {
            throw new WakamitiException(e);
        }
    }


    private Predicate<File> toPredicate(ExecutionCriteria criteria) {
        Predicate<File> filter = x->true;
        if (criteria.getExecutionDate() != null) {
            filter = filter.and(
        		file -> lastModified(file).toLocalDate().equals(criteria.getExecutionDate())
    		);
        }
        if (criteria.getExecutionIntervalFrom() != null) {
            filter = filter.and(
        		file -> lastModified(file).compareTo(criteria.getExecutionIntervalFrom()) >= 0
    		);
        }
        if (criteria.getExecutionIntervalTo() != null) {
            filter = filter.and(
        		file -> lastModified(file).compareTo(criteria.getExecutionIntervalTo()) <= 0
    		);
        }
        return filter;
    }




    private Path resultFile(String owner, String executionID) {
        try {
            return executionPath(owner).resolve(executionID).resolve(OUTPUT_FILE);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }



    private WakamitiExecution readFile (Path file, String owner) {
        try {
            return WakamitiExecution.fromSnapshot(Wakamiti.planSerializer().read(file), owner);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }


    private Optional<WakamitiExecution> readOutputFileInExecutionFolder (Path folder, String owner) {
        try {
            return Optional.of( WakamitiExecution.fromSnapshot(
                Wakamiti.planSerializer().read(folder.resolve(OUTPUT_FILE)),
                owner
            ));
        } catch (IOException e) {
            return Optional.empty();
        }
    }


    private void writeFile (WakamitiExecution execution, Path file) {
        try {
        	Files.createDirectories(file.getParent());
            Wakamiti.planSerializer().write(Files.newBufferedWriter(file),execution.getData());
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }


    private Stream<File> executionFolders(String owner) {
        try {
            return Files.walk(executionPath(owner), 1).map(Path::toFile);
        } catch (IOException e) {
            throw new WakamitiException(e.toString(),e);
        }
    }




    private Stream<File> filteredExecutionFolders(String owner, Predicate<File> predicate, int skip, int limit) {
        return executionFolders(owner)
        		.filter(predicate)
                .sorted(FILE_COMPARATOR)
                .skip(skip)
                .limit(limit);
    }



    private List<WakamitiExecution> findExecutions(String owner, Predicate<File> predicate, int skip, int limit) {
        return filteredExecutionFolders(owner, predicate,skip,limit)
            .map(File::toPath)
            .map(path -> readOutputFileInExecutionFolder(path, owner))
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
    }


    private List<String> findExecutionIDs(String owner, Predicate<File> predicate, int skip, int limit) {
        return filteredExecutionFolders(owner,predicate,skip,limit)
            .map(File::getName)
            .collect(Collectors.toList());
    }



    private LocalDateTime lastModified(File file) {
        return Instant
    		.ofEpochMilli(file.lastModified())
    		.atZone(ZoneId.systemDefault())
    		.toLocalDateTime();
    }



    private Path executionPath(String owner) throws IOException {
        var userExecutionPath =
            Path.of(executionPath.orElseThrow())
                .resolve("users")
                .resolve(owner)
                .resolve("executions");

        if (!Files.exists(userExecutionPath)) {
            Files.createDirectories(userExecutionPath);
        }

        return userExecutionPath;
    }


}