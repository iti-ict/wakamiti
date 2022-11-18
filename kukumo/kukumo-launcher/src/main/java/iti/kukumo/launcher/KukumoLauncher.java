/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher;


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
        try {
            logger = createLogger(arguments.kukumoConfiguration().inner("log"), debugMode);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(2);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{}", arguments);
        }

        if (arguments.isSshowContributionsEnabled()) {
            logger().info("The available contributions are the following:");
            logger().info("------------------------------------");
            logger().info(new KukumoRunner(arguments).getContributions());
            logger().info("------------------------------------");
        }


        try {
            new KukumoLauncherFetcher(arguments).fetchAndUpdateClasspath();
            boolean passed = new KukumoRunner(arguments).run();
            if (!passed)
                System.exit(1);
        } catch (Exception e) {
            logger.error("Error: {}", e.toString());
            if (logger.isDebugEnabled()) {
                logger.error("<exception stack trace>", e);
            }
            System.exit(2);
        }
    }

    private static Logger createLogger(Configuration conf, boolean debug) {
        String loggerName = "iti.kukumo";
        Optional<String> level = conf.get("level", String.class);
        Optional<String> path = conf.get("path", String.class);

        if (path.isPresent()) {
            String filename = path.get() + "/kukumo-"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss")) + ".log";
            System.setProperty("path", filename);
            System.setProperty("log4j.configurationFile", "log4j2_file.xml");
        }
        if (level.isEmpty()) {
            if (debug) {
                level = Optional.of("DEBUG");
            } else {
                level = Optional.of("INFO");
            }
        }
        level.map(Level::toLevel).ifPresent(l -> Configurator.setLevel(loggerName, l));
        return LoggerFactory.getLogger(loggerName);
    }


}