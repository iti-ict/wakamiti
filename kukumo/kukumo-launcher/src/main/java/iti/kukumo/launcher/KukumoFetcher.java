package iti.kukumo.launcher;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.commons.configurer.ConfigurationException;
import iti.commons.maven.fetcher.MavenFetchRequest;
import iti.commons.maven.fetcher.MavenFetcher;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * @author ITI
 * Created by ITI on 8/04/19
 */
public class KukumoFetcher {

	private final ConfigurationBuilder confBuilder = ConfigurationBuilder.instance();
	private final Arguments arguments;


	public KukumoFetcher(Arguments arguments) {
	    this.arguments = arguments;
    }
	

    public List<Path> fetch() {
        try  {

            AppDirs appDirs = AppDirsFactory.getInstance();

            addModulesFromConfigFile(arguments);
            if (arguments.modules().isEmpty()) {
                KukumoLauncher.logger().info("Nothing to fetch");
                return Collections.emptyList();
            }

            Path mavenRepo = Paths.get(appDirs.getUserDataDir("kukumo", "repository", "iti"));
            Files.createDirectories(mavenRepo);
            Configuration conf = arguments.mavenFetcherConfiguration().appendProperty("localRepository",mavenRepo.toString());

            KukumoLauncher.logger().info("Fetching dependencies...");
            MavenFetcher mavenFetcher = new MavenFetcher()
                .logger(KukumoLauncher.logger())
                .config(conf.asProperties());
            MavenFetchRequest fetchRequest = new MavenFetchRequest(arguments.modules())
                    .scopes("compile","provided")
                    .retrieveOptionals(false);
            mavenFetcher.fetchArtifacts(fetchRequest);
            return mavenFetcher.resolveLocalArtifacts(fetchRequest);
        } catch (Exception e) {
            KukumoLauncher.logger().error("Error fetching dependencies: {}", e.getLocalizedMessage());
            KukumoLauncher.logger().debug("<error>",e);
            throw new RuntimeException(e);
        }
    }



    private void addModulesFromConfigFile(Arguments arguments) throws ConfigurationException {
        Configuration conf = arguments.kukumoConfiguration();
        arguments.modules().addAll(conf.getList("launcher.modules",String.class));
    }

    
}
