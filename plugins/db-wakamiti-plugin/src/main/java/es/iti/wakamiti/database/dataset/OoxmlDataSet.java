/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.dataset;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


/**
 * Represents a data set extracted from an OOXML (Office Open XML) file.
 */
public class OoxmlDataSet extends MultiDataSet {

    private static final Logger logger = WakamitiLogger.forClass(OoxmlDataSet.class);

    private final File file;
    private final String ignoreSheetRegex;
    private final Workbook workbook;
    private final String nullSymbol;

    /**
     * Constructs an OoxmlDataSet from an OOXML file.
     *
     * @param file             The OOXML file.
     * @param ignoreSheetRegex A regex pattern to ignore certain sheets.
     * @param nullSymbol       The symbol representing {@code null} values.
     * @throws IOException If an I/O error occurs.
     */
    public OoxmlDataSet(File file, String ignoreSheetRegex, String nullSymbol) throws IOException {
        this.workbook = WorkbookFactory.create(file, null, true);
        this.file = file;
        this.ignoreSheetRegex = ignoreSheetRegex;
        this.nullSymbol = nullSymbol;
        try {
            Pattern ignoreSheetPattern = Pattern.compile(ignoreSheetRegex);
            Iterator<Sheet> sheetIterator = this.workbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                Sheet sheet = sheetIterator.next();
                if (ignoreSheetPattern.matcher(sheet.getSheetName()).matches()) {
                    continue;
                }
                addDataSet(new OoxmlSheetDataSet(sheet, file, nullSymbol));
            }
        } catch (Exception e) {
            close();
            throw new WakamitiException(e);
        }
    }

    /**
     * Closes the workbook associated with this data set.
     */
    @Override
    public void close() {
        try {
            this.workbook.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Creates a copy of the multi-data set.
     *
     * @return A copy of the multi-data set.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public MultiDataSet copy() throws IOException {
        return new OoxmlDataSet(file, ignoreSheetRegex, nullSymbol);
    }

    /**
     * Represents a data set extracted from a specific sheet within the OOXML file.
     */
    private static class OoxmlSheetDataSet extends DataSet {

        private final Iterator<Row> rowIterator;
        private Row currentRow;

        /**
         * Constructs an OoxmlSheetDataSet from a sheet within the workbook.
         *
         * @param sheet      The sheet containing the data.
         * @param file       The OOXML file.
         * @param nullSymbol The symbol representing {@code null} values.
         */
        public OoxmlSheetDataSet(Sheet sheet, File file, String nullSymbol) {
            super(sheet.getSheetName(), "file '" + file + "'", nullSymbol);
            this.rowIterator = sheet.rowIterator();
            this.columns = sheetHeaders(rowIterator.next());
        }

        /**
         * Extracts headers from the first row of the sheet.
         *
         * @param row The row containing headers.
         * @return An array of column headers.
         */
        private String[] sheetHeaders(Row row) {
            List<String> rawHeaders = new ArrayList<>();
            row.cellIterator().forEachRemaining(cell -> {
                String header = Optional.ofNullable(cell.getStringCellValue()).map(String::trim).orElse(null);
                if (header != null && !header.isEmpty()) {
                    rawHeaders.add(header);
                }
            });
            return rawHeaders.toArray(new String[0]);
        }

        /**
         * Checks if the data set is empty.
         *
         * @return {@code true} if the data set is empty, {@code false} otherwise.
         */
        @Override
        public boolean isEmpty() {
            return !rowIterator.hasNext();
        }

        /**
         * Moves to the next row in the data set.
         *
         * @return {@code true} if there is a next row, {@code false} otherwise.
         */
        @Override
        public boolean nextRow() {
            if (!rowIterator.hasNext()) {
                return false;
            } else {
                currentRow = rowIterator.next();
                return true;
            }
        }

        /**
         * Retrieves the value of a specific column in the current row.
         *
         * @param columnIndex The index of the column.
         * @return The value of the column.
         */
        @Override
        public Object rowValue(int columnIndex) {
            Cell cell = currentRow.getCell(columnIndex);
            Object value = null;
            if (cell != null) {
                switch (cell.getCellType()) {
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
                }
            }
            return value;
        }

        /**
         * Closes the data set.
         */
        @Override
        public void close() {
            // nothing
        }

        /**
         * Creates a copy of the data set. This operation is not supported.
         *
         * @return A new data set.
         */
        @Override
        public DataSet copy() {
            throw new UnsupportedOperationException(
                    "Cannot copy data set outside the container multi data set"
            );
        }
    }

}