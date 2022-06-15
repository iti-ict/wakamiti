/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher;


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

        if (arguments.isSshowContributionsEnabled()) {
            logger().info("The available contributions are the following:");
            logger().info("------------------------------------");
            logger().info(new KukumoRunner(arguments).getContributions());
            logger().info("------------------------------------");
        }


        try {
            new KukumoLauncherFetcher(arguments).fetchAndUpdateClasspath();
            new KukumoRunner(arguments).run();
        } catch (Exception e) {
            logger.error("Error: {}", e.toString());
            if (logger.isDebugEnabled()) {
                logger.error("<exception stack trace>",e);
            }
            System.exit(2);
        }
    }


    private static Logger createLogger(boolean debug) {
        if (debug) {
            Configurator.setLevel("iti.kukumo", Level.DEBUG);
        }
        return LoggerFactory.getLogger("iti.kukumo");
    }







}