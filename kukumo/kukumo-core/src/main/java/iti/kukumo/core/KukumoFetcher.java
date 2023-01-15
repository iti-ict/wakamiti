/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import imconfig.Configuration;
import iti.kukumo.api.ClasspathAgent;
import iti.kukumo.api.KukumoException;
import maven.fetcher.*;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;


import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;


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

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Modules requested to fetch are:{}",
                    modules.stream().collect(Collectors.joining("\n -", "\n -",""))
                );
                logger.debug("Maven fetcher properties:");
                logger.debug("{}", conf);
            }

            logger.info("Fetching dependencies...");
            MavenFetcher mavenFetcher = new MavenFetcher().logger(NOPLogger.NOP_LOGGER);
            if (!conf.isEmpty()) {
                mavenFetcher.config(conf.asProperties());
            }
            mavenFetcher.logger(logger);

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