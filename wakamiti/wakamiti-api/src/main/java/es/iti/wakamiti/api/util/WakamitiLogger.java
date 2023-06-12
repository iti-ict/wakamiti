/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.WakamitiAPI;
import imconfig.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slf4jansi.AnsiLogger;

import static es.iti.wakamiti.api.WakamitiConfiguration.LOGS_ANSI_ENABLED;
import static es.iti.wakamiti.api.WakamitiConfiguration.LOGS_ANSI_STYLES;


public class WakamitiLogger {

    public static String logo() {
        return "\n"+
            "██╗    ██╗ █████╗ ██╗  ██╗ █████╗ ███╗   ███╗██╗████████╗██╗\n"+
            "██║    ██║██╔══██╗██║ ██╔╝██╔══██╗████╗ ████║██║╚══██╔══╝██║\n"+
            "██║ █╗ ██║███████║█████╔╝ ███████║██╔████╔██║██║   ██║   ██║\n"+
            "██║███╗██║██╔══██║██╔═██╗ ██╔══██║██║╚██╔╝██║██║   ██║   ██║\n"+
            "╚███╔███╔╝██║  ██║██║  ██╗██║  ██║██║ ╚═╝ ██║██║   ██║   ██║\n"+
            " ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝   ╚═╝   ╚═╝  "+ WakamitiAPI.instance().version()+"\n";
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