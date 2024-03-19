/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;


/**
 * A data set that completes missing non-nullable columns with default values.
 */
public class CompletableDataSet extends DataSet {

    private final Map<String, Object> nonNullableColumns = new LinkedHashMap<>();
    private final Map<String, Integer> nonNullableColumnsWithType;
    private final DataSet wrapped;

    /**
     * Constructs a CompletableDataSet.
     *
     * @param wrapped                    The wrapped data set.
     * @param nonNullableColumnsWithType A map of non-nullable columns with their SQL types.
     */
    public CompletableDataSet(DataSet wrapped, Map<String, Integer> nonNullableColumnsWithType) {
        super(wrapped.table(), wrapped.origin(), wrapped.nullSymbol());
        this.wrapped = wrapped;
        this.nonNullableColumnsWithType = nonNullableColumnsWithType;
        List<String> originalColumnList = Arrays.asList(wrapped.columns());
        for (Entry<String, Integer> nonNullableColumnWithType : nonNullableColumnsWithType
                .entrySet()) {
            if (!containsIgnoringCase(originalColumnList, nonNullableColumnWithType.getKey())) {
                this.nonNullableColumns.put(
                        nonNullableColumnWithType.getKey(),
                        nonNullValueFor(nonNullableColumnWithType.getValue())
                );
            }
        }
        this.columns = Stream.concat(
                Stream.of(wrapped.columns()),
                nonNullableColumns.keySet().stream()
        ).toArray(String[]::new);
    }

    /**
     * Retrieves the non-nullable default value for a given SQL type.
     *
     * @param sqlType The SQL type.
     * @return The default value.
     */
    private static Object nonNullValueFor(Integer sqlType) {
        switch (sqlType) {
            case Types.BIT:
            case Types.BIGINT:
            case Types.BOOLEAN:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.DECIMAL:
            case Types.NUMERIC:
                return 1;
            case Types.CHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.VARCHAR:
                return "A";
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return Date.valueOf(LocalDate.of(2000, 1, 1));
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                return Time.valueOf(LocalTime.of(12, 0));
            default:
                return 0;
        }
    }

    /**
     * Retrieves the value of a specific column in the current row.
     *
     * @param columnIndex The index of the column.
     * @return The value of the column.
     */
    @Override
    public Object rowValue(int columnIndex) {
        Object value = getIgnoringCase(nonNullableColumns, columns[columnIndex]);
        return value == null ? wrapped.rowValue(columnIndex) : value;
    }

    private Object getIgnoringCase(Map<String, Object> map, String key) {
        return map.getOrDefault(key, map.get(key.toUpperCase()));
    }

    /**
     * Checks if the data set is empty.
     *
     * @return {@code true} if the data set is empty, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    /**
     * Moves to the next row in the data set.
     *
     * @return {@code true} if there is a next row, {@code false} otherwise.
     */
    @Override
    public boolean nextRow() {
        return wrapped.nextRow();
    }

    /**
     * Closes the data set.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        wrapped.close();
    }

    /**
     * Creates a copy of the data set.
     *
     * @return A copy of the data set.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public DataSet copy() throws IOException {
        return new CompletableDataSet(wrapped.copy(), nonNullableColumnsWithType);
    }

}