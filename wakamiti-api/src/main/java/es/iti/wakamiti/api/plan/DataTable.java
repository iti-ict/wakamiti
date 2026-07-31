/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import java.util.Arrays;
import java.util.function.UnaryOperator;


/**
 * Represents a data table for a test plan node.
 */
public class DataTable implements PlanNodeData {

    private final String[][] values;

    /**
     * Creates a table backed by the supplied two-dimensional array.
     * <p>
     * The array is retained rather than copied. Callers that require isolation
     * should pass their own defensive copy or use {@link #copy()} afterwards.
     * Rows are expected to form a rectangular table.
     * </p>
     *
     * @param values the table cells, indexed by row and then column
     */
    public DataTable(
            String[][] values
    ) {
        this.values = values;
    }

    private static String[][] copy(
            String[][] src,
            UnaryOperator<String> replacer
    ) {
        final String[][] dst = new String[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = Arrays.copyOf(src[i], src[i].length);
            for (int j = 0; j < dst[i].length; j++) {
                dst[i][j] = replacer.apply(dst[i][j]);
            }
        }
        return dst;
    }

    /**
     * Returns the backing table data.
     *
     * @return the mutable array supplied at construction time
     */
    public String[][] getValues() {
        return values;
    }

    /**
     * Returns the number of rows, including an optional header row.
     *
     * @return the row count
     */
    public int rows() {
        return values.length;
    }

    /**
     * Returns the width reported by the first row.
     *
     * @return the first row's column count, or {@code 0} for an empty table
     */
    public int columns() {
        return (values.length == 0 ? 0 : values[0].length);
    }

    /**
     * Retrieves a cell by its zero-based coordinates.
     *
     * @param row    the zero-based row index
     * @param column the zero-based column index
     * @return the cell value
     * @throws ArrayIndexOutOfBoundsException if either coordinate is outside
     *                                       the backing array
     */
    public String value(
            int row,
            int column
    ) {
        return values[row][column];
    }

    @Override
    public PlanNodeData copy() {
        return new DataTable(copy(values, UnaryOperator.identity()));
    }

    @Override
    public PlanNodeData copyReplacingVariables(
            UnaryOperator<String> replacer
    ) {
        return new DataTable(copy(values, replacer));
    }

}
