/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.database.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import iti.commons.testing.TestingException;
import iti.commons.testing.database.DataLoader;
import iti.commons.testing.database.DataSet;

public class CSVDataLoader implements DataLoader {


    private String separator;
    private String stringDelimitier;


    public CSVDataLoader () {
        this(",","'");
    }

    public CSVDataLoader (String separator, String stringDelimitier) {
        this.separator = separator;
        this.stringDelimitier = stringDelimitier;
    }



    @Override
    public List<DataSet> resolveData(Object... arguments) {
         try {
            if (arguments[0] instanceof String) {
                if (arguments[1] instanceof String) {
                    return resolveDataFromCSV((String)arguments[0],(String)arguments[1]);
                }
                else if (arguments[1] instanceof InputStream) {
                    return resolveDataFromCSV((String)arguments[0],(InputStream)arguments[1]);
                }
            }
            throw new IllegalArgumentException();
         } catch (IOException e) {
             throw new TestingException(e);
         }
    }


    private List<DataSet> resolveDataFromCSV(String tableName, String path) throws IOException {
        try(InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return resolveDataFromCSV(tableName,stream);
        }
    }


    private List<DataSet> resolveDataFromCSV(String tableName, InputStream stream) throws IOException {

        String line = "";
        List<String> columnNames = null;
        List<Object[]> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            // headers
            line = reader.readLine();
            columnNames = Arrays.asList(line.split(separator));
            // body
            while ((line = reader.readLine()) != null) {
                Object[] row = line.split(separator);
                for (int i=0;i<row.length;i++) {
                    String value = (String) row[i];
                    if (value.startsWith(stringDelimitier) && value.endsWith(stringDelimitier)) {
                        row[i] = value.substring(1, value.length()-1);
                    }
                }
                data.add(row);

            }
        }
        return Arrays.asList(new DataSet(tableName,columnNames,data));
    }



}
