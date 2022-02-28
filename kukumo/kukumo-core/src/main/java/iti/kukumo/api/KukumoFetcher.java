/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.maven.fetcher.FetchedArtifact;
import iti.commons.maven.fetcher.MavenFetchException;
import iti.commons.maven.fetcher.MavenFetchRequest;
import iti.commons.maven.fetcher.MavenFetchResult;
import iti.commons.maven.fetcher.MavenFetcher;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import static org.apache.commons.io.FileUtils.listFiles;


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

//            if (updateClasspath) {
                mavenFetcher.updateClasspath(paths);
//            }
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

    public List<Path> loadGroovyClasses() throws URISyntaxException {
        logger.info("Fetching groovy files...");
        List<Path> groovyPaths = listGroovyPaths(conf);

        if (groovyPaths.isEmpty()) {
            logger.debug("No groovy classes to load");
            return Collections.emptyList();
        } else {
            groovyPaths.forEach(path -> logger.debug("Groovy file [{}] found", path.getFileName()));
        }

        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        groovyPaths.forEach(p -> groovyClassLoader.addClasspath(p.getParent().toString()));
        Thread.currentThread().setContextClassLoader(groovyClassLoader);

        groovyPaths.forEach(path -> {
            try {
                Class cls = groovyClassLoader.parseClass(path.toFile());
                logger.debug("Parsed class [{}]", cls.getName());
            } catch (IOException e) {
                logger.error("Cannot parse file [{}]", path, e);
            }
        });

        return groovyPaths;
    }

    private List<Path> listGroovyPaths(Configuration conf) {
        return conf.getList(KukumoConfiguration.RESOURCE_PATH, String.class).stream()
                .map(File::new)
                .flatMap(file -> listFiles(file, new String[]{"groovy"}, true).stream()
                        .map(File::toURI))
                .map(Path::of)
                .collect(Collectors.toList());
    }

}
