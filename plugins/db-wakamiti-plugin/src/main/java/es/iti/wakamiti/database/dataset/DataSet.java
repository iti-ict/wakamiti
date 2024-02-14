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


public abstract class DataSet implements Closeable {

    protected String table;
    protected String[] columns;
    protected String origin;
    protected String nullSymbol;


    public DataSet(String table, String origin, String nullSymbol) {
        this.table = table;
        this.origin = origin;
        this.nullSymbol = nullSymbol;
    }

    protected static boolean containsIgnoringCase(List<String> list, String string) {
        for (String item : list) {
            if (item.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    public int columnNumbers() {
        return columns.length;
    }

    public String table() {
        return table;
    }

    public String origin() {
        return origin;
    }

    public String nullSymbol() {
        return nullSymbol;
    }

    public String collectColumns(CharSequence delimiter) {
        return String.join(delimiter, columns);
    }

    public String collectColumns(UnaryOperator<String> columnMapper, CharSequence delimiter) {
        return Stream.of(columns).map(columnMapper).collect(Collectors.joining(delimiter));
    }

    public String column(int index) {
        return columns[index];
    }

    public String[] columns() {
        return columns;
    }

    public Object[] values() {
        return IntStream.range(0, columns.length).mapToObj(this::rowValue).toArray();
    }

    public Map<String, Object> rowAsMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            map.put(columns[i], rowValue(i));
        }
        return map;
    }

    public int columnIndex(String columnName) {
        for (int index = 0; index < columns.length; index++) {
            if (columns[index].equalsIgnoreCase(columnName)) {
                return index;
            }
        }
        return -1;
    }

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

    public abstract boolean isEmpty();

    public boolean containColumns(Iterable<String> columns) {
        for (String column : columns) {
            if (columnIndex(column) == -1) {
                return false;
            }
        }
        return true;
    }

    public boolean containColumns(String... columns) {
        return containColumns(Arrays.asList(columns));
    }

    public abstract boolean nextRow();

    public abstract Object rowValue(int columnIndex);

    public abstract DataSet copy() throws IOException;

    protected Object nullIfMatchNullSymbol(Object value) {
        if (value instanceof String && nullSymbol.equals(value)) {
            return null;
        }
        return value;
    }

}