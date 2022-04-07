/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.annotations.TearDown;
import iti.kukumo.api.datatypes.Assertion;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import iti.kukumo.database.dataset.CsvDataSet;
import iti.kukumo.database.dataset.DataSet;
import iti.kukumo.database.dataset.DataTableDataSet;
import iti.kukumo.database.dataset.InlineDataSet;
import iti.kukumo.database.dataset.MultiDataSet;
import iti.kukumo.database.dataset.OoxmlDataSet;
import iti.kukumo.util.KukumoLogger;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.io.IOUtils;



@Extension(provider = "iti.kukumo", name = "database-steps", version = "1.1")
@I18nResource("iti_kukumo_kukumo-database")
public class DatabaseStepContributor implements StepContributor {

    private static final Logger LOGGER = KukumoLogger
        .of(LoggerFactory.getLogger("iti.kukumo.database"));

    private static ConnectionManager connectionManager = Kukumo.extensionManager()
        .getExtension(ConnectionManager.class)
        .orElseThrow(() -> new KukumoException("Cannot find a connection manager"));

    private final ConnectionParameters connectionParameters = new ConnectionParameters();
    private final DatabaseHelper helper = new DatabaseHelper(connectionParameters,this::connection,this::nullSymbol);

    private Connection connection;
    private String xlsIgnoreSheetRegex;
    private String nullSymbol;
    private String csvFormat;
    private char csvDelimiter;
    private boolean enableCleanupUponCompletion;


    public Connection connection() throws SQLException {
        if (connection == null) {
            connection = connectionManager.obtainConnection(connectionParameters);
            LOGGER.debug(
                    "Using database connection of type {} provided by {contributor}",
                    connection.getClass().getSimpleName(),
                    connectionManager.info()
            );
        } else {
            connection = connectionManager.refreshConnection(connection, connectionParameters);
        }
        return connection;
    }


    public ConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }


    public void setXlsIgnoreSheetRegex(String ignoreSheetRegex) {
        this.xlsIgnoreSheetRegex = ignoreSheetRegex;
    }

    public void setNullSymbol(String nullSymbol) {
        this.nullSymbol = nullSymbol;
    }


    public void setCsvFormat(String csvFormat) {
        this.csvFormat = csvFormat;
    }

    public void setCsvDelimiter(char csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }


    public void setEnableCleanupUponCompletion(boolean enableCleanupUponCompletion) {
        this.enableCleanupUponCompletion = enableCleanupUponCompletion;
    }

    public void setCaseSensitivity(CaseSensitivity caseSensitivity) {
        this.helper.setCaseSensitivity(caseSensitivity);
    }


    private Assertion<Long> matcherEmpty() {
        return new MatcherAssertion<>(Matchers.equalTo(0L));
    }


    private Assertion<Long> matcherNonEmpty() {
        return new MatcherAssertion<>(Matchers.greaterThan(0L));
    }

    private String nullSymbol() {
        return nullSymbol;
    }

    @TearDown(order = 1)
    public void cleanUp() {
        helper.cleanUp();
    }


    @TearDown(order = 2)
    public void releaseConnection() throws SQLException {
        if (this.connection != null) {
            connectionManager.releaseConnection(connection);
        }
    }

    @Step("db.define.cleanup.document")
    public void setManualCleanup(Document document) {
        helper.setCleanUpOperations(document.getContent());
    }


    @Step("db.define.cleanup.file")
    public void setManualCleanup(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            helper.setCleanUpOperations(IOUtils.toString(reader), file.toString());
        }
    }

    @Step(
        value = "db.define.connection.parameters",
           args = { "url:text", "username:text", "password:text" }
    )
    public void setConnectionParameters(String url, String username, String password) throws SQLException {
        LOGGER.debug(
            "Setting database connection parameters [url={}, username={}, password={}]",
            url,
            username,
            password
        );
        this.connectionParameters.url(url).username(username).password(password);
        this.releaseConnection();
    }


    @Step("db.action.script.document")
    public void executeSQLScript(Document document) throws SQLException, JSQLParserException {
        helper.executeSQLStatements(document.getContent(), enableCleanupUponCompletion);
    }


    @Step("db.action.script.file")
    public void executeSQLScript(File file) throws IOException, SQLException, JSQLParserException {
        try (Reader reader = new FileReader(file)) {
            helper.executeSQLStatements(IOUtils.toString(reader), file.toString(), enableCleanupUponCompletion);
        }
    }


    @Step(value = "db.action.insert.from.data", args = "word")
    public void insertFromDataTable(
        String table,
        DataTable dataTable
    ) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            helper.deleteDataSet(dataSet, false);
            helper.insertDataSet(dataSet.copy(), enableCleanupUponCompletion);
        }
    }


    @Step("db.action.insert.from.xls")
    public void insertFromXLSFile(File file) throws IOException, SQLException {
        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            helper.deleteMultiDataSet(multiDataSet, false);
            helper.insertMultiDataSet(multiDataSet.copy(), enableCleanupUponCompletion);
        }
    }


    @Step(value = "db.action.insert.from.csv", args = { "csv:file", "table:word" })
    public void insertFromCSVFile(File file, String table) throws IOException, SQLException {
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, csvDelimiter, nullSymbol)) {
            helper.deleteDataSet(dataSet, false);
            helper.insertDataSet(dataSet.copy(), enableCleanupUponCompletion);
        }
    }


    @Step(value = "db.action.delete.from.data", args = "word")
    public void deleteFromDataTable(
        String table,
        DataTable dataTable
    ) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            helper.deleteDataSet(dataSet, enableCleanupUponCompletion);
        }
    }


    @Step("db.action.delete.from.xls")
    public void deleteFromXLSFile(
        File file
    ) throws IOException, SQLException, InvalidFormatException {
        try (MultiDataSet multiDataSet = new OoxmlDataSet(
            file, xlsIgnoreSheetRegex, nullSymbol
        )) {
            helper.deleteMultiDataSet(multiDataSet, enableCleanupUponCompletion);
        }
    }


    @Step(value = "db.action.delete.from.csv", args = { "csv:file", "table:word" })
    public void deleteFromCSVFile(File file, String table) throws IOException, SQLException {
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, csvDelimiter, nullSymbol)) {
            helper.deleteDataSet(dataSet, enableCleanupUponCompletion);
        }
    }


    @Step(value = "db.action.clear.table.all", args = "word")
    public void clearTable(String table) throws SQLException {
        helper.truncateTable(table);
    }


    @Step(value = "db.action.clear.table.row.one.column", args = { "table:word", "column:word",
                    "value:text" })
    public void clearTableRowOneColumn(
        String table,
        String column,
        String value
    ) throws SQLException, IOException {
        try (DataSet dataSet = new InlineDataSet(
            table, new String[] { column }, new Object[] { value }, nullSymbol
        )) {
            helper.deleteDataSet(dataSet, false);
        }
    }


    @Step(value = "db.action.clear.table.row.two.columns", args = { "table:word", "column1:word",
                    "value1:text", "column2:word", "value2:text" })
    public void clearTableRowTwoColumns(
        String table,
        String column1,
        String value1,
        String column2,
        String value2
    ) throws SQLException, IOException {
        try (DataSet dataSet = new InlineDataSet(
            table, new String[] { column1, column2 }, new Object[] { value1, value2 }, nullSymbol
        )) {
            helper.deleteDataSet(dataSet, false);
        }
    }


    @Step(value = "db.assert.table.exists.row.single.id", args = { "id:text", "table:word" })
    public void assertRowExistsBySingleId(String id, String table) throws SQLException {
        String keyColumn = helper.primaryKey(table).get()[0];
        helper.assertCountRowsInTableByColumns(
            matcherNonEmpty(),
            table,
            new String[] { keyColumn },
            new Object[] { id }
        );
    }


    @Step(value = "db.assert.table.exists.row.one.column", args = { "table:word", "column:word",
                    "value:text" })
    public void assertRowExistsByOneColumn(
        String table,
        String column,
        String value
    ) throws SQLException {
        helper.assertCountRowsInTableByColumns(
            matcherNonEmpty(),
            table,
            new String[] { column },
            new Object[] { value }
        );
    }


    @Step(value = "db.assert.table.exists.row.two.columns", args = { "table:word", "column1:word",
                    "value1:text", "column2:word", "value2:text" })
    public void assertRowExistsByTwoColumns(
        String table,
        String column1,
        String value1,
        String column2,
        String value2
    ) throws SQLException {
        helper.assertCountRowsInTableByColumns(
            matcherNonEmpty(),
            table,
            new String[] { column1, column2 },
            new Object[] { value1, value2 }
        );
    }


    @Step(value = "db.assert.table.exists.sql.where", args = { "table:word" })
    public void assertRowExistsByClause(String table, Document clause) throws SQLException, JSQLParserException {
        helper.assertCountRowsInTableByClause(matcherNonEmpty(), table, clause.getContent());
    }


    @Step(value = "db.assert.table.exists.data", args = "table:word")
    public void assertDataTableExists(
        String table,
        DataTable dataTable
    ) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            helper.assertDataSetExists(dataSet);
        }
    }


    @Step("db.assert.table.exists.xls")
    public void assertXLSFileExists(
        File file
    ) throws InvalidFormatException, IOException, SQLException {
        try (MultiDataSet multiDataSet = new OoxmlDataSet(
            file, xlsIgnoreSheetRegex, nullSymbol
        )) {
            helper.assertMultiDataSetExists(multiDataSet);
        }
    }


    @Step(value = "db.assert.table.exists.csv", args = { "csv:file", "table:word" })
    public void assertCSVFileExists(File file, String table) throws IOException, SQLException {
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, csvDelimiter, nullSymbol)) {
            helper.assertDataSetExists(dataSet);
        }
    }


    @Step(value = "db.assert.table.not.exists.row.single.id", args = { "id:text", "table:word" })
    public void assertRowNotExistsBySingleId(String id, String table) throws SQLException {
        String keyColumn = helper.primaryKey(table).get()[0];
        helper.assertCountRowsInTableByColumns(
            matcherEmpty(),
            table,
            new String[] { keyColumn },
            new Object[] { id }
        );
    }


    @Step(value = "db.assert.table.not.exists.row.one.column", args = { "table:word", "column:word",
                    "value:text" })
    public void assertRowNotExistsByOneColumn(
        String table,
        String column,
        String value
    ) throws SQLException {
        helper.assertCountRowsInTableByColumns(
            matcherEmpty(),
            table,
            new String[] { column },
            new Object[] { value }
        );
    }


    @Step(value = "db.assert.table.not.exists.row.two.columns", args = { "table:word",
                    "column1:word", "value1:text", "column2:word", "value2:text" })
    public void assertRowNotExistsByTwoColumns(
        String table,
        String column1,
        String value1,
        String column2,
        String value2
    ) throws SQLException {
        helper.assertCountRowsInTableByColumns(
            matcherEmpty(),
            table,
            new String[] { column1, column2 },
            new Object[] { value1, value2 }
        );
    }


    @Step(value = "db.assert.table.not.exists.sql.where", args = { "table:word" })
    public void assertRowNotExistsByClause(String table, Document clause) throws SQLException, JSQLParserException {
        helper.assertCountRowsInTableByClause(matcherEmpty(), table, clause.getContent());
    }


    @Step(value = "db.assert.table.not.exists.data", args = "table:word")
    public void assertDataTableNotExists(
        String table,
        DataTable dataTable
    ) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            helper.assertDataSetNotExists(dataSet);
        }
    }


    @Step(value = "db.assert.table.count.row.one.column", args = { "table:word", "column:word",
                    "value:text", "matcher:long-assertion" })
    public void assertRowCountByOneColumn(
        String table,
        String column,
        String value,
        Assertion<Long> matcher
    ) throws SQLException {
        helper.assertCountRowsInTableByColumns(
            matcher,
            table,
            new String[] { column },
            new Object[] { value }
        );
    }


    @Step(value = "db.assert.table.count.row.two.columns", args = { "table:word", "column1:word",
                    "value1:text", "column2:word", "value2:text", "matcher:long-assertion" })
    public void assertRowCountByOneColumn(
        String table,
        String column1,
        String value1,
        String column2,
        String value2,
        Assertion<Long> matcher
    ) throws SQLException {
        helper.assertCountRowsInTableByColumns(
            matcher,
            table,
            new String[] { column1, column2 },
            new Object[] { value1, value2 }
        );
    }


    @Step(value = "db.assert.table.count.sql.where", args = { "table:word", "matcher:long-assertion" })
    public void assertRowCountByClause(
        String table,
        Assertion<Long> matcher,
        Document clause
    ) throws SQLException, JSQLParserException {
        helper.assertCountRowsInTableByClause(matcher, table, clause.getContent());
    }


    @Step(value = "db.assert.table.count.data", args = { "table:word", "matcher:long-assertion" })
    public void assertDataTableCount(
        String table,
        Assertion<Long> matcher,
        DataTable dataTable
    ) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            helper.assertCountRowsInTableByDataSet(dataSet, matcher);
        }
    }


    @Step("db.assert.table.not.exists.xls")
    public void assertXLSFileNotExists(
        File file
    ) throws InvalidFormatException, IOException, SQLException {
        try (MultiDataSet multiDataSet = new OoxmlDataSet(
            file, xlsIgnoreSheetRegex, nullSymbol
        )) {
            helper.assertMultiDataSetNotExists(multiDataSet);
        }
    }


    @Step(value = "db.assert.table.not.exists.csv", args = { "csv:file", "table:word" })
    public void assertCSVFileNotExists(File file, String table) throws IOException, SQLException {
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, csvDelimiter, nullSymbol)) {
            helper.assertDataSetNotExists(dataSet);
        }
    }


    @Step(value = "db.assert.table.empty", args = "word")
    public void assertTableIsEmpty(String table) throws SQLException, JSQLParserException {
        helper.assertCountRowsInTableByClause(matcherEmpty(), table, "1=1");
    }


    @Step(value = "db.assert.table.not.empty", args = "word")
    public void assertTableIsNotEmpty(String table) throws SQLException, JSQLParserException {
        helper.assertCountRowsInTableByClause(matcherNonEmpty(), table, "1=1");
    }


}