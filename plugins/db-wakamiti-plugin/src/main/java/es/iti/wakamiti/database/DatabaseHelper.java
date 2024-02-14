/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.database.exception.SQLRuntimeException;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;


public final class DatabaseHelper {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static <T> T[] concat(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public static boolean isDateOrDateTime(String str) {
        return Stream.of(DATE_TIME_FORMATTER, DATE_FORMATTER).anyMatch(formatter -> {
            try {
                formatter.parse(str);
                return true;
            } catch (RuntimeException parseEx) {
                return false;
            }
        });
    }

    public static boolean isDate(String str) {
        try {
            DATE_FORMATTER.parse(str);
            return true;
        } catch (RuntimeException parseEx) {
            return false;
        }
    }

    public static boolean isDateTime(String str) {
        try {
            DATE_TIME_FORMATTER.parse(str);
            return true;
        } catch (RuntimeException parseEx) {
            return false;
        }
    }

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

    public static Map<String,String> formatToMap(ResultSet rs) {
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

    public static <K,V> Pair<List<K>, List<V>> toPair(Map<K, V> map) {
        return new Pair<>(new LinkedList<>(map.keySet()), new LinkedList<>(map.values()));
    }

    public static <K,V> Map<K, V> toMap(K[] columns, V[] values) {
        if (columns.length != values.length) {
            throw new WakamitiException("Keys and values must have the same length");
        }
        Map<K, V> result = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            result.put(columns[i], values[i]);
        }
        return result;
    }

    public static <T> String[] toString(T[] array) {
        return Stream.of(array).map(DatabaseHelper::toString).toArray(String[]::new);
    }

    public static String toString(Object o) {
        return o == null ? null : Objects.toString(o);
    }

    public static String unquotedRegex(String unquoted) {
        return unquoted + "(?=([^']*'[^']*')*[^']*$)";
    }

    public static <T, K, U> Collector<T, ?, Map<K,U>> collectToMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper
    ) {
        return Collector.of(
                LinkedHashMap::new,
                (m, e) -> m.put(keyMapper.apply(e), valueMapper.apply(e)),
                (m, r) -> m
        );
    }

    public static <K, U> Collector<Map.Entry<K,U>, ?, Map<K,U>> collectToMap() {
        return Collector.of(
                LinkedHashMap::new,
                (m, e) -> m.put(e.getKey(), e.getValue()),
                (m, r) -> m
        );
    }

}
