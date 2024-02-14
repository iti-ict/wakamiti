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


public final class LogUtils {

    private static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.database");

    private LogUtils() {

    }

    public static void traceSQL(String sql) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[SQL] {sql} ", normalize(sql));
        }
    }

    public static void traceSQL(String sql, String... values) {
        if (LOGGER.isTraceEnabled()) {
            traceSQL(Stream.of(values).reduce(sql, (v1, v2) ->
                    v1.replaceFirst("\\?", v2 == null ? "null" : String.format("'%s'", v2))));
        }
    }

    public static void traceResultRow(Object row) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[SQL] Returning row: {}",
                    row.getClass().isArray() ? Arrays.deepToString((Object[]) row) : row);
        }
    }

    public static void debugRows(int count) {
        LOGGER.debug("[SQL] Executed SQL script; {} rows affected", count);
    }

    public static String normalize(String sql) {
        String unquoted = unquotedRegex("%s+");
        return sql.replaceAll(String.format(unquoted, System.lineSeparator()), " ")
                .replaceAll(String.format(unquoted, "\\s+"), " "); // replace unquoted spaces
    }

    public static void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }

    public static String message(String message, Object... args) {
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }
}
