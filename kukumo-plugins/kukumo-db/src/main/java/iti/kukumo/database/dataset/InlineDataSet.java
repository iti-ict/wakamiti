package iti.kukumo.database.dataset;

import java.io.IOException;

public class InlineDataSet extends DataSet {

    private final Object[] values;
    private boolean consumed;
    
    public InlineDataSet(String table, String[] columns, Object[] values) {
        super(table,"inline values");
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
        return values[columnIndex];
    }

    @Override
    public void close() throws IOException {
        // nothing to do
    }

    
    @Override
    public DataSet copy() throws IOException {
    	return new InlineDataSet(table, columns, values);
    }
}
