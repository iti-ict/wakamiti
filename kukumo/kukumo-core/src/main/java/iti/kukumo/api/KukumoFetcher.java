/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import groovy.lang.GroovyClassLoader;
import imconfig.Configuration;
import maven.fetcher.*;
import org.slf4j.Logger;


import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

//import static org.apache.commons.io.FileUtils.listFiles;


public class KukumoFetcher {

    private Logger logger;
    private Configuration conf;


    public KukumoFetcher() {
        this(Kukumo.LOGGER, Kukumo.defaultConfiguration());
    }

    protected KukumoFetcher(Logger logger, Configuration mavenFetcherConfiguration) {
        this.logger = logger;
        this.conf = mavenFetcherConfiguration;
    }


    public KukumoFetcher withConfiguration(Configuration mavenFetcherConfiguration) {
        return new KukumoFetcher(logger,mavenFetcherConfiguration);
    }


    public List<Path> fetch(List<String> modules, boolean mustClean) {
        try {

            if (modules.isEmpty()) {
                logger.info("Nothing to fetch");
                return Collections.emptyList();
            }

            AppDirs appDirs = AppDirsFactory.getInstance();
            Path mavenRepo = Paths.get(appDirs.getUserDataDir("kukumo", "repository", "iti"));
            if (mustClean) {
                cleanCache(mavenRepo);
            }
            Files.createDirectories(mavenRepo);
            logger.debug("Using local Maven repository {}",mavenRepo);

            logger.info("Fetching dependencies...");
            MavenFetcher mavenFetcher = new MavenFetcher()
                .logger(logger)
                .config(conf.asProperties());

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Modules requested to fetch are:{}",
                    modules.stream().collect(Collectors.joining("\n -", "\n -",""))
                );
            }
            MavenFetchRequest fetchRequest = new MavenFetchRequest(modules)
                .scopes("compile", "provided");
            MavenFetchResult fetchedArtifacts = mavenFetcher.fetchArtifacts(fetchRequest);

            if (logger.isDebugEnabled()) {
                logger.debug("{}", fetchedArtifacts);
            }
            List<Path> paths = fetchedArtifacts
                .allArtifacts()
                .map(FetchedArtifact::path)
                .collect(Collectors.toList());

            updateClasspath(paths);
            return paths;

        } catch (RuntimeException | IOException e) {
            logger.error("Error fetching dependencies");
            throw new KukumoException(e);
        }
    }

    private void cleanCache(Path mavenRepo) throws IOException {
        try (Stream<Path> walker = Files.walk(mavenRepo)) {
            walker
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    public List<Path> loadGroovyClasses() {
        logger.info("Fetching groovy files...");
        List<Path> groovyPaths = listGroovyPaths();

        if (groovyPaths.isEmpty()) {
            logger.debug("No groovy classes to load");
            return Collections.emptyList();
        } else {
            groovyPaths.forEach(path -> logger.debug("Groovy file [{}] found", path.getFileName()));
        }

        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        groovyPaths.stream().map(Path::getParent).map(Objects::toString).distinct()
                .forEach(groovyClassLoader::addClasspath);
        Thread.currentThread().setContextClassLoader(groovyClassLoader);

        groovyPaths.forEach(path -> {
            try {
                Class<?> cls = groovyClassLoader.parseClass(path.toFile());
                logger.debug("Parsed class [{}]", cls.getName());
            } catch (IOException e) {
                logger.error("Cannot parse file [{}]", path, e);
            }
        });

        return groovyPaths;
    }

    private List<Path> listGroovyPaths() {
        return conf.getList(KukumoConfiguration.RESOURCE_PATH, String.class).stream()
                .map(Paths::get)
                .flatMap(this::listFiles)
                .filter(file -> file.toFile().getName().endsWith(".groovy"))
                .collect(Collectors.toList());
    }

    private Stream<Path> listFiles(Path dir) {
        try {
            return Stream.concat(
                Files.list(dir).filter(Files::isDirectory).flatMap(this::listFiles),
                Files.list(dir).filter(Files::isRegularFile)
            );
        } catch (IOException e) {
            return null;
        }
    }


    private void updateClasspath(List<Path> artifacts) {
        for (Path artifact : artifacts) {
            if (artifact.toString().endsWith(".jar")) {
                if (!artifact.toFile().exists()) {
                    logger.warn(
                        "Cannot include JAR in the classpath (the file no exists): {}",
                        artifact
                    );
                    continue;
                }
                try {
                    JarFile jarFile = new JarFile(artifact.toFile());
                    ClasspathAgent.appendJarFile(jarFile);
                    logger.debug("Added JAR {} to the classpath", artifact);
                } catch (IOException e) {
                    logger.error("Cannot include JAR in the classpath: {}", artifact);
                    logger.debug(e.getMessage(), e);
                }
            }
        }
    }
}