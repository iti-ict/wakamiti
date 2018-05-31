/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.database;

import java.util.Locale;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import iti.commons.testing.AbstractLocaleSteps;
import iti.commons.testing.TestingException;
import iti.commons.testing.database.loaders.CSVDataLoader;
import iti.commons.testing.database.loaders.CucumberDataTableLoader;
import iti.commons.testing.database.loaders.XLSDataLoader;

public class DatabaseSteps extends AbstractLocaleSteps<DatabaseHelper> implements En {

    private static final String CRITERIA_TRUE = "1=1";

    protected static final String SET_PREDEFINED_TABLE = "set.predefined.table";

    protected static final String CLEANUP_TABLE = "cleanup.table";
    protected static final String CLEANUP_PREDEFINED_TABLE = "cleanup.predefined.table";
    protected static final String CLEANUP_TABLE_CRITERIA = "cleanup.table.criteria";
    protected static final String CLEANUP_PREDEFINED_TABLE_CRITERIA = "cleanup.predefined.table.criteria";
    protected static final String CLEANUP_SQL_SENTENCE = "cleanup.sql.sentence";
    protected static final String CLEANUP_SQL_SCRIPT = "cleanup.sql.script";

    protected static final String LOAD_TABLE_FROM_CUCUMBER_TABLE = "load.table.from.cucumber.table";
    protected static final String LOAD_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE = "load.predefined.table.from.cucumber.table";
    protected static final String LOAD_TABLE_FROM_CSV = "load.table.from.csv";
    protected static final String LOAD_PREDEFINED_TABLE_FROM_CSV = "load.predefined.table.from.csv";
    protected static final String LOAD_TABLE_FROM_XLS = "load.table.from.xls";
    protected static final String LOAD_SQL_SENTENCE = "load.sql.sentence";
    protected static final String LOAD_SQL_SCRIPT = "load.sql.script";

    protected static final String ACTION_INSERT_DATA_IN_TABLE_FROM_CUCUMBER_TABLE = "action.insert.data.in.table.from.cucumber.table";
    protected static final String ACTION_INSERT_DATA_IN_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE = "action.insert.data.in.predefined.table.from.cucumber.table";
    protected static final String ACTION_INSERT_DATA_IN_TABLE_FROM_CSV = "action.insert.data.in.table.from.csv";
    protected static final String ACTION_INSERT_DATA_IN_PREDEFINED_TABLE_FROM_CSV = "action.insert.data.in.predefined.table.from.csv";
    protected static final String ACTION_INSERT_DATA_FROM_XLS = "action.insert.data.from.xls";
    protected static final String ACTION_UPDATE_DATA_IN_TABLE_FROM_CUCUMBER_TABLE = "action.update.data.in.table.from.cucumber.table";
    protected static final String ACTION_UPDATE_DATA_IN_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE = "action.update.data.in.predefined.table.from.cucumber.table";
    protected static final String ACTION_UPDATE_DATA_IN_TABLE_FROM_CSV = "action.update.data.in.table.from.csv";
    protected static final String ACTION_UPDATE_DATA_IN_PREDEFINED_TABLE_FROM_CSV = "action.update.data.in.predefined.table.from.csv";
    protected static final String ACTION_UPDATE_DATA_FROM_XLS = "action.update.data.from.xls";
    protected static final String ACTION_DELETE_TABLE_WHERE_CRITERIA = "action.delete.table.where.criteria";
    protected static final String ACTION_DELETE_PREDEFINED_TABLE_WHERE_CRITERIA = "action.delete.predefined.table.where.criteria";
    protected static final String ACTION_DELETE_TABLE = "action.delete.table";
    protected static final String ACTION_DELETE_PREDEFINED_TABLE = "action.delete.predefined.table";
    protected static final String ACTION_SQL_SENTENCE = "action.sql.sentence";
    protected static final String ACTION_SQL_SCRIPT = "action.sql.script";


    protected static final String ASSERT_DATA_EXISTS_IN_TABLE_FROM_CUCUMBER_TABLE = "assert.data.exists.in.table.from.cucumber.table";
    protected static final String ASSERT_DATA_EXISTS_IN_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE = "assert.data.exists.in.predefined.table.from.cucumber.table";
    protected static final String ASSERT_DATA_EXISTS_IN_TABLE_FROM_CSV = "assert.data.exists.in.table.from.csv";
    protected static final String ASSERT_DATA_EXISTS_IN_PREDEFINED_TABLE_FROM_CSV = "assert.data.exists.in.predefined.table.from.csv";
    protected static final String ASSERT_DATA_EXISTS_FROM_XLS = "assert.data.exists.from.xls";
    protected static final String ASSERT_DATA_NOT_EXISTS_IN_TABLE_FROM_CUCUMBER_TABLE = "assert.data.not.exists.in.table.from.cucumber.table";
    protected static final String ASSERT_DATA_NOT_EXISTS_IN_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE = "assert.data.not.exists.in.predefined.table.from.cucumber.table";
    protected static final String ASSERT_DATA_NOT_EXISTS_IN_TABLE_FROM_CSV = "assert.data.not.exists.in.table.from.csv";
    protected static final String ASSERT_DATA_NOT_EXISTS_IN_PREDEFINED_TABLE_FROM_CSV = "assert.data.not.exists.in.predefined.table.from.csv";
    protected static final String ASSERT_DATA_NOT_EXISTS_FROM_XLS = "assert.data.not.exists.from.xls";
    protected static final String ASSERT_ID_EXISTS_IN_TABLE = "assert.id.exists.in.table";
    protected static final String ASSERT_ID_EXISTS_IN_PREDEFINED_TABLE = "assert.id.exists.in.predefined.table";
    protected static final String ASSERT_ID_NOT_EXISTS_IN_TABLE = "assert.id.not.exists.in.table";
    protected static final String ASSERT_ID_NOT_EXISTS_IN_PREDEFINED_TABLE = "assert.id.not.exists.in.predefined.table";
    protected static final String ASSERT_COUNT_TABLE_WHERE_CRITERIA ="assert.count.table.where.criteria";
    protected static final String ASSERT_COUNT_PREDEFINED_TABLE_WHERE_CRITERIA ="assert.count.predefined.table.where.criteria";
    protected static final String ASSERT_COUNT_TABLE ="assert.count.table";
    protected static final String ASSERT_COUNT_PREDEFINED_TABLE ="assert.count.predefined.table";
    protected static final String ASSERT_EMPTY_TABLE_WHERE_CRITERIA ="assert.empty.table.where.criteria";
    protected static final String ASSERT_EMPTY_PREDEFINED_TABLE_WHERE_CRITERIA ="assert.empty.predefined.table.where.criteria";
    protected static final String ASSERT_NOT_EMPTY_TABLE_WHERE_CRITERIA ="assert.not.empty.table.where.criteria";
    protected static final String ASSERT_NOT_EMPTY_PREDEFINED_TABLE_WHERE_CRITERIA ="assert.not.empty.predefined.table.where.criteria";
    protected static final String ASSERT_EMPTY_TABLE ="assert.empty.table";
    protected static final String ASSERT_EMPTY_PREDEFINED_TABLE ="assert.empty.predefined.table";
    protected static final String ASSERT_NOT_EMPTY_TABLE ="assert.not.empty.table";
    protected static final String ASSERT_NOT_EMPTY_PREDEFINED_TABLE ="assert.not.empty.predefined.table";


    private String predefinedTable;


    public DatabaseSteps (DatabaseHelper helper) {
        super(helper, "databaseSteps");
    }


    public DatabaseSteps(DatabaseHelper helper, ClassLoader classLoader, Locale locale, String localeDefinitionFile) {
        super(helper, classLoader, locale, localeDefinitionFile);
    }

    public DatabaseSteps(DatabaseHelper helper, String localeDefinitionFile) {
        super(helper, localeDefinitionFile);
    }



    @Override
    public void registerSteps() {

        Given(resolve(SET_PREDEFINED_TABLE),
            this::setPredefinedTable);

        Given(resolve(CLEANUP_TABLE), (String table)->
            helper.addCleanUpTable(table,CRITERIA_TRUE));
        Given(resolve(CLEANUP_PREDEFINED_TABLE), ()->
            helper.addCleanUpTable(predefinedTable(),CRITERIA_TRUE));
        Given(resolve(CLEANUP_TABLE_CRITERIA), (String table,String criteria)->
            helper.addCleanUpTable(table,criteria));
        Given(resolve(CLEANUP_PREDEFINED_TABLE_CRITERIA), (String criteria)->
            helper.addCleanUpTable(predefinedTable(),criteria));
        Given(resolve(CLEANUP_SQL_SENTENCE), (String sql)->
            helper.addCleanUpSentence(sql));
        Given(resolve(CLEANUP_SQL_SCRIPT), (String file)->
            helper.addCleanUpScript(file));

        Given(resolve(LOAD_TABLE_FROM_CUCUMBER_TABLE), (String table, DataTable data)->
            helper.insertData(new CucumberDataTableLoader(), table, data));
        Given(resolve(LOAD_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE), (DataTable data)->
            helper.insertData(new CucumberDataTableLoader(), predefinedTable(), data));
        Given(resolve(LOAD_TABLE_FROM_CSV), (String table, String file)->
            helper.insertData(new CSVDataLoader(), table, file));
        Given(resolve(LOAD_PREDEFINED_TABLE_FROM_CSV), (String file)->
            helper.insertData(new CSVDataLoader(), predefinedTable(), file));
        Given(resolve(LOAD_TABLE_FROM_XLS), (String file)->
            helper.insertData(new XLSDataLoader(), file));
        Given(resolve(LOAD_SQL_SENTENCE), (String sql)->
            helper.executeSentence(sql));
        Given(resolve(LOAD_SQL_SCRIPT), (String file)->
            helper.executeScript(file));


        When(resolve(ACTION_INSERT_DATA_IN_TABLE_FROM_CUCUMBER_TABLE), (String table, DataTable data)->
            helper.insertData(new CucumberDataTableLoader(), table, data));
        When(resolve(ACTION_INSERT_DATA_IN_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE), (DataTable data)->
            helper.insertData(new CucumberDataTableLoader(), predefinedTable(), data));
        When(resolve(ACTION_INSERT_DATA_IN_TABLE_FROM_CSV), (String table, String file)->
            helper.insertData(new CSVDataLoader(), table, file));
        When(resolve(ACTION_INSERT_DATA_IN_PREDEFINED_TABLE_FROM_CSV), (String file)->
            helper.insertData(new CSVDataLoader(), predefinedTable(), file));
        When(resolve(ACTION_INSERT_DATA_FROM_XLS), (String file)->
            helper.insertData(new XLSDataLoader(), file));
        When(resolve(ACTION_UPDATE_DATA_IN_TABLE_FROM_CUCUMBER_TABLE), (String table, DataTable data)->
            helper.updateData(new CucumberDataTableLoader(), table, data));
        When(resolve(ACTION_UPDATE_DATA_IN_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE), (DataTable data)->
            helper.updateData(new CucumberDataTableLoader(), predefinedTable(), data));
        When(resolve(ACTION_UPDATE_DATA_IN_TABLE_FROM_CSV), (String table, String file)->
            helper.updateData(new CSVDataLoader(), table, file));
        When(resolve(ACTION_UPDATE_DATA_IN_PREDEFINED_TABLE_FROM_CSV), (String file)->
            helper.updateData(new CSVDataLoader(), predefinedTable(), file));
        When(resolve(ACTION_UPDATE_DATA_FROM_XLS), (String file)->
            helper.updateData(new XLSDataLoader(), file));
        When(resolve(ACTION_DELETE_TABLE_WHERE_CRITERIA), (String table, String criteria)->
            helper.deleteData(table,criteria));
        When(resolve(ACTION_DELETE_PREDEFINED_TABLE_WHERE_CRITERIA), (String criteria)->
            helper.deleteData(predefinedTable(),criteria));
        When(resolve(ACTION_DELETE_TABLE), (String table)->
            helper.deleteData(table,CRITERIA_TRUE));
        When(resolve(ACTION_DELETE_PREDEFINED_TABLE), ()->
            helper.deleteData(predefinedTable(), CRITERIA_TRUE));
        Given(resolve(ACTION_SQL_SENTENCE), (String sql)->
            helper.executeSentence(sql));
        Given(resolve(ACTION_SQL_SCRIPT), (String file)->
            helper.executeScript(file));



        Then(resolve(ASSERT_DATA_EXISTS_IN_TABLE_FROM_CUCUMBER_TABLE), (String table, DataTable data)->
            helper.assertDataExists(new CucumberDataTableLoader(), table, data));
        Then(resolve(ASSERT_DATA_EXISTS_IN_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE), (DataTable data)->
            helper.assertDataExists(new CucumberDataTableLoader(), predefinedTable(), data));
        Then(resolve(ASSERT_DATA_EXISTS_IN_TABLE_FROM_CSV), (String table, String file)->
            helper.assertDataExists(new CSVDataLoader(), table, file));
        Then(resolve(ASSERT_DATA_EXISTS_IN_PREDEFINED_TABLE_FROM_CSV), (String file)->
            helper.assertDataExists(new CSVDataLoader(), predefinedTable(), file));
        Then(resolve(ASSERT_DATA_EXISTS_FROM_XLS), (String file)->
            helper.assertDataExists(new XLSDataLoader(), file));

        Then(resolve(ASSERT_DATA_NOT_EXISTS_IN_TABLE_FROM_CUCUMBER_TABLE), (String table, DataTable data)->
            helper.assertDataNotExists(new CucumberDataTableLoader(), table, data));
        Then(resolve(ASSERT_DATA_NOT_EXISTS_IN_PREDEFINED_TABLE_FROM_CUCUMBER_TABLE), (DataTable data)->
            helper.assertDataNotExists(new CucumberDataTableLoader(), predefinedTable(), data));
        Then(resolve(ASSERT_DATA_NOT_EXISTS_IN_TABLE_FROM_CSV), (String table, String file)->
            helper.assertDataNotExists(new CSVDataLoader(), table, file));
        Then(resolve(ASSERT_DATA_NOT_EXISTS_IN_PREDEFINED_TABLE_FROM_CSV), (String file)->
            helper.assertDataNotExists(new CSVDataLoader(), predefinedTable(), file));
        Then(resolve(ASSERT_DATA_NOT_EXISTS_FROM_XLS), (String file)->
            helper.assertDataNotExists(new XLSDataLoader(), file));

        Then(resolve(ASSERT_ID_EXISTS_IN_TABLE), (String id, String table)->
            helper.assertIDExistsInTable(id, table));
        Then(resolve(ASSERT_ID_EXISTS_IN_PREDEFINED_TABLE), (String id)->
            helper.assertIDExistsInTable(id, predefinedTable()));
        Then(resolve(ASSERT_ID_NOT_EXISTS_IN_TABLE), (String id, String table)->
            helper.assertIDNotExistsInTable(id, table));
        Then(resolve(ASSERT_ID_NOT_EXISTS_IN_PREDEFINED_TABLE), (String id)->
            helper.assertIDNotExistsInTable(id, predefinedTable()));

        Then(resolve(ASSERT_COUNT_TABLE_WHERE_CRITERIA), (Integer rowCount, String table, String criteria)->
            helper.assertRowCountInTable(table,criteria,rowCount));
        Then(resolve(ASSERT_COUNT_PREDEFINED_TABLE_WHERE_CRITERIA), (Integer rowCount, String criteria)->
            helper.assertRowCountInTable(predefinedTable(),criteria,rowCount));
        Then(resolve(ASSERT_COUNT_TABLE), (Integer rowCount, String table)->
            helper.assertRowCountInTable(table,CRITERIA_TRUE,rowCount));
        Then(resolve(ASSERT_COUNT_PREDEFINED_TABLE), (Integer rowCount)->
            helper.assertRowCountInTable(predefinedTable(),CRITERIA_TRUE,rowCount));

        Then(resolve(ASSERT_EMPTY_TABLE_WHERE_CRITERIA), (String table, String criteria)->
            helper.assertTableIsEmpty(table,criteria));
        Then(resolve(ASSERT_EMPTY_TABLE), (String table)->
            helper.assertTableIsEmpty(table,CRITERIA_TRUE));
        Then(resolve(ASSERT_EMPTY_PREDEFINED_TABLE_WHERE_CRITERIA), (String criteria)->
            helper.assertTableIsEmpty(predefinedTable(),criteria));
        Then(resolve(ASSERT_EMPTY_PREDEFINED_TABLE), ()->
            helper.assertTableIsEmpty(predefinedTable(),CRITERIA_TRUE));

        Then(resolve(ASSERT_NOT_EMPTY_TABLE_WHERE_CRITERIA), (String table, String criteria)->
            helper.assertTableIsNotEmpty(table,criteria));
        Then(resolve(ASSERT_NOT_EMPTY_TABLE), (String table)->
            helper.assertTableIsNotEmpty(table,CRITERIA_TRUE));
        Then(resolve(ASSERT_NOT_EMPTY_PREDEFINED_TABLE_WHERE_CRITERIA), (String criteria)->
            helper.assertTableIsNotEmpty(predefinedTable(),criteria));
        Then(resolve(ASSERT_NOT_EMPTY_PREDEFINED_TABLE), ()->
            helper.assertTableIsNotEmpty(predefinedTable(),CRITERIA_TRUE));

        After(100,helper::performCleanUp);
    }




    public void setPredefinedTable (String predefinedTabled) {
        this.predefinedTable = predefinedTabled;
    }

    public String predefinedTable() {
        if (predefinedTable == null) {
            throw new TestingException("predefined table is not set");
        }
        return predefinedTable;
    }

}
