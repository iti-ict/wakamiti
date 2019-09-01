package iti.kukumo.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.aether.collection.DependencyCollectionException;
import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.maven.fetcher.MavenFetchRequest;
import iti.commons.maven.fetcher.MavenFetchResult;
import iti.commons.maven.fetcher.MavenFetchResult.FetchedArtifact;
import iti.commons.maven.fetcher.MavenFetcher;
import iti.kukumo.api.KukumoException;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

/**
 * @author ITI
 * Created by ITI on 8/04/19
 */
public class KukumoFetcher {

    private final Arguments arguments;


    public KukumoFetcher(Arguments arguments) {
        this.arguments = arguments;
    }


    public List<Path> fetch() {

        Logger logger = KukumoLauncher.logger();

        try  {

            AppDirs appDirs = AppDirsFactory.getInstance();

            addModulesFromConfigFile(arguments);
            if (arguments.modules().isEmpty()) {
                KukumoLauncher.logger().info("Nothing to fetch");
                return Collections.emptyList();
            }

            Path mavenRepo = Paths.get(appDirs.getUserDataDir("kukumo", "repository", "iti"));
            Files.createDirectories(mavenRepo);
            Configuration conf = arguments
                .mavenFetcherConfiguration()
                .appendProperty("localRepository",mavenRepo.toString());

            logger.info("Fetching dependencies...");
            MavenFetcher mavenFetcher = new MavenFetcher()
                .logger(logger)
                .config(conf.asProperties());
            MavenFetchRequest fetchRequest = new MavenFetchRequest(arguments.modules())
                    .scopes("compile","provided")
                    .retrieveOptionals(false);
            MavenFetchResult fetchedArtifacts = mavenFetcher.fetchArtifacts(fetchRequest);

            if (logger.isDebugEnabled()) {
                logger.debug("{}",fetchedArtifacts);
            }
            return fetchedArtifacts
                .allDepedencies()
                .map(FetchedArtifact::path)
                .collect(Collectors.toList());

        } catch (RuntimeException | DependencyCollectionException | IOException e) {
            logger.error("Error fetching dependencies: {}", e.getLocalizedMessage());
            logger.debug("<error>",e);
            throw new KukumoException(e);
        }
    }



    private void addModulesFromConfigFile(Arguments arguments) {
        Configuration conf = arguments.kukumoConfiguration();
        arguments.modules().addAll(conf.getList("launcher.modules",String.class));
    }


}
