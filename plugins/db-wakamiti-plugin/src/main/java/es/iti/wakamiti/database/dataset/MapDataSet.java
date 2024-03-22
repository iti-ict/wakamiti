/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


/**
 * Represents a data set backed by a two-dimensional array.
 */
public class MapDataSet extends DataSet {

    private final Object[][] values;
    private int rowNumber = -1;

    /**
     * Constructs a MapDataSet object with the specified table name, columns, values, and {@code null} symbol.
     *
     * @param table      The name of the table.
     * @param columns    The array of column names.
     * @param values     The two-dimensional array of values.
     * @param nullSymbol The symbol representing {@code null} values.
     */
    public MapDataSet(String table, String[] columns, Object[][] values, String nullSymbol) {
        super(table, "map", nullSymbol);
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
        if (rowNumber + 1 < values.length) {
            rowNumber++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves the value of the specified column in the current row.
     *
     * @param columnIndex The index of the column.
     * @return The value of the specified column in the current row.
     */
    @Override
    public Object rowValue(int columnIndex) {
        return nullIfMatchNullSymbol(values[rowNumber][columnIndex]);
    }

    /**
     * Creates a copy of the map data set.
     *
     * @return A copy of the map data set.
     */
    @Override
    public DataSet copy() {
        return new MapDataSet(this.table, this.columns, this.values, this.nullSymbol);
    }

    /**
     * Closes the data set.
     */
    @Override
    public void close() {
        // nothing to do
    }

    /**
     * Retrieves all values in the data set as a two-dimensional array.
     *
     * @return All values in the data set as a two-dimensional array.
     */
    public Object[][] allValues() {
        return values;
    }

}