/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import es.iti.wakamiti.api.WakamitiException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;


/**
 * Represents a data set loaded from a CSV file.
 */
public class CsvDataSet extends DataSet {

    private final File file;
    private final CSVFormat format;
    private final Reader reader;
    private final Iterator<CSVRecord> iterator;
    private CSVRecord currentRecord;

    /**
     * Constructs a CsvDataSet object from a CSV file.
     *
     * @param table      The name of the table.
     * @param file       The CSV file.
     * @param csvFormat  The CSV format.
     * @param nullSymbol The {@code null} symbol.
     * @throws IOException If an I/O error occurs.
     */
    public CsvDataSet(String table, File file, String csvFormat, String nullSymbol) throws IOException {
        this(table, file, Delimiter.valueOf(csvFormat.toUpperCase()).getFormat(), nullSymbol);
    }

    private CsvDataSet(String table, File file, CSVFormat format, String nullSymbol) throws IOException {
        super(table, "file '" + file + "'", nullSymbol);
        this.file = file;
        this.format = format;
        this.reader = new FileReader(file);
        try {
            this.iterator = format.parse(reader).iterator();
            CSVRecord header = iterator.next();
            this.columns = new String[header.size()];
            for (int c = 0; c < columns.length; c++) {
                this.columns[c] = header.get(c);
            }
        } catch (Exception e) {
            reader.close();
            throw new WakamitiException(e);
        }
    }

    /**
     * Closes the CSV data set.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Checks if the CSV data set is empty.
     *
     * @return {@code true} if the data set is empty, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return !iterator.hasNext();
    }

    /**
     * Moves to the next row in the CSV data set.
     *
     * @return {@code true} if there is a next row, {@code false} otherwise.
     */
    @Override
    public boolean nextRow() {
        if (!iterator.hasNext()) {
            return false;
        } else {
            currentRecord = iterator.next();
            return true;
        }
    }

    /**
     * Retrieves the value of the specified column in the current row.
     *
     * @param columnIndex The index of the column.
     * @return The value of the column.
     */
    @Override
    public Object rowValue(int columnIndex) {
        return nullIfMatchNullSymbol(currentRecord.get(columnIndex));
    }

    /**
     * Creates a copy of the CSV data set.
     *
     * @return A copy of the data set.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public DataSet copy() throws IOException {
        return new CsvDataSet(table, file, format, nullSymbol);
    }

    /**
     * Enum representing CSV delimiters.
     */
    private enum Delimiter {
        DEFAULT(CSVFormat.DEFAULT),
        EXCEL(CSVFormat.EXCEL),
        INFORMIX_UNLOAD(CSVFormat.INFORMIX_UNLOAD),
        INFORMIX_UNLOAD_CSV(CSVFormat.INFORMIX_UNLOAD_CSV),
        MONGODB_CSV(CSVFormat.MONGODB_CSV),
        MONGODB_TSV(CSVFormat.MONGODB_TSV),
        MYSQL(CSVFormat.MYSQL),
        ORACLE(CSVFormat.ORACLE),
        POSTGRESQL_CSV(CSVFormat.POSTGRESQL_CSV),
        POSTGRESQL_TEXT(CSVFormat.POSTGRESQL_TEXT),
        RFC4180(CSVFormat.RFC4180),
        TDF(CSVFormat.TDF);

        private final CSVFormat format;

        Delimiter(CSVFormat format) {
            this.format = format;
        }

        public CSVFormat getFormat() {
            return this.format;
        }
    }
}