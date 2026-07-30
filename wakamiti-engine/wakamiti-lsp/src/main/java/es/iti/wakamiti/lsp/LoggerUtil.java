/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility methods for working with Logger Util.
 */
public final class LoggerUtil {

    private static final String DASHES = " --------------------------------------------------- ";
    private static Map<String, Logger> loggers = new HashMap<>();

    private LoggerUtil() { /* avoid instantiation */ }

    static Logger get(
            String logger
    ) {
        return loggers.computeIfAbsent("es.iti.wakamiti.lsp." + logger, LoggerFactory::getLogger);
    }

    /**
     * Logs an incoming protocol value and returns that same value unchanged.
     * <p>
     * Returning the argument makes the method suitable for inline use in
     * request-processing pipelines.
     *
     * @param logger logical protocol operation appended to the LSP logger name
     * @param params request or event payload to log
     * @param <T> payload type
     * @return {@code params}
     */
    public static <T> T logEntry(
            String logger,
            T params
    ) {
        get(logger).debug("\n{}\n >> REQUEST {}\n{}\n{}\n{}", DASHES, logger, DASHES, params, DASHES);
        return params;
    }

    /**
     * Logs an outgoing protocol value and returns that same value unchanged.
     *
     * @param logger logical protocol operation appended to the LSP logger name
     * @param response response payload to log
     * @param <T> payload type
     * @return {@code response}
     */
    public static <T> T logExit(
            String logger,
            T response
    ) {
        get(logger).debug("\n{}\n << RESPONSE {}\n{}\n{}\n", DASHES, response, DASHES, DASHES);
        return response;
    }

}
