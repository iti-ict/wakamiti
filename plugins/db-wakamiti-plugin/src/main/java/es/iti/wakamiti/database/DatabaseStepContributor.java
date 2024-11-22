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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static es.iti.wakamiti.database.jdbc.LogUtils.message;


/**
 * A contributor class for database-related steps in the test scenarios.
 */
@Extension(provider = "es.iti.wakamiti", name = "database-steps", version = "2.6")
@I18nResource("iti_wakamiti_wakamiti-database")
public class DatabaseStepContributor extends DatabaseSupport implements StepContributor {

    /**
     * Cleans up operations after scenario execution.
     */
    @TearDown(order = 1)
    public void cleanUp() {
        this.enableCleanupUponCompletion = false;
        cleanUpOperations.forEach(Runnable::run);
    }

    /**
     * Releases database connections after scenario execution.
     */
    @TearDown(order = 2)
    public void releaseConnection() {
        connections.values().forEach(ConnectionProvider::close);
        connections.clear();
    }

    /**
     * Sets the default database connection parameters.
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
     * Configures a named database connection URL, username, and password for
     * the specified connection alias.
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
     * Sets the SQL statements that will be executed by the default SQL
     * connection after the scenario ends, regardless of execution status.
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
     * connection after the scenario ends, regardless of execution status.
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
     * connection after the scenario ends, regardless of execution status.
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
     * connection after the scenario ends, regardless of execution status.
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
     * Sets the SQL procedure that will be executed by the default SQL
     * connection after the scenario ends, regardless of execution status.
     *
     * @param document The script content
     */
    @Step(value = "db.define.cleanup.procedure.document")
    public void setCleanupProcedure(Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.executeProcedure(document);
        });
    }

    /**
     * Sets the SQL procedure that will be executed by a named SQL
     * connection after the scenario ends, regardless of execution status.
     *
     * @param alias    The SQL connection name
     * @param document The script content
     */
    @Step(value = "db.define.cleanup.procedure.document.alias", args = {"alias:text"})
    public void setCleanupProcedure(String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.executeProcedure(document);
        });
    }

    /**
     * Sets the SQL procedure that will be executed by the default SQL
     * connection after the scenario ends, regardless of execution status.
     *
     * @param file The script content
     */
    @Step(value = "db.define.cleanup.procedure.file", args = {"proc:file"})
    public void setCleanupProcedure(File file) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.executeProcedure(file);
        });
    }

    /**
     * Sets the SQL procedure that will be executed by a named SQL
     * connection after the scenario ends, regardless of execution status.
     *
     * @param file  The script content
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.cleanup.procedure.file.alias", args = {"proc:file", "alias:text"})
    public void setCleanupProcedure(File file, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.executeProcedure(file);
        });
    }

    /**
     * Clears the specified table, first attempting to execute {@code TRUNCATE},
     * and then using {@code DELETE FROM} as a fallback, using the default SQL
     * connection after the scenario ends, regardless of execution status.
     *
     * @param table The name of the table to clear
     */
    @Step(value = "db.define.cleanup.clear.all", args = {"table:word"})
    public void setCleanupClear(String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.clearTable(table);
        });
    }

    /**
     * Clears the specified table, first attempting to execute {@code TRUNCATE},
     * and then using {@code DELETE FROM} as fallback, using a named SQL
     * connection after the scenario ends, regardless execution status.
     *
     * @param table The name of the table to clear
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
     * Deletes rows from the specified table that match the given condition
     * using the default SQL connection after the scenario ends, regardless
     * of execution status.
     *
     * @param column The name of the column to match
     * @param value  The value to match in the specified column
     * @param table  The name of the table from which to delete rows
     */
    @Step(value = "db.define.cleanup.clear.row", args = {"column:word", "value:text", "table:word"})
    public void setCleanupClear(String column, String value, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.clearTableByRow(table, column, value);
        });
    }

    /**
     * Deletes rows from the specified table that match the given condition
     * using a named SQL connection after the scenario ends, regardless of
     * execution status.
     *
     * @param column The name of the column to match
     * @param value  The value to match in the specified column
     * @param table  The name of the table from which to delete rows
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
     * Deletes rows from the specified table that match the given WHERE clause
     * using the default SQL connection after the scenario ends, regardless of
     * execution status.
     *
     * @param table    The name of the table from which to delete rows
     * @param document The document representing the WHERE clause
     */
    @Step(value = "db.define.cleanup.clear.where", args = {"table:word"})
    public void setCleanupClear(String table, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.clearTableByClause(table, document);
        });
    }

    /**
     * Deletes rows from the specified table that match the given WHERE clause
     * using a named SQL connection after the scenario ends, regardless of
     * execution status.
     *
     * @param table    The name of the table from which to delete rows
     * @param alias    The SQL connection name
     * @param document The document representing the WHERE clause
     */
    @Step(value = "db.define.cleanup.clear.where.alias", args = {"table:word", "alias:text"})
    public void setCleanupClear(String table, String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.clearTableByClause(table, document);
        });
    }

    /**
     * Deletes the specified rows from a given table, as specified in the data table,
     * using the default SQL connection after the scenario ends, regardless of
     * execution status.
     *
     * @param table The name of the table from which to delete rows
     * @param data  The data table containing the rows to delete
     */
    @Step(value = "db.define.cleanup.delete.from.data", args = {"table:word"})
    public void setCleanupDelete(String table, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.deleteFromDataTable(table, data);
        });
    }

    /**
     * Deletes the specified rows from a given table, as specified in the data table,
     * using a named SQL connection after the scenario ends, regardless of execution
     * status.
     *
     * @param table The name of the table from which to delete rows
     * @param alias The SQL connection name
     * @param data  The data table containing the rows to delete
     */
    @Step(value = "db.define.cleanup.delete.from.data.alias", args = {"table:word", "alias:text"})
    public void setCleanupDelete(String table, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.deleteFromDataTable(table, data);
        });
    }

    /**
     * Deletes rows from tables based on the contents of an Excel file, where each
     * sheet in the Excel file represents a table to delete rows from. This
     * operation is performed using the default SQL connection after the scenario
     * ends, regardless of execution status.
     *
     * @param xls The Excel file containing data to delete from tables
     */
    @Step(value = "db.define.cleanup.delete.from.xls", args = {"xls:file"})
    public void setCleanupDeleteXLS(File xls) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.deleteFromXLSFile(xls);
        });
    }

    /**
     * Deletes rows from tables based on the contents of an Excel file, where each
     * sheet in the Excel file represents a table to delete rows from. This
     * operation is performed using a named SQL connection after the scenario
     * ends, regardless of execution status.
     *
     * @param xls   The Excel file containing data to delete from tables
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.cleanup.delete.from.xls.alias", args = {"xls:file", "alias:text"})
    public void setCleanupDeleteXLS(File xls, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.deleteFromXLSFile(xls);
        });
    }

    /**
     * Deletes rows from a specified table based on the contents of a CSV file,
     * using the default SQL connection after the scenario ends, regardless of
     * execution status.
     *
     * @param csv   The CSV file containing the rows to delete
     * @param table The name of the table from which to delete the rows
     */
    @Step(value = "db.define.cleanup.delete.from.csv", args = {"csv:file", "table:word"})
    public void setCleanupDeleteCSV(File csv, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.deleteFromCSVFile(csv, table);
        });
    }

    /**
     * Deletes rows from a specified table based on the contents of a CSV file,
     * using a named SQL connection after the scenario ends, regardless of
     * execution status.
     *
     * @param csv   The CSV file containing the rows to delete
     * @param table The name of the table from which to delete the rows
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.cleanup.delete.from.csv.alias", args = {"csv:file", "table:word", "alias:text"})
    public void setCleanupDeleteCSV(File csv, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.deleteFromCSVFile(csv, table);
        });
    }

    /**
     * Inserts data into the specified table based on the contents of a DataTable object.
     * This operation is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The name of the table to insert data into
     * @param data  The DataTable object containing the data to be inserted
     */
    @Step(value = "db.define.cleanup.insert.from.data", args = {"table:word"})
    public void setCleanupInsert(String table, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.insertFromDataTable(table, data);
        });
    }

    /**
     * Inserts data into the specified table based on the contents of a DataTable object.
     * This operation is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The name of the table to insert data into
     * @param alias The SQL connection name
     * @param data  The DataTable object containing the data to be inserted
     */
    @Step(value = "db.define.cleanup.insert.from.data.alias", args = {"table:word", "alias:text"})
    public void setCleanupInsert(String table, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.insertFromDataTable(table, data);
        });
    }

    /**
     * Inserts rows into tables based on the contents of an Excel file, where each
     * sheet in the Excel file represents a table to insert rows from. This
     * operation is performed using the default SQL connection after the scenario
     * ends, regardless of execution status.
     *
     * @param xls The Excel file containing data to insert from tables
     */
    @Step(value = "db.define.cleanup.insert.from.xls", args = {"xls:file"})
    public void setCleanupInsertXLS(File xls) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.insertFromXLSFile(xls);
        });
    }

    /**
     * Inserts rows into tables based on the contents of an Excel file, where each
     * sheet in the Excel file represents a table to insert rows from. This
     * operation is performed using a named SQL connection after the scenario
     * ends, regardless of execution status.
     *
     * @param xls   The Excel file containing data to insert from tables
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.cleanup.insert.from.xls.alias", args = {"xls:file", "alias:text"})
    public void setCleanupInsertXLS(File xls, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.insertFromXLSFile(xls);
        });
    }

    /**
     * Inserts rows into a specified table based on the contents of a CSV file,
     * using the default SQL connection after the scenario ends, regardless of
     * execution status.
     *
     * @param csv   The CSV file containing the rows to delete
     * @param table The name of the table from which to delete the rows
     */
    @Step(value = "db.define.cleanup.insert.from.csv", args = {"csv:file", "table:word"})
    public void setCleanupInsertCSV(File csv, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.insertFromCSVFile(csv, table);
        });
    }

    /**
     * Inserts rows into a specified table based on the contents of a CSV file,
     * using a named SQL connection after the scenario ends, regardless of
     * execution status.
     *
     * @param csv   The CSV file containing the rows to delete
     * @param table The name of the table from which to delete the rows
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.cleanup.insert.from.csv.alias", args = {"csv:file", "table:word", "alias:text"})
    public void setCleanupInsertCSV(File csv, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.insertFromCSVFile(csv, table);
        });
    }

    /**
     * Asserts that a row with the specified single ID exists in the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param id    The single ID value to be checked
     * @param table The table name
     */
    @Step(value = "db.define.assert.table.exists.row.single.id", args = {"id:text", "table:word"})
    public void setCleanupAssertRowExistsBySingleId(String id, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowExistsBySingleId(id, table);
        });
    }

    /**
     * Asserts that a row with the specified single ID exists in the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param id    The single ID value to be checked
     * @param table The table name
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.assert.table.exists.row.single.id.alias", args = {"id:text", "table:word", "alias:text"})
    public void setCleanupAssertRowExistsBySingleId(String id, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowExistsBySingleId(id, table);
        });
    }

    /**
     * Asserts that a row with the specified single ID does not exist in the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param id    The single ID value to be checked
     * @param table The table name
     */
    @Step(value = "db.define.assert.table.not.exists.row.single.id", args = {"id:text", "table:word"})
    public void setCleanupAssertRowNotExistsBySingleId(String id, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowNotExistsBySingleId(id, table);
        });
    }

    /**
     * Asserts that a row with the specified single ID does not exist in the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param id    The single ID value to be checked
     * @param table The table name
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.assert.table.not.exists.row.single.id.alias",
            args = {"id:text", "table:word", "alias:text"})
    public void setCleanupAssertRowNotExistsBySingleId(String id, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowNotExistsBySingleId(id, table);
        });
    }

    /**
     * Asserts that a row with the specified value in the given column exists in the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param column The column name
     * @param value  The column value to be checked
     * @param table  The table name
     */
    @Step(value = "db.define.assert.table.exists.row.one.column", args = {"column:word", "value:text", "table:word"})
    public void setCleanupAssertRowExistsByOneColumn(String column, String value, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowExistsByOneColumn(column, value, table);
        });
    }

    /**
     * Asserts that a row with the specified value in the given column exists in the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param column The column name
     * @param value  The column value to be checked
     * @param table  The table name
     * @param alias  The SQL connection name
     */
    @Step(value = "db.define.assert.table.exists.row.one.column.alias",
            args = {"column:word", "value:text", "table:word", "alias:text"})
    public void setCleanupAssertRowExistsByOneColumn(String column, String value, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowExistsByOneColumn(column, value, table);
        });
    }

    /**
     * Asserts that a row with the specified value in the given column does not exist in the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param column The column name
     * @param value  The column value to be checked
     * @param table  The table name
     */
    @Step(value = "db.define.assert.table.not.exists.row.one.column",
            args = {"column:word", "value:text", "table:word"})
    public void setCleanupAssertRowNotExistsByOneColumn(String column, String value, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowNotExistsByOneColumn(column, value, table);
        });
    }

    /**
     * Asserts that a row with the specified value in the given column does not exist in the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param column The column name
     * @param value  The column value to be checked
     * @param table  The table name
     * @param alias  The SQL connection name
     */
    @Step(value = "db.define.assert.table.not.exists.row.one.column.alias",
            args = {"column:word", "value:text", "table:word", "alias:text"})
    public void setCleanupAssertRowNotExistsByOneColumn(String column, String value, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowNotExistsByOneColumn(column, value, table);
        });
    }

    /**
     * Asserts the count of rows with the specified value in the given column in the given table
     * using the provided assertion matcher.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param column  The column name
     * @param value   The column value to be checked
     * @param table   The table name
     * @param matcher The assertion matcher for the row count
     */
    @Step(value = "db.define.assert.table.count.row.one.column",
            args = {"column:word", "value:text", "table:word", "matcher:long-assertion"})
    public void setCleanupAssertRowCountByOneColumn(
            String column, String value, String table, Assertion<Long> matcher) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowCountByOneColumn(column, value, table, matcher);
        });
    }

    /**
     * Asserts the count of rows with the specified value in the given column in the given table
     * using the provided assertion matcher.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param column  The column name
     * @param value   The column value to be checked
     * @param table   The table name
     * @param matcher The assertion matcher for the row count
     * @param alias   The SQL connection name
     */
    @Step(value = "db.define.assert.table.count.row.one.column.alias",
            args = {"column:word", "value:text", "table:word", "matcher:long-assertion", "alias:text"})
    public void setCleanupAssertRowCountByOneColumn(
            String column, String value, String table, Assertion<Long> matcher, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowCountByOneColumn(column, value, table, matcher);
        });
    }

    /**
     * Asserts that the rows based on the specified SQL WHERE clause exists in the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table    The table name
     * @param document The SQL WHERE clause
     */
    @Step(value = "db.define.assert.table.exists.sql.where", args = {"table:word"})
    public void setCleanupAssertRowExistsByClause(String table, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowExistsByClause(table, document);
        });
    }

    /**
     * Asserts that the rows based on the specified SQL WHERE clause exists in the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table    The table name
     * @param document The SQL WHERE clause
     * @param alias    The SQL connection name
     */
    @Step(value = "db.define.assert.table.exists.sql.where.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertRowExistsByClause(String table, String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowExistsByClause(table, document);
        });
    }

    /**
     * Asserts that the rows based on the specified SQL WHERE clause do not exist in the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table    The table name
     * @param document The SQL WHERE clause
     */
    @Step(value = "db.define.assert.table.not.exists.sql.where", args = {"table:word"})
    public void setCleanupAssertRowNotExistsByClause(String table, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowNotExistsByClause(table, document);
        });
    }

    /**
     * Asserts that the rows based on the specified SQL WHERE clause do not exist in the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table    The table name
     * @param document The SQL WHERE clause
     * @param alias    The SQL connection name
     */
    @Step(value = "db.define.assert.table.not.exists.sql.where.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertRowNotExistsByClause(String table, String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowNotExistsByClause(table, document);
        });
    }

    /**
     * Asserts the row count in the given table based on the specified SQL WHERE clause.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table    The table name
     * @param matcher  The assertion for the row count
     * @param document The SQL WHERE clause
     */
    @Step(value = "db.define.assert.table.count.sql.where", args = {"table:word", "matcher:long-assertion"})
    public void setCleanupAssertRowCountByClause(String table, Assertion<Long> matcher, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertRowCountByClause(table, matcher, document);
        });
    }

    /**
     * Asserts the row count in the given table based on the specified SQL WHERE clause.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table    The table name
     * @param matcher  The assertion for the row count
     * @param alias    The SQL connection name
     * @param document The SQL WHERE clause
     */
    @Step(value = "db.define.assert.table.count.sql.where.alias",
            args = {"table:word", "matcher:long-assertion", "alias:text"})
    public void setCleanupAssertRowCountByClause(
            String table, Assertion<Long> matcher, String alias, Document document) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertRowCountByClause(table, matcher, document);
        });
    }

    /**
     * Asserts that the rows based on the specified data table exist in the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The table name
     * @param data  The data table to be asserted
     */
    @Step(value = "db.define.assert.table.exists.data", args = {"table:word"})
    public void setCleanupAssertDataTableExists(String table, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertDataTableExists(table, data);
        });
    }

    /**
     * Asserts that the rows based on the specified data table exist in the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The table name
     * @param alias The SQL connection name
     * @param data  The data table to be asserted
     */
    @Step(value = "db.define.assert.table.exists.data.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertDataTableExists(String table, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertDataTableExists(table, data);
        });
    }

    /**
     * Asserts that the rows based on the specified data table do not exist in the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The table name
     * @param data  The data table to be asserted
     */
    @Step(value = "db.define.assert.table.not.exists.data", args = {"table:word"})
    public void setCleanupAssertDataTableNotExists(String table, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertDataTableNotExists(table, data);
        });
    }

    /**
     * Asserts that the rows based on the specified data table do not exist in the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The table name
     * @param alias The SQL connection name
     * @param data  The data table to be asserted
     */
    @Step(value = "db.define.assert.table.not.exists.data.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertDataTableNotExists(String table, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertDataTableNotExists(table, data);
        });
    }

    /**
     * Asserts the row count in the given table based on the specified data table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table   The table name
     * @param matcher The assertion for the row count
     * @param data    The data table to be asserted
     */
    @Step(value = "db.define.assert.table.count.data", args = {"table:word", "matcher:long-assertion"})
    public void setCleanupAssertDataTableCount(String table, Assertion<Long> matcher, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertDataTableCount(table, matcher, data);
        });
    }

    /**
     * Asserts the row count in the given table based on the specified data table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table   The table name
     * @param matcher The assertion for the row count
     * @param alias   The SQL connection name
     * @param data    The data table to be asserted
     */
    @Step(value = "db.define.assert.table.count.data.alias",
            args = {"table:word", "matcher:long-assertion", "alias:text"})
    public void setCleanupAssertDataTableCount(String table, Assertion<Long> matcher, String alias, DataTable data) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertDataTableCount(table, matcher, data);
        });
    }

    /**
     * Asserts that the data rows included in the specified XLS file exist.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param xls The XLS file containing the data rows to be asserted
     */
    @Step(value = "db.define.assert.table.exists.xls", args = {"xls:file"})
    public void setCleanupAssertXLSFileExists(File xls) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertXLSFileExists(xls);
        });
    }

    /**
     * Asserts that the data rows included in the specified XLS file exist.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param xls   The XLS file containing the data rows to be asserted
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.assert.table.exists.xls.alias", args = {"xls:file", "alias:text"})
    public void setCleanupAssertXLSFileExists(File xls, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertXLSFileExists(xls);
        });
    }

    /**
     * Asserts that the data rows included in the specified XLS file do not exist.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param xls The XLS file containing the data rows to be asserted
     */
    @Step(value = "db.define.assert.table.not.exists.xls", args = {"xls:file"})
    public void setCleanupAssertXLSFileNotExists(File xls) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertXLSFileNotExists(xls);
        });
    }

    /**
     * Asserts that the data rows included in the specified XLS file do not exist.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param xls   The XLS file containing the data rows to be asserted
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.assert.table.not.exists.xls.alias", args = {"xls:file", "alias:text"})
    public void setCleanupAssertXLSFileNotExists(File xls, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertXLSFileNotExists(xls);
        });
    }

    /**
     * Asserts that the data rows included in the specified CSV file exist for the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param csv   The CSV file to be asserted
     * @param table The table name corresponding to the data in the CSV file
     */
    @Step(value = "db.define.assert.table.exists.csv", args = {"csv:file", "table:word"})
    public void setCleanupAssertCSVFileExists(File csv, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertCSVFileExists(csv, table);
        });
    }

    /**
     * Asserts that the data rows included in the specified CSV file exist for the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param csv   The CSV file to be asserted
     * @param table The table name corresponding to the data in the CSV file
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.assert.table.exists.csv.alias", args = {"csv:file", "table:word", "alias:text"})
    public void setCleanupAssertCSVFileExists(File csv, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertCSVFileExists(csv, table);
        });
    }

    /**
     * Asserts that the data rows included in the specified CSV file do not exist for the given table.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param csv   The CSV file to be asserted
     * @param table The table name corresponding to the data in the CSV file
     */
    @Step(value = "db.define.assert.table.not.exists.csv", args = {"csv:file", "table:word"})
    public void setCleanupAssertCSVFileNotExists(File csv, String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertCSVFileNotExists(csv, table);
        });
    }

    /**
     * Asserts that the data rows included in the specified CSV file do not exist for the given table.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param csv   The CSV file to be asserted
     * @param table The table name corresponding to the data in the CSV file
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.assert.table.not.exists.csv.alias", args = {"csv:file", "table:word", "alias:text"})
    public void setCleanupAssertCSVFileNotExists(File csv, String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertCSVFileNotExists(csv, table);
        });
    }

    /**
     * Asserts that the specified table is empty.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The table name to be asserted
     */
    @Step(value = "db.define.assert.table.empty", args = {"table:word"})
    public void setCleanupAssertTableIsEmpty(String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertTableIsEmpty(table);
        });
    }

    /**
     * Asserts that the specified table is empty.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The table name to be asserted
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.assert.table.empty.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertTableIsEmpty(String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertTableIsEmpty(table);
        });
    }

    /**
     * Asserts that the specified table is not empty.
     * This assertion is performed using the default SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The table name to be asserted
     */
    @Step(value = "db.define.assert.table.not.empty", args = {"table:word"})
    public void setCleanupAssertTableIsNotEmpty(String table) {
        cleanUpOperations.add(() -> {
            this.switchConnection();
            this.assertTableIsNotEmpty(table);
        });
    }

    /**
     * Asserts that the specified table is not empty.
     * This assertion is performed using a named SQL connection after the scenario ends,
     * regardless of execution status.
     *
     * @param table The table name to be asserted
     * @param alias The SQL connection name
     */
    @Step(value = "db.define.assert.table.not.empty.alias", args = {"table:word", "alias:text"})
    public void setCleanupAssertTableIsNotEmpty(String table, String alias) {
        cleanUpOperations.add(() -> {
            this.switchConnection(alias);
            this.assertTableIsNotEmpty(table);
        });
    }

    /**
     * Retrieves data from the specified SQL SELECT as a JSON object.
     *
     * @param document The SQL SELECT statement document
     * @return The selected data as a JSON object
     */
    @Step("db.select.data")
    public Object selectData(Document document) {
        return json(executeSelect(document.getContent()).stream()
                .map(nullSymbolMapper).collect(Collectors.toList()));
    }

    /**
     * Retrieves data from the specified SQL SELECT as a JSON object.
     *
     * @param file The file containing the SQL SELECT statement
     * @return The selected data as a JSON object
     */
    @Step(value = "db.select.file", args = {"sql:file"})
    public Object selectData(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        return json(executeSelect(resourceLoader().readFileAsString(file)).stream()
                .map(nullSymbolMapper).collect(Collectors.toList()));
    }

    /**
     * Switches the active database connection to the one specified by the given alias.
     *
     * @param alias The alias of the database connection to switch to
     * @throws WakamitiException if the specified connection alias does not exist
     */
    @Step(value = "db.switch.connection", args = {"alias:text"})
    public void switchConnection(String alias) {
        if (!connections.containsKey(alias)) {
            throw new WakamitiException(message("The connection named '{}' does not exist", alias));
        }
        currentConnection.set(alias);
        LOGGER.trace("Switched to '{}' connection", alias);
    }

    /**
     * Switches the active database connection to the default one.
     * If no default connection is set, it selects the first available connection.
     *
     * @throws WakamitiException if the specified connection alias does not exist
     */
    @Step("db.switch.connection.default")
    public void switchConnection() {
        Set<String> aliases = connections.keySet();
        String alias = aliases.contains(DEFAULT) ? DEFAULT : aliases.stream().findFirst()
                .orElseThrow(() -> new WakamitiException("There is no default connection"));
        LOGGER.trace("Switched to '{}' connection", alias);
        currentConnection.set(alias);
    }

    /**
     * Executes the specified SQL statements.
     *
     * @param document The SQL statements document
     * @return The rows inserted or updated as a json object
     */
    @Step("db.action.script.document")
    public Object executeSQLScript(Document document) {
        return json(executeScript(document.getContent(), enableCleanupUponCompletion));
    }

    /**
     * Executes the specified SQL statements.
     *
     * @param file The file containing the SQL statements
     * @return The rows inserted or updated as a json object
     */
    @Step(value = "db.action.script.file", args = {"script:file"})
    public Object executeSQLScript(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        return json(executeScript(resourceLoader().readFileAsString(file), enableCleanupUponCompletion));
    }

    /**
     * Executes the specified procedure and returns the result sets as JSON objects.
     *
     * @param document The procedure
     * @return The results of the procedure execution
     */
    @Step("db.action.procedure.document")
    public Object executeProcedure(Document document) {
        return json(executeCall(document.getContent(), enableCleanupUponCompletion));
    }

    /**
     * Executes the specified procedure and returns the result sets as JSON objects.
     *
     * @param file The file containing the procedure
     * @return The results of the procedure execution
     */
    @Step(value = "db.action.procedure.file", args = {"proc:file"})
    public Object executeProcedure(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        return json(executeCall(resourceLoader().readFileAsString(file), enableCleanupUponCompletion));
    }

    /**
     * Inserts rows from the provided DataTable into the specified table.
     *
     * @param table     The table name
     * @param dataTable The data table containing the rows to be inserted
     * @return The rows inserted as a json object
     */
    @Step(value = "db.action.insert.from.data", args = "table:word")
    public Object insertFromDataTable(String table, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            return json(insertDataSet(dataSet, enableCleanupUponCompletion));
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Inserts rows of the sheets of the provided XLS file into the database.
     *
     * @param file The XLS file containing the rows to be inserted
     * @return The rows inserted as a json object
     */
    @Step(value = "db.action.insert.from.xls", args = {"xls:file"})
    public Object insertFromXLSFile(File file) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            List<Map<String, String>> results = new LinkedList<>();
            multiDataSet.forEach(dataSet -> results.addAll(insertDataSet(dataSet, enableCleanupUponCompletion)));
            return json(results);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Inserts rows of the provided CSV file into the given database table.
     *
     * @param file  The CSV file containing the rows to be inserted
     * @param table The name of the table where the data will be inserted
     * @return The rows inserted as a json object
     */
    @Step(value = "db.action.insert.from.csv", args = {"csv:file", "table:word"})
    public Object insertFromCSVFile(File file, String table) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);
        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            return json(insertDataSet(dataSet, enableCleanupUponCompletion));
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Deletes rows from the specified database table using the data provided
     * in the given DataTable.
     *
     * @param table     The name of the table from which rows will be deleted
     * @param dataTable The DataTable containing the rows to be deleted
     */
    @Step(value = "db.action.delete.from.data", args = "table:word")
    public void deleteFromDataTable(String table, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            deleteDataSet(dataSet, enableCleanupUponCompletion);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Deletes rows from the specified database table using the data provided
     * in the sheets of the given XLS file.
     *
     * @param file The XLS file containing the data to be deleted
     */
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

    /**
     * Deletes rows from the specified database table using the data provided
     * in the given CSV file.
     *
     * @param file  The CSV file containing the data to be deleted
     * @param table The name of the table from which rows will be deleted
     */
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

    /**
     * Deletes all rows from the given database table.
     *
     * @param table The name of the table to be cleared
     */
    @Step(value = "db.action.clear.table.all", args = "table:word")
    public void clearTable(String table) {
        truncateTable(table, enableCleanupUponCompletion);
    }

    /**
     * Deletes rows from the specified database table where the given column matches
     * the provided value.
     *
     * @param table  The name of the table from which rows will be cleared
     * @param column The column name to match
     * @param value  The value to match in the specified column
     */
    @Step(value = "db.action.clear.table.row", args = {"table:word", "column:word", "value:text"})
    public void clearTableByRow(String table, String column, String value) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new Object[]{value}, nullSymbol)) {
            deleteDataSet(dataSet, enableCleanupUponCompletion);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Deletes rows from the specified database table based on the provided
     * SQL WHERE clause.
     *
     * @param table  The name of the table from which rows will be cleared
     * @param clause The SQL WHERE clause
     */
    @Step(value = "db.action.clear.table.where", args = {"table:word"})
    public void clearTableByClause(String table, Document clause) {
        deleteTable(table, clause.getContent(), enableCleanupUponCompletion);
    }

    /**
     * Asserts that a row with the specified ID exists in the given table.
     *
     * @param id    The ID of the row to be checked for existence
     * @param table The name of the table
     */
    @Step(value = "db.assert.table.exists.row.single.id", args = {"id:text", "table:word"})
    public void assertRowExistsBySingleId(String id, String table) {
        String keyColumn = primaryKey(table);
        try (DataSet dataSet = new InlineDataSet(table, new String[]{keyColumn}, new String[]{id}, nullSymbol)) {
            assertNonEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that a row with the specified ID exists in the given table
     * asynchronously within the specified time.
     *
     * @param id       The ID of the row to be checked for existence
     * @param table    The name of the table
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.exists.row.single.id.async", args = {"id:text", "table:word", "duration:duration"})
    public void assertRowExistsBySingleIdAsync(String id, String table, Duration duration) {
        String keyColumn = primaryKey(table);
        try (DataSet dataSet = new InlineDataSet(table, new String[]{keyColumn}, new String[]{id}, nullSymbol)) {
            assertNonEmptyAsync(dataSet, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that a row with the specified ID does not exist in the given table.
     *
     * @param id    The ID of the row to be checked for non-existence
     * @param table The name of the table
     */
    @Step(value = "db.assert.table.not.exists.row.single.id", args = {"id:text", "table:word"})
    public void assertRowNotExistsBySingleId(String id, String table) {
        String keyColumn = primaryKey(table);
        try (DataSet dataSet = new InlineDataSet(table, new String[]{keyColumn}, new String[]{id}, nullSymbol)) {
            assertEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that a row with the specified ID does not exist in the given table
     * asynchronously within the specified time.
     *
     * @param id       The ID of the row to be checked for non-existence
     * @param table    The name of the table
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.not.exists.row.single.id.async",
            args = {"id:text", "table:word", "duration:duration"})
    public void assertRowNotExistsBySingleIdAsync(String id, String table, Duration duration) {
        String keyColumn = primaryKey(table);
        try (DataSet dataSet = new InlineDataSet(table, new String[]{keyColumn}, new String[]{id}, nullSymbol)) {
            assertEmptyAsync(dataSet, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that a row with the specified value in the given column exists in
     * the table.
     *
     * @param column The name of the column
     * @param value  The value to be checked for existence
     * @param table  The name of the table
     */
    @Step(value = "db.assert.table.exists.row.one.column", args = {"column:word", "value:text", "table:word"})
    public void assertRowExistsByOneColumn(String column, String value, String table) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertNonEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that a row with the specified value in the given column exists in
     * the table asynchronously within the specified time.
     *
     * @param table    The name of the table
     * @param column   The name of the column
     * @param value    The value to be checked for existence
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.exists.row.one.column.async",
            args = {"column:word", "value:text", "table:word", "duration:duration"})
    public void assertRowExistsByOneColumnAsync(String column, String value, String table, Duration duration) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertNonEmptyAsync(dataSet, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that a row with the specified value in the given column does not
     * exist in the table.
     *
     * @param column The name of the column
     * @param value  The value to be checked for non-existence
     * @param table  The name of the table
     */
    @Step(value = "db.assert.table.not.exists.row.one.column", args = {"column:word", "value:text", "table:word"})
    public void assertRowNotExistsByOneColumn(String column, String value, String table) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that a row with the specified value in the given column does not
     * exist in the table asynchronously within the specified time.
     *
     * @param column   The name of the column
     * @param value    The value to be checked for non-existence
     * @param table    The name of the table
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.not.exists.row.one.column.async",
            args = {"column:word", "value:text", "table:word", "duration:duration"})
    public void assertRowNotExistsByOneColumnAsync(String column, String value, String table, Duration duration) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertEmptyAsync(dataSet, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts the row count with the specified value in the given column in
     * the table.
     *
     * @param column  The name of the column
     * @param value   The value to be checked for row count
     * @param table   The name of the table
     * @param matcher The assertion to be applied to the row count
     */
    @Step(value = "db.assert.table.count.row.one.column",
            args = {"column:word", "value:text", "table:word", "matcher:long-assertion"})
    public void assertRowCountByOneColumn(String column, String value, String table, Assertion<Long> matcher) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertCount(dataSet, matcher);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts the row count with the specified value in the given column in the
     * table asynchronously within the specified time.
     *
     * @param column   The name of the column
     * @param value    The value to be checked for row count
     * @param table    The name of the table
     * @param matcher  The assertion to be applied to the row count
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.count.row.one.column.async",
            args = {"column:word", "value:text", "table:word", "matcher:long-assertion", "duration:duration"})
    public void assertRowCountByOneColumnAsync(
            String column, String value, String table, Assertion<Long> matcher, Duration duration) {
        try (DataSet dataSet = new InlineDataSet(table, new String[]{column}, new String[]{value}, nullSymbol)) {
            assertCountAsync(dataSet, matcher, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that at least one row exists in the given table that matches the
     * provided WHERE clause.
     *
     * @param table  The name of the table
     * @param clause The SQL WHERE clause
     */
    @Step(value = "db.assert.table.exists.sql.where", args = {"table:word"})
    public void assertRowExistsByClause(String table, Document clause) {
        if (!matcherNonEmpty().test(countBy(table, clause.getContent()))) {
            Assertions.fail(message(
                    ERROR_ASSERT_SOME_RECORD_EXPECTED,
                    GIVEN_WHERE_CLAUSE, Database.from(connection()).table(table), "it doesn't"
            ));
        }
    }

    /**
     * Asserts asynchronously that at least one row exists in the given table
     * that matches the provided WHERE clause.
     *
     * @param table    The name of the table
     * @param duration The maximum time to wait for the assertion to complete
     * @param clause   The SQL WHERE clause
     */
    @Step(value = "db.assert.table.exists.sql.where.async", args = {"table:word", "duration:duration"})
    public void assertRowExistsByClauseAsync(String table, Duration duration, Document clause) {
        assertAsync(
                () -> matcherNonEmpty().test(countBy(table, clause.getContent())),
                duration,
                () -> Assertions.fail(message(
                        ERROR_ASSERT_SOME_RECORD_EXPECTED,
                        GIVEN_WHERE_CLAUSE, Database.from(connection()).table(table), "it doesn't"
                )));
    }

    /**
     * Asserts that no row exists in the given table that matches the provided
     * WHERE clause.
     *
     * @param table  The name of the table
     * @param clause The SQL WHERE clause
     */
    @Step(value = "db.assert.table.not.exists.sql.where", args = {"table:word"})
    public void assertRowNotExistsByClause(String table, Document clause) {
        if (!matcherEmpty().test(countBy(table, clause.getContent()))) {
            Assertions.fail(message(
                    ERROR_ASSERT_NO_RECORD_EXPECTED,
                    GIVEN_WHERE_CLAUSE, Database.from(connection()).table(table), "it does"
            ));
        }
    }

    /**
     * Asserts asynchronously that no row exists in the given table that matches
     * the provided WHERE clause.
     *
     * @param table    The name of the table
     * @param duration The maximum time to wait for the assertion to complete
     * @param clause   The SQL WHERE clause
     */
    @Step(value = "db.assert.table.not.exists.sql.where.async", args = {"table:word", "duration:duration"})
    public void assertRowNotExistsByClauseAsync(String table, Duration duration, Document clause) {
        assertAsync(() -> matcherEmpty().test(countBy(table, clause.getContent())), duration, () ->
                Assertions.fail(message(
                        ERROR_ASSERT_NO_RECORD_EXPECTED,
                        GIVEN_WHERE_CLAUSE, Database.from(connection()).table(table), "it does"
                )));
    }

    /**
     * Asserts the count of rows in the given table that match the provided WHERE
     * clause.
     *
     * @param table   The name of the table
     * @param matcher The assertion to be applied on the count of rows
     * @param clause  The SQL WHERE clause
     */
    @Step(value = "db.assert.table.count.sql.where", args = {"table:word", "matcher:long-assertion"})
    public void assertRowCountByClause(String table, Assertion<Long> matcher, Document clause) {
        long count = countBy(table, clause.getContent());
        if (!matcher.test(count)) {
            Assertions.fail(message(
                    ERROR_ASSERT_SOME_RECORD_EXPECTED,
                    GIVEN_WHERE_CLAUSE,
                    Database.from(connection()).table(table),
                    matcher.describeFailure(count)
            ));
        }
    }

    /**
     * Asserts asynchronously the count of rows in the given table that match the
     * provided WHERE clause.
     *
     * @param table    The name of the table
     * @param matcher  The assertion to be applied on the count of rows
     * @param duration The maximum time to wait for the assertion to complete
     * @param clause   The SQL WHERE clause
     */
    @Step(value = "db.assert.table.count.sql.where.async",
            args = {"table:word", "matcher:long-assertion", "duration:duration"})
    public void assertRowCountByClauseAsync(String table, Assertion<Long> matcher, Duration duration, Document clause) {
        AtomicLong result = new AtomicLong(0);
        assertAsync(() -> matcher.test(result.addAndGet(countBy(table, clause.getContent()))), duration, () ->
                Assertions.fail(message(
                        ERROR_ASSERT_SOME_RECORD_EXPECTED,
                        GIVEN_WHERE_CLAUSE,
                        Database.from(connection()).table(table),
                        matcher.describeFailure(result.get())
                )));
    }

    /**
     * Asserts that the data provided in the DataTable exists in the given table.
     *
     * @param table     The name of the table
     * @param dataTable The DataTable containing the data to be checked for existence
     */
    @Step(value = "db.assert.table.exists.data", args = "table:word")
    public void assertDataTableExists(String table, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertNonEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }


    /**
     * Asserts asynchronously that the specified DataTable exists in the database
     * table within the given time frame.
     *
     * @param table     The name of the database table
     * @param duration  The maximum time to wait for the assertion to complete
     * @param dataTable The DataTable to check for existence in the database table
     */
    @Step(value = "db.assert.table.exists.data.async", args = {"table:word", "duration:duration"})
    public void assertDataTableExistsAsync(String table, Duration duration, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertNonEmptyAsync(dataSet, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that the specified DataTable does not exist in the database table.
     *
     * @param table     The name of the database table
     * @param dataTable The DataTable to check for absence in the database table
     */
    @Step(value = "db.assert.table.not.exists.data", args = "table:word")
    public void assertDataTableNotExists(String table, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertEmpty(dataSet);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts asynchronously that the specified DataTable does not exist in the
     * database table within a given time frame.
     *
     * @param table     The name of the database table
     * @param duration  The maximum time to wait for the assertion to complete
     * @param dataTable The DataTable to check for absence in the database table
     */
    @Step(value = "db.assert.table.not.exists.data.async", args = {"table:word", "duration:duration"})
    public void assertDataTableNotExistsAsync(String table, Duration duration, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertEmptyAsync(dataSet, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts the count of rows in the specified DataTable against the provided
     * assertion matcher.
     *
     * @param table     The name of the database table
     * @param matcher   The assertion matcher for the row count
     * @param dataTable The DataTable containing the rows to be counted
     */
    @Step(value = "db.assert.table.count.data", args = {"table:word", "matcher:long-assertion"})
    public void assertDataTableCount(String table, Assertion<Long> matcher, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertCount(dataSet, matcher);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts asynchronously the count of rows in the specified DataTable against
     * the provided assertion matcher.
     *
     * @param table     The name of the database table
     * @param matcher   The assertion matcher for the row count
     * @param duration  The maximum time to wait for the assertion to complete
     * @param dataTable The DataTable containing the rows to be counted
     */
    @Step(value = "db.assert.table.count.data.async",
            args = {"table:word", "matcher:long-assertion", "duration:duration"})
    public void assertDataTableCountAsync(
            String table, Assertion<Long> matcher, Duration duration, DataTable dataTable) {
        try (DataSet dataSet = new DataTableDataSet(table, dataTable, nullSymbol)) {
            assertCountAsync(dataSet, matcher, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that the data rows included in the specified XLS file exist.
     *
     * @param file The XLS file containing the data rows to be asserted
     */
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

    /**
     * Asserts asynchronously that the data rows included in the specified XLS file exist.
     *
     * @param file     The XLS file containing the data rows to be asserted
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.exists.xls.async", args = {"xls:file", "duration:duration"})
    public void assertXLSFileExistsAsync(File file, Duration duration) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            for (DataSet dataSet : multiDataSet) {
                duration = duration.minus(assertNonEmptyAsync(dataSet, duration));
            }
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that the data rows included in the specified XLS file do not exist.
     *
     * @param file The XLS file containing the data rows to be asserted
     */
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

    /**
     * Asserts asynchronously that the data rows included in the specified XLS file do not exist.
     *
     * @param file     The XLS file containing the data rows to be asserted
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.not.exists.xls.async", args = {"xls:file", "duration:duration"})
    public void assertXLSFileNotExistsAsync(File file, Duration duration) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (MultiDataSet multiDataSet = new OoxmlDataSet(file, xlsIgnoreSheetRegex, nullSymbol)) {
            for (DataSet dataSet : multiDataSet) {
                duration = duration.minus(assertEmptyAsync(dataSet, duration));
            }
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that the data rows included in the specified CSV file exist.
     *
     * @param file  The CSV file containing the data rows to be asserted
     * @param table The name of the table where data is to be asserted
     */
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

    /**
     * Asserts asynchronously that the data rows included in the specified CSV file exist.
     *
     * @param file     The CSV file containing the data rows to be asserted
     * @param table    The name of the table where data is to be asserted
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.exists.csv.async", args = {"csv:file", "table:word", "duration:duration"})
    public void assertCSVFileExistsAsync(File file, String table, Duration duration) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            assertNonEmptyAsync(dataSet, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that the data rows included in the specified CSV file do not exist.
     *
     * @param file  The CSV file containing the data rows to be asserted
     * @param table The name of the table where data is to be asserted
     */
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

    /**
     * Asserts asynchronously that the data rows included in the specified CSV file do not exist.
     *
     * @param file     The CSV file containing the data rows to be asserted
     * @param table    The name of the table where data is to be asserted
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.not.exists.csv.async", args = {"csv:file", "table:word", "duration:duration"})
    public void assertCSVFileNotExistsAsync(File file, String table, Duration duration) {
        file = resourceLoader().absolutePath(file);
        assertFileExists(file);

        try (DataSet dataSet = new CsvDataSet(table, file, csvFormat, nullSymbol)) {
            assertEmptyAsync(dataSet, duration);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Asserts that the specified database table is not empty.
     *
     * @param table The name of the database table to be asserted
     */
    @Step(value = "db.assert.table.not.empty", args = "table:word")
    public void assertTableIsNotEmpty(String table) {
        if (!matcherNonEmpty().test(countBy(table, "1=1"))) {
            Assertions.fail(message(
                    "It was expected some record exist in table {}, but it doesn't",
                    Database.from(connection()).table(table)
            ));
        }
    }

    /**
     * Asserts asynchronously that the specified database table is not empty.
     *
     * @param table    The name of the database table to be asserted
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.not.empty.async", args = {"table:word", "duration:duration"})
    public void assertTableIsNotEmptyAsync(String table, Duration duration) {
        assertAsync(() -> matcherNonEmpty().test(countBy(table, "1=1")), duration, () ->
                Assertions.fail(message(
                        "It was expected some record exist in table {}, but it doesn't",
                        Database.from(connection()).table(table)
                ))
        );
    }

    /**
     * Asserts that the specified database table is empty.
     *
     * @param table The name of the database table to be asserted
     */
    @Step(value = "db.assert.table.empty", args = "table:word")
    public void assertTableIsEmpty(String table) {
        if (!matcherEmpty().test(countBy(table, "1=1"))) {
            Assertions.fail(message(
                    "It was expected no record exist in table {}, but it does",
                    Database.from(connection()).table(table)
            ));
        }
    }

    /**
     * Asserts asynchronously that the specified database table is empty.
     *
     * @param table    The name of the database table to be asserted
     * @param duration The maximum time to wait for the assertion to complete
     */
    @Step(value = "db.assert.table.empty.async", args = {"table:word", "duration:duration"})
    public void assertTableIsEmptyAsync(String table, Duration duration) {
        assertAsync(() -> matcherEmpty().test(countBy(table, "1=1")), duration, () ->
                Assertions.fail(message(
                        "It was expected no record exist in table {}, but it does",
                        Database.from(connection()).table(table)
                ))
        );
    }

}