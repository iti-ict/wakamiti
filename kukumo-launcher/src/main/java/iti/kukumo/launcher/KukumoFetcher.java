package iti.kukumo.launcher;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.commons.configurer.ConfigurationException;
import iti.commons.maven.fetcher.MavenFetcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * @author ITI
 * Created by ITI on 8/04/19
 */
public class KukumoFetcher {


    public void fetch(Arguments arguments) {
        try  {

            addModulesFromConfigFile(arguments);

            if (arguments.modules().isEmpty()) {
                KukumoLauncher.logger().info("Nothing to fetch");
                return;
            }
            Path mavenRepo = Paths.get(".kukumo/mvn-repo");
            Path libModules = Paths.get(".kukumo/lib");
            arguments.mavenFetcherProperties().put("localRepository",mavenRepo.toString());

            KukumoLauncher.logger().info("Fetching dependencies...");
            Configuration conf = new ConfigurationBuilder().buildFromMap(arguments.mavenFetcherProperties());

            if (arguments.confFile().isPresent()) {
                conf = conf.appendFromPath(arguments.confFile().get(),"mavenFetcher");
            }
            
            if (arguments.mustClean()) {
                deleteRecursive(mavenRepo);
                deleteRecursive(libModules);
            }
            
            new MavenFetcher()
                    .logger(KukumoLauncher.logger())
                    .config(conf.asProperties())
                    .fetchArtifacts(arguments.modules(), Arrays.asList("compile","provided"),false);

            KukumoLauncher.logger().info("Copying jar files to lib-modules...");
            if (!libModules.toFile().exists()) {
                libModules.toFile().mkdirs();
            }
            Files.walkFileTree(mavenRepo,new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path target = libModules.resolve(file.getFileName());
                    if (file.toString().endsWith(".jar") && !target.toFile().exists()) {
                        Files.copy(file,target);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (Exception e) {
            KukumoLauncher.logger().error("Error fetching dependencies: {}", e.getLocalizedMessage());
            KukumoLauncher.logger().debug("<error>",e);
            throw new RuntimeException(e);
        }
    }



    private void addModulesFromConfigFile(Arguments arguments) throws ConfigurationException {
        File confFile = new File(arguments.confFile().orElse("kukumo.yaml"));
        if (confFile.exists()) {
            Configuration conf = new ConfigurationBuilder().buildFromPath(confFile.getPath());
            arguments.modules().addAll(conf.getStringList("kukumo.launcher.modules"));
        }
    }
    

    
    private void deleteRecursive(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
    
}
