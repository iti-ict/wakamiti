/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.database.dataset;


import iti.wakamiti.database.CaseSensitivity;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
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


    public int columnNumbers() {
        return columns.length;
    }


    public String table() {
        return table;
    }


    public String origin() {
        return origin;
    }


    public String nullSymbol() { return nullSymbol; }


    public String collectColumns(CharSequence delimiter) {
        return Stream.of(columns).collect(Collectors.joining(delimiter));
    }

    public String collectColumns(UnaryOperator<String> columnMapper, CharSequence delimiter) {
        return Stream.of(columns).map(columnMapper).collect(Collectors.joining(delimiter));
    }

    public String collectColumns(CharSequence delimiter, CaseSensitivity caseSensitivity) {
        return Stream.of(columns).map(caseSensitivity::format).collect(Collectors.joining(delimiter));
    }

    public String collectColumns(UnaryOperator<String> columnMapper, CharSequence delimiter, CaseSensitivity caseSensitivity) {
        return Stream.of(columns).map(columnMapper).collect(Collectors.joining(delimiter));
    }


    public String column(int index) {
        return columns[index];
    }


    public String[] columns() {
        return columns;
    }


    public Map<String, Object> rowAsMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            map.put(columns[i], rowValue(i));
        }
        return map;
    }


    public int columnIndex(String columnName) {
        int index = 0;
        for (index = 0; index < columns.length; index++) {
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


    public boolean containColumns(Iterable<String> columns) {
        Iterator<String> iterator = columns.iterator();
        while (iterator.hasNext()) {
            if (columnIndex(iterator.next()) == -1) {
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


    protected static boolean containsIgnoringCase(List<String> list, String string) {
        for (String item : list) {
            if (item.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    protected Object nullIfMatchNullSymbol(Object value) {
        if (value instanceof String && nullSymbol.equals(value)) {
            return null;
        }
        return value;
    }

}