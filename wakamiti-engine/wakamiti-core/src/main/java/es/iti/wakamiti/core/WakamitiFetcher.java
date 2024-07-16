/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core;


import es.iti.wakamiti.api.ClasspathAgent;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.imconfig.Configuration;
import maven.fetcher.FetchedArtifact;
import maven.fetcher.MavenFetchRequest;
import maven.fetcher.MavenFetchResult;
import maven.fetcher.MavenFetcher;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class provides functionality for fetching Maven dependencies.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiFetcher {

    private final Logger logger;
    private final Configuration conf;


    public WakamitiFetcher() {
        this(Wakamiti.LOGGER, Wakamiti.defaultConfiguration());
    }

    protected WakamitiFetcher(Logger logger, Configuration mavenFetcherConfiguration) {
        this.logger = logger;
        this.conf = mavenFetcherConfiguration;
    }

    /**
     * Creates a new instance of WakamitiFetcher with the specified configuration.
     *
     * @param mavenFetcherConfiguration The configuration to be used by the new fetcher.
     * @return A new WakamitiFetcher instance with the specified configuration.
     */
    public WakamitiFetcher withConfiguration(Configuration mavenFetcherConfiguration) {
        return new WakamitiFetcher(logger, mavenFetcherConfiguration);
    }

    /**
     * Fetches Maven dependencies for the specified modules.
     *
     * @param modules   The list of modules to fetch.
     * @param mustClean Flag indicating whether to clean the local Maven repository before fetching.
     * @return A list of paths to the fetched artifacts.
     * @throws WakamitiException If an error occurs during the fetching process.
     */
    public List<Path> fetch(List<String> modules, boolean mustClean) {
        try {

            if (modules.isEmpty()) {
                logger.info("Nothing to fetch");
                return Collections.emptyList();
            }

            AppDirs appDirs = AppDirsFactory.getInstance();
            Path mavenRepo = Paths.get(appDirs.getUserDataDir("wakamiti", "repository", "iti"));
            if (mustClean) {
                cleanCache(mavenRepo);
            }
            Files.createDirectories(mavenRepo);
            logger.debug("Using local Maven repository {}", mavenRepo);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Modules requested to fetch are:{}",
                        modules.stream().collect(Collectors.joining("\n -", "\n -", ""))
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
            throw new WakamitiException(e);
        }
    }

    /**
     * Cleans the local Maven repository cache.
     *
     * @param mavenRepo     The path to the local Maven repository.
     * @throws IOException  If an I/O error occurs during the cache cleaning process.
     */
    private void cleanCache(Path mavenRepo) throws IOException {
        try (Stream<Path> walker = Files.walk(mavenRepo)) {
            walker
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Updates the classpath with the specified artifacts.
     *
     * @param artifacts     The list of paths to the fetched artifacts.
     */
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