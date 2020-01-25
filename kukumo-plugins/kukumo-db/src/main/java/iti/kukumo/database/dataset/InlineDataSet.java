/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database.dataset;


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
