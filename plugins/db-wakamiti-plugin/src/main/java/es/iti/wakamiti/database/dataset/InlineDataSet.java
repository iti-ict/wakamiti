/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import java.io.IOException;


public class InlineDataSet extends DataSet {

    private final Object[] values;
    private boolean consumed;


    public InlineDataSet(String table, String[] columns, Object[] values, String nullSymbol) {
        super(table, "inline values", nullSymbol);
        this.columns = columns;
        this.values = values;
    }


    @Override
    public boolean isEmpty() {
        return values.length == 0;
    }

    @Override
    public boolean nextRow() {
        if (!consumed) {
            consumed = true;
            return true;
        }
        return false;
    }


    @Override
    public Object rowValue(int columnIndex) {
        return nullIfMatchNullSymbol(values[columnIndex]);
    }


    @Override
    public void close() throws IOException {
        // nothing to do
    }


    @Override
    public DataSet copy() throws IOException {
        return new InlineDataSet(table, columns, values, nullSymbol);
    }
}