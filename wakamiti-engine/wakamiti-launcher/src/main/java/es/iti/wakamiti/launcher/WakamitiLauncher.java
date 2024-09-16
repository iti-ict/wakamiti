/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.launcher;


import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.core.generator.features.OpenAIService;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


/**
 * Main class for the WakamitiLauncher application.
 *
 * <p>This class contains the main method for launching Wakamiti. It parses command-line arguments,
 * initializes the logger, fetches and updates classpath, and runs the WakamitiRunner.</p>
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiLauncher {

    private static Logger logger;

    /**
     * Retrieves the logger instance.
     *
     * @return The logger instance.
     */
    static Logger logger() {
        return logger;
    }

    /**
     * The main entry point for the WakamitiLauncher application.
     *
     * @param args The command-line arguments.
     */
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

        if (arguments.isFeatureGeneratorEnabled()) {
            OpenAIService openAIService = new OpenAIService();
            FeatureGeneratorRunner featureGeneratorRunner = new FeatureGeneratorRunner(arguments, openAIService);
            featureGeneratorRunner.run();
            return;
        }

        boolean debugMode = arguments.isDebugActive();
        try {
            logger = createLogger(arguments.wakamitiConfiguration().inner("log"), debugMode);
        } catch (URISyntaxException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{}", arguments);
        }

        try {
            new WakamitiLauncherFetcher(arguments).fetchAndUpdateClasspath();
            WakamitiRunner runner = new WakamitiRunner(arguments);

            if (arguments.isShowContributionsEnabled()) {
                logger().info("The available contributions are the following:");
                logger().info("------------------------------------");
                logger().info(runner.getContributions());
                logger().info("------------------------------------");
            }

            boolean passed = runner.run();
            if (!passed)
                System.exit(3);
        } catch (Exception e) {
            logger.error("Error: {}", e.toString());
            if (logger.isDebugEnabled()) {
                logger.error("<exception stack trace>", e);
            }
            System.exit(2);
        }
    }

    private static Logger createLogger(Configuration conf, boolean debug) {
        String loggerName = "es.iti.wakamiti";
        Optional<Level> level = conf.get("level", String.class).map(String::toUpperCase).map(Level::toLevel);
        Optional<String> path = conf.get("path", String.class);

        if (path.isPresent()) {
            String filename = path.get() + "/wakamiti-"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss")) + ".log";
            System.setProperty("path", filename);
            System.setProperty("log4j.configurationFile", "log4j2_file.xml");
        }
        if (level.isEmpty()) {
            if (debug) {
                level = Optional.of(Level.DEBUG);
            } else {
                level = Optional.of(Level.INFO);
            }
        }

        level.ifPresent(l -> {
            Configurator.setLevel(loggerName, l);          //NOSONAR
//            Configurator.setLevel("es.iti.commons", l);    //NOSONAR
        });

        conf.inner("loggers").asProperties()
                .forEach((k, v) -> Configurator.setLevel(k.toString(), Level.toLevel(v.toString())));

        return LoggerFactory.getLogger(loggerName);
    }

}