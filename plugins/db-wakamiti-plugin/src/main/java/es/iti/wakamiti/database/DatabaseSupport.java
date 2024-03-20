/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.database.dataset.DataSet;
import es.iti.wakamiti.database.dataset.EmptyDataSet;
import es.iti.wakamiti.database.dataset.MapDataSet;
import es.iti.wakamiti.database.jdbc.Record;
import es.iti.wakamiti.database.jdbc.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;
import org.awaitility.Durations;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.Matchers;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static es.iti.wakamiti.database.DatabaseHelper.*;
import static es.iti.wakamiti.database.jdbc.LogUtils.message;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;


/**
 * Provides support for database operations and assertions.
 */
public class DatabaseSupport {

    public static final String DEFAULT = "default";
    protected static final String ERROR_ASSERT_NO_RECORD_EXPECTED = "It was expected no record satisfying {} exist in table {}, but {}";
    protected static final String ERROR_ASSERT_SOME_RECORD_EXPECTED = "It was expected some record satisfying {} exist in table {}, but {}";
    protected static final String GIVEN_WHERE_CLAUSE = "the given WHERE clause";
    protected static final String ERROR_CLOSING_DATASET = "Error closing dataset";
    protected static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.database");
    protected final Map<String, ConnectionProvider> connections = new HashMap<>();
    protected final Deque<Runnable> cleanUpOperations = new LinkedList<>();
    protected final AtomicReference<String> currentConnection = new AtomicReference<>();
    protected String xlsIgnoreSheetRegex;
    protected String nullSymbol;
    protected String csvFormat;
    protected boolean enableCleanupUponCompletion;
    protected boolean healthcheck;
    protected Function<Map<String, String>, Map<String, String>> nullSymbolMapper = map ->
            map.entrySet().stream().collect(collectToMap(
                    Map.Entry::getKey,
                    e -> e.getValue().equals(nullSymbol) ? null : e.getValue()));

    protected static ResourceLoader resourceLoader() {
        return WakamitiAPI.instance().resourceLoader();
    }

    /**
     * Sets the regular expression for ignoring sheets in XLS files.
     *
     * @param ignoreSheetRegex The regular expression to ignore sheets in XLS files.
     */
    public void setXlsIgnoreSheetRegex(String ignoreSheetRegex) {
        this.xlsIgnoreSheetRegex = ignoreSheetRegex;
    }

    /**
     * Sets the symbol representing {@code null} values.
     *
     * @param nullSymbol The symbol representing {@code null} values.
     */
    public void setNullSymbol(String nullSymbol) {
        this.nullSymbol = nullSymbol;
    }

    /**
     * Sets the format for CSV files.
     *
     * @param csvFormat The format for CSV files.
     */
    public void setCsvFormat(String csvFormat) {
        this.csvFormat = csvFormat;
    }

    /**
     * Sets whether to enable cleanup upon completion.
     *
     * @param enableCleanupUponCompletion {@code true} to enable cleanup upon completion, {@code false} otherwise.
     */
    public void setEnableCleanupUponCompletion(boolean enableCleanupUponCompletion) {
        this.enableCleanupUponCompletion = enableCleanupUponCompletion;
        LOGGER.trace("Cleanup " + (enableCleanupUponCompletion ? "enabled" : "disabled"));
    }

    /**
     * Sets whether to perform a health check on connections.
     *
     * @param healthcheck {@code true} to perform a health check on connections, {@code false} otherwise.
     */
    public void setHealthcheck(boolean healthcheck) {
        this.healthcheck = healthcheck;
    }

    /**
     * Adds a database connection with the specified alias and parameters.
     *
     * @param alias      The alias for the connection.
     * @param parameters The connection parameters.
     */
    public void addConnection(String alias, ConnectionParameters parameters) {
        LOGGER.debug("Setting '{}' connection parameters {}", alias, parameters);
        if (connections.containsKey(alias)) {
            connections.remove(alias).close();
        }
        ConnectionProvider connectionProvider = new ConnectionProvider(parameters);
        if (healthcheck) {
            connectionProvider.test();
        }
        connections.put(alias, connectionProvider);
    }

    /**
     * Adds a database connection with the default alias and the specified parameters.
     *
     * @param parameters The connection parameters.
     */
    public void addConnection(ConnectionParameters parameters) {
        addConnection(DEFAULT, parameters);
    }

    /**
     * Matches an assertion for an empty result.
     *
     * @return An assertion for an empty result.
     */
    protected Assertion<Long> matcherEmpty() {
        return new MatcherAssertion<>(Matchers.equalTo(0L));
    }

    /**
     * Matches an assertion for a non-empty result.
     *
     * @return An assertion for a non-empty result.
     */
    protected Assertion<Long> matcherNonEmpty() {
        return new MatcherAssertion<>(Matchers.greaterThan(0L));
    }

    /**
     * Retrieves the current database connection.
     *
     * @return The current database connection.
     */
    protected ConnectionProvider connection() {
        String alias = Optional.ofNullable(currentConnection.get()).orElse(
                connections.keySet().stream().findFirst()
                        .orElseThrow(() -> new WakamitiException("There is no default connection"))
        );
        LOGGER.trace("Using '{}' connection", alias);
        return connections.get(alias);
    }

    /**
     * Asserts that the given file exists.
     *
     * @param file The file to check.
     */
    protected void assertFileExists(File file) {
        if (!file.exists()) {
            throw new WakamitiException("File '{}' not found", file.getAbsolutePath());
        }
    }

    /**
     * Executes the given SQL script.
     *
     * @param script                The SQL script to execute.
     * @param cleanupUponCompletion {@code true} to perform cleanup upon completion, {@code false} otherwise.
     * @return Inserted and/or updated rows
     */
    protected List<Map<String, String>> executeScript(String script, boolean cleanupUponCompletion) {
        List<Map<String, String>> results = new LinkedList<>();
        Stream.of(script.split(unquotedRegex(";+")))  // split unquoted ';'
                .map(String::trim).filter(s -> !s.isEmpty())
                .forEach(sentence -> {
                    try {
                        if (cleanupUponCompletion) {
                            SQLParser.parseStatement(sentence).accept(new PreCleanUpStatementVisitorAdapter());
                        }
                        Database db = Database.from(connection());
                        try (Update update = db.update(sentence).execute()) {
                            Statement statement = SQLParser.parseStatement(sentence);

                            PostCleanUpStatementVisitorAdapter adapter = new PostCleanUpStatementVisitorAdapter();
                            statement.accept(adapter);
                            Optional<DataSet> result = adapter.getResult();

                            if (result.isPresent() && cleanupUponCompletion
                                    && statement instanceof net.sf.jsqlparser.statement.insert.Insert) {
                                cleanUpOperations.addFirst(() -> {
                                    try (DataSet dataSet = result.get().copy()) {
                                        deleteDataSet(dataSet, false);
                                    } catch (IOException e) {
                                        LOGGER.error(ERROR_CLOSING_DATASET, e);
                                    }
                                });
                            }
                            result.map(DatabaseHelper::read).ifPresent(list -> {
                                results.addAll(
                                        list.stream().map(m -> m.entrySet().stream().collect(
                                                        collectToMap(Map.Entry::getKey, e -> DatabaseHelper.toString(e.getValue()))))
                                                .collect(Collectors.toList())
                                );
                            });
                        }
                    } catch (JSQLParserException e) {
                        if (enableCleanupUponCompletion) {
                            throw new WakamitiException(
                                    message("Cannot parse script. Please, disable the '{}' property",
                                            DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION), e);
                        } else {
                            LOGGER.error("Cannot retrieve statement results", e);
                        }
                    }
                });
        return results;
    }

    /**
     * Executes the given SQL SELECT statement and returns the result as
     * a list of maps.
     *
     * @param sql The SQL SELECT statement to execute.
     * @return A list of maps representing the result set, where each map
     * corresponds to a row, and keys are column names.
     */
    protected List<Map<String, String>> executeSelect(String sql) {
        try (Select<Map<String, String>> select = Database.from(connection()).select(sql)
                .get(DatabaseHelper::formatToMap)) {
            return select.map(map -> map.entrySet().stream().collect(collectToMap(
                            Map.Entry::getKey,
                            e -> Optional.ofNullable(e.getValue()).orElse(nullSymbol)
                    )))
                    .stream().collect(Collectors.toList());
        }
    }

    /**
     * Executes the given SQL CALL statement and returns the result as a
     * list of maps.
     *
     * @param sql                   The SQL CALL statement to execute.
     * @param cleanupUponCompletion Flag indicating whether to perform cleanup upon
     *                              completion (not supported for CALL statements).
     * @return A list of maps representing the result set, where each map corresponds
     * to a row, and keys are column names.
     * @throws WakamitiException If an error occurs during SQL execution.
     */
    protected List<List<Map<String, String>>> executeCall(String sql, boolean cleanupUponCompletion) {
        if (cleanupUponCompletion) {
            LOGGER.warn("Unable to obtain the clean-up statements of a procedure");
        }
        try (Call<Map<String, String>> call = Database.from(connection()).call(sql)
                .get(DatabaseHelper::formatToMap)) {
            return call.map(map -> map.entrySet().stream().collect(collectToMap(
                            Map.Entry::getKey,
                            e -> Optional.ofNullable(e.getValue()).orElse(nullSymbol)
                    )))
                    .execute()
                    .stream().collect(Collectors.toList());
        }
    }

    /**
     * Retrieves the primary key column of the specified table.
     *
     * @param table The name of the table.
     * @return The primary key column.
     * @throws WakamitiException If more than one primary key column is found or if
     *                           no primary key is found.
     */
    protected String primaryKey(String table) {
        String[] keyColumn = primaryKeys(table);
        if (keyColumn.length > 1) {
            throw new WakamitiException(message("A single primary key in table {} is required", table));
        }
        return keyColumn[0];
    }

    /**
     * Retrieves the primary key columns of the specified table.
     *
     * @param table The name of the table.
     * @return The array of primary key columns.
     * @throws WakamitiException If no primary key is found in the table.
     */
    protected String[] primaryKeys(String table) {
        Database db = Database.from(connection());
        String[] keyColumn = db.primaryKey(table).toArray(String[]::new);
        if (keyColumn.length < 1) {
            throw new WakamitiException(message("A primary key in table {} is required.", table));
        }
        return keyColumn;
    }

    /**
     * Counts the number of rows in the specified table that match the given conditions.
     *
     * @param table   The name of the table.
     * @param columns The array of column names to match.
     * @param values  The array of corresponding values to match.
     * @return The count of rows that satisfy the conditions.
     */
    protected long countBy(String table, String[] columns, Object[] values) {
        Database db = Database.from(connection());
        return countBy(db, db.parser().sqlSelectCountFrom(db.table(table),
                Stream.of(columns).map(c -> db.column(db.table(table), c)).toArray(String[]::new), values).toString());
    }

    /**
     * Counts the number of rows in the specified table that match the given SQL WHERE clause.
     *
     * @param table The name of the table.
     * @param where The SQL WHERE clause.
     * @return The count of rows that satisfy the conditions.
     */
    protected long countBy(String table, String where) {
        Database db = Database.from(connection());
        return countBy(db, message("SELECT count(*) FROM {} WHERE {}",
                db.parser().format(db.table(table)), where));
    }

    /**
     * Counts the number of rows returned by the provided SQL query.
     *
     * @param db  The database instance.
     * @param sql The SQL query.
     * @return The count of rows returned by the query.
     */
    private long countBy(Database db, String sql) {
        try (Select<String[]> select = db.select(sql).get(DatabaseHelper::format)) {
            return select.stream().findFirst().map(v -> v[0]).map(Long::parseLong).orElse(0L);
        }
    }

    /**
     * Finds a record in the specified table that is similar to the provided values in the specified columns.
     *
     * @param table   The name of the table.
     * @param columns The columns to search for similarity.
     * @param values  The values to compare for similarity.
     * @return An optional containing a map representing the similar record if found, empty otherwise.
     */
    protected Optional<Map<String, String>> similarBy(String table, String[] columns, Object[] values) {
        Database db = Database.from(connection());
        String sql = db.parser().sqlSelectFrom(db.parser().format(db.table(table)),
                Stream.of(columns).map(c -> db.parser().format(db.column(db.table(table), c)))
                        .toArray(String[]::new)).toString();
        try (Select<String[]> select = db.select(sql).get(DatabaseHelper::format)) {
            return select.map(row -> new Record(row, IntStream.range(0, values.length)
                            .mapToDouble(i -> {
                                String expectedValue = Optional.ofNullable(values[i])
                                        .map(DatabaseHelper::toString).orElse("");
                                String rowValue = Optional.ofNullable(row[i]).orElse("");
                                int maxLength = Math.max(expectedValue.length(), rowValue.length());
                                double distance = new org.apache.commons.text.similarity.LevenshteinDistance()
                                        .apply(expectedValue, rowValue);
                                if (maxLength == 0) return 1.0;
                                return (maxLength - distance) / (double) maxLength;
                            }).sum() / values.length))
                    .filter(rec -> rec.score() > 0.7)
                    .reduce((rec1, rec2) -> rec1.score() > rec2.score() ? rec1 : rec2)
                    .stream().peek(rec -> LOGGER.trace("Found {}", rec)).findFirst()
                    .map(rec -> toMap(select.getColumnNames(), rec.data()));
        }
    }

    /**
     * Processes a single row of data for a specified table.
     *
     * @param table   The name of the table.
     * @param columns The columns of the row.
     * @param values  The values of the row.
     * @return A pair containing the processed columns and values.
     */
    protected Pair<String[], Object[]> processRow(String table, String[] columns, String[] values) {
        Database db = Database.from(connection());
        return toPair(db.processData(table, toMap(columns, values)))
                .map((k, v) -> new Pair<>(k.toArray(new String[0]), v.toArray()));
    }

    /**
     * Processes all rows in a DataSet and returns a list of pairs containing the
     * processed columns and values for each row.
     *
     * @param dataSet The DataSet containing the rows to process.
     * @return A list of pairs containing the processed columns and values for each row.
     */
    protected List<Pair<String[], Object[]>> processRows(DataSet dataSet) {
        List<Pair<String[], Object[]>> rows = new LinkedList<>();
        while (dataSet.nextRow()) {
            rows.add(processRow(dataSet.table(), dataSet.columns(),
                    Stream.of(dataSet.values()).map(DatabaseHelper::toString).toArray(String[]::new)));
        }
        return rows;
    }

    /**
     * Asserts that the given DataSet is not empty.
     *
     * @param dataSet The DataSet to be checked.
     */
    protected void assertNonEmpty(DataSet dataSet) {
        List<Pair<String[], Object[]>> rows = processRows(dataSet);
        for (Pair<String[], Object[]> row : rows) {
            if (!matcherNonEmpty().test(countBy(dataSet.table(), row.key(), row.value()))) {
                similarBy(dataSet.table(), row.key(), row.value()).ifPresentOrElse(result ->
                                assertThat(result).containsExactlyEntriesOf(toMap(row.key(),
                                        Stream.of(row.value()).map(DatabaseHelper::toString).toArray(String[]::new))),
                        () -> fail(message(
                                ERROR_ASSERT_SOME_RECORD_EXPECTED,
                                toMap(row.key(), row.value()),
                                Database.from(connection()).table(dataSet.table()), "it doesn't"
                        )));
            }
        }
    }

    /**
     * Asserts that the given DataSet is empty.
     *
     * @param dataSet The DataSet to be checked.
     */
    protected void assertEmpty(DataSet dataSet) {
        List<Pair<String[], Object[]>> rows = processRows(dataSet);
        for (Pair<String[], Object[]> row : rows) {
            if (!matcherEmpty().test(countBy(dataSet.table(), row.key(), row.value()))) {
                fail(message(
                        ERROR_ASSERT_NO_RECORD_EXPECTED,
                        toMap(row.key(), row.value()),
                        Database.from(connection()).table(dataSet.table()), "it does"
                ));
            }
        }
    }

    /**
     * Asserts that the count of records in the given DataSet satisfies the specified matcher.
     *
     * @param dataSet The DataSet to be checked.
     * @param matcher The assertion to be applied to the count of records.
     */
    protected void assertCount(DataSet dataSet, Assertion<Long> matcher) {
        List<Pair<String[], Object[]>> rows = processRows(dataSet);
        long count = rows
                .stream()
                .mapToLong(row -> countBy(dataSet.table(), row.key(), row.value()))
                .sum();
        if (!matcher.test(count)) {
            fail(message(
                    ERROR_ASSERT_SOME_RECORD_EXPECTED,
                    rows.size() == 1 ? toMap(rows.get(0).key(), rows.get(0).value()) : "the given data",
                    Database.from(connection()).table(dataSet.table()), matcher.describeFailure(count)
            ));
        }
    }

    /**
     * Asserts asynchronously that the count of records in the given DataSet
     * satisfies the specified matcher within the specified time.
     *
     * @param dataSet The DataSet to be checked.
     * @param matcher The assertion to be applied to the count of records.
     * @param time    The maximum time to wait for the assertion to succeed, in milliseconds.
     */
    protected void assertCountAsync(DataSet dataSet, Assertion<Long> matcher, int time) {
        List<Pair<String[], Object[]>> rows = processRows(dataSet);
        AtomicLong count = new AtomicLong(0);
        assertAsync(() -> {
            count.set(rows.stream()
                    .mapToLong(row -> countBy(dataSet.table(), row.key(), row.value()))
                    .sum());
            return matcher.test(count.get());
        }, time, () -> fail(message(
                ERROR_ASSERT_SOME_RECORD_EXPECTED,
                rows.size() == 1 ? toMap(rows.get(0).key(), rows.get(0).value()) : "the given data",
                Database.from(connection()).table(dataSet.table()), matcher.describeFailure(count.get())
        )));
    }

    /**
     * Asserts asynchronously that the given action satisfies the specified condition within the specified time.
     * If the condition is not satisfied within the specified time, executes the catch action.
     *
     * @param action      The action to be checked asynchronously.
     * @param time        The maximum time to wait for the condition to be satisfied, in seconds.
     * @param catchAction The action to be executed if the condition is not satisfied within the specified time.
     */
    protected void assertAsync(BooleanSupplier action, int time, Runnable catchAction) {
        try {
            await()
                    .atMost(time, TimeUnit.SECONDS)
                    .pollInterval(Durations.ONE_HUNDRED_MILLISECONDS)
                    .until(action::getAsBoolean);
        } catch (ConditionTimeoutException ignored) {
            catchAction.run();
        }
    }

    /**
     * Asserts asynchronously that each row in the given data set is not empty within the specified time.
     * If any row is found to be empty within the specified time, executes further assertions on the empty row.
     *
     * @param dataSet The data set to be checked asynchronously.
     * @param time    The maximum time to wait for each row to be non-empty, in seconds.
     * @return The duration taken to perform the assertion asynchronously.
     */
    protected Duration assertNonEmptyAsync(DataSet dataSet, Integer time) {
        List<Pair<String[], Object[]>> rows = processRows(dataSet);
        AtomicReference<Pair<String[], Object[]>> currentRow = new AtomicReference<>();

        Temporal start = Instant.now();
        assertAsync(() -> {
            for (Pair<String[], Object[]> row : rows) {
                if (!matcherNonEmpty().test(countBy(dataSet.table(), row.key(), row.value()))) {
                    currentRow.set(row);
                    return false;
                }
            }
            return true;
        }, time, () -> {
            Pair<String[], Object[]> processed = currentRow.get();
            similarBy(dataSet.table(), processed.key(), processed.value()).ifPresentOrElse(row ->
                            assertThat(row).containsExactlyEntriesOf(
                                    toMap(processed.key(), DatabaseHelper.toString(processed.value()))),
                    failSomeRecordExpected(dataSet.table(), currentRow)
            );
        });
        return Duration.between(start, Instant.now());
    }

    /**
     * Asserts asynchronously that each row in the given data set is empty within the specified time.
     * If any row is found to be non-empty within the specified time, executes further assertions on the non-empty row.
     *
     * @param dataSet The data set to be checked asynchronously.
     * @param time    The maximum time to wait for each row to be empty, in seconds.
     * @return The duration taken to perform the assertion asynchronously.
     */
    protected Duration assertEmptyAsync(DataSet dataSet, Integer time) {
        List<Pair<String[], Object[]>> rows = processRows(dataSet);
        AtomicReference<Pair<String[], Object[]>> currentRow = new AtomicReference<>();

        Temporal start = Instant.now();
        assertAsync(() -> {
            for (Pair<String[], Object[]> row : rows) {
                if (!matcherEmpty().test(countBy(dataSet.table(), row.key(), row.value()))) {
                    currentRow.set(row);
                    return false;
                }
            }
            return true;
        }, time, failNoRecordExpected(dataSet.table(), currentRow));
        return Duration.between(start, Instant.now());
    }

    /**
     * Creates a runnable action to fail the assertion when some record is expected but not found.
     *
     * @param table The name of the table where the record was expected.
     * @param row   The atomic reference to the row that was expected but not found.
     * @return A runnable action to fail the assertion.
     */
    protected Runnable failSomeRecordExpected(String table, AtomicReference<Pair<String[], Object[]>> row) {
        return () -> fail(message(
                ERROR_ASSERT_SOME_RECORD_EXPECTED,
                toMap(row.get().key(), row.get().value()), Database.from(connection()).table(table), "it doesn't"
        ));
    }

    /**
     * Creates a runnable action to fail the assertion when no record is expected but found.
     *
     * @param table The name of the table where no record was expected.
     * @param row   The atomic reference to the row that was found unexpectedly.
     * @return A runnable action to fail the assertion.
     */
    protected Runnable failNoRecordExpected(String table, AtomicReference<Pair<String[], Object[]>> row) {
        return () -> fail(message(
                ERROR_ASSERT_NO_RECORD_EXPECTED,
                toMap(row.get().key(), row.get().value()), Database.from(connection()).table(table), "it does"
        ));
    }

    /**
     * Inserts rows from the given DataSet into the database table.
     *
     * @param dataSet             The DataSet containing rows to insert.
     * @param addCleanUpOperation Flag indicating whether to add clean-up operations after insertion.
     * @return Inserted rows
     */
    protected List<Map<String, String>> insertDataSet(DataSet dataSet, boolean addCleanUpOperation) {
        LOGGER.debug("Inserting rows in table {} from {}...", dataSet.table(), dataSet.origin());

        List<Map<String, String>> results = new LinkedList<>();
        Database db = Database.from(connection());
        String table = db.table(dataSet.table());
        while (dataSet.nextRow()) {
            Map<String, Object> row = db.processData(dataSet.table(),
                    dataSet.rowAsMap().entrySet().stream().collect(collectToMap(
                            Map.Entry::getKey, e -> DatabaseHelper.toString(e.getValue()))));
            net.sf.jsqlparser.statement.insert.Insert insert = db.parser().toInsert(table, row);
            try (Update update = db.update(insert.toString()).execute()) {
                PostCleanUpStatementVisitorAdapter adapter = new PostCleanUpStatementVisitorAdapter();
                insert.accept(adapter);
                Optional<DataSet> result = adapter.getResult();

                if (result.isPresent() && addCleanUpOperation) {
                    cleanUpOperations.addFirst(() -> {
                        try (DataSet ds = result.get().copy()) {
                            deleteDataSet(ds, false);
                        } catch (IOException e) {
                            LOGGER.error(ERROR_CLOSING_DATASET, e);
                        }
                    });
                }
                result.map(DatabaseHelper::read).ifPresent(list -> {
                    results.addAll(
                            list.stream().map(m -> m.entrySet().stream().collect(
                                            collectToMap(Map.Entry::getKey, e -> DatabaseHelper.toString(e.getValue()))))
                                    .collect(Collectors.toList())
                    );
                });
            }
        }
        return results;
    }

    /**
     * Deletes rows from the given DataSet from the database table.
     *
     * @param dataSet             The DataSet containing rows to delete.
     * @param addCleanUpOperation Flag indicating whether to add clean-up operations before deletion.
     */
    protected void deleteDataSet(DataSet dataSet, boolean addCleanUpOperation) {
        LOGGER.debug("Deleting rows in table {} from {}...", dataSet.table(), dataSet.origin());

        Database db = Database.from(connection());
        String table = db.table(dataSet.table());
        while (dataSet.nextRow()) {
            Map<String, Object> row = db.processData(dataSet.table(),
                    dataSet.rowAsMap().entrySet().stream().collect(collectToMap(
                            Map.Entry::getKey, e -> DatabaseHelper.toString(e.getValue()))));
            net.sf.jsqlparser.statement.delete.Delete delete = db.parser().toDelete(table, row);
            if (addCleanUpOperation) {
                delete.accept(new PreCleanUpStatementVisitorAdapter());
            }
            db.update(delete.toString()).execute().close();
        }
    }

    /**
     * Deletes all rows from the given database table.
     *
     * @param table               The name of the table to truncate.
     * @param addCleanUpOperation Flag indicating whether to add cleanup operations
     *                            before truncation.
     */
    protected void truncateTable(String table, boolean addCleanUpOperation) {
        LOGGER.debug("Deleting all rows in table {}...", table);

        Database db = Database.from(connection());
        table = db.table(table);
        if (addCleanUpOperation) {
            db.parser().toDelete(table).accept(new PreCleanUpStatementVisitorAdapter());
        }
        db.truncate(table);
    }

    /**
     * Deletes rows from the given table based on the provided WHERE clause.
     *
     * @param table               The name of the table from which to delete rows.
     * @param where               The WHERE clause to specify which rows to delete.
     * @param addCleanUpOperation Flag indicating whether to add cleanup operations before deletion.
     */
    protected void deleteTable(String table, String where, boolean addCleanUpOperation) {
        LOGGER.debug("Deleting rows in table {} from {}...", table, "clause");

        Database db = Database.from(connection());
        table = db.table(table);
        if (addCleanUpOperation) {
            try {
                net.sf.jsqlparser.statement.delete.Delete delete = db.parser().toDelete(table);
                delete.setWhere(SQLParser.parseExpression(where));
                delete.accept(new PreCleanUpStatementVisitorAdapter());
            } catch (JSQLParserException e) {
                String message = message("Cannot parse the where clause. Please, disable the '{}' property",
                        DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION);
                throw new WakamitiException(message, e);
            }
        }
        db.update(message("DELETE FROM {} WHERE {}", table, where)).execute().close();
    }

    /**
     * Updates rows in the given table based on the provided data set.
     *
     * @param dataSet The data set containing the rows to be updated.
     */
    protected void updateDataSet(DataSet dataSet, List<UpdateSet> updateSets) {
        LOGGER.debug("Updating rows in table {} from {}...", dataSet.table(), dataSet.origin());

        Database db = Database.from(connection());
        String table = db.table(dataSet.table());

        List<String> setColumns = updateSets.stream()
                .flatMap(set -> set.getColumns().stream())
                .map(Column::getColumnName)
                .collect(Collectors.toList());

        while (dataSet.nextRow()) {
            Map<String, Object> row = db.processData(dataSet.table(),
                    dataSet.rowAsMap().entrySet().stream().collect(collectToMap(
                            Map.Entry::getKey, e -> DatabaseHelper.toString(e.getValue()))));
            Map<String, Object> sets = row.entrySet().stream()
                    .filter(e -> setColumns.contains(e.getKey()))
                    .collect(collectToMap());
            Map<String, Object> where = row.entrySet().stream()
                    .filter(e -> !setColumns.contains(e.getKey()))
                    .collect(collectToMap());
            List<Expression> whereList = new LinkedList<>();
            whereList.add(db.parser().toWhere(updateSets));
            if (!where.isEmpty()) whereList.add(db.parser().createWhere(where));

            net.sf.jsqlparser.statement.update.Update update = db.parser().toUpdate(table, sets,
                    new MultiAndExpression(whereList));

            db.update(update.toString()).execute().close();
        }
    }

    /**
     * Performs a SELECT operation and returns the result as a MapDataSet.
     *
     * @param select The SELECT statement to execute.
     * @return The result of the SELECT operation as a MapDataSet.
     */
    private MapDataSet doSelect(net.sf.jsqlparser.statement.select.Select select) {
        String table = ((net.sf.jsqlparser.statement.select.PlainSelect) select).getFromItem().toString();
        Database db = Database.from(connection());
        try (Select<Object[]> s = db.select(select.toString()).get(DatabaseHelper::format)) {
            String[] columns = s.getColumnNames();
            Object[][] values = s.stream().toArray(Object[][]::new);
            return new MapDataSet(db.table(table), columns, values, nullSymbol);
        }
    }

    /**
     * An adapter class for pre-cleanup operations in SQL statements.
     */
    private class PreCleanUpStatementVisitorAdapter extends net.sf.jsqlparser.statement.StatementVisitorAdapter {

        // TODO: Does not work with cascade deletion
        @Override
        public void visit(net.sf.jsqlparser.statement.truncate.Truncate truncate) {
            Database db = Database.from(connection());
            String table = db.table(truncate.getTable().getName());
            Delete delete = new net.sf.jsqlparser.statement.delete.Delete();
            delete.setTable(new Table(db.parser().format(table)));
            visit(delete);
        }

        @Override
        public void visit(net.sf.jsqlparser.statement.delete.Delete delete) {
            Database db = Database.from(connection());
            String table = db.table(delete.getTable().getName());
            delete.setTable(new Table(db.parser().format(table)));
            db.parser().formatColumns(delete.getWhere(), column -> db.parser().format(db.column(table, column)));

            DataSet dataSet = db.parser()
                    .toSelect(delete)
                    .map(DatabaseSupport.this::doSelect)
                    .map(ds -> (DataSet) ds)
                    .orElse(new EmptyDataSet(table));
            cleanUpOperations.addFirst(() -> {
                try {
                    insertDataSet(dataSet, false);
                    dataSet.close();
                } catch (IOException e) {
                    LOGGER.error(ERROR_CLOSING_DATASET, e);
                }
            });
        }

        @Override
        public void visit(net.sf.jsqlparser.statement.update.Update update) {
            Database db = Database.from(connection());
            String table = db.table(update.getTable().getName());
            update.setTable(new Table(db.parser().format(table)));
            db.parser().formatColumns(update.getWhere(), column -> db.parser().format(db.column(table, column)));

            update.getUpdateSets().stream().flatMap(set -> set.getColumns().stream()).forEach(c ->
                    db.parser().formatColumns(c, column -> db.parser().format(db.column(table, column)))
            );
            update.getUpdateSets().stream().flatMap(set -> set.getValues().stream()).forEach(v ->
                    db.parser().formatColumns(v, column -> db.parser().format(db.column(table, column)))
            );

            DataSet dataSet = db.parser()
                    .toSelect(update)
                    .map(DatabaseSupport.this::doSelect)
                    .map(ds -> (DataSet) ds)
                    .orElse(new EmptyDataSet(table));
            cleanUpOperations.addFirst(() -> {
                try {
                    updateDataSet(dataSet, update.getUpdateSets());
                    dataSet.close();
                } catch (IOException e) {
                    LOGGER.error(ERROR_CLOSING_DATASET, e);
                }
            });
        }

    }

    /**
     * An adapter class for post-cleanup operations in SQL statements.
     */
    private class PostCleanUpStatementVisitorAdapter extends net.sf.jsqlparser.statement.StatementVisitorAdapter {

        private DataSet result;

        public Optional<DataSet> getResult() {
            return Optional.ofNullable(result);
        }

        @Override
        public void visit(net.sf.jsqlparser.statement.insert.Insert insert) {
            Database db = Database.from(connection());
            String table = db.table(insert.getTable().getName());
            insert.setTable(new Table(db.parser().format(table)));
            db.parser().formatColumns(insert.getColumns(),
                    column -> db.parser().format(db.column(table, column)));

            String[] columns;
            Object[][] values;

            if (insert.getSelect() instanceof PlainSelect) {
                try (Select<Object[]> select = db.select(insert.getSelect().toString()).get()) {
                    AtomicReference<String[]> cols = new AtomicReference<>();
                    values = select.stream().map(row -> db.parser().toValues(row)).flatMap(v -> {
                        insert.setSelect(v);
                        try (MapDataSet ds = db.parser()
                                .toSelect(insert)
                                .map(DatabaseSupport.this::doSelect)
                                .get()) {
                            cols.set(ds.columns());
                            return Stream.of(ds.allValues());
                        }
                    }).toArray(Object[][]::new);
                    columns = cols.get();
                } catch (NoSuchElementException e) {
                    LOGGER.warn("No results found in table " + table);
                    result = new EmptyDataSet(table);
                    return;
                }
            } else {
                try (MapDataSet ds = db.parser()
                        .toSelect(insert)
                        .map(DatabaseSupport.this::doSelect)
                        .get()) {
                    columns = ds.columns();
                    values = ds.allValues();
                } catch (NoSuchElementException e) {
                    LOGGER.warn("No results found in table " + table);
                    result = new EmptyDataSet(table);
                    return;
                }
            }
            result = new MapDataSet(table, columns, values, nullSymbol);
        }

        public void visit(net.sf.jsqlparser.statement.update.Update update) {
            Database db = Database.from(connection());
            String table = db.table(update.getTable().getName());
            result = db.parser()
                    .toSelect(update)
                    .map(DatabaseSupport.this::doSelect)
                    .map(ds -> (DataSet) ds)
                    .orElse(new EmptyDataSet(table));
        }
    }

}
