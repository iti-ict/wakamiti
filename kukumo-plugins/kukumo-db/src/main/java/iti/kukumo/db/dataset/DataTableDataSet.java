package iti.kukumo.db.dataset;

import java.io.IOException;

import iti.kukumo.api.plan.DataTable;

public class DataTableDataSet extends DataSet {
    
    
    private DataTable dataTable;
    private int rowNumber;
    
    
    public DataTableDataSet(String table, DataTable dataTable) {
        super(table,"data table");
        this.dataTable = dataTable;
        this.columns = new String[dataTable.columns()];
        for (int c=0;c<dataTable.columns();c++) {
            this.columns[c] = dataTable.value(0, c);
        }
    }
    
    @Override
    public boolean nextRow() {
        if (dataTable.rows()-1 <= rowNumber) {
            return false;
        } else {
            rowNumber ++;
            return true;
        }
    }
    
    @Override
    public Object rowValue(int columnIndex) {
        return dataTable.value(rowNumber, columnIndex);
    }
    
    
    @Override
    public void close() throws IOException {
        // nothing to do
    }
    
    @Override
    public DataSet copy() throws IOException {
    	return new DataTableDataSet(table, dataTable);
    }

}
