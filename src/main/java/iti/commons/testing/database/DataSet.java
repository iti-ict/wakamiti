/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.database;

import java.util.List;

public class DataSet {

    public DataSet(String tableName, List<String> columnNames, List<Object[]> data) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.data = data;
    }

    public DataSet(String tableName, List<String> columnIDs, List<String> columnNames, List<Object[]> data) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.columnIDs = columnIDs;
        this.data = data;
    }

    private String tableName;
    private List<String> columnIDs;
    private List<String> columnNames;
    private List<Object[]> data;


    public List<String> columnNames() {
        return columnNames;
    }

    public List<String> columnIDs() {
        return columnIDs;
    }

    public String tableName() {
        return tableName;
    }

    public List<Object[]> data() {
        return data;
    }

    public void setColumnIDs(List<String> columnIDs) {
        this.columnIDs = columnIDs;
    }

}
