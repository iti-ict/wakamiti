/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.database.loaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import cucumber.api.DataTable;
import iti.commons.testing.database.DataLoader;
import iti.commons.testing.database.DataSet;

public class CucumberDataTableLoader implements DataLoader {

    @Override
    public List<DataSet> resolveData(Object... arguments) {
        if (arguments[0] instanceof String && arguments[1] instanceof DataTable) {
            return loadDataFromCucumberDataTable((String)arguments[0], (DataTable)arguments[1]);
        }
        throw new IllegalArgumentException();
    }


    private List<DataSet> loadDataFromCucumberDataTable(String tableName, DataTable data) {
        Iterator<List<Object>> rowsIterator = data.asLists(Object.class).iterator();
        List<Object> tableHeaders = rowsIterator.next();
        List<String> columnNames = tableHeaders.stream().map(Object::toString).collect(Collectors.toList());
        List<Object[]> rows = new ArrayList<>();
        while (rowsIterator.hasNext()) {
            rows.add(rowsIterator.next().toArray());
        }
       return Arrays.asList(new DataSet(tableName,columnNames,rows));
    }

}
