/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import java.io.IOException;


/**
 * Represents a data set with inline values.
 */
public class InlineDataSet extends DataSet {

    private final Object[] values;
    private boolean consumed;

    /**
     * Constructs an InlineDataSet object with the specified table name, columns, values, and {@code null} symbol.
     *
     * @param table      The name of the table.
     * @param columns    The array of column names.
     * @param values     The array of values.
     * @param nullSymbol The symbol representing {@code null} values.
     */
    public InlineDataSet(String table, String[] columns, Object[] values, String nullSymbol) {
        super(table, "inline values", nullSymbol);
        this.columns = columns;
        this.values = values;
    }

    /**
     * Checks if the data set is empty.
     *
     * @return {@code true} if the data set is empty, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return values.length == 0;
    }

    /**
     * Moves to the next row in the data set.
     *
     * @return {@code true} if there is a next row, {@code false} otherwise.
     */
    @Override
    public boolean nextRow() {
        if (!consumed) {
            consumed = true;
            return true;
        }
        return false;
    }

    /**
     * Retrieves the value of the specified column in the current row.
     *
     * @param columnIndex The index of the column.
     * @return The value of the specified column in the current row.
     */
    @Override
    public Object rowValue(int columnIndex) {
        return nullIfMatchNullSymbol(values[columnIndex]);
    }

    /**
     * Closes the data set.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        // nothing to do
    }

    /**
     * Creates a copy of the inline data set.
     *
     * @return A copy of the inline data set.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public DataSet copy() throws IOException {
        return new InlineDataSet(table, columns, values, nullSymbol);
    }

}