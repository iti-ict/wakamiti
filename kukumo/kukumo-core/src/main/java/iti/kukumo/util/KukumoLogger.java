/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.util;


import static iti.kukumo.api.KukumoConfiguration.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.commons.configurer.Configuration;
import iti.commons.slf4jansi.AnsiLogger;



public class KukumoLogger {

    public static String logo() {
        return "\n" +
        "----------------------------------------------\n" +
        "    | |/ /   _| | ___   _ _ __ ___   ___\n" +
        "    | ' / | | | |/ / | | | '_ ` _ \\ / _ \\ \n" +
        "    | . \\ |_| |   <| |_| | | | | | | (_) | \n" +
        "    |_|\\_\\__,_|_|\\_\\\\__,_|_| |_| |_|\\___/\n" +
        "----------------------------------------------";
    }


    public static Logger forClass(Class<?> logger) {
        return of(LoggerFactory.getLogger(logger));
    }


    public static Logger of(Logger logger) {
        return AnsiLogger.of(logger);
    }


    public static void configure(Configuration configuration) {
        AnsiLogger.setAnsiEnabled(
            configuration.get(LOGS_ANSI_ENABLED, Boolean.class).orElse(true)
        );
        AnsiLogger.setStyles(
            configuration.inner(LOGS_ANSI_STYLES).asProperties()
        );
    }

}