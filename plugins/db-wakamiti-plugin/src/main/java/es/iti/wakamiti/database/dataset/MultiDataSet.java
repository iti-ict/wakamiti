/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Represents a collection of data sets.
 */
public abstract class MultiDataSet implements Closeable, Iterable<DataSet> {

    private final List<DataSet> dataSets = new ArrayList<>();

    /**
     * Adds a data set to the collection.
     *
     * @param dataSet The data set to add.
     */
    protected void addDataSet(DataSet dataSet) {
        dataSets.add(dataSet);
    }

    /**
     * Returns an iterator over the data sets in the collection.
     *
     * @return An iterator.
     */
    @Override
    public Iterator<DataSet> iterator() {
        return dataSets.iterator();
    }

    /**
     * Creates a copy of the multi-data set.
     *
     * @return A copy of the multi-data set.
     * @throws IOException If an I/O error occurs.
     */
    public abstract MultiDataSet copy() throws IOException;

}