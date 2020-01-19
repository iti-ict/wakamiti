/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class KukumoLauncher {


    private static Logger logger;

    static Logger logger() {
        return logger;
    }

    public static void main(final String[] args) {

        CliArguments arguments = new CliArguments();
        try {
            arguments.parse(args);
            if (arguments.isHelpActive()) {
                arguments.printUsage();
                return;
            }
        } catch (ParseException e) {
            arguments.printUsage();
            System.exit(1);
        }

        boolean debugMode = arguments.isDebugActive();
        logger = createLogger(debugMode);
        if (logger.isDebugEnabled()) {
            logger.debug("{}",arguments);
        }

        try {
            List<Path> fetchedArtifacts = new KukumoFetcher(arguments).fetch();
            updateClasspath(fetchedArtifacts);
            new KukumoRunner(arguments).run();
        } catch (Exception e) {
            logger.error("Error: {}", e.toString(), e);
            System.exit(2);
        }
    }


    private static Logger createLogger(boolean debug) {
        if (debug) {
            Configurator.setLevel("iti.kukumo", Level.DEBUG);
        }
        return LoggerFactory.getLogger("iti.kukumo");
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
