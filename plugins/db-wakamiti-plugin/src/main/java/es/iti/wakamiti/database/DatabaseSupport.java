/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.util.*;
import es.iti.wakamiti.database.dataset.DataSet;
import es.iti.wakamiti.database.dataset.EmptyDataSet;
import es.iti.wakamiti.database.dataset.MapDataSet;
import es.iti.wakamiti.database.exception.SQLRuntimeException;
import es.iti.wakamiti.database.jdbc.Record;
import es.iti.wakamiti.database.jdbc.*;
import es.iti.wakamiti.database.lucene.LuceneIndex;
import es.iti.wakamiti.database.lucene.LuceneIndexFactory;
import es.iti.wakamiti.database.lucene.LuceneIndexKey;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.cnfexpression.MultiAndExpression;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.awaitility.Durations;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.Matchers;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;
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
    protected static final String ERROR_ASSERT_NO_RECORD_EXPECTED =
            "It was expected no record satisfying {} exist in table {}, but {}";
    protected static final String ERROR_ASSERT_SOME_RECORD_EXPECTED =
            "It was expected some record satisfying {} exist in table {}, but {}";
    protected static final String GIVEN_WHERE_CLAUSE = "the given WHERE clause";
    protected static final String ERROR_CLOSING_DATASET = "Error closing dataset";
    protected static final double SIMILARITY_THRESHOLD = 0.7;
    protected static final LevenshteinDistance LEVENSHTEIN_DISTANCE = new LevenshteinDistance();
    protected static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.database");
    protected final Map<String, ConnectionProvider> connections = new HashMap<>();
    protected final Deque<Runnable> cleanUpOperations = new LinkedList<>();
    protected final AtomicReference<String> currentConnection = new AtomicReference<>();
    protected String xlsIgnoreSheetRegex;
    protected String nullSymbol;
    protected String csvFormat;
    protected boolean enableCleanupUponCompletion;
    protected boolean healthcheck;
    protected long similarSearchTimeoutMs = 10_000L;
    protected boolean luceneSimilarSearchEnabled;
    protected int luceneSimilarSearchTopK = 10;
    protected String luceneSimilarSearchIndexDir;
    protected final Map<LuceneIndexKey, LuceneIndex> luceneIndexes = new HashMap<>();
    protected UnaryOperator<Map<String, String>> nullSymbolMapper = map ->
            map.entrySet().stream().collect(MapUtils.toMap(v -> v.equals(nullSymbol) ? null : v));

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
        LOGGER.trace("Cleanup {}", (enableCleanupUponCompletion ? "enabled" : "disabled"));
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
     * Sets end-to-end timeout for "closest record" lookup.
     * <p>
     * When timeout is reached the feature fails safe (returns empty) to avoid
     * blocking scenario execution.
     *
     * @param timeout timeout in milliseconds; values {@code <= 0} disable timeout
     */
    public void setSimilarSearchTimeoutMs(long timeout) {
        this.similarSearchTimeoutMs = timeout;
    }

    /**
     * Enables or disables Lucene-based similar search.
     * <p>
     * Disabling Lucene also closes current Lucene resources immediately.
     *
     * @param enabled {@code true} to enable Lucene-based search.
     */
    public void setLuceneSimilarSearchEnabled(boolean enabled) {
        if (!enabled) {
            closeAllLuceneIndexes();
        }
        this.luceneSimilarSearchEnabled = enabled;
    }

    /**
     * Sets the number of Lucene candidates evaluated by final Levenshtein score.
     * <p>
     * Higher values can improve nearest-match quality but increase CPU time.
     *
     * @param topK requested candidate count; values lower than 1 are clamped to 1
     */
    public void setLuceneSimilarSearchTopK(int topK) {
        this.luceneSimilarSearchTopK = Math.max(1, topK);
    }

    /**
     * Sets the base directory for Lucene indexes.
     * <p>
     * When directory changes, previous Lucene instances are closed so no stale
     * resources remain attached to old paths.
     *
     * @param indexDir directory path to store indexes; {@code null} or blank for in-memory
     */
    public void setLuceneSimilarSearchIndexDir(String indexDir) {
        boolean hasChanged = !Objects.equals(this.luceneSimilarSearchIndexDir, indexDir);
        if (hasChanged) {
            closeAllLuceneIndexes();
        }
        this.luceneSimilarSearchIndexDir = indexDir;
    }

    /**
     * Adds a database connection with the specified alias and parameters.
     *
     * @param alias      The alias for the connection.
     * @param parameters The connection parameters.
     */
    public void addConnection(String alias, ConnectionParameters parameters) {
        LOGGER.debug("Setting '{}' connection parameters {}", alias, parameters);
        ConnectionProvider previous = connections.remove(alias);
        if (previous != null) {
            previous.close();
            closeLuceneIndexes(alias);
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
        try {
            SQLParser.parseStatements(script).forEach(statement -> {
                if (cleanupUponCompletion) {
                    statement.accept(new PreCleanUpStatementVisitorAdapter());
                }
                Database db = Database.from(connection());
                try (Update update = db.update(statement.toString()).execute()) {
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
                    result.map(DatabaseHelper::read).ifPresent(list ->
                            results.addAll(
                                    list.stream().map(m -> m.entrySet().stream().collect(
                                                    MapUtils.toMap(DatabaseHelper::toString)))
                                            .collect(Collectors.toList())
                            ));
                }
            });
        } catch (JSQLParserException e) {
            throw new WakamitiException("Cannot retrieve statement results", e);
        }
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
            return select.map(map -> map.entrySet().stream()
                            .collect(MapUtils.toMap(v -> Optional.ofNullable(v).orElse(nullSymbol))))
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
        try (Call<Map<String, String>> call = Database.from(connection()).call(sql).get(DatabaseHelper::formatToMap)) {
            return call.map(map -> map.entrySet().stream()
                            .collect(MapUtils.toMap(v -> Optional.ofNullable(v).orElse(nullSymbol))))
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
        String normalizedTable = db.table(table);
        String[] normalizedColumns = Stream.of(columns)
                .map(c -> db.column(normalizedTable, c))
                .toArray(String[]::new);
        return countBy(db, db.parser().sqlSelectCountFrom(normalizedTable, normalizedColumns, values).toString());
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
     * Finds the closest record for the given column/value pair set.
     * <p>
     * This method is used only to improve assertion error messages ("closest record"),
     * it is not part of exact matching logic.
     * <p>
     * Search flow:
     * <ol>
     *   <li>Normalize table/column names according to the current JDBC dialect.</li>
     *   <li>If Lucene is enabled, try a Lucene preselection first.</li>
     *   <li>Fallback to full table scan and compute Levenshtein-based score.</li>
     *   <li>Return the best candidate above {@link #SIMILARITY_THRESHOLD}.</li>
     * </ol>
     * Timeout and SQL timeout are honored in every stage. If timeout is reached,
     * this method returns {@link Optional#empty()} and the assertion continues
     * without "closest record" enrichment.
     *
     * @param table table name from the step or dataset
     * @param columns columns used to compare expected and actual values
     * @param values expected values aligned by index with {@code columns}
     * @return closest record as a map (column -&gt; value), or empty if no safe candidate was found
     */
    protected Optional<Map<String, String>> similarBy(String table, String[] columns, Object[] values) {
        Database db = Database.from(connection());
        String normalizedTable = db.table(table);
        String[] formattedColumns = Stream.of(columns)
                .map(c -> db.parser().format(db.column(normalizedTable, c)))
                .toArray(String[]::new);
        long deadlineNanos = similarSearchDeadlineNanos();

        try {
            if (luceneSimilarSearchEnabled) {
                Optional<Map<String, String>> luceneResult = similarByLucene(db, table, formattedColumns, values, deadlineNanos);
                if (luceneResult.isPresent()) {
                    return luceneResult;
                }
            }

            throwIfSimilarSearchTimedOut(deadlineNanos);
            String sql = db.parser().sqlSelectFrom(db.parser().format(normalizedTable), formattedColumns).toString();
            try (Select<String[]> select = selectForSimilarSearch(db, sql).get(DatabaseHelper::format)) {
                Optional<Record> result = bestSimilarRecord(select, values, deadlineNanos);
                result.ifPresent(rec -> LOGGER.trace("Found {}", rec));
                return result.map(rec -> toMap(formattedColumns, rec.data()));
            }
        } catch (SimilarSearchTimeoutException e) {
            logSimilarSearchTimeout(table);
            return Optional.empty();
        } catch (SQLRuntimeException e) {
            if (hasCause(e, SQLTimeoutException.class)) {
                logSimilarSearchTimeout(table);
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Executes the Lucene branch of similar search.
     * <p>
     * Index contents are refreshed before each search so external database
     * changes are reflected.
     * Lucene is used only for candidate retrieval; final acceptance still uses
     * the same Levenshtein score as SQL fallback.
     *
     * @param db active database wrapper
     * @param table table name (for logs only)
     * @param columns normalized/formatted columns to read from Lucene documents
     * @param values expected values to score against each candidate
     * @param deadlineNanos absolute timeout deadline in nanoseconds
     * @return closest candidate from Lucene branch, or empty when no valid candidate exists
     */
    private Optional<Map<String, String>> similarByLucene(
            Database db, String table, String[] columns, Object[] values, long deadlineNanos) {
        LuceneIndex index = luceneIndexFor(db, table, columns);
        if (index == null) {
            return Optional.empty();
        }
        try {
            throwIfSimilarSearchTimedOut(deadlineNanos);
            index.ensureUpToDate(
                    db,
                    table,
                    columns,
                    similarSearchTimeoutSeconds(),
                    () -> throwIfSimilarSearchTimedOut(deadlineNanos)
            );
            throwIfSimilarSearchTimedOut(deadlineNanos);
        } catch (IOException e) {
            LOGGER.warn("Unable to rebuild Lucene index for {}", table, e);
            return Optional.empty();
        }

        String queryText = Stream.of(values)
                .map(v -> Optional.ofNullable(v).map(DatabaseHelper::toString).orElse(""))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" "));

        try (DirectoryReader reader = DirectoryReader.open(index.directory())) {
            throwIfSimilarSearchTimedOut(deadlineNanos);
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = buildLuceneQuery(index.analyzer(), columns, queryText);
            TopDocs topDocs = searcher.search(query, luceneSimilarSearchTopK);
            if (topDocs.scoreDocs.length == 0) {
                return Optional.empty();
            }
            Optional<Record> result = bestSimilarRecord(Stream.of(topDocs.scoreDocs)
                    .map(scoreDoc -> toRow(searcher, scoreDoc, columns))
                    .filter(Objects::nonNull)
                    .map(row -> scoreRecord(row, values)), deadlineNanos);
            result.ifPresent(rec -> LOGGER.trace("Found {}", rec));
            return result.map(rec -> toMap(columns, rec.data()));
        } catch (IOException | ParseException e) {
            LOGGER.warn("Lucene similar search failed for {}", table, e);
            return Optional.empty();
        }
    }

    /**
     * Builds a Lucene query for multi-column candidate retrieval.
     * <p>
     * User values are escaped to avoid syntax issues with Lucene special
     * characters (for example: {@code (}, {@code :}, {@code +}).
     * If all values are blank, a {@link MatchAllDocsQuery} is used.
     *
     * @param analyzer analyzer used by Lucene parser
     * @param columns fields to query
     * @param queryText plain text created from expected values
     * @return Lucene query ready to run with {@link IndexSearcher}
     * @throws ParseException if Lucene parser cannot parse the escaped expression
     */
    private Query buildLuceneQuery(Analyzer analyzer, String[] columns, String queryText) throws ParseException {
        if (queryText.isBlank()) {
            return new MatchAllDocsQuery();
        }
        MultiFieldQueryParser parser = new MultiFieldQueryParser(columns, analyzer);
        parser.setDefaultOperator(QueryParser.Operator.OR);
        return parser.parse(QueryParserBase.escape(queryText));
    }

    /**
     * Loads one Lucene document and maps it to row-like array aligned with
     * provided columns.
     *
     * @param searcher Lucene searcher for current directory reader
     * @param scoreDoc Lucene hit descriptor
     * @param columns ordered field list
     * @return row values aligned with {@code columns}, or {@code null} if document cannot be read
     */
    private String[] toRow(IndexSearcher searcher, ScoreDoc scoreDoc, String[] columns) {
        try {
            Document document = searcher.doc(scoreDoc.doc);
            String[] rowValues = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                rowValues[i] = document.get(columns[i]);
            }
            return rowValues;
        } catch (IOException e) {
            LOGGER.warn("Unable to load Lucene document", e);
            return null;
        }
    }

    /**
     * Calculates average similarity score for one candidate row.
     *
     * @param rowValues actual values read from DB/Lucene candidate
     * @param values expected values from assertion context
     * @return {@link Record} carrying original data and computed score
     */
    private Record scoreRecord(String[] rowValues, Object[] values) {
        double score = IntStream.range(0, values.length)
                .mapToDouble(i -> similarityScore(values[i], rowValues[i]))
                .sum() / values.length;
        return new Record(rowValues, score);
    }

    /**
     * Selects best candidate above the configured similarity threshold.
     * <p>
     * Iteration is manual (instead of {@code stream().reduce(...)}) to check timeout
     * between elements and fail fast when deadline is exceeded.
     *
     * @param records candidate records to evaluate
     * @param deadlineNanos absolute timeout deadline in nanoseconds
     * @return best candidate above threshold, or empty if none qualify
     */
    private Optional<Record> bestSimilarRecord(Stream<Record> records, long deadlineNanos) {
        Record best = null;
        Iterator<Record> iterator = records.iterator();
        while (iterator.hasNext()) {
            throwIfSimilarSearchTimedOut(deadlineNanos);
            Record next = iterator.next();
            if (next.score() > SIMILARITY_THRESHOLD && (best == null || next.score() > best.score())) {
                best = next;
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * SQL-backed best-candidate selection that evaluates timeout between fetched
     * rows, avoiding full in-memory materialization before deadline checks.
     *
     * @param select open SELECT cursor with candidate rows
     * @param values expected values used for score calculation
     * @param deadlineNanos absolute timeout deadline in nanoseconds
     * @return best candidate above threshold, or empty if none qualify
     */
    private Optional<Record> bestSimilarRecord(Select<String[]> select, Object[] values, long deadlineNanos) {
        AtomicReference<Record> best = new AtomicReference<>();
        select.forEachRow(row -> {
            throwIfSimilarSearchTimedOut(deadlineNanos);
            Record candidate = scoreRecord(row, values);
            Record current = best.get();
            if (candidate.score() > SIMILARITY_THRESHOLD
                    && (current == null || candidate.score() > current.score())) {
                best.set(candidate);
            }
        });
        return Optional.ofNullable(best.get());
    }

    /**
     * Computes normalized Levenshtein similarity in range {@code [0.0, 1.0]}.
     * <p>
     * Both values are normalized to uppercase and trimmed to minimize noise
     * from case and edge spaces.
     *
     * @param expectedValue expected value from step/dataset
     * @param rowValue actual value from DB candidate
     * @return similarity where {@code 1.0} means exact match after normalization
     */
    private double similarityScore(Object expectedValue, String rowValue) {
        String expected = Optional.ofNullable(expectedValue)
                .map(DatabaseHelper::toString).orElse("").trim().toUpperCase();
        String actual = Optional.ofNullable(rowValue).orElse("").trim().toUpperCase();
        int maxLength = Math.max(expected.length(), actual.length());
        double distance = LEVENSHTEIN_DISTANCE.apply(expected, actual);
        if (maxLength == 0) return 1.0;
        return (maxLength - distance) / maxLength;
    }

    /**
     * Returns cached Lucene index instance for the current
     * connection/table/column-set tuple.
     *
     * @param db active database wrapper
     * @param table table name
     * @param columns normalized/formatted column names
     * @return cached or newly created index, or {@code null} when index cannot be created
     */
    private LuceneIndex luceneIndexFor(Database db, String table, String[] columns) {
        String alias = currentConnectionAlias();
        String normalizedTable = db.table(table);
        LuceneIndexKey key = new LuceneIndexKey(alias, normalizedTable, columns);
        return luceneIndexes.computeIfAbsent(key, this::createLuceneIndex);
    }

    /**
     * Creates a Lucene index from key and current index directory configuration.
     * Any I/O error is converted to warning and caller receives {@code null}
     * so similar search can gracefully fallback.
     *
     * @param key index identity key
     * @return created index, or {@code null} on failure
     */
    private LuceneIndex createLuceneIndex(LuceneIndexKey key) {
        try {
            return LuceneIndexFactory.createIndex(key, luceneSimilarSearchIndexDir);
        } catch (IOException e) {
            LOGGER.warn("Unable to initialize Lucene directory for {}", key.table(), e);
            return null;
        }
    }

    /**
     * Resolves currently active connection alias.
     * <p>
     * If no explicit alias is selected, it falls back to first available one.
     *
     * @return connection alias used for cache keying
     */
    private String currentConnectionAlias() {
        return Optional.ofNullable(currentConnection.get()).orElse(
                connections.keySet().stream().findFirst()
                        .orElseThrow(() -> new WakamitiException("There is no default connection"))
        );
    }

    /**
     * Builds a SELECT builder configured for similar-search timeout policy.
     *
     * @param db active database wrapper
     * @param sql SQL select statement
     * @return builder with query timeout when timeout is enabled
     */
    private Select.Builder selectForSimilarSearch(Database db, String sql) {
        Select.Builder builder = db.select(sql);
        int timeoutSeconds = similarSearchTimeoutSeconds();
        if (timeoutSeconds > 0) {
            builder.queryTimeoutSeconds(timeoutSeconds);
        }
        return builder;
    }

    /**
     * Converts {@code similarSearchTimeoutMs} into absolute nano-time deadline.
     * <p>
     * Using absolute deadline makes checks cheap and consistent across the whole
     * method chain.
     *
     * @return deadline in {@code System.nanoTime()} domain, or {@link Long#MAX_VALUE} when disabled
     */
    private long similarSearchDeadlineNanos() {
        if (similarSearchTimeoutMs <= 0) {
            return Long.MAX_VALUE;
        }
        long timeoutNanos;
        try {
            timeoutNanos = Math.multiplyExact(similarSearchTimeoutMs, 1_000_000L);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
        long now = System.nanoTime();
        if (Long.MAX_VALUE - now < timeoutNanos) {
            return Long.MAX_VALUE;
        }
        return now + timeoutNanos;
    }

    /**
     * Converts timeout in milliseconds to JDBC query-timeout seconds.
     * JDBC timeout is second-based, so values are rounded up.
     *
     * @return timeout seconds for JDBC statements, or {@code 0} when disabled
     */
    private int similarSearchTimeoutSeconds() {
        if (similarSearchTimeoutMs <= 0) {
            return 0;
        }
        long seconds = (similarSearchTimeoutMs + 999L) / 1000L;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, seconds));
    }

    /**
     * Fast timeout guard used inside loops and between expensive steps.
     *
     * @param deadlineNanos absolute timeout deadline in nanoseconds
     */
    private void throwIfSimilarSearchTimedOut(long deadlineNanos) {
        if (deadlineNanos != Long.MAX_VALUE && System.nanoTime() > deadlineNanos) {
            throw new SimilarSearchTimeoutException();
        }
    }

    /**
     * Centralized timeout warning log to keep timeout observability consistent.
     *
     * @param table table where timeout occurred
     */
    private void logSimilarSearchTimeout(String table) {
        LOGGER.warn("Similar search timed out for table {} after {} ms", table, similarSearchTimeoutMs);
    }

    /**
     * Utility method to detect a specific cause type in exception chain.
     *
     * @param throwable root throwable
     * @param type target cause type
     * @return {@code true} if any cause matches target type
     */
    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Internal control-flow exception used to abort similar-search pipeline when
     * in-memory deadline is exceeded.
     */
    private static final class SimilarSearchTimeoutException extends RuntimeException {
    }


    /**
     * Closes all Lucene indexes associated to given connection alias.
     * This is used when a connection is replaced.
     *
     * @param alias connection alias
     */
    protected void closeLuceneIndexes(String alias) {
        Iterator<Map.Entry<LuceneIndexKey, LuceneIndex>> iterator = luceneIndexes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LuceneIndexKey, LuceneIndex> entry = iterator.next();
            if (entry.getKey().alias().equals(alias)) {
                entry.getValue().close();
                iterator.remove();
            }
        }
    }

    /**
     * Closes and removes all cached Lucene indexes.
     * Safe to call multiple times.
     */
    protected void closeAllLuceneIndexes() {
        luceneIndexes.values().forEach(LuceneIndex::close);
        luceneIndexes.clear();
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
                                assertThat(result)
                                        .as("The closest record")
                                        .containsExactlyEntriesOf(toMap(row.key(),
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
     * @param dataSet  The DataSet to be checked.
     * @param matcher  The assertion to be applied to the count of records.
     * @param duration The maximum time to wait for the assertion to succeed.
     */
    protected void assertCountAsync(DataSet dataSet, Assertion<Long> matcher, Duration duration) {
        List<Pair<String[], Object[]>> rows = processRows(dataSet);
        AtomicLong count = new AtomicLong(0);
        assertAsync(() -> {
            count.set(rows.stream()
                    .mapToLong(row -> countBy(dataSet.table(), row.key(), row.value()))
                    .sum());
            return matcher.test(count.get());
        }, duration, () -> fail(message(
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
     * @param duration    The maximum time to wait for the condition to be satisfied.
     * @param catchAction The action to be executed if the condition is not satisfied within the specified time.
     */
    protected void assertAsync(BooleanSupplier action, Duration duration, Runnable catchAction) {
        try {
            await()
                    .atMost(duration)
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
     * @param dataSet  The data set to be checked asynchronously.
     * @param duration The maximum time to wait for each row to be non-empty.
     * @return The duration taken to perform the assertion asynchronously.
     */
    protected Duration assertNonEmptyAsync(DataSet dataSet, Duration duration) {
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
        }, duration, () -> {
            Pair<String[], Object[]> processed = currentRow.get();
            similarBy(dataSet.table(), processed.key(), processed.value()).ifPresentOrElse(row ->
                            assertThat(row)
                                    .as("The closest record")
                                    .containsExactlyEntriesOf(
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
     * @param dataSet  The data set to be checked asynchronously.
     * @param duration The maximum time to wait for each row to be empty, in seconds.
     * @return The duration taken to perform the assertion asynchronously.
     */
    protected Duration assertEmptyAsync(DataSet dataSet, Duration duration) {
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
        }, duration, failNoRecordExpected(dataSet.table(), currentRow));
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
                    dataSet.rowAsMap().entrySet().stream().collect(MapUtils.toMap(DatabaseHelper::toString)));
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
                result.map(DatabaseHelper::read).ifPresent(list ->
                        results.addAll(
                                list.stream().map(m -> m.entrySet().stream().collect(MapUtils.toMap(DatabaseHelper::toString)))
                                        .collect(Collectors.toList())
                        ));
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
                    dataSet.rowAsMap().entrySet().stream().collect(MapUtils.toMap(DatabaseHelper::toString)));
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
    protected void updateDataSet(DataSet dataSet, UpdateSet updateSet) {
        LOGGER.debug("Updating rows in table {} from {}...", dataSet.table(), dataSet.origin());

        Database db = Database.from(connection());
        String table = db.table(dataSet.table());

        List<String> setColumns = updateSet.getColumns().stream()
                .map(Column::getColumnName)
                .collect(Collectors.toList());

        while (dataSet.nextRow()) {
            Map<String, Object> row = db.processData(dataSet.table(),
                    dataSet.rowAsMap().entrySet().stream().collect(MapUtils.toMap(DatabaseHelper::toString)));
            Map<String, Object> sets = row.entrySet().stream()
                    .filter(e -> setColumns.contains(db.parser().format(e.getKey())))
                    .collect(collectToMap());
            Map<String, Object> where = row.entrySet().stream()
                    .filter(e -> !setColumns.contains(db.parser().format(e.getKey())))
                    .collect(collectToMap());
            List<Expression> whereList = new LinkedList<>();
            whereList.add(db.parser().toWhere(updateSet));
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

    private String format(String table, String column) {
        Database db = Database.from(connection());
        return db.parser().format(db.column(table, column));
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
            db.parser().formatColumns(delete.getWhere(), column -> format(table, column));

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
            db.parser().formatColumns(update.getWhere(), column -> format(table, column));

            update.getUpdateSets().stream().flatMap(set -> set.getColumns().stream()).forEach(c ->
                    db.parser().formatColumns(c, column -> format(table, column))
            );
            update.getUpdateSets().stream().flatMap(set -> set.getValues().stream()).forEach(v ->
                    db.parser().formatColumns(v, column -> format(table, column))
            );

            DataSet dataSet = db.parser()
                    .toSelect(update)
                    .map(DatabaseSupport.this::doSelect)
                    .map(ds -> (DataSet) ds)
                    .orElse(new EmptyDataSet(table));
            cleanUpOperations.addFirst(() -> {
                try {
                    if (update.getUpdateSets() == null || update.getUpdateSets().size() != 1) {
                        throw new WakamitiException("Update must have one update set. {}", update);
                    }
                    updateDataSet(dataSet, update.getUpdateSets().get(0));
                } finally {
                    try {
                        dataSet.close();
                    } catch (IOException e) {
                        LOGGER.error(ERROR_CLOSING_DATASET, e);
                    }
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
            db.parser().formatColumns(insert.getColumns(), column -> format(table, column));

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
                                .orElseThrow()) {
                            cols.set(ds.columns());
                            return Stream.of(ds.allValues());
                        }
                    }).toArray(Object[][]::new);
                    columns = cols.get();
                } catch (NoSuchElementException e) {
                    LOGGER.warn("No results found in table {}", table);
                    result = new EmptyDataSet(table);
                    return;
                }
            } else {
                try (MapDataSet ds = db.parser()
                        .toSelect(insert)
                        .map(DatabaseSupport.this::doSelect)
                        .orElseThrow()) {
                    columns = ds.columns();
                    values = ds.allValues();
                } catch (NoSuchElementException e) {
                    LOGGER.warn("No results found in table {}", table);
                    result = new EmptyDataSet(table);
                    return;
                }
            }
            result = new MapDataSet(table, columns, values, nullSymbol);
        }

        @Override
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
