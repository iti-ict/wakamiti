/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.database.dataset;

public class MapDataSet extends DataSet {

    private String[][] values;
    private int rowNumber = -1;

    public MapDataSet(String table, String[] columns, String[][] values, String nullSymbol) {
        super(table, "map", nullSymbol);
        this.columns = columns;
        this.values = values;
    }

    @Override
    public boolean nextRow() {
        if (rowNumber + 1 < values.length) {
            rowNumber++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object rowValue(int columnIndex) {
        return nullIfMatchNullSymbol(values[rowNumber][columnIndex]);
    }

    @Override
    public DataSet copy() {
        return new MapDataSet(this.table, this.columns, this.values, this.nullSymbol);
    }

    @Override
    public void close() {
        // nothing to do
    }

}