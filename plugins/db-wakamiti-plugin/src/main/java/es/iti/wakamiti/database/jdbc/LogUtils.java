/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.stream.Stream;

import static es.iti.wakamiti.database.DatabaseHelper.unquotedRegex;


/**
 * Provides utility methods for logging SQL queries and results.
 */
public final class LogUtils {

    private static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.database");

    /**
     * Private constructor to prevent instantiation.
     */
    private LogUtils() {

    }

    /**
     * Logs a SQL query at trace level.
     *
     * @param sql The SQL query to log
     */
    public static void traceSQL(Object sql) {
        if (sql != null && LOGGER.isTraceEnabled()) {
            LOGGER.trace("[SQL] {sql} ", normalize(sql.toString()));
        }
    }

    /**
     * Logs a SQL query with parameter values at trace level.
     *
     * @param sql    The SQL query string
     * @param values The parameter values
     */
    public static void traceSQL(Object sql, String... values) {
        if (sql != null && LOGGER.isTraceEnabled()) {
            traceSQL(Stream.of(values).reduce(sql.toString(), (v1, v2) ->
                    v1.replaceFirst("\\?", v2 == null ? "null" : String.format("'%s'", v2))));
        }
    }

    /**
     * Logs a result row at trace level.
     *
     * @param row The result row object
     */
    public static void traceResultRow(Object row) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[SQL] Returning row: {}",
                    row.getClass().isArray() ? Arrays.deepToString((Object[]) row) : row);
        }
    }

    /**
     * Logs the number of rows affected by an SQL operation at debug level.
     *
     * @param count The number of rows affected
     */
    public static void debugRows(int count) {
        LOGGER.debug("[SQL] Executed SQL script; {} rows affected", count);
    }

    /**
     * Normalizes an SQL query string for logging.
     *
     * @param sql The SQL query string to normalize
     * @return The normalized SQL query string
     */
    public static String normalize(String sql) {
        String unquoted = unquotedRegex("%s+");
        return sql.replaceAll(String.format(unquoted, System.lineSeparator()), " ")
                .replaceAll(String.format(unquoted, "\\s+"), " "); // replace unquoted spaces
    }

    /**
     * Logs a warning message.
     *
     * @param message The warning message
     * @param args    The arguments to format the message
     */
    public static void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }

    /**
     * Formats a message with arguments.
     *
     * @param message The message format string
     * @param args    The arguments to format the message
     * @return The formatted message
     */
    public static String message(String message, Object... args) {
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }

}
