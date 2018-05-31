/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.database.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import iti.commons.testing.TestingException;
import iti.commons.testing.database.DataLoader;
import iti.commons.testing.database.DataSet;

public class XLSDataLoader implements DataLoader {


    @Override
    public List<DataSet> resolveData(Object... arguments) {
         try {
            if (arguments[0] instanceof String) {
                return resolveDataFromXLS((String)arguments[0]);
            }
            else if (arguments[0] instanceof InputStream) {
                return resolveDataFromXLS((InputStream)arguments[0]);
            }
            else {
                throw new IllegalArgumentException();
            }
         } catch (IOException e) {
             throw new TestingException(e);
         }
    }


    private List<DataSet> resolveDataFromXLS(String path) throws IOException {
        try(InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return resolveDataFromXLS(stream);
        }
    }


    private List<DataSet> resolveDataFromXLS(InputStream stream) throws IOException {
        List<DataSet> dataSets = new ArrayList<>();
        try(Workbook workbook = new XSSFWorkbook(stream)) {
            // each table in a different sheet
            for (int i=0; i<workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                DataSet dataSet = resolveTableFromSheet(sheet);
                if (dataSet != null) {
                    dataSets.add(dataSet);
                }
            }
        }
        return dataSets;
    }


    protected DataSet resolveTableFromSheet(Sheet sheet)  {
        Iterator<Row> rows = sheet.iterator();
        if (!rows.hasNext()) {
            // skip if the sheet is empty
            return null;
        }

        String tableName = sheet.getSheetName();
        // first row has the column names
        List<String> columnNames = new ArrayList<>();
        Row header = rows.next();
        header.cellIterator().forEachRemaining(cell -> columnNames.add((String)getCellValue(cell)));

        List<Object[]> data = new ArrayList<>();
        while (rows.hasNext()) {
            Row row = rows.next();
            Object[] rowData = new Object[columnNames.size()];
            for (int i=0; i<rowData.length; i++) {
                rowData[i] = getCellValue(row.getCell(i));
            }
            data.add(rowData);
        }

        return new DataSet(tableName, columnNames, data);

    }


    private Object getCellValue(Cell cell) {
        Object value = null;
        switch(cell.getCellTypeEnum()) {
        case STRING: value = cell.getStringCellValue(); break;
        case NUMERIC: value = cell.getNumericCellValue(); break;
        case BOOLEAN: value = cell.getBooleanCellValue(); break;
        default: value = null; break;
        }
        return value;
    }

}
