/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.kukumo.api.KukumoException;



public class KukumoLauncher {

    private static Logger logger;


    public static Logger logger() {
        return logger;
    }


    public static void main(final String[] args) {

        boolean debugMode = Arrays.asList(args).contains("-debug");
        logger = createLogger(debugMode);
        if (logger.isDebugEnabled()) {
            logger.debug("arguments = {}", Arrays.toString(args));
        }
        checkJavaVersion();
        Arguments arguments = null;

        try {
            Path localConfigFile = jarFolder().resolve("launcher.properties");
            arguments = new Arguments(localConfigFile, args);
        } catch (IllegalArgumentException e) {
            printUsageAndExit();
        } catch (Exception e) {
            logger.error("Fatal error: {}", e.getLocalizedMessage(), e);
            System.exit(-1);
        }
        try {
            List<Path> fetchedArtifacts = new KukumoFetcher(arguments).fetch();
            updateClasspath(fetchedArtifacts);
            new KukumoVerifier(arguments).verify();
        } catch (KukumoException e) {
            logger.error(e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Fatal error: {}", e.getLocalizedMessage(), e);
            System.exit(-1);
        }
    }


    private static Logger createLogger(boolean debug) {
        if (debug) {
            Configurator.setLevel("iti.kukumo", Level.DEBUG);
        }
        return LoggerFactory.getLogger("iti.kukumo");
    }


    private static void checkJavaVersion() {
        if (getJavaVersion() < 8) {
            logger.error("Java must be version 8 or newer");
            System.exit(-1);
        }
    }


    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        // Allow these formats:
        // 1.8.0_72-ea
        // 9-ea
        // 9
        // 9.0.1
        int dotPos = version.indexOf('.');
        int dashPos = version.indexOf('-');
        return Integer.parseInt(
            version.substring(
                0,
                dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : 1
            )
        );
    }


    public static Path jarFolder() throws URISyntaxException {
        return Paths.get(
            KukumoLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()
                .getPath()
        ).getParent();
    }


    private static void printUsageAndExit() {
        StringBuilder string = new StringBuilder("usage:  kukumo <parameters>\n")
            .append("parameters:\n")

            .append("\t-modules <module1> [<module2>, ...]\n")
            .append(
                "\t\t Kukumo modules to use (specified as Maven packages <groupId:artifactId:version>)\n"
            )

            .append("\t-conf <configuration-file>\n")
            .append("\t\t Path of the configuration file used (by default ./kukumo.yaml)\n")
            .append("\t\t Kukumo properties from external files must have the prefix 'kukumo.'\n")
            .append(
                "\t\t Maven fetcher properties from external files must have the prefix 'mavenFetcher.'\n"
            )
            .append("\t\t Accepted formats are: *.properties, *.xml, *.json, *.yaml\n")

            .append("\t-K<property>=<value>\n")
            .append("\t\tSpecific Kukumo properties passed directly\n")

            .append("\t-M<property>=value\n")
            .append("\t\tSpecific Maven fetcher properties passed directly\n");
        logger.error("{}", string);
        System.exit(-1);
    }


    private static void updateClasspath(List<Path> artifacts) {
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
