/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Abstract class representing a data set.
 */
public abstract class DataSet implements Closeable {

    protected String table;
    protected String[] columns;
    protected String origin;
    protected String nullSymbol;

    /**
     * Constructs a DataSet object.
     *
     * @param table      The name of the table.
     * @param origin     The origin of the data set.
     * @param nullSymbol The {@code null} symbol used in the data set.
     */
    public DataSet(String table, String origin, String nullSymbol) {
        this.table = table;
        this.origin = origin;
        this.nullSymbol = nullSymbol;
    }

    /**
     * Checks if the list contains the given string, ignoring case.
     *
     * @param list   The list to search.
     * @param string The string to find.
     * @return {@code true} if the list contains the string-ignoring case, {@code false} otherwise.
     */
    protected static boolean containsIgnoringCase(List<String> list, String string) {
        for (String item : list) {
            if (item.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of columns in the data set.
     *
     * @return The number of columns.
     */
    public int columnNumbers() {
        return columns.length;
    }

    /**
     * Returns the name of the table.
     *
     * @return The table name.
     */
    public String table() {
        return table;
    }

    /**
     * Returns the origin of the data set.
     *
     * @return The data set origin.
     */
    public String origin() {
        return origin;
    }

    /**
     * Returns the {@code null} symbol used in the data set.
     *
     * @return The {@code null} symbol.
     */
    public String nullSymbol() {
        return nullSymbol;
    }

    /**
     * Collects the columns into a single string using the specified delimiter.
     *
     * @param delimiter The delimiter to use.
     * @return A string containing all columns separated by the delimiter.
     */
    public String collectColumns(CharSequence delimiter) {
        return String.join(delimiter, columns);
    }

    /**
     * Collects the columns into a single string using the specified delimiter and mapper.
     *
     * @param columnMapper The mapper to apply to each column.
     * @param delimiter    The delimiter to use.
     * @return A string containing all mapped columns separated by the delimiter.
     */
    public String collectColumns(UnaryOperator<String> columnMapper, CharSequence delimiter) {
        return Stream.of(columns).map(columnMapper).collect(Collectors.joining(delimiter));
    }

    /**
     * Retrieves the column name at the specified index.
     *
     * @param index The index of the column.
     * @return The name of the column at the specified index.
     */
    public String column(int index) {
        return columns[index];
    }

    /**
     * Retrieves an array containing all column names.
     *
     * @return An array containing all column names.
     */
    public String[] columns() {
        return columns;
    }

    /**
     * Retrieves an array containing all row values.
     *
     * @return An array containing all row values.
     */
    public Object[] values() {
        return IntStream.range(0, columns.length).mapToObj(this::rowValue).toArray();
    }

    /**
     * Retrieves the row as a map, with column names as keys and row values as values.
     *
     * @return A map representing the row.
     */
    public Map<String, Object> rowAsMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            map.put(columns[i], rowValue(i));
        }
        return map;
    }

    /**
     * Retrieves the index of the specified column name.
     *
     * @param columnName The name of the column.
     * @return The index of the column, or {@code -1} if not found.
     */
    public int columnIndex(String columnName) {
        for (int index = 0; index < columns.length; index++) {
            if (columns[index].equalsIgnoreCase(columnName)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Retrieves the value of the specified column.
     *
     * @param columnName The name of the column.
     * @return The value of the column.
     */
    public Object rowValue(String columnName) {
        int columnIndex = columnIndex(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException(
                    "Column " + columnName + " is not present in the data set (" + Arrays
                            .toString(columns) + ")"
            );
        }
        return rowValue(columnIndex);
    }

    /**
     * Checks if the data set is empty.
     *
     * @return {@code true} if the data set is empty, {@code false} otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Checks if the data set contains the specified columns.
     *
     * @param columns The columns to check.
     * @return {@code true} if all columns are present, {@code false} otherwise.
     */
    public boolean containColumns(Iterable<String> columns) {
        for (String column : columns) {
            if (columnIndex(column) == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the data set contains the specified columns.
     *
     * @param columns The columns to check.
     * @return {@code true} if all columns are present, {@code false} otherwise.
     */
    public boolean containColumns(String... columns) {
        return containColumns(Arrays.asList(columns));
    }

    /**
     * Moves to the next row in the data set.
     *
     * @return {@code true} if there is a next row, {@code false} otherwise.
     */
    public abstract boolean nextRow();

    /**
     * Retrieves the value of the specified column.
     *
     * @param columnIndex The index of the column.
     * @return The value of the column.
     */
    public abstract Object rowValue(int columnIndex);

    /**
     * Creates a copy of the data set.
     *
     * @return A copy of the data set.
     * @throws IOException If an I/O error occurs.
     */
    public abstract DataSet copy() throws IOException;

    /**
     * Replaces the value with {@code null} if it matches the {@code null} symbol.
     *
     * @param value The value to check.
     * @return The original value or null if it matches the {@code null} symbol.
     */
    protected Object nullIfMatchNullSymbol(Object value) {
        if (value instanceof String && nullSymbol.equals(value)) {
            return null;
        }
        return value;
    }

}