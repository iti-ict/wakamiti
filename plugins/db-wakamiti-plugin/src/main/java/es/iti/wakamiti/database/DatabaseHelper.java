/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.database.dataset.DataSet;
import es.iti.wakamiti.database.exception.SQLRuntimeException;

import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;


/**
 * Provides methods for working with databases and result sets.
 */
public final class DatabaseHelper {

    /**
     * The date time formatter used for formatting timestamps with milliseconds.
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * The date formatter used for formatting dates without time.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DatabaseHelper() {
        // prevents instantiation
    }

    /**
     * Checks if a string represents a date or date time.
     *
     * @param str The string to check
     * @return {@code true} if the string is a date or date time, {@code false} otherwise
     */
    public static boolean isDateOrDateTime(String str) {
        return Stream.of(DATE_TIME_FORMATTER, DATE_TIME_FORMATTER_2, DATE_FORMATTER).anyMatch(formatter -> {
            try {
                formatter.parse(str);
                return true;
            } catch (RuntimeException parseEx) {
                return false;
            }
        });
    }

    /**
     * Checks if a string represents a date.
     *
     * @param str The string to check
     * @return {@code true} if the string is a date, {@code false} otherwise
     */
    public static boolean isDate(String str) {
        try {
            DATE_FORMATTER.parse(str);
            return true;
        } catch (RuntimeException parseEx) {
            return false;
        }
    }

    /**
     * Formats a result set row into an array of strings.
     *
     * @param rs The result set to format
     * @return The formatted row as an array of strings
     * @throws SQLRuntimeException If an SQL exception occurs
     */
    public static String[] format(ResultSet rs) {
        try {
            ResultSetMetaData metadata = rs.getMetaData();
            String[] row = new String[metadata.getColumnCount()];
            for (int c = 1; c <= metadata.getColumnCount(); c++) {
                switch (JDBCType.valueOf(metadata.getColumnType(c))) {
                    case DATE:
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTimeZone(TimeZone.getDefault());
                        calendar1.setLenient(true);
                        Timestamp timestamp1 = rs.getTimestamp(c, calendar1);
                        if (rs.wasNull()) {
                            row[c - 1] = null;
                            break;
                        }
                        row[c - 1] = DATE_FORMATTER.format(timestamp1.toLocalDateTime());
                        break;
                    case TIMESTAMP:
                    case TIME:
                    case TIME_WITH_TIMEZONE:
                    case TIMESTAMP_WITH_TIMEZONE:
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getDefault());
                        calendar.setLenient(true);
                        Timestamp timestamp = rs.getTimestamp(c, calendar);
                        if (rs.wasNull()) {
                            row[c - 1] = null;
                            break;
                        }
                        row[c - 1] = DATE_TIME_FORMATTER.format(timestamp.toLocalDateTime());
                        break;

                    default:
                        String value = rs.getString(c);
                        if (rs.wasNull()) {
                            row[c - 1] = null;
                            continue;
                        }
                        row[c - 1] = value;
                }
            }
            return row;
        } catch (SQLException e) {
            throw new SQLRuntimeException("Cannot read result set", e);
        }
    }

    /**
     * Formats a result set row into a map of column names to values.
     *
     * @param rs The result set to format
     * @return The formatted row as a map of column names to values
     * @throws SQLRuntimeException If an SQL exception occurs
     */
    public static Map<String, String> formatToMap(ResultSet rs) {
        try {
            ResultSetMetaData metadata = rs.getMetaData();
            Map<String, String> row = new LinkedHashMap<>();
            for (int c = 1; c <= metadata.getColumnCount(); c++) {
                String column = metadata.getColumnName(c);
                switch (JDBCType.valueOf(metadata.getColumnType(c))) {
                    case DATE:
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTimeZone(TimeZone.getDefault());
                        calendar1.setLenient(true);
                        Timestamp timestamp1 = rs.getTimestamp(c, calendar1);
                        if (rs.wasNull()) {
                            row.put(column, null);
                            break;
                        }
                        row.put(column, DATE_FORMATTER.format(timestamp1.toLocalDateTime()));
                        break;
                    case TIMESTAMP:
                    case TIME:
                    case TIME_WITH_TIMEZONE:
                    case TIMESTAMP_WITH_TIMEZONE:
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getDefault());
                        calendar.setLenient(true);
                        Timestamp timestamp = rs.getTimestamp(c, calendar);
                        if (rs.wasNull()) {
                            row.put(column, null);
                            break;
                        }
                        row.put(column, DATE_TIME_FORMATTER.format(timestamp.toLocalDateTime()));
                        break;

                    default:
                        String value = rs.getString(c);
                        if (rs.wasNull()) {
                            row.put(column, null);
                            continue;
                        }
                        row.put(column, value);
                }
            }
            return row;
        } catch (SQLException e) {
            throw new SQLRuntimeException("Cannot read result set", e);
        }
    }

    /**
     * Converts a map to a pair of lists.
     *
     * @param map The map to convert
     * @param <K> The type of keys
     * @param <V> The type of values
     * @return A pair of lists containing keys and values from the map
     */
    public static <K, V> Pair<List<K>, List<V>> toPair(Map<K, V> map) {
        return new Pair<>(new LinkedList<>(map.keySet()), new LinkedList<>(map.values()));
    }

    /**
     * Converts arrays of keys and values into a map.
     *
     * @param columns The array of keys
     * @param values  The array of values
     * @param <K>     The type of keys
     * @param <V>     The type of values
     * @return A map containing the keys and values from the arrays
     * @throws WakamitiException If the arrays have different lengths
     */
    public static <K, V> Map<K, V> toMap(K[] columns, V[] values) {
        if (columns.length != values.length) {
            throw new WakamitiException("Keys and values must have the same length");
        }
        Map<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            result.put(columns[i], values[i]);
        }
        return result;
    }

    /**
     * Converts an array of objects to an array of strings using their {@code toString()} method.
     *
     * @param array The array of objects to convert
     * @param <T>   The type of objects in the array
     * @return An array of strings representing the objects
     */
    public static <T> String[] toString(T[] array) {
        return Stream.of(array).map(DatabaseHelper::toString).toArray(String[]::new);
    }

    /**
     * Converts an object to a string using its {@link Object#toString()} method.
     *
     * @param o The object to convert
     * @return The string representation of the object, or {@code null} if the object is {@code null}
     */
    public static String toString(Object o) {
        return o == null ? null : Objects.toString(o);
    }

    /**
     * Generates a regular expression for matching an unquoted string in an SQL query.
     *
     * @param unquoted The unquoted string
     * @return The regular expression for matching the unquoted string
     */
    public static String unquotedRegex(String unquoted) {
        return unquoted + "(?=([^']*'[^']*')*[^']*$)";
    }

    /**
     * Collects key-value pairs into a map.
     *
     * @param <K> The type of keys
     * @param <U> The type of values
     * @return A collector that accumulates key-value pairs into a map
     */
    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> collectToMap() {
        return Collector.of(
                LinkedHashMap::new,
                (m, e) -> m.put(e.getKey(), e.getValue()),
                (m, r) -> m
        );
    }

    public static List<Map<String, Object>> read(DataSet dataSet) {
        List<Map<String, Object>> results = new LinkedList<>();
        while (dataSet.nextRow()) {
            results.add(dataSet.rowAsMap());
        }
        try {
            dataSet.close();
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
        return results;
    }

}
