package iti.kukumo.db.dataset;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import iti.kukumo.api.KukumoException;

public class CsvDataSet extends DataSet {

    private final File file;
    private final CSVFormat format;
    private final Reader reader;
    private final Iterator<CSVRecord> iterator;
    private CSVRecord currentRecord;
    
    
    public CsvDataSet(String table, File file, String csvFormat) throws IOException {
        this(table,file,CSVFormat.valueOf(csvFormat));
    }
    
    private CsvDataSet(String table, File file, CSVFormat format) throws IOException {
        super(table,"file '"+file+"'");
        this.file = file;
        this.format = format;
        this.reader = new FileReader(file);
        try {
            this.iterator = format.parse(reader).iterator();
            CSVRecord header = iterator.next();
            this.columns = new String[header.size()];
            for (int c=0;c<columns.length;c++) {
                this.columns[c] = header.get(c);
            }
        } catch (Exception e) {
            reader.close();
            throw new KukumoException(e);
        }
    }
    
    
    @Override
    public void close() throws IOException {
        reader.close();
    }

    
    @Override
    public boolean nextRow() {
        if (!iterator.hasNext()) {
            return false;
        } else {
            currentRecord = iterator.next();
            return true;
        }
    }

    
    
    @Override
    public Object rowValue(int columnIndex) {
        return currentRecord.get(columnIndex);
    }

    
    @Override
    public DataSet copy() throws IOException {
        return new CsvDataSet(table, file, format);
    }
}
