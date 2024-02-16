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
import es.iti.wakamiti.database.dataset.MapDataSet;
import es.iti.wakamiti.database.exception.PrimaryKeyNotFoundException;
import es.iti.wakamiti.database.jdbc.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.PlainSelect;
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



public class DatabaseSupport {

    public static final String DEFAULT = "default";
    protected static final String ERROR_ASSERT_NO_RECORD_EXPECTED = "It was expected no record satisfying {} exist in table {}, but {}";
    protected static final String ERROR_ASSERT_SOME_RECORD_EXPECTED = "It was expected some record satisfying {} exist in table {}, but {}";
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

    public void setXlsIgnoreSheetRegex(String ignoreSheetRegex) {
        this.xlsIgnoreSheetRegex = ignoreSheetRegex;
    }

    public void setNullSymbol(String nullSymbol) {
        this.nullSymbol = nullSymbol;
    }

    public void setCsvFormat(String csvFormat) {
        this.csvFormat = csvFormat;
    }

    public void setEnableCleanupUponCompletion(boolean enableCleanupUponCompletion) {
        this.enableCleanupUponCompletion = enableCleanupUponCompletion;
        LOGGER.trace("Cleanup " + (enableCleanupUponCompletion ? "enabled" : "disabled"));
    }

    public void setHealthcheck(boolean healthcheck) {
        this.healthcheck = healthcheck;
    }

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

    public void addConnection(ConnectionParameters parameters) {
        addConnection(DEFAULT, parameters);
    }

    protected Assertion<Long> matcherEmpty() {
        return new MatcherAssertion<>(Matchers.equalTo(0L));
    }

    protected Assertion<Long> matcherNonEmpty() {
        return new MatcherAssertion<>(Matchers.greaterThan(0L));
    }

    protected ConnectionProvider connection() {
        String alias = Optional.ofNullable(currentConnection.get()).orElse(
                connections.keySet().stream().findFirst()
                        .orElseThrow(() -> new WakamitiException("There is no default connection"))
        );
        LOGGER.trace("Using '{}' connection", alias);
        return connections.get(alias);
    }

    protected void assertFileExists(File file) {
        if (!file.exists()) {
            throw new WakamitiException("File '{}' not found", file.getAbsolutePath());
        }
    }


    protected void executeScript(String script, boolean cleanupUponCompletion) {
        Stream.of(script.split(unquotedRegex(";+")))  // split unquoted ';'
                .map(String::trim).filter(s -> !s.isEmpty())
                .forEach(sentence -> {
                    try {
                        if (cleanupUponCompletion) {
                            SQLParser.parseStatement(sentence).accept(new PreCleanUpStatementVisitorAdapter());
                        }
                        Database db = Database.from(connection());
                        try (Update update = db.update(sentence).execute()) {
                            if (cleanupUponCompletion && SQLParser.parseStatement(sentence) instanceof
                                    net.sf.jsqlparser.statement.insert.Insert) {
//                                List<Map<String, Object>> keys = update.generatedKeys()
//                                        .collect(Collectors.toList());
                                SQLParser.parseStatement(sentence).accept(new PostCleanUpStatementVisitorAdapter());
                            }
                        }
                    } catch (JSQLParserException e) {
                        throw new WakamitiException(
                                message("Cannot parse script. Please, disable the '{}' property",
                                        DatabaseConfigContributor.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION), e);
                    }
                });
    }

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

    protected String primaryKey(String table) {
        String[] keyColumn = primaryKeys(table);
        if (keyColumn.length > 1) {
            throw new WakamitiException(message("A single primary key in table {} is required", table));
        }
        return keyColumn[0];
    }

    protected String[] primaryKeys(String table) {
        Database db = Database.from(connection());
        String[] keyColumn = db.primaryKey(table).toArray(String[]::new);
        if (keyColumn.length < 1) {
            throw new WakamitiException(message("A primary key in table {} is required.", table));
        }
        return keyColumn;
    }

    protected long countBy(String table, String[] columns, Object[] values) {
        Database db = Database.from(connection());
        return countBy(db, db.parser().sqlSelectCountFrom(db.table(table),
                Stream.of(columns).map(c -> db.column(db.table(table), c)).toArray(String[]::new), values).toString());
    }

    protected long countBy(String table, String where) {
        Database db = Database.from(connection());
        return countBy(db, message("SELECT count(*) FROM {} WHERE {}",
                db.parser().format(db.table(table)), where));
    }

    private long countBy(Database db, String sql) {
        try (Select<String[]> select = db.select(sql).get(DatabaseHelper::format)) {
            return select.stream().findFirst().map(v -> v[0]).map(Long::parseLong).orElse(0L);
        }
    }

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

    protected Pair<String[], Object[]> processRow(String table, String[] columns, String[] values) {
        Database db = Database.from(connection());
        return toPair(db.processData(table, toMap(columns, values)))
                .map((k, v) -> new Pair<>(k.toArray(new String[0]), v.toArray()));
    }

    protected List<Pair<String[], Object[]>> processRows(DataSet dataSet) {
        List<Pair<String[], Object[]>> rows = new LinkedList<>();
        while (dataSet.nextRow()) {
            rows.add(processRow(dataSet.table(), dataSet.columns(),
                    Stream.of(dataSet.values()).map(DatabaseHelper::toString).toArray(String[]::new)));
        }
        return rows;
    }

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

    protected Runnable failSomeRecordExpected(String table, AtomicReference<Pair<String[], Object[]>> row) {
        return () -> fail(message(
                ERROR_ASSERT_SOME_RECORD_EXPECTED,
                toMap(row.get().key(), row.get().value()), Database.from(connection()).table(table), "it doesn't"
        ));
    }

    protected Runnable failNoRecordExpected(String table, AtomicReference<Pair<String[], Object[]>> row) {
        return () -> fail(message(
                ERROR_ASSERT_NO_RECORD_EXPECTED,
                toMap(row.get().key(), row.get().value()), Database.from(connection()).table(table), "it does"
        ));
    }

    protected void insertDataSet(DataSet dataSet, boolean addCleanUpOperation) {
        LOGGER.debug("Inserting rows in table {} from {}...", dataSet.table(), dataSet.origin());

        Database db = Database.from(connection());
        String table = db.table(dataSet.table());
        while (dataSet.nextRow()) {
            Map<String, Object> row = db.processData(dataSet.table(),
                    dataSet.rowAsMap().entrySet().stream().collect(collectToMap(
                            Map.Entry::getKey, e -> DatabaseHelper.toString(e.getValue()))));
            net.sf.jsqlparser.statement.insert.Insert insert = db.parser().toInsert(table, row);
            try (Update update = db.update(insert.toString()).execute()) {
                if (addCleanUpOperation) {
//                    List<Map<String, Object>> keys = update.generatedKeys().collect(Collectors.toList());
//                    LOGGER.trace("Retrieved primary key {}", keys);
                    insert.accept(new PostCleanUpStatementVisitorAdapter());
                }
            }
        }
    }

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

    protected void truncateTable(String table, boolean addCleanUpOperation) {
        LOGGER.debug("Deleting all rows in table {}...", table);

        Database db = Database.from(connection());
        table = db.table(table);
        if (addCleanUpOperation) {
            db.parser().toDelete(table).accept(new PreCleanUpStatementVisitorAdapter());
        }
        db.truncate(table);
    }

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

    protected void updateDataSet(DataSet dataSet) throws PrimaryKeyNotFoundException {
        LOGGER.debug("Updating rows in table {} from {}...", dataSet.table(), dataSet.origin());

        Database db = Database.from(connection());
        String table = db.table(dataSet.table());
        String[] primaryKey = db.primaryKey(table).toArray(String[]::new);
        if (primaryKey.length == 0) {
            throw new PrimaryKeyNotFoundException(table);
        }

        while (dataSet.nextRow()) {
            Map<String, Object> row = db.processData(dataSet.table(),
                    dataSet.rowAsMap().entrySet().stream().collect(collectToMap(
                            Map.Entry::getKey, e -> DatabaseHelper.toString(e.getValue()))));
            Map<String, Object> sets = row.entrySet().stream()
                    .filter(e -> !List.of(primaryKey).contains(e.getKey()))
                    .collect(collectToMap());
            Map<String, Object> where = row.entrySet().stream()
                    .filter(e -> List.of(primaryKey).contains(e.getKey()))
                    .collect(collectToMap());
            net.sf.jsqlparser.statement.update.Update update = db.parser().toUpdate(table, sets, where);

            db.update(update.toString()).execute().close();
        }
    }

    private MapDataSet doSelect(net.sf.jsqlparser.statement.select.Select select) {
        String table = ((net.sf.jsqlparser.statement.select.PlainSelect) select).getFromItem().toString();
        Database db = Database.from(connection());
        try (Select<Object[]> s = db.select(select.toString()).get()) {
            String[] columns = s.getColumnNames();
            Object[][] values = s.stream().toArray(Object[][]::new);
            return new MapDataSet(db.table(table), columns, values, nullSymbol);
        }
    }

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
                    .orElseThrow(() -> new PrimaryKeyNotFoundException(table));
            cleanUpOperations.addFirst(() -> {
                try {
                    insertDataSet(dataSet, false);
                    dataSet.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing dataset", e);
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
                    .orElseThrow(() -> new PrimaryKeyNotFoundException(table));
            cleanUpOperations.addFirst(() -> {
                try {
                    updateDataSet(dataSet);
                    dataSet.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing dataset", e);
                }
            });
        }

    }

    private class PostCleanUpStatementVisitorAdapter extends net.sf.jsqlparser.statement.StatementVisitorAdapter {

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
                        MapDataSet ds = db.parser()
                                .toSelect(insert)
                                .map(DatabaseSupport.this::doSelect)
                                .orElseThrow(() -> new PrimaryKeyNotFoundException(table));
                        cols.set(ds.columns());
                        return Stream.of(ds.allValues());
                    }).toArray(Object[][]::new);
                    columns = cols.get();
                }
            } else {
                try (MapDataSet ds = db.parser()
                        .toSelect(insert)
                        .map(DatabaseSupport.this::doSelect)
                        .orElseThrow(() -> new PrimaryKeyNotFoundException(table))) {
                    columns = ds.columns();
                    values = ds.allValues();
                }
            }

            cleanUpOperations.addFirst(() -> {
                try (DataSet dataSet = new MapDataSet(table, columns, values, nullSymbol)){
                    deleteDataSet(dataSet, false);
                } catch (IOException e) {
                    LOGGER.error("Error closing dataset", e);
                }
            });
        }
    }

}
