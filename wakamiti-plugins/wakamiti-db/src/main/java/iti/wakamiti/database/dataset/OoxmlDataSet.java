/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.database.dataset;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import iti.wakamiti.api.util.WakamitiLogger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;


import iti.wakamiti.api.WakamitiException;



public class OoxmlDataSet extends MultiDataSet {

    private static final Logger logger = WakamitiLogger.forClass(OoxmlDataSet.class);

    private final File file;
    private final String ignoreSheetRegex;
    private final Workbook workbook;
    private final String nullSymbol;


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


    @Override
    public void close() {
        try {
            this.workbook.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    private class OoxmlSheetDataSet extends DataSet {

        private final Iterator<Row> rowIterator;
        private Row currentRow;


        public OoxmlSheetDataSet(Sheet sheet, File file, String nullSymbol) {
            super(sheet.getSheetName(), "file '" + file + "'", nullSymbol);
            this.rowIterator = sheet.rowIterator();
            this.columns = sheetHeaders(rowIterator.next());
        }


        private String[] sheetHeaders(Row row) {
            List<String> rawHeaders = new ArrayList<>();
            row.cellIterator().forEachRemaining(cell -> {
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
            throw new UnsupportedOperationException(
                "Cannot copy data set outside the container multi data set"
            );
        }
    }


    @Override
    public MultiDataSet copy() throws IOException {
        return new OoxmlDataSet(file, ignoreSheetRegex, nullSymbol);
    }

}