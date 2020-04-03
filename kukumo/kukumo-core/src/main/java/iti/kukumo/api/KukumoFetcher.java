/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.maven.fetcher.FetchedArtifact;
import iti.commons.maven.fetcher.MavenFetchException;
import iti.commons.maven.fetcher.MavenFetchRequest;
import iti.commons.maven.fetcher.MavenFetchResult;
import iti.commons.maven.fetcher.MavenFetcher;
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


    public List<Path> fetch(List<String> modules, boolean mustClean, boolean updateClasspath) {
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
                .scopes("compile", "provided")
                .retrieveOptionals(false);
            MavenFetchResult fetchedArtifacts = mavenFetcher.fetchArtifacts(fetchRequest);

            if (logger.isDebugEnabled()) {
                logger.debug("{}", fetchedArtifacts);
            }
            List<Path> paths = fetchedArtifacts
                .allArtifacts()
                .map(FetchedArtifact::path)
                .collect(Collectors.toList());

            if (updateClasspath) {
                mavenFetcher.updateClasspath(paths);
            }
            return paths;

        } catch (RuntimeException | MavenFetchException | IOException e) {
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


}
