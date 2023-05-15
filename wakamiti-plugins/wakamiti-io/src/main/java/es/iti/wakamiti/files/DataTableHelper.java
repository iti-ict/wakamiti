/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.files;

import es.iti.wakamiti.api.plan.DataTable;

import java.text.MessageFormat;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataTableHelper {

    public static String FROM_POSITION_COLUMN = "from";
    public static String TO_POSITION_COLUMN = "to";
    public static String VALUE_COLUMN = "value";

    private DataTable dataTable;
    private List<String> columns;
    private String[][] values;

    public DataTableHelper(DataTable dataTable) {
        this.dataTable = dataTable;
        this.columns = Arrays.asList(dataTable.getValues()[0]);
        this.values = Arrays.copyOfRange(dataTable.getValues(), 1, dataTable.rows());

        validate();
    }

    private void validate() {
        List<String> expectedColumns = Arrays.asList(FROM_POSITION_COLUMN, TO_POSITION_COLUMN, VALUE_COLUMN);
        if (!columns.containsAll(expectedColumns)) {
            throw new IllegalArgumentException("The table must contain columns " + expectedColumns);
        }

        for (int row = 0; row < values.length; row++) {
            for (String column : Arrays.asList(FROM_POSITION_COLUMN, TO_POSITION_COLUMN)) {
                String value = values[row][getColumnIndex(column)];
                if (!value.matches("\\d+")) {
                    throw new NumberFormatException(
                            MessageFormat.format("The value \"{0}\" must be an integer. column: {1}, row: {2}",
                                    value, column, row + 1));
                }
            }

        }
    }

    public ValueRange getRange(int row) {
        try {
            return ValueRange.of(
                    Integer.parseInt(values[row][getColumnIndex(FROM_POSITION_COLUMN)]),
                    Integer.parseInt(values[row][getColumnIndex(TO_POSITION_COLUMN)])
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + ". row: " + (row + 1));
        }
    }

    public String getExpectedValue(int row) {
        return values[row][getColumnIndex(VALUE_COLUMN)];
    }

    public List<String> columns() {
        return columns;
    }

    public String[][] values() {
        return values;
    }

    public int count() {
        return values.length;
    }

    public int getColumnIndex(String name) {
        return columns.indexOf(name);
    }


    public void orderValues() {
        Arrays.sort(values, Comparator.comparing(o ->
                o[getColumnIndex(FROM_POSITION_COLUMN)] + o[getColumnIndex(TO_POSITION_COLUMN)]));
    }

    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append("(\n")
                .append("\t").append(columns).append("\n")
                .append(Stream.of(values).map(row -> "\t" + Arrays.deepToString(row)).collect(Collectors.joining("\n")))
                .append("\n)").toString();
    }
}