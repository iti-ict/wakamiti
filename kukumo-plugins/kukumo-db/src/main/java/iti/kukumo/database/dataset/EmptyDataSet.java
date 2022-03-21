/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database.dataset;


import java.io.IOException;


public class EmptyDataSet extends DataSet {

    public EmptyDataSet(String table) {
        super(table, "empty", "");
        this.columns = new String[0];
    }


    @Override
    public boolean nextRow() {
        return false;
    }


    @Override
    public Object rowValue(int columnIndex) {
        return null;
    }


    @Override
    public void close() throws IOException {
        // nothing
    }


    @Override
    public DataSet copy() throws IOException {
        return new EmptyDataSet(table);
    }

}