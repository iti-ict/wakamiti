/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.WakamitiAPI;
import imconfig.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import static es.iti.wakamiti.api.WakamitiConfiguration.LOGS_ANSI_ENABLED;
import static es.iti.wakamiti.api.WakamitiConfiguration.LOGS_ANSI_STYLES;


/**
 * Utility class for configuring and obtaining SLF4J Logger instances
 * with AnsiLogger support.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiLogger {

    /**
     * Generates the Wakamiti logo.
     *
     * @return The Wakamiti logo as a string.
     */
    public static String logo() {
        return "\n" +
                "██╗    ██╗ █████╗ ██╗  ██╗ █████╗ ███╗   ███╗██╗████████╗██╗\n" +
                "██║    ██║██╔══██╗██║ ██╔╝██╔══██╗████╗ ████║██║╚══██╔══╝██║\n" +
                "██║ █╗ ██║███████║█████╔╝ ███████║██╔████╔██║██║   ██║   ██║\n" +
                "██║███╗██║██╔══██║██╔═██╗ ██╔══██║██║╚██╔╝██║██║   ██║   ██║\n" +
                "╚███╔███╔╝██║  ██║██║  ██╗██║  ██║██║ ╚═╝ ██║██║   ██║   ██║\n" +
                " ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝   ╚═╝   ╚═╝  " + WakamitiAPI.instance().version() + "\n";
    }

    /**
     * Retrieves a logger based on the given name.
     *
     * @param name The name of the logger.
     * @return The logger instance.
     */
    public static Logger forName(String name) {
        return of(LoggerFactory.getLogger(name));
    }

    /**
     * Retrieves a logger based on the given class.
     *
     * @param logger The class for which the logger is obtained.
     * @return The logger instance.
     */
    public static Logger forClass(Class<?> logger) {
        return of(LoggerFactory.getLogger(logger));
    }

    /**
     * Wraps an existing logger with AnsiLogger.
     *
     * @param logger The logger instance to wrap.
     * @return The wrapped logger with AnsiLogger support.
     */
    public static Logger of(Logger logger) {
        return AnsiLogger.of(logger);
    }

    /**
     * Configures AnsiLogger based on the provided configuration.
     *
     * @param configuration The configuration containing AnsiLogger settings.
     */
    public static void configure(Configuration configuration) {
        AnsiLogger.setAnsiEnabled(
                configuration.get(LOGS_ANSI_ENABLED, Boolean.class).orElse(true)
        );
        AnsiLogger.setStyles(
                configuration.inner(LOGS_ANSI_STYLES).asProperties()
        );
    }

}