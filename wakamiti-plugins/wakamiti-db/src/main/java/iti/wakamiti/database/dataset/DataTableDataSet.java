/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.database.dataset;


import java.io.IOException;

import iti.wakamiti.api.plan.DataTable;


public class DataTableDataSet extends DataSet {

    private final DataTable dataTable;
    private int rowNumber;


    public DataTableDataSet(String table, DataTable dataTable, String nullSymbol) {
        super(table, "data table", nullSymbol);
        this.dataTable = dataTable;
        this.columns = new String[dataTable.columns()];
        for (int c = 0; c < dataTable.columns(); c++) {
            this.columns[c] = dataTable.value(0, c);
        }
    }


    @Override
    public boolean nextRow() {
        if (dataTable.rows() - 1 <= rowNumber) {
            return false;
        } else {
            rowNumber++;
            return true;
        }
    }


    @Override
    public Object rowValue(int columnIndex) {
        return nullIfMatchNullSymbol(dataTable.value(rowNumber, columnIndex));
    }


    @Override
    public void close() throws IOException {
        // nothing to do
    }


    @Override
    public DataSet copy() throws IOException {
        return new DataTableDataSet(table, dataTable, nullSymbol);
    }

}