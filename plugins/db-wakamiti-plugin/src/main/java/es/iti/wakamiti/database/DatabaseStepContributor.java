/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.database.dataset.*;
import es.iti.wakamiti.database.jdbc.ConnectionProvider;
import es.iti.wakamiti.database.jdbc.Database;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static es.iti.wakamiti.database.jdbc.LogUtils.message;


/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
@Extension(provider = "es.iti.wakamiti", name = "database-steps", version = "1.1")
@I18nResource("iti_wakamiti_wakamiti-database")
public class DatabaseStepContributor extends DatabaseSupport implements StepContributor {

    @TearDown(order = 1)
    public void cleanUp() {
        cleanUpOperations.forEach(Runnable::run);
    }

    @TearDown(order = 2)
    public void releaseConnection() {
        connections.values().forEach(ConnectionProvider::close);
        connections.clear();
    }

    /**
     * Configure the default database connection URL, username and
     * password for following connections.
     *
     * @param url      The URL connection
     * @param username The username
     * @param password The password
     */
    @Step(value = "db.define.connection.parameters", args = {"url:text", "username:text", "password:text"})
    public void setConnectionParameters(String url, String username, String password) {
        ConnectionParameters parameters = connections.containsKey(DEFAULT)
                ? connections.get(DEFAULT).parameters()
                : new ConnectionParameters();
        addConnection(parameters.url(url).username(username).password(password));
    }

    /**
     * Configure a named database connection URL, username and
     * password for following connections.
     *
     * @param url      The URL connection
     * @param username The username
     * @param password The password
     * @param alias    The database connection name
     */
    @Step(value = "db.define.connection.parameters.alias",
            args = {"url:text", "username:text", "password:text", "alias:text"})
    public void setConnectionParameters(String url, String username, String password, String alias) {
        ConnectionParameters parameters = connections.containsKey(alias)
                ? connections.get(alias).parameters()
                : new ConnectionParameters();
        addConnection(alias, parameters.url(url).username(username).password(password));
    }

    /**
     * Sets the SQL statements that will be executed by the default
     * SQL connection after the scenario ends, regardless execution
     * status.
     *
     * @param document The script content
     */
    @Step("db.define.cleanup.document")
    public void setCleanupScript(Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.executeSQLScript(document);
        });
    }

    /**
     * Sets the SQL statements that will be executed by a named SQL
     * connection after the scenario ends, regardless execution status.
     *
     * @param alias    The SQL connection name
     * @param document The script content
     */
    @Step(value = "db.define.cleanup.document.alias", args = {"alias:text"})
    public void setCleanupScript(String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.executeSQLScript(document);
        });
    }

    /**
     * Sets the SQL statements that will be executed by the default SQL
     * connection after the scenario ends, regardless execution status.
     *
     * @param file The script content
     */
    @Step(value = "db.define.cleanup.file", args = {"script:file"})
    public void setCleanupScript(File file) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.executeSQLScript(file);
        });
    }

    /**
     * Sets the SQL statements that will be executed by a named SQL
     * connection after the scenario ends, regardless execution status.
     *
     * @param file  The script content
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.cleanup.file.alias", args = {"script:file", "alias:text"})
    public void setCleanupScript(File file, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.executeSQLScript(file);
        });
    }

    /**
     * Clear the given table, first attempting to execute
     * {@code TRUNCATE}, and then using {@code DELETE FROM} as fallback,
     * using the default SQL connection after the scenario ends,
     * regardless execution status.
     *
     * @param table The table name
     */
    @Step(value = "db.define.cleanup.clear.all", args = {"table:word"})
    public void setCleanupClear(String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.clearTable(table);
        });
    }

    /**
     * Clear the given table, first attempting to execute
     * {@code TRUNCATE}, and then using {@code DELETE FROM} as fallback,
     * using a named SQL connection after the scenario ends, regardless
     * execution status.
     *
     * @param table The table name
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.cleanup.clear.all.alias", args = {"table:word", "alias:text"})
    public void setCleanupClear(String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.clearTable(table);
        });
    }

    /**
     * Deletes rows from a given table that match the specified condition
     * using the default SQL connection after the scenario ends,
     * regardless execution status.
     *
     * @param column The column name
     * @param value  The column value
     * @param table  The table name
     */
    @Step(value = "db.define.cleanup.clear.row", args = {"column:word", "value:text", "table:word"})
    public void setCleanupClear(String column, String value, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.clearTableByRow(table, column, value);
        });
    }

    /**
     * Deletes rows from a given table that match the specified condition
     * using a named SQL connection after the scenario ends, regardless
     * execution status.
     *
     * @param column The column name
     * @param value  The column value
     * @param table  The table name
     * @param alias  The SQL connection name
     */
    @Step(value = "db.define.cleanup.clear.row.alias",
            args = {"column:word", "value:text", "table:word", "alias:text"})
    public void setCleanupClear(String column, String value, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.clearTableByRow(table, column, value);
        });
    }

    /**
     * Deletes rows from a given table that match the specified where
     * clause using the default SQL connection after the scenario ends,
     * regardless execution status.
     *
     * @param table    The table name
     * @param document The where clause
     */
    @Step(value = "db.define.cleanup.clear.where", args = {"table:word"})
    public void setCleanupClear(String table, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.clearTableByClause(table, document);
        });
    }

    /**
     * Deletes rows from a given table that match the specified where
     * clause using a named SQL connection after the scenario ends,
     * regardless execution status.
     *
     * @param table    The table name
     * @param alias    The SQL connection name
     * @param document The where clause
     */
    @Step(value = "db.define.cleanup.clear.where.alias", args = {"table:word", "alias:text"})
    public void setCleanupClear(String table, String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.clearTableByClause(table, document);
        });
    }

    /**
     * Deletes the specified rows in a data table from a given table
     * using the default SQL connection after the scenario ends,
     * regardless execution status.
     *
     * @param table The table name
     * @param data  The rows
     */
    @Step(value = "db.define.cleanup.delete.from.data", args = {"table:word"})
    public void setCleanupDelete(String table, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.deleteFromDataTable(table, data);
        });
    }

    /**
     * Deletes the specified rows in a data table from a given table
     * using a named SQL connection after the scenario ends,
     * regardless execution status.
     *
     * @param table The table name
     * @param alias The SQL connection name
     * @param data  The rows
     */
    @Step(value = "db.define.cleanup.delete.from.data.alias", args = {"table:word", "alias:text"})
    public void setCleanupDelete(String table, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.deleteFromDataTable(table, data);
        });
    }

    @Step(value = "db.define.cleanup.delete.from.xls", args = {"xls:file"})
    public void setCleanupDeleteXLS(File xls) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.deleteFromXLSFile(xls);
        });
    }

    @Step(value = "db.define.cleanup.delete.from.xls.alias", args = {"xls:file", "alias:text"})
    public void setCleanupDeleteXLS(File xls, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.deleteFromXLSFile(xls);
        });
    }

    @Step(value = "db.define.cleanup.delete.from.csv", args = {"csv:file", "table:word"})
    public void setCleanupDeleteCSV(File csv, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.deleteFromCSVFile(csv, table);
        });
    }

    @Step(value = "db.define.cleanup.delete.from.csv.alias", args = {"csv:file", "table:word", "alias:text"})
    public void setCleanupDeleteCSV(File csv, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.deleteFromCSVFile(csv, table);
        });
    }

    @Step(value = "db.define.cleanup.insert.from.data", args = {"table:word"})
    public void setCleanupInsert(String table, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.insertFromDataTable(table, data);
        });
    }

    @Step(value = "db.define.cleanup.insert.from.data.alias", args = {"table:word", "alias:text"})
    public void setCleanupInsert(String table, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.insertFromDataTable(table, data);
        });
    }

    @Step(value = "db.define.cleanup.insert.from.xls", args = {"xls:file"})
    public void setCleanupInsertXLS(File xls) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.insertFromXLSFile(xls);
        });
    }

    @Step(value = "db.define.cleanup.insert.from.xls.alias", args = {"xls:file", "alias:text"})
    public void setCleanupInsertXLS(File xls, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.insertFromXLSFile(xls);
        });
    }

    @Step(value = "db.define.cleanup.insert.from.csv", args = {"csv:file", "table:word"})
    public void setCleanupInsertCSV(File csv, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.insertFromCSVFile(csv, table);
        });
    }

    @Step(value = "db.define.cleanup.insert.from.csv.alias", args = {"csv:file", "table:word", "alias:text"})
    public void setCleanupInsertCSV(File csv, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.insertFromCSVFile(csv, table);
        });
    }

    @Step(value = "db.define.assert.table.exists.row.single.id", args = {"id:text", "table:word"})
    public void setCleanupAssertRowExistsBySingleId(String id, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowExistsBySingleId(id, table);
        });
    }

    @Step(value = "db.define.assert.table.exists.row.single.id.alias", args = {"id:text", "table:word", "alias:text"})
    public void setCleanupAssertRowExistsBySingleId(String id, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowExistsBySingleId(id, table);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.row.single.id", args = {"id:text", "table:word"})
    public void setCleanupAssertRowNotExistsBySingleId(String id, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowNotExistsBySingleId(id, table);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.row.single.id.alias",
            args = {"id:text", "table:word", "alias:text"})
    public void setCleanupAssertRowNotExistsBySingleId(String id, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowNotExistsBySingleId(id, table);
        });
    }

    @Step(value = "db.define.assert.table.exists.row.one.column", args = {"column:word", "value:text", "table:word"})
    public void setCleanupAssertRowExistsByOneColumn(String column, String value, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowExistsByOneColumn(column, value, table);
        });
    }

    @Step(value = "db.define.assert.table.exists.row.one.column.alias",
            args = {"column:word", "value:text", "table:word", "alias:text"})
    public void setCleanupAssertRowExistsByOneColumn(String column, String value, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowExistsByOneColumn(column, value, table);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.row.one.column",
            args = {"column:word", "value:text", "table:word"})
    public void setCleanupAssertRowNotExistsByOneColumn(String column, String value, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowNotExistsByOneColumn(column, value, table);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.row.one.column.alias",
            args = {"column:word", "value:text", "table:word", "alias:text"})
    public void setCleanupAssertRowNotExistsByOneColumn(String column, String value, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowNotExistsByOneColumn(column, value, table);
        });
    }

    @Step(value = "db.define.assert.table.count.row.one.column",
            args = {"column:word", "value:text", "table:word", "matcher:long-assertion"})
    public void setCleanupAssertRowCountByOneColumn(
            String column, String value, String table, Assertion<Long> matcher) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowCountByOneColumn(column, value, table, matcher);
        });
    }

    @Step(value = "db.define.assert.table.count.row.one.column.alias",
            args = {"column:word", "value:text", "table:word", "matcher:long-assertion", "alias:text"})
    public void setCleanupAssertRowCountByOneColumn(
            String column, String value, String table, Assertion<Long> matcher, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowCountByOneColumn(column, value, table, matcher);
        });
    }

    @Step(value = "db.define.assert.table.exists.sql.where", args = {"table:word"})
    public void setCleanupAssertRowExistsByClause(String table, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowExistsByClause(table, document);
        });
    }

    @Step(value = "db.define.assert.table.exists.sql.where.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertRowExistsByClause(String table, String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowExistsByClause(table, document);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.sql.where", args = {"table:word"})
    public void setCleanupAssertRowNotExistsByClause(String table, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowNotExistsByClause(table, document);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.sql.where.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertRowNotExistsByClause(String table, String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowNotExistsByClause(table, document);
        });
    }

    @Step(value = "db.define.assert.table.count.sql.where", args = {"table:word", "matcher:long-assertion"})
    public void setCleanupAssertRowCountByClause(String table, Assertion<Long> matcher, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowCountByClause(table, matcher, document);
        });
    }

    @Step(value = "db.define.assert.table.count.sql.where.alias",
            args = {"table:word", "matcher:long-assertion", "alias:text"})
    public void setCleanupAssertRowCountByClause(
            String table, Assertion<Long> matcher, String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowCountByClause(table, matcher, document);
        });
    }

    @Step(value = "db.define.assert.table.exists.data", args = {"table:word"})
    public void setCleanupAssertDataTableExists(String table, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertDataTableExists(table, data);
        });
    }

    @Step(value = "db.define.assert.table.exists.data.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertDataTableExists(String table, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertDataTableExists(table, data);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.data", args = {"table:word"})
    public void setCleanupAssertDataTableNotExists(String table, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertDataTableNotExists(table, data);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.data.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertDataTableNotExists(String table, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertDataTableNotExists(table, data);
        });
    }

    @Step(value = "db.define.assert.table.count.data", args = {"table:word", "matcher:long-assertion"})
    public void setCleanupAssertDataTableCount(String table, Assertion<Long> matcher, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertDataTableCount(table, matcher, data);
        });
    }

    @Step(value = "db.define.assert.table.count.data.alias",
            args = {"table:word", "matcher:long-assertion", "alias:text"})
    public void setCleanupAssertDataTableCount(String table, Assertion<Long> matcher, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertDataTableCount(table, matcher, data);
        });
    }

    @Step(value = "db.define.assert.table.exists.xls", args = {"xls:file"})
    public void setCleanupAssertXLSFileExists(File xls) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertXLSFileExists(xls);
        });
    }

    @Step(value = "db.define.assert.table.exists.xls.alias", args = {"xls:file", "alias:text"})
    public void setCleanupAssertXLSFileExists(File xls, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertXLSFileExists(xls);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.xls", args = {"xls:file"})
    public void setCleanupAssertXLSFileNotExists(File xls) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertXLSFileNotExists(xls);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.xls.alias", args = {"xls:file", "alias:text"})
    public void setCleanupAssertXLSFileNotExists(File xls, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertXLSFileNotExists(xls);
        });
    }

    @Step(value = "db.define.assert.table.exists.csv", args = {"cav:file", "table:word"})
    public void setCleanupAssertCSVFileExists(File csv, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertCSVFileExists(csv, table);
        });
    }

    @Step(value = "db.define.assert.table.exists.csv.alias", args = {"cav:file", "table:word", "alias:text"})
    public void setCleanupAssertCSVFileExists(File csv, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertCSVFileExists(csv, table);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.csv", args = {"csv:file", "table:word"})
    public void setCleanupAssertCSVFileNotExists(File csv, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertCSVFileNotExists(csv, table);
        });
    }

    @Step(value = "db.define.assert.table.not.exists.csv.alias", args = {"csv:file", "table:word", "alias:text"})
    public void setCleanupAssertCSVFileNotExists(File csv, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertCSVFileNotExists(csv, table);
        });
    }

    @Step(value = "db.define.assert.table.empty", args = {"table:word"})
    public void setCleanupAssertTableIsEmpty(String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertTableIsEmpty(table);
        });
    }

    @Step(value = "db.define.assert.table.empty.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertTableIsEmpty(String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertTableIsEmpty(table);
        });
    }

    @Step(value = "db.define.assert.table.not.empty", args = {"table:word"})
    public void setCleanupAssertTableIsNotEmpty(String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertTableIsNotEmpty(table);
        });
    }

    @Step(value = "db.define.assert.table.not.empty.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertTableIsNotEmpty(String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertTableIsNotEmpty(table);
        });
    }


    @Step("db.select.data")
    public Object selectData(Document document) {
        return json(executeSelect(document.getContent()).stream()
                .map(nullSymbolMapper).collect(Collectors.toList()));
    }

    @Step(value = "db.select.file", args = {"sql:file"})
    public Object selectData(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        return json(executeSelect(resourceLoader().readFileAsString(file)).stream()
                .map(nullSymbolMapper).collect(Collectors.toList()));
    }

    @Step(value = "db.switch.connection", args = {"alias:text"})
    public void switchConnection(String alias) {
        if (!connections.containsKey(alias)) {
            throw new WakamitiException(message("The connection named '{}' does not exist", alias));
        }
        currentConnection.set(alias);
        LOGGER.trace("Switched to '{}' connection", alias);
    }

    @Step("db.switch.connection.default")
    public void switchConnection() {
        Set<String> aliases = connections.keySet();
        String alias = aliases.contains(DEFAULT) ? DEFAULT : aliases.stream().findFirst()
                .orElseThrow(() -> new WakamitiException("There is no default connection"));
        LOGGER.trace("Switched to '{}' connection", alias);
        currentConnection.set(alias);
    }


    @Step("db.action.script.document")
    public void executeSQLScript(Document document) {
        executeScript(document.getContent(), enableCleanupUponCompletion);
    }

    @Step(value = "db.action.script.file", args = {"script:file"})
    public void executeSQLScript(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        executeScript(resourceLoader().readFileAsString(file), enableCleanupUponCompletion);
    }

    @Step("db.action.procedure.document")
    public Object executeProcedure(Document document) {
        return json(executeCall(document.getContent(), enableCleanupUponCompletion));
    }

    @Step(value = "db.action.procedure.file", args = {"proc:file"})
    public Object executeProcedure(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        return json(executeCall(resourceLoader().readFileAsString(file), enableCleanupUponCompletion));
    }


    @Step(value = "db.action.insert.from.data", args = "table:word")
    public void insertFromDataTable(String table, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            insertDataSet(dataSet, enableCleanupUponCompletion);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.action.insert.from.xls", args = {"xls:file"})
    public void insertFromXLSFile(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            multiDataSet.forEach(dataSet -> insertDataSet(dataSet, enableCleanupUponCompletion));
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.action.insert.from.csv", args = {"csv:file", "table:word"})
    public void insertFromCSVFile(File file, String table) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            insertDataSet(dataSet, enableCleanupUponCompletion);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.action.delete.from.data", args = "table:word")
    public void deleteFromDataTable(String table, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            deleteDataSet(dataSet, enableCleanupUponCompletion);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.action.delete.from.xls", args = "xls:file")
    public void deleteFromXLSFile(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            multiDataSet.forEach(dataSet -> deleteDataSet(dataSet, enableCleanupUponCompletion));
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.action.delete.from.csv", args = {"csv:file", "table:word"})
    public void deleteFromCSVFile(File file, String table) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            deleteDataSet(dataSet, enableCleanupUponCompletion);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.action.clear.table.all", args = "table:word")
    public void clearTable(String table) {
        truncateTable(table, enableCleanupUponCompletion);
    }

    @Step(value = "db.action.clear.table.row", args = {"table:word", "column:word", "value:text"})
    public void clearTableByRow(String table, String column, String value) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new Object[]{value}, nullSymbol)) {
            deleteDataSet(dataSet, enableCleanupUponCompletion);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.action.clear.table.where", args = {"table:word"})
    public void clearTableByClause(String table, Document clause) {
        deleteTable(table, clause.getContent(), enableCleanupUponCompletion);
    }


    @Step(value = "db.assert.table.exists.row.single.id", args = {"id:text", "table:word"})
    public void assertRowExistsBySingleId(String id, String table) {
        String keyColumn = primaryKey(table);
        try (DataSet dataSet = new InlineDataSet(table, new String[]{keyColumn}, new String[]{id}, nullSymbol)) {
            assertNonEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.row.single.id.async", args = {"id:text", "table:word", "time:int"})
    public void assertRowExistsBySingleIdAsync(String id, String table, Integer time) {
        String keyColumn = primaryKey(table);
        try (DataSet dataSet = new InlineDataSet(table, new String[]{keyColumn}, new String[]{id}, nullSymbol)) {
            assertNonEmptyAsync(dataSet, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.row.single.id", args = {"id:text", "table:word"})
    public void assertRowNotExistsBySingleId(String id, String table) {
        String keyColumn = primaryKey(table);
        try (DataSet dataSet = new InlineDataSet(table, new String[]{keyColumn}, new String[]{id}, nullSymbol)) {
            assertEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.row.single.id.async", args = {"id:text", "table:word", "time:int"})
    public void assertRowNotExistsBySingleIdAsync(String id, String table, Integer time) {
        String keyColumn = primaryKey(table);
        try (DataSet dataSet = new InlineDataSet(table, new String[]{keyColumn}, new String[]{id}, nullSymbol)) {
            assertEmptyAsync(dataSet, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.row.one.column", args = {"table:word", "column:word", "value:text"})
    public void assertRowExistsByOneColumn(String table, String column, String value) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertNonEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.row.one.column.async",
            args = {"table:word", "column:word", "value:text", "time:int"})
    public void assertRowExistsByOneColumnAsync(String table, String column, String value, Integer time) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertNonEmptyAsync(dataSet, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.row.one.column", args = {"table:word", "column:word", "value:text"})
    public void assertRowNotExistsByOneColumn(String table, String column, String value) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.row.one.column.async",
            args = {"table:word", "column:word", "value:text", "time:int"})
    public void assertRowNotExistsByOneColumnAsync(String table, String column, String value, Integer time) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertEmptyAsync(dataSet, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.count.row.one.column",
            args = {"column:word", "value:text", "table:word", "matcher:long-assertion"})
    public void assertRowCountByOneColumn(String column, String value, String table, Assertion<Long> matcher) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertCount(dataSet, matcher);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.count.row.one.column.async",
            args = {"column:word", "value:text", "table:word", "matcher:long-assertion", "time:int"})
    public void assertRowCountByOneColumnAsync(
            String column, String value, String table, Assertion<Long> matcher, Integer time) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertCountAsync(dataSet, matcher, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.sql.where", args = {"table:word"})
    public void assertRowExistsByClause(String table, Document clause) {
        if (!matcherNonEmpty().test(countBy(table, clause.getContent()))) {
            Assertions.fail(message(
                    ERROR_ASSERT_SOME_RECORD_EXPECTED,
                    "the given WHERE clause", Database.from(connection()).table(table), "it doesn't"
            ));
        }
    }

    @Step(value = "db.assert.table.exists.sql.where.async", args = {"table:word", "time:int"})
    public void assertRowExistsByClauseAsync(String table, Integer time, Document clause) {
        assertAsync(
                () -> matcherNonEmpty().test(countBy(table, clause.getContent())),
                time,
                () -> Assertions.fail(message(
                        ERROR_ASSERT_SOME_RECORD_EXPECTED,
                        "the given WHERE clause", Database.from(connection()).table(table), "it doesn't"
                )));
    }

    @Step(value = "db.assert.table.not.exists.sql.where", args = {"table:word"})
    public void assertRowNotExistsByClause(String table, Document clause) {
        if (!matcherEmpty().test(countBy(table, clause.getContent()))) {
            Assertions.fail(message(
                    ERROR_ASSERT_NO_RECORD_EXPECTED,
                    "the given WHERE clause", Database.from(connection()).table(table), "it does"
            ));
        }
    }

    @Step(value = "db.assert.table.not.exists.sql.where.async", args = {"table:word", "time:int"})
    public void assertRowNotExistsByClauseAsync(String table, Integer time, Document clause) {
        assertAsync(() -> matcherEmpty().test(countBy(table, clause.getContent())), time, () ->
                Assertions.fail(message(
                        ERROR_ASSERT_NO_RECORD_EXPECTED,
                        "the given WHERE clause", Database.from(connection()).table(table), "it does"
                )));
    }

    @Step(value = "db.assert.table.count.sql.where", args = {"table:word", "matcher:long-assertion"})
    public void assertRowCountByClause(String table, Assertion<Long> matcher, Document clause) {
        long count = countBy(table, clause.getContent());
        if (!matcher.test(count)) {
            Assertions.fail(message(
                    ERROR_ASSERT_SOME_RECORD_EXPECTED,
                    "the given WHERE clause",
                    Database.from(connection()).table(table),
                    matcher.describeFailure(count)
            ));
        }
    }

    @Step(value = "db.assert.table.count.sql.where.async", args = {"table:word", "matcher:long-assertion", "time:int"})
    public void assertRowCountByClauseAsync(String table, Assertion<Long> matcher, Integer time, Document clause) {
        AtomicLong result = new AtomicLong(0);
        assertAsync(() -> matcher.test(result.addAndGet(countBy(table, clause.getContent()))), time, () ->
                Assertions.fail(message(
                        ERROR_ASSERT_SOME_RECORD_EXPECTED,
                        "the given WHERE clause",
                        Database.from(connection()).table(table),
                        matcher.describeFailure(result.get())
                )));
    }

    @Step(value = "db.assert.table.exists.data", args = "table:word")
    public void assertDataTableExists(String table, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertNonEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.data.async", args = {"table:word", "time:int"})
    public void assertDataTableExistsAsync(String table, Integer time, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertNonEmptyAsync(dataSet, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.data", args = "table:word")
    public void assertDataTableNotExists(String table, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.data.async", args = {"table:word", "time:int"})
    public void assertDataTableNotExistsAsync(String table, Integer time, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertEmptyAsync(dataSet, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.count.data", args = {"table:word", "matcher:long-assertion"})
    public void assertDataTableCount(String table, Assertion<Long> matcher, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertCount(dataSet, matcher);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.count.data.async", args = {"table:word", "matcher:long-assertion", "time:int"})
    public void assertDataTableCountAsync(String table, Assertion<Long> matcher, Integer time, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertCountAsync(dataSet, matcher, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.xls", args = "xls:file")
    public void assertXLSFileExists(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            for (DataSet dataSet : multiDataSet) {
                assertNonEmpty(dataSet);
            }
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.xls.async", args = {"xls:file", "time:int"})
    public void assertXLSFileExistsAsync(File file, Integer time) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            for (DataSet dataSet : multiDataSet) {
                Duration duration = assertNonEmptyAsync(dataSet, time);
                time -= Long.valueOf(duration.toSeconds()).intValue();
            }
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.xls", args = "xls:file")
    public void assertXLSFileNotExists(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            for (DataSet dataSet : multiDataSet) {
                assertEmpty(dataSet);
            }
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.xls.async", args = {"xls:file", "time:int"})
    public void assertXLSFileNotExistsAsync(File file, Integer time) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            for (DataSet dataSet : multiDataSet) {
                Duration duration = assertEmptyAsync(dataSet, time);
                time -= Long.valueOf(duration.toSeconds()).intValue();
            }
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.csv", args = {"csv:file", "table:word"})
    public void assertCSVFileExists(File file, String table) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            assertNonEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.exists.csv.async", args = {"csv:file", "table:word", "time:int"})
    public void assertCSVFileExistsAsync(File file, String table, Integer time) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            assertNonEmptyAsync(dataSet, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.csv", args = {"csv:file", "table:word"})
    public void assertCSVFileNotExists(File file, String table) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            assertEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.exists.csv.async", args = {"csv:file", "table:word", "time:int"})
    public void assertCSVFileNotExistsAsync(File file, String table, Integer time) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            assertEmptyAsync(dataSet, time);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    @Step(value = "db.assert.table.not.empty", args = "table:word")
    public void assertTableIsNotEmpty(String table) {
        if (!matcherNonEmpty().test(countBy(table, "1=1"))) {
            Assertions.fail(message(
                    "It was expected some record exist in table {}, but it doesn't",
                    Database.from(connection()).table(table)
            ));
        }
    }

    @Step(value = "db.assert.table.not.empty.async", args = {"table:word", "time:int"})
    public void assertTableIsNotEmptyAsync(String table, Integer time) {
        assertAsync(() -> matcherNonEmpty().test(countBy(table, "1=1")), time, () ->
                Assertions.fail(message(
                        "It was expected some record exist in table {}, but it doesn't",
                        Database.from(connection()).table(table)
                ))
        );
    }

    @Step(value = "db.assert.table.empty", args = "table:word")
    public void assertTableIsEmpty(String table) {
        if (!matcherEmpty().test(countBy(table, "1=1"))) {
            Assertions.fail(message(
                    "It was expected no record exist in table {}, but it does",
                    Database.from(connection()).table(table)
            ));
        }
    }

    @Step(value = "db.assert.table.empty.async", args = {"table:word", "time:int"})
    public void assertTableIsEmptyAsync(String table, Integer time) {
        assertAsync(() -> matcherEmpty().test(countBy(table, "1=1")), time, () ->
                Assertions.fail(message(
                        "It was expected no record exist in table {}, but it does",
                        Database.from(connection()).table(table)
                ))
        );
    }

}