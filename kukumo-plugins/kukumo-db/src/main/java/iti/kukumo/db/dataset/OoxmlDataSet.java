package iti.kukumo.db.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import iti.kukumo.api.KukumoException;

public class OoxmlDataSet extends MultiDataSet {
    
    private final File file;
    private final String ignoreSheetRegex;
    private final XSSFWorkbook workbook;
    private final String nullSymbol;


    public OoxmlDataSet(File file, String ignoreSheetRegex, String nullSymbol) throws IOException {
        try {
			this.workbook = new XSSFWorkbook(file);
		} catch (InvalidFormatException e) {
			throw new IOException(e);
		}
        this.file = file;
        this.ignoreSheetRegex = ignoreSheetRegex;
        try {
            this.nullSymbol = nullSymbol;
            Pattern ignoreSheetPattern = Pattern.compile(ignoreSheetRegex);
            Iterator<Sheet> sheetIterator = this.workbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                if (ignoreSheetPattern.matcher(sheet.getSheetName()).matches()) {
                    continue;
                }
                addDataSet(new OoxmlSheetDataSet(sheet,file));
            }
        } catch (Exception e) {
            this.workbook.close();
            throw new KukumoException(e);
        }
    }
    
    
    
    @Override
    public void close() throws IOException {
        this.workbook.close();
                
    }
    
    
    private class OoxmlSheetDataSet extends DataSet {
        
        private final Iterator<Row> rowIterator;
        private Row currentRow;
        
        public OoxmlSheetDataSet(Sheet sheet, File file) {
            super(sheet.getSheetName(), "file '"+file+"'");
            this.rowIterator = sheet.rowIterator();
            this.columns = sheetHeaders(rowIterator.next());
        }
        
        private String[] sheetHeaders(Row row) {
            List<String> rawHeaders = new ArrayList<>();
            row.cellIterator().forEachRemaining(cell->{
                String header = cell.getStringCellValue().trim();
                if (header != null && !header.isEmpty()) {
                    rawHeaders.add(header);
                }
            });
            return rawHeaders.toArray(new String[0]);
        }
        
        
        @Override
        public boolean nextRow() {
            if (!rowIterator.hasNext()) {
                return false;
            } else {
                currentRow = rowIterator.next();
                return true;
            }
        }
        
        @Override
        public Object rowValue(int columnIndex) {
            Cell cell = currentRow.getCell(columnIndex);
            Object value = null;
            if (cell != null) {                
                switch(cell.getCellType()) {
                case BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case NUMERIC:
                    value = cell.getNumericCellValue();
                    break;
                case STRING:
                    value = cell.getStringCellValue().trim();
                    if (nullSymbol.equals(value)) {
                        value = null;
                    }
                    break;
                default:
                    value = null;
                    break;
               }                
            }
            return value;
        }
        

        @Override
        public void close() throws IOException {
            // nothing
        }
        
        @Override
        public DataSet copy() throws IOException {
            throw new UnsupportedOperationException("Cannot copy data set outside the container multi data set");
        }
    }


    @Override
    public MultiDataSet copy() throws IOException {
    	return new OoxmlDataSet(file, ignoreSheetRegex, nullSymbol);
    }
    
}
