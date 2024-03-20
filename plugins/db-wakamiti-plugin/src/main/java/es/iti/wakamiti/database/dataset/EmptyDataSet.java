/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import java.io.IOException;


/**
 * Represents an empty data set.
 */
public class EmptyDataSet extends DataSet {

    /**
     * Constructs an EmptyDataSet object with the specified table name.
     *
     * @param table The name of the table.
     */
    public EmptyDataSet(String table) {
        super(table, "empty", "");
        this.columns = new String[0];
    }

    /**
     * Checks if the data set is empty.
     *
     * @return Always returns {@code true} since it represents an empty data set.
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * Moves to the next row in the data set.
     *
     * @return Always returns {@code false} since the data set is empty.
     */
    @Override
    public boolean nextRow() {
        return false;
    }

    /**
     * Retrieves the value of the specified column in the current row.
     *
     * @param columnIndex The index of the column.
     * @return Always returns {@code null} since the data set is empty.
     */
    @Override
    public Object rowValue(int columnIndex) {
        return null;
    }

    /**
     * Closes the data set.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        // nothing
    }

    /**
     * Creates a copy of the empty data set.
     *
     * @return A copy of the empty data set.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public DataSet copy() throws IOException {
        return new EmptyDataSet(table);
    }

}