/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.files;


import java.text.MessageFormat;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.iti.wakamiti.api.plan.DataTable;


/**
 * Provides the Data Table Helper functionality used by Wakamiti.
 */
public class DataTableHelper {

    /** Required column containing the inclusive start character offset. */
    public static final String FROM_POSITION_COLUMN = "from";
    /** Required column containing the exclusive end character offset. */
    public static final String TO_POSITION_COLUMN = "to";
    /** Required column containing the text expected within the range. */
    public static final String VALUE_COLUMN = "value";

    private DataTable dataTable;
    private List<String> columns;
    private String[][] values;

    /**
     * Parses and validates a positional text-assertion table.
     * <p>
     * The first row is treated as the header and must contain {@code from},
     * {@code to} and {@code value}. Every range endpoint in subsequent rows
     * must be an unsigned integer.
     *
     * @param dataTable source table including its header row
     * @throws IllegalArgumentException if required columns or valid ranges are
     *                                  missing
     * @throws NumberFormatException if an endpoint is not numeric
     */
    public DataTableHelper(
            DataTable dataTable
    ) {
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

    /**
     * Returns the range declared by a data row.
     *
     * @param row zero-based data-row index, excluding the header
     * @return range whose minimum is the substring start and maximum is the
     *         exclusive substring end
     * @throws IllegalArgumentException if the row or endpoints are invalid
     */
    public ValueRange getRange(
            int row
    ) {
        try {
            return ValueRange.of(
                    Integer.parseInt(values[row][getColumnIndex(FROM_POSITION_COLUMN)]),
                    Integer.parseInt(values[row][getColumnIndex(TO_POSITION_COLUMN)])
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + ". row: " + (row + 1));
        }
    }

    /**
     * Returns the expected cell text for a row in the configured comparison range.

     * @param row zero-based data-row index

     * @return expected text for that range

     */
    public String getExpectedValue(
            int row
    ) {
        return values[row][getColumnIndex(VALUE_COLUMN)];
    }

    /**
     * @return header columns in source order
     */
    public List<String> columns() {
        return columns;
    }

    /**
     * @return data rows excluding the header
     */
    public String[][] values() {
        return values;
    }

    /**
     * @return number of data rows excluding the header
     */
    public int count() {
        return values.length;
    }

    /**
     * Resolves a column by the header text supplied in the Wakamiti data table.
     *
     * @param name header name
     * @return zero-based index, or {@code -1} when absent
     */
    public int getColumnIndex(
            String name
    ) {
        return columns.indexOf(name);
    }

    /**
     * Sorts data rows in place by their textual {@code from}/{@code to}
     * composite key.
     */
    public void orderValues() {
        Arrays.sort(values, Comparator.comparing(o ->
                o[getColumnIndex(FROM_POSITION_COLUMN)] + o[getColumnIndex(TO_POSITION_COLUMN)]));
    }

    /**
     * Renders the validated header and data rows for diagnostic logging.
     *
     * @return multiline table representation
     */
    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append("(\n")
                .append("\t").append(columns).append("\n")
                .append(Stream.of(values).map(row -> "\t" + Arrays.deepToString(row)).collect(Collectors.joining("\n")))
                .append("\n)").toString();
    }

}
