/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import es.iti.wakamiti.api.plan.DataTable;

import java.io.IOException;


/**
 * Represents a data set loaded from a DataTable.
 */
public class DataTableDataSet extends DataSet {

    private final DataTable dataTable;
    private int rowNumber;

    /**
     * Constructs a DataTableDataSet object from a DataTable.
     *
     * @param table      The name of the table.
     * @param dataTable  The DataTable object.
     * @param nullSymbol The {@code null} symbol.
     */
    public DataTableDataSet(String table, DataTable dataTable, String nullSymbol) {
        super(table, "data table", nullSymbol);
        this.dataTable = dataTable;
        this.columns = new String[dataTable.columns()];
        for (int c = 0; c < dataTable.columns(); c++) {
            this.columns[c] = dataTable.value(0, c);
        }
    }

    /**
     * Checks if the DataTable data set is empty.
     *
     * @return {@code true} if the data set is empty, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return dataTable.rows() == 0;
    }

    /**
     * Moves to the next row in the DataTable data set.
     *
     * @return {@code true} if there is a next row, {@code false} otherwise.
     */
    @Override
    public boolean nextRow() {
        if (dataTable.rows() - 1 <= rowNumber) {
            return false;
        } else {
            rowNumber++;
            return true;
        }
    }

    /**
     * Retrieves the value of the specified column in the current row.
     *
     * @param columnIndex The index of the column.
     * @return The value of the column.
     */
    @Override
    public Object rowValue(int columnIndex) {
        return nullIfMatchNullSymbol(dataTable.value(rowNumber, columnIndex));
    }

    /**
     * Closes the DataTable data set.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        // nothing to do
    }

    /**
     * Creates a copy of the DataTable data set.
     *
     * @return A copy of the data set.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public DataSet copy() throws IOException {
        return new DataTableDataSet(table, dataTable, nullSymbol);
    }

}