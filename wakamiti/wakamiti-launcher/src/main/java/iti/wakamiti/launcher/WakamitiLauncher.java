/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.launcher;


import imconfig.Configuration;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


public class WakamitiLauncher {


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

            if (arguments.isSshowContributionsEnabled()) {
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
        String loggerName = "iti.wakamiti";
        Optional<Level> level = conf.get("level", String.class).map(Level::toLevel);
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
        level.ifPresent(l -> Configurator.setLevel(loggerName, l)); //NOSONAR

        return LoggerFactory.getLogger(loggerName);
    }


}