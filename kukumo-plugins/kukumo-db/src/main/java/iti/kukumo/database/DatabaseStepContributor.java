package iti.kukumo.database;

import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.annotations.TearDown;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import iti.kukumo.database.dataset.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ITI
 * Created by ITI on 18/04/19
 */
@Extension(provider="iti.kukumo", name="kukumo-database-steps", version="1.0")
@I18nResource("iti_kukumo_kukumo-database")
public class DatabaseStepContributor implements StepContributor {

    private static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.database");


    private static ConnectionManager connectionManager = Kukumo.getExtensionManager()
            .getExtension(ConnectionManager.class)
            .orElseThrow(()->new KukumoException("Cannot find a connection manager"));

    private final ConnectionParameters connectionParameters = new ConnectionParameters();

    private DatabaseHelper helper = new DatabaseHelper(this::connection);
    private Connection connection;
    private String xlsIgnoreSheetRegex;
    private String xlsNullSymbol;
    private String csvFormat;
    private boolean enableCleanupUponCompletion;


    public Connection connection() throws SQLException {
        if (connection == null) {
            connection = connectionManager.obtainConnection(connectionParameters);
        } else {
            connection = connectionManager.refreshConnection(connection,connectionParameters);
        }
        return connection;
    }


    public ConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }

    public void setXlsIgnoreSheetRegex(String ignoreSheetRegex) {
        this.xlsIgnoreSheetRegex = ignoreSheetRegex;
    }

    public void setXlsNullSymbol(String nullSymbol) {
        this.xlsNullSymbol = nullSymbol;
    }

    public void setCsvFormat(String csvFormat) {
        this.csvFormat = csvFormat;
    }

    public void setEnableCleanupUponCompletion(boolean enableCleanupUponCompletion) {
        this.enableCleanupUponCompletion = enableCleanupUponCompletion;
    }



    private Matcher<Long> matcherEmpty() {
        return Matchers.equalTo(0L);
    }

    private Matcher<Long> matcherNonEmpty() {
        return Matchers.greaterThan(0L);
    }


    @TearDown(order=1)
    public void cleanUp() {
        helper.cleanUp();
    }


    @TearDown(order=2)
    public void releaseConnection() throws SQLException {
        if (this.connection != null) {
            connectionManager.releaseConnection(connection);
        }
    }


    @Step("db.define.database.schema")
    public void setSchema(String schema) throws SQLException {
        connection().setSchema(schema);
    }


    @Step(value="db.define.connection.parameters", args={"url:text","username:text","password:text"})
    public void setConnectionParameters(String url, String username, String password) {
        LOGGER.debug("Setting database connection parameters [url={}, username={}, password={}]",url,username,password);
        this.connectionParameters.url(url).username(username).password(password);
    }


    @Step("db.action.script.document")
    public void executeSQLScript(Document document) throws IOException, SQLException {
        helper.executeSQLStatements(new SQLReader().parseStatements(new StringReader(document.getContent())));
    }


    @Step("db.action.script.file")
    public void executeSQLScript(File file) throws IOException, SQLException {
        try(Reader reader = new FileReader(file)) {
            helper.executeSQLStatements(new SQLReader().parseStatements(reader),file.toString());
        }
    }


    @Step(value="db.action.insert.from.data",args="word")
    public void insertFromDataTable(String table, DataTable dataTable) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table,dataTable)) {
            helper.deleteDataSet(dataSet);
            helper.insertDataSet(dataSet.copy(), enableCleanupUponCompletion);
        }
    }


    @Step("db.action.insert.from.xls")
    public void insertFromXLSFile(File file) throws IOException, SQLException {
        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, xlsNullSymbol)) {
            helper.deleteMultiDataSet(multiDataSet);
            helper.insertMultiDataSet(multiDataSet.copy(), enableCleanupUponCompletion);
        }
    }


    @Step(value="db.action.insert.from.csv", args={"csv:file","table:word"})
    public void insertFromCSVFile(File file, String table) throws IOException, SQLException {
        try (DataSet dataSet = new CsvDataSet(table,file,csvFormat)) {
            helper.deleteDataSet(dataSet);
            helper.insertDataSet(dataSet.copy(), enableCleanupUponCompletion);
        }
    }


    @Step(value="db.action.delete.from.data",args="word")
    public void deleteFromDataTable(String table, DataTable dataTable) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table,dataTable)) {
            helper.deleteDataSet(dataSet);
        }
    }


    @Step("db.action.delete.from.xls")
    public void deleteFromXLSFile(File file) throws IOException, SQLException, InvalidFormatException {
        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, xlsNullSymbol)) {
            helper.deleteMultiDataSet(multiDataSet);
        }
    }


    @Step(value="db.action.delete.from.csv", args={"csv:file","table:word"})
    public void deleteFromCSVFile(File file, String table) throws IOException, SQLException {
        try (DataSet dataSet = new CsvDataSet(table,file,csvFormat)) {
            helper.deleteDataSet(dataSet);
        }
    }


    @Step(value="db.action.clear.table.all",args="word")
    public void clearTable(String table) throws SQLException {
        helper.truncateTable(table);
    }


    @Step(value="db.action.clear.table.row.one.column",args= {"table:word","column:word","value:text"})
    public void clearTableRowOneColumn(String table, String column, String value) throws SQLException, IOException {
        try (DataSet dataSet = new InlineDataSet(table, new String[] {column}, new Object[] {value})) {
            helper.deleteDataSet(dataSet);
        }
    }


    @Step(value="db.action.clear.table.row.two.columns",args= {"table:word","column1:word","value1:text","column2:word","value2:text"})
    public void clearTableRowTwoColumns(String table, String column1, String value1, String column2, String value2)
    throws SQLException, IOException {
        try (DataSet dataSet = new InlineDataSet(table, new String[] {column1,column2}, new Object[] {value1,value2})) {
            helper.deleteDataSet(dataSet);
        }
    }


    @Step(value="db.assert.table.exists.row.single.id", args= {"id:text","table:word"})
    public void assertRowExistsBySingleId(String id, String table) throws SQLException {
        String keyColumn = helper.primaryKey(table, true)[0];
        helper.assertCountRowsInTableByColumns(matcherNonEmpty(), table, new String[] {keyColumn}, new Object[] {id});
    }


    @Step(value="db.assert.table.exists.row.one.column", args= {"table:word","column:word","value:text"})
    public void assertRowExistsByOneColumn(String table, String column, String value) throws SQLException {
        helper.assertCountRowsInTableByColumns(matcherNonEmpty(), table, new String[] {column}, new Object[] {value});
    }


    @Step(value="db.assert.table.exists.row.two.columns", args= {"table:word","column1:word","value1:text","column2:word","value2:text"})
    public void assertRowExistsByTwoColumns(String table, String column1, String value1, String column2, String value2)
    throws SQLException {
        helper.assertCountRowsInTableByColumns(matcherNonEmpty(), table, new String[] {column1,column2}, new Object[] {value1,value2});
    }


    @Step(value="db.assert.table.exists.sql.where", args= {"table:word","sql:text"})
    public void assertRowExistsByClause(String table, String clause) throws SQLException {
        helper.assertCountRowsInTableByClause(matcherNonEmpty(), table, clause);
    }


    @Step(value="db.assert.table.exists.data",args="word")
    public void assertDataTableExists(String table, DataTable dataTable) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table,dataTable)) {
            helper.assertDataSetExists(dataSet);
        }
    }


    @Step("db.assert.table.exists.xls")
    public void assertXLSFileExists(File file) throws InvalidFormatException, IOException, SQLException {
        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, xlsNullSymbol)) {
            helper.assertMultiDataSetExists(multiDataSet);
        }
    }

    @Step(value="db.assert.table.exists.csv", args= {"csv:file","table:word"})
    public void assertCSVFileExists(File file, String table) throws IOException, SQLException {
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat)) {
            helper.assertDataSetExists(dataSet);
        }
    }


    @Step(value="db.assert.table.not.exists.row.single.id", args= {"id:text","table:word"})
    public void assertRowNotExistsBySingleId(String id, String table) throws SQLException {
        String keyColumn = helper.primaryKey(table, true)[0];
        helper.assertCountRowsInTableByColumns(matcherEmpty(), table, new String[] {keyColumn}, new Object[] {id});
    }


    @Step(value="db.assert.table.not.exists.row.one.column", args= {"table:word","column:word","value:text"})
    public void assertRowNotExistsByOneColumn(String table, String column, String value) throws SQLException {
        helper.assertCountRowsInTableByColumns(matcherEmpty(), table, new String[] {column}, new Object[] {value});
    }


    @Step(value="db.assert.table.not.exists.row.two.columns", args= {"table:word","column1:word","value1:text","column2:word","value2:text"})
    public void assertRowNotExistsByTwoColumns(String table, String column1, String value1, String column2, String value2)
    throws SQLException {
        helper.assertCountRowsInTableByColumns(matcherEmpty(), table, new String[] {column1,column2}, new Object[] {value1,value2});
    }


    @Step(value="db.assert.table.not.exists.sql.where", args= {"table:word","sql:text"})
    public void assertRowNotExistsByClause(String table, String clause) throws SQLException {
        helper.assertCountRowsInTableByClause(matcherEmpty(), table, clause);
    }


    @Step(value="db.assert.table.not.exists.data",args="word")
    public void assertDataTableNotExists(String table, DataTable dataTable) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table,dataTable)) {
            helper.assertDataSetNotExists(dataSet);
        }
    }


    @Step(value="db.assert.table.count.row.single.id", args= {"id:text","table:word","matcher:long-assertion"})
    public void assertRowCountBySingleId(String id, String table, Matcher<Long> matcher) throws SQLException {
        String keyColumn = helper.primaryKey(table, true)[0];
        helper.assertCountRowsInTableByColumns(matcher, table, new String[] {keyColumn}, new Object[] {id});
    }


    @Step(value="db.assert.table.count.row.one.column", args= {"table:word","column:word","value:text","matcher:long-assertion"})
    public void assertRowCountByOneColumn(String table, String column, String value, Matcher<Long> matcher) throws SQLException {
        helper.assertCountRowsInTableByColumns(matcher, table, new String[] {column}, new Object[] {value});
    }


    @Step(value="db.assert.table.count.row.two.columns", args= {"table:word","column1:word","value1:text","column2:word","value2:text","matcher:long-assertion"})
    public void assertRowCountByOneColumn(String table, String column1, String value1, String column2, String value2, Matcher<Long> matcher)
    throws SQLException {
        helper.assertCountRowsInTableByColumns(matcher, table, new String[] {column1,column2}, new Object[] {value1,value2});
    }


    @Step(value="db.assert.table.count.sql.where", args= {"table:word","sql:text","matcher:long-assertion"})
    public void assertRowCountByClause(String table, String clause, Matcher<Long> matcher) throws SQLException {
        helper.assertCountRowsInTableByClause(matcher, table, clause);
    }


    @Step(value="db.assert.table.count.data",args= {"table:word","matcher:long-assertion"})
    public void assertDataTableCount(String table, Matcher<Long> matcher, DataTable dataTable) throws IOException, SQLException {
        try (DataSet dataSet = new DataTableDataSet(table,dataTable)) {
            helper.assertCountRowsInTableByDataSet(dataSet,matcher);
        }
    }


    @Step("db.assert.table.not.exists.xls")
    public void assertXLSFileNotExists(File file) throws InvalidFormatException, IOException, SQLException {
        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, xlsNullSymbol)) {
            helper.assertMultiDataSetNotExists(multiDataSet);
        }
    }

    @Step(value="db.assert.table.not.exists.csv", args= {"csv:file","table:word"})
    public void assertCSVFileNotExists(File file, String table) throws IOException, SQLException {
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat)) {
            helper.assertDataSetNotExists(dataSet);
        }
    }


    @Step(value="db.assert.table.empty", args="word")
    public void assertTableIsEmpty(String table) throws SQLException {
       helper.assertCountRowsInTableByClause(matcherEmpty(),table,"1=1");
    }

    @Step(value="db.assert.table.not.empty",args="word")
    public void assertTableIsNotEmpty(String table) throws SQLException {
        helper.assertCountRowsInTableByClause(matcherNonEmpty(),table,"1=1");
    }





}
