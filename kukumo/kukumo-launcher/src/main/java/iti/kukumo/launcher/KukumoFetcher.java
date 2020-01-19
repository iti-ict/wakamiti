/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import iti.kukumo.api.KukumoException;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;



public class KukumoFetcher {

    private final CliArguments arguments;


    public KukumoFetcher(CliArguments arguments) {
        this.arguments = arguments;
    }


    public List<Path> fetch() {
        Logger logger = KukumoLauncher.logger();
        try {

            List<String> modules = modulesToFetch();
            if (modules.isEmpty()) {
                KukumoLauncher.logger().info("Nothing to fetch");
                return Collections.emptyList();
            }

            AppDirs appDirs = AppDirsFactory.getInstance();
            Path mavenRepo = Paths.get(appDirs.getUserDataDir("kukumo", "repository", "iti"));
            if (arguments.mustClean()) {
                cleanCache(mavenRepo);
            }
            Files.createDirectories(mavenRepo);

            Configuration conf = arguments
                .mavenFetcherConfiguration()
                .appendProperty("localRepository", mavenRepo.toString());

            logger.info("Fetching dependencies...");
            MavenFetcher mavenFetcher = new MavenFetcher()
                .logger(logger)
                .config(conf.asProperties());
            MavenFetchRequest fetchRequest = new MavenFetchRequest(arguments.modules())
                .scopes("compile", "provided")
                .retrieveOptionals(false);
            MavenFetchResult fetchedArtifacts = mavenFetcher.fetchArtifacts(fetchRequest);

            if (logger.isDebugEnabled()) {
                logger.debug("{}", fetchedArtifacts);
            }
            return fetchedArtifacts
                .allDepedencies()
                .map(FetchedArtifact::path)
                .collect(Collectors.toList());

        } catch (RuntimeException | MavenFetchException | IOException | URISyntaxException e) {
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


    private List<String> modulesToFetch() throws URISyntaxException {
        Configuration conf = arguments.kukumoConfiguration();
        List<String> modules = new ArrayList<>();
        modules.addAll(arguments.modules());
        modules.addAll(conf.getList("launcher.modules", String.class));
        return modules;
    }

}
