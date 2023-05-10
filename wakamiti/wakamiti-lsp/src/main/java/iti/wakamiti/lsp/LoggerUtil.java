/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.lsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class LoggerUtil {

    private static final String DASHES = " --------------------------------------------------- ";
    private static Map<String, Logger> loggers = new HashMap<>();

    private LoggerUtil() { /* avoid instantiation */ }

    static Logger get(String logger) {
        return loggers.computeIfAbsent("iti.wakamiti.lsp." + logger, LoggerFactory::getLogger);
    }


    public static <T> T logEntry(String logger, T params) {
        get(logger).debug("\n{}\n >> REQUEST {}\n{}\n{}\n{}", DASHES, logger, DASHES, params, DASHES);
        return params;
    }


    public static <T> T logExit(String logger, T response) {
        get(logger).debug("\n{}\n << RESPONSE {}\n{}\n{}\n", DASHES, response, DASHES, DASHES);
        return response;
    }




}