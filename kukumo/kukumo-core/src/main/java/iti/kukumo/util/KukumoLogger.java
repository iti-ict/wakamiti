package iti.kukumo.util;

import iti.commons.configurer.Configuration;
import iti.commons.slf4jjansi.AnsiLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static iti.kukumo.api.KukumoConfiguration.LOGS_ANSI_ENABLED;
import static iti.kukumo.api.KukumoConfiguration.LOGS_ANSI_STYLES;

/**
 * @author ITI
 * Created by ITI on 28/08/19
 */
public class KukumoLogger {


    public static String logo() {
        return
               "----------------------------------------------\n"+
               "    | |/ /   _| | ___   _ _ __ ___   ___\n" +
               "    | ' / | | | |/ / | | | '_ ` _ \\ / _ \\ \n" +
               "    | . \\ |_| |   <| |_| | | | | | | (_) | \n"+
               "    |_|\\_\\__,_|_|\\_\\\\__,_|_| |_| |_|\\___/\n"+
               "----------------------------------------------"
               ;
    }



    public static Logger forClass(Class<?> logger) {
        return of(LoggerFactory.getLogger(logger));
    }


    public static Logger of(Logger logger) {
        return AnsiLogger.of(logger);
    }

    public static void configure(Configuration configuration) {
        AnsiLogger.setAnsiEnabled(
            configuration.get(LOGS_ANSI_ENABLED,Boolean.class).orElse(true)
        );
        AnsiLogger.setStyles(
            configuration.inner(LOGS_ANSI_STYLES).asProperties()
        );
    }




}
