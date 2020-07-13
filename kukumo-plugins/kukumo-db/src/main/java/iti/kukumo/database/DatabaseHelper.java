/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;


import iti.commons.slf4jansi.AnsiLogger;
import iti.kukumo.api.KukumoException;
import iti.kukumo.database.dataset.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class DatabaseHelper {

    public interface ConnectionProvider {

        Connection obtainConnection() throws SQLException;
    }


    private static final Logger LOGGER = AnsiLogger.of(LoggerFactory.getLogger("iti.kukumo.database"));

    private final Map<String, List<String>> primaryKeyCache = new HashMap<>();
    private final Map<String, Map<String, Integer>> nonNullabeColumnCache = new HashMap<>();
    private final ConnectionProvider connectionProvider;
    private final ConnectionParameters connectionParameters;
    private final Deque<Runnable> cleanUpOperations = new LinkedList<>();
    private final Supplier<String> nullSymbol;
    private SQLParser parser;
    //private boolean manualCleanup = false;

    private CaseSensitivity caseSensitivity = CaseSensitivity.INSENSITIVE;

    public DatabaseHelper(
        ConnectionParameters connectionParameters,
        ConnectionProvider connectionProvider,
        Supplier<String> nullSymbol
    ) {
        this.connectionProvider = connectionProvider;
        this.connectionParameters = connectionParameters;
        this.nullSymbol = nullSymbol;
        this.parser = new SQLParser(caseSensitivity);
        AnsiLogger.addStyle("sql","yellow,bold");
    }


    protected Connection connection() throws SQLException {
        return connectionProvider.obtainConnection();
    }

    public DatabaseHelper setCaseSensitivity(CaseSensitivity caseSensitivity) {
        this.caseSensitivity = caseSensitivity;
        this.parser = new SQLParser(caseSensitivity);
        return this;
    }

    public DatabaseHelper setCleanUpOperations(String sql) {
       this.cleanUpOperations.addLast(() -> {
           try {
               executeSQLStatements(sql, false);
           } catch (SQLException | JSQLParserException e) {
               LOGGER.debug("Error on clean-up operation: {}", e.getMessage(), e);
           }
       });
       return this;
    }

    public DatabaseHelper setCleanUpOperations(String sql, String fileName) {
        this.cleanUpOperations.addLast(() -> {
            try {
                executeSQLStatements(sql, fileName, false);
            } catch (SQLException | JSQLParserException e) {
                LOGGER.debug("Error on clean-up operation: {}", e.getMessage(), e);
            }
        });
        return this;
    }

    public void bindRowValues(
        PreparedStatement statement,
        DataSet dataSet,
        boolean nullControl
    ) throws SQLException {
        bindRowValues(statement, dataSet, dataSet.columns(), nullControl);
    }


    public void bindRowValues(
        PreparedStatement statement,
        DataSet dataSet,
        String[] columns,
        boolean nullControl
    ) throws SQLException {
        bindRowValues(statement, dataSet, columns, 0, nullControl);
    }

    public void bindRowValues(
            PreparedStatement statement,
            DataSet dataSet,
            String[] columns,
            int startIndex,
            boolean nullControl
    ) throws SQLException {
        // if nullControl = true, the statement will contain 2 entries of the same
        // parameters
        int factor = nullControl ? 2 : 1;
        for (int i = 0; i < columns.length; i++) {
            statement.setObject((startIndex + i) * factor + 1, dataSet.rowValue(columns[i]));
            if (nullControl) {
                statement.setObject((startIndex + i) * factor + 2, dataSet.rowValue(columns[i]));
            }
        }
    }


    private PreparedStatement createRowStatement(
        CharSequence sql,
        DataSet dataSet,
        boolean nullControl
    ) throws SQLException {
        PreparedStatement statement = connection().prepareStatement(sql.toString());
        bindRowValues(statement, dataSet, nullControl);
        return statement;
    }


    private <T> T extractSingleResult(PreparedStatement statement, Class<T> type)
    throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return result.getObject(1, type);
        }
    }


    private List<String> detectPrimaryKey(String table, boolean throwIfAbsent) {
        try {
            DatabaseMetaData metadata = connection().getMetaData();
            ArrayList<String> primaryKeys = new ArrayList<>();
            ResultSet resultSet = metadata.getPrimaryKeys(catalog(), schema(), caseSensitivity.format(table));
            while (resultSet != null && resultSet.next()) {
                primaryKeys.add(resultSet.getString("COLUMN_NAME"));
            }
            if (primaryKeys.isEmpty() && throwIfAbsent) {
                throw new KukumoException("Cannot determine primary key for table " + caseSensitivity.format(table));
            }
            LOGGER.trace("Getting {} primary keys: {}", table, primaryKeys);
            return primaryKeys;
        } catch (Exception e) {
            throw new KukumoException(e);
        }
    }


    private Map<String, Integer> collectNonNullableColumns(String table) {
        try {
            DatabaseMetaData metadata = connection().getMetaData();
            Map<String, Integer> nonNullableColumns = new LinkedHashMap<>();
            ResultSet resultSet = metadata.getColumns(catalog(), schema(), caseSensitivity.format(table), null);
            while (resultSet != null && resultSet.next()) {
                if (resultSet.getInt("NULLABLE") == DatabaseMetaData.attributeNoNulls) {
                    nonNullableColumns
                        .put(resultSet.getString("COLUMN_NAME"), resultSet.getInt("DATA_TYPE"));
                }
            }
            return nonNullableColumns;
        } catch (Exception e) {
            throw new KukumoException(e);
        }
    }


    private void assertCount(
        PreparedStatement statement,
        Matcher<Long> matcher
    ) throws SQLException {
        MatcherAssert.assertThat(extractSingleResult(statement, Long.class), matcher);
    }


    public void assertCountRowsInTableByColumns(
        Matcher<Long> matcher,
        String table,
        String[] columns,
        Object[] values
    ) throws SQLException {
        String sql = parser.sqlSelectCountFrom(table, columns).toString();
        try (PreparedStatement statement =  createRowStatement(
            sql,
            new InlineDataSet(table, columns, values, nullSymbol.get()),
            true
        )) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("[SQL] {sql}  | {}", sql, mapValues(columns, values));
            }
            assertCount(statement, matcher);
        } catch (SQLException e) {
            LOGGER.error("[SQL] {sql} | {}", sql, mapValues(columns, values));
            throw e;
        }
    }


    public void assertCountRowsInTableByClause(
        Matcher<Long> matcher,
        String table,
        String clause
    ) throws SQLException {
        StringBuilder sql = new StringBuilder(parser.sqlSelectCountFrom(table).toString()).append(" where ").append(clause);
        try (PreparedStatement statement = createRowStatement(
            sql,
            new EmptyDataSet(table),
            false
        )) {
            LOGGER.trace("[SQL] {sql}", sql);
            assertCount(statement, matcher);
        }  catch (SQLException e) {
            LOGGER.error("[SQL] {sql}", sql);
            throw e;
        }
    }


    public long executeSQLStatements(String sql, boolean addCleanUpOperation) throws SQLException, JSQLParserException {
        return executeSQLStatements(sql, null, addCleanUpOperation);
    }

    public long executeSQLStatements(
            String sql,
            String scriptFileName,
            boolean addCleanUpOperation
    ) throws SQLException, JSQLParserException {
        if (scriptFileName != null) {
            LOGGER.debug("Executing SQL script from '{}'...", scriptFileName);
        } else {
            LOGGER.debug("Executing SQL script...");
        }

        List<net.sf.jsqlparser.statement.Statement> statements = parser.parseStatements(sql);
        if (addCleanUpOperation) {
            statements.forEach(this::sqlCleanUpOperations);
        }

        long count = 0;
        for (net.sf.jsqlparser.statement.Statement statementLine : statements) {
            try (PreparedStatement statement = connection().prepareStatement(statementLine.toString(), Statement.RETURN_GENERATED_KEYS)) {
                statement.addBatch();
                LOGGER.trace("[SQL] {sql}", statementLine);
                count = count + countResults(statement.executeBatch());
                if (statementLine instanceof Insert && addCleanUpOperation) {
                    DataSet pks = getGeneratedKeys(statement, (Insert) statementLine);
                    cleanUpOperations.addFirst(deleteDataSetRunner(pks));
                }
            } catch (SQLException e) {
                statements.forEach(statement -> LOGGER.error("[SQL] {sql}", statement));
                throw e;
            }
        }
        if (scriptFileName != null) {
            LOGGER.debug("Executed SQL script '{}'; {} rows affected", scriptFileName, count);
        } else {
            LOGGER.debug("Executed SQL script; {} rows affected", count);
        }
        return count;
    }

    private DataSet getGeneratedKeys(Statement statement, Insert insert) throws SQLException {
        String[] columns = insert.getColumns().stream().map(Objects::toString).toArray(String[]::new);
        String[] pkColumns = primaryKey(insert.getTable().getName(), false);
        String[] pkValues = new String[pkColumns.length];
        if (Arrays.asList(columns).containsAll(Arrays.asList(pkColumns))) {
            String[] values = ((ExpressionList) insert.getItemsList()).getExpressions().stream()
                    .map(parser::extractValue).toArray(String[]::new);
            int n = 0;
            for (int i = 0; i < columns.length; i++) {
                if (Arrays.asList(pkColumns).contains(columns[i])) {
                    pkValues[n] = values[i];
                    n++;
                }
            }
        } else {
            ResultSet rs = statement.getGeneratedKeys();
            while (rs.next()) {
                for (int i = 0; i < pkColumns.length; i++) {
                    pkValues[i] = rs.getString(i+1);
                }
            }
        }
        return new InlineDataSet(insert.getTable().getName(), pkColumns, pkValues, nullSymbol.get());
    }

    private void sqlCleanUpOperations(net.sf.jsqlparser.statement.Statement statement) {
        statement.accept(new StatementVisitorAdapter() {
            @Override
            public void visit(Delete delete) {
                cleanUpOperations.addFirst(parser.toSelect(delete)
                        .map(select -> executeSelect(select))
                        .map(dataSet -> insertDataSetRunner(dataSet))
                        .get());
            }

            @Override
            public void visit(Update update) {
                cleanUpOperations.addFirst(parser.toSelect(update)
                        .map(select -> executeSelect(select))
                        .map(dataSet -> updateDataSetRunner(dataSet, update))
                        .get());
            }
        });
    }

    public DataSet executeSelect(Select select) {
        try (Statement statement = connection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            LOGGER.trace("[SQL] {sql}", select);
            ResultSet resultSet = statement.executeQuery(select.toString());
            String table = ((PlainSelect) select.getSelectBody()).getFromItem().toString();
            return read(table, resultSet);
        } catch (SQLException e) {
            LOGGER.error("[SQL] {sql}", select);
            throw new RuntimeException(e);
        }
    }

    public DataSet executeSelect(Select select, DataSet dataSet, String[] columns) throws SQLException {
        try (PreparedStatement statement = connection()
                .prepareStatement(select.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            traceSQL(select.toString(), dataSet);
            bindRowValues(statement, dataSet, columns, true);
            ResultSet resultSet = statement.executeQuery();
            return read(dataSet.table(), resultSet);
        } catch (SQLException e) {
            LOGGER.error("[SQL] {sql} | {}", select, dataSet.rowAsMap());
            throw e;
        }
    }

    private DataSet read(String table, ResultSet resultSet) throws SQLException {
        ResultSetMetaData md = resultSet.getMetaData();
        String[] columns = new String[md.getColumnCount()];
        for (int i = 0; i < columns.length; ++i) {
            columns[i] = md.getColumnName(i + 1);
        }
        List<String[]> values = new LinkedList<>();
        while (resultSet.next()) {
            String[] row = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                row[i] = Optional.ofNullable(resultSet.getString(i + 1)).map(String::trim).orElse(null);
            }
            values.add(row);
        }
        return new MapDataSet(table, columns, values.toArray(new String[values.size()][]), nullSymbol.get());
    }

    public long insertDataSet(
        DataSet dataSet,
        boolean addCleanUpOperation
    ) throws SQLException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "Inserting rows in table {} from {}...",
                caseSensitivity.format(dataSet.table()),
                dataSet.origin()
            );
        }
        Insert insert = parser.sqlInsertIntoValues(dataSet.copy());
        try (PreparedStatement statement = connection().prepareStatement(insert.toString(), Statement.RETURN_GENERATED_KEYS)) {
            while (dataSet.nextRow()) {
                bindRowValues(statement, dataSet, false);
                statement.addBatch();
                traceSQL(insert.toString(), dataSet);
            }
            long count = countResults(statement.executeBatch());
            if (addCleanUpOperation) {
                DataSet pks = getGeneratedKeys(statement, insert);
                cleanUpOperations.addFirst(deleteDataSetRunner(pks));
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                    "Inserted {} rows in table {} from {}",
                    count,
                    caseSensitivity.format(dataSet.table()),
                    dataSet.origin()
                );
            }
            dataSet.close();
            return count;
        }
    }

    public long updateDataSet(
            DataSet beforeDataSet,
            DataSet afterDataSet
    ) throws SQLException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Updating rows in table {} from {}...",
                    caseSensitivity.format(beforeDataSet.table()),
                    beforeDataSet.origin()
            );
        }
        String[] primaryKey = primaryKey(beforeDataSet.table(), false);
        if (primaryKey.length == 0 || !beforeDataSet.containColumns(primaryKey)) {
            throw new IOException("Primary key " + Arrays.asList(primaryKey) + " is needed");
        }

        Update sql = parser.sqlUpdateSet(beforeDataSet, primaryKey);

        //list to get ordered column replacements in prepared statement
        List<String> setColumns = sql.getColumns().stream().map(Object::toString).collect(Collectors.toCollection(LinkedList::new));
        DataSet whereDataSet = merge(beforeDataSet.copy(), afterDataSet.copy());

        try (PreparedStatement statement = connection().prepareStatement(sql.toString())) {
            while (beforeDataSet.nextRow() && whereDataSet.nextRow()) {
                bindRowValues(statement, beforeDataSet, setColumns.toArray(String[]::new), false);
                bindRowValues(statement, whereDataSet, primaryKey, setColumns.size(), false);
                statement.addBatch();
                traceSQL(sql.toString(), beforeDataSet, whereDataSet);
            }
            long count = countResults(statement.executeBatch());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Updated {} rows in table {} from {}",
                        count,
                        caseSensitivity.format(beforeDataSet.table()),
                        beforeDataSet.origin()
                );
            }
            return count;
        }
    }

    private DataSet merge(DataSet dataSet1, DataSet dataSet2) {
        Set<String> aux = new HashSet<>(Set.of(dataSet1.columns()));
        aux.addAll(Set.of(dataSet1.columns()));
        String[] columns = aux.toArray(String[]::new);
        List<String[]> values = new LinkedList<>();
        dataSet2.nextRow();
        while (dataSet1.nextRow()) {
            String[] row = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                row[i] = Optional.ofNullable((dataSet2.containColumns(columns[i]) ? dataSet2 : dataSet1).rowValue(columns[i]))
                        .map(Object::toString).orElse(null);
            }
            values.add(row);
        }
        return new MapDataSet(dataSet1.table(), columns, values.toArray(String[][]::new), nullSymbol.get());
    }

    private Runnable deleteDataSetRunner(final DataSet dataSet) {
        return () -> {
            try {
                deleteDataSet(dataSet, false);
            } catch (SQLException | IOException e) {
                LOGGER.debug("Error on clean-up operation: {}", e.getMessage(), e);
            }
        };
    }

    private Runnable insertDataSetRunner(final DataSet dataSet) {
        return () -> {
            try {
                insertDataSet(dataSet, false);
            } catch (SQLException | IOException e) {
                LOGGER.debug("Error on clean-up operation: {}", e.getMessage(), e);
            }
        };
    }

    private Runnable updateDataSetRunner(final DataSet dataSet, Update update) {
        return () -> {
            try {
                List<String> columns = update.getColumns().stream()
                        .map(Column::getColumnName)
                        .collect(Collectors.toCollection(LinkedList::new));
                Object[] values = update.getExpressions().stream().map(parser::extractValue).toArray();
                DataSet newDataSet = new InlineDataSet(update.getTable().getName(),
                        columns.toArray(String[]::new), values, nullSymbol.get());

                updateDataSet(dataSet, newDataSet);
            } catch (SQLException | IOException e) {
                LOGGER.debug("Error on clean-up operation: {}", e.getMessage(), e);
            }
        };
    }

    public long deleteDataSet(DataSet dataSet, boolean addCleanUpOperation) throws SQLException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Deleting rows in table {} from {}...", caseSensitivity.format(dataSet.table()), dataSet.origin());
        }

        String[] primaryKey = primaryKey(dataSet.table(), false);
        boolean deleteByPrimaryKey = primaryKey.length > 0 && dataSet.containColumns(primaryKey);
        Delete sql = parser.sqlDeleteFrom(dataSet.table(), deleteByPrimaryKey ? primaryKey : dataSet.columns());
        if (addCleanUpOperation) {
            Optional<Select> select = parser.toSelect(sql);
            if (select.isPresent()) {
                DataSet data = executeSelect(select.get(), dataSet, deleteByPrimaryKey ? primaryKey : dataSet.columns());
                cleanUpOperations.addFirst(insertDataSetRunner(data));
            }
        }
        try (PreparedStatement statement = connection().prepareStatement(sql.toString())) {
            while (dataSet.nextRow()) {
                if (deleteByPrimaryKey) {
                    bindRowValues(statement, dataSet, primaryKey, true);
                } else {
                    bindRowValues(statement, dataSet, true);
                }
                statement.addBatch();
                traceSQL(sql.toString(), dataSet);
            }
            long count = countResults(statement.executeBatch());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                    "Deleted {} rows in table {} from {}",
                    count,
                    caseSensitivity.format(dataSet.table()),
                    dataSet.origin()
                );
            }
            dataSet.close();
            return count;

        }
    }


    public void truncateTable(String table) throws SQLException {
        LOGGER.debug("Deleting all rows in table {}...", table);
        try (Statement truncate = connection().createStatement()) {
            truncate.execute("truncate table " + table);
            LOGGER.debug("Deleted all rows in table {}", table);
        } catch (SQLException e) {
            // truncate may not work in every situation, try to do a delete instead
            try (Statement delete = connection().createStatement()) {
                delete.execute("delete from " + table);
                LOGGER.debug("Deleted all rows in table {}", table);
            }
        }
    }


    public long insertMultiDataSet(
        MultiDataSet multiDataSet,
        boolean addCleanUpOperation
    ) throws SQLException, IOException {
        long count = 0;
        for (DataSet dataSet : multiDataSet) {
            count += insertDataSet(dataSet, false); // adding the complete multidataset as cleanup
                                                    // operation
        }
        if (addCleanUpOperation) {
            multiDataSet.copy().forEach(ds -> cleanUpOperations.addFirst(deleteDataSetRunner(ds)));
        }
        return count;
    }


    public long deleteMultiDataSet(MultiDataSet multiDataSet, boolean addCleanUpOperation) throws SQLException, IOException {
        long count = 0;
        for (DataSet dataSet : multiDataSet) {
            count += deleteDataSet(dataSet, addCleanUpOperation);
        }
        return count;
    }


    public void assertDataSetExists(DataSet dataSet) throws SQLException {
        String sql = parser.sqlSelectFrom(dataSet.table(), dataSet.columns()).toString();
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            while (dataSet.nextRow()) {
                bindRowValues(statement, dataSet, true);
                assertRowExists(dataSet, statement);
            }
        }
    }


    public void assertDataSetNotExists(DataSet dataSet) throws SQLException {
        String sql = parser.sqlSelectFrom(dataSet.table(), dataSet.columns()).toString();
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            while (dataSet.nextRow()) {
                bindRowValues(statement, dataSet, true);
                assertRowNotExists(dataSet, statement);
            }
        }
    }


    private void assertRowExists(DataSet dataSet, PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                Assertions.fail(logRowNotFound(dataSet));
            }
        }
    }


    private void assertRowNotExists(
        DataSet dataSet,
        PreparedStatement statement
    ) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                Assertions.fail(
                    String.format(
                        "Expected row %s not to exist in table %s but it does",
                        dataSet.rowAsMap(),
                        caseSensitivity.format(dataSet.table())
                    )
                );
            }
        }
    }


    public void assertCountRowsInTableByDataSet(
        DataSet dataSet,
        Matcher<Long> matcher
    ) throws SQLException {
        String sql = parser.sqlSelectCountFrom(dataSet.table(), dataSet.columns()).toString();
        long count = 0;
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            while (dataSet.nextRow()) {
                bindRowValues(statement, dataSet, true);
                count += extractSingleResult(statement, Long.class);
            }
        } catch (SQLException e) {
            LOGGER.error("[SQL] {sql}",sql);
        }
        MatcherAssert.assertThat(count, matcher);
    }


    public void assertMultiDataSetExists(MultiDataSet multiDataSet) throws SQLException {
        for (DataSet dataSet : multiDataSet) {
            assertDataSetExists(dataSet);
        }
    }


    public void assertMultiDataSetNotExists(MultiDataSet multiDataSet) throws SQLException {
        for (DataSet dataSet : multiDataSet) {
            assertDataSetNotExists(dataSet);
        }
    }


    private String logRowNotFound(DataSet dataSet) throws SQLException {
        String message = "Expected row " + dataSet.rowAsMap() + " existed in table " + dataSet
            .table();
        // try to locate the actual row values according the primary keys
        String[] primaryKey = primaryKey(dataSet.table(), false);
        if (primaryKey == null || primaryKey.length == 0 || !dataSet.containColumns(primaryKey)) {
            message += " but was not found";
        } else {
            message = logRowNotFoundCompared(dataSet, message, primaryKey);
        }
        return message;
    }


    private String logRowNotFoundCompared(
        DataSet dataSet,
        String message,
        String[] primaryKey
    ) throws SQLException {
        String sql = parser.sqlSelectFrom(dataSet.table(), primaryKey).toString();
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            bindRowValues(statement, dataSet, primaryKey, true);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    message += " but was actually " + mapValues(dataSet.columns(), resultSet);
                }
            }
        }
        return message;
    }


    public void cleanUp() {
        LOGGER.debug("Performing clean-up operations...");
        for (Runnable cleanUp : cleanUpOperations) {
            cleanUp.run();
        }
        LOGGER.debug("Clean-up finished");
    }


    private long countResults(int[] results) {
        return IntStream.of(results).filter(count -> count > 0).count();
    }


    public String[] primaryKey(String table, boolean throwIfAbsent) {
        return primaryKeyCache.computeIfAbsent(table, t -> detectPrimaryKey(t, throwIfAbsent))
            .toArray(new String[0]);
    }


    public Map<String, Integer> nonNullableColumns(String table) {
        return nonNullabeColumnCache.computeIfAbsent(table, this::collectNonNullableColumns);
    }


    private Map<String, Object> mapValues(String[] columns, Object[] values) {
        Map<String, Object> log = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            log.put(columns[i], values[i]);
        }
        return log;
    }


    private Map<String, Object> mapValues(
        String[] columns,
        ResultSet resultSet
    ) throws SQLException {
        Map<String, Object> log = new LinkedHashMap<>();
        for (String column : columns) {
            log.put(column, resultSet.getObject(column));
        }
        return log;
    }


    private void traceSQL(String sql, DataSet... dataSet) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[SQL] {sql} | {}", sql, Stream.of(dataSet).map(DataSet::rowAsMap).collect(Collectors.toList()));
        }
    }

    private String catalog() {
        try {
            if (connectionParameters.catalog() != null) {
                return connectionParameters.catalog();
            }
            if (connection().getCatalog() != null) {
                return connection().getCatalog();
            }
            return null;
        } catch (SQLException e) {
            LOGGER.trace(e.toString());
            return null;
        }
    }



    private String schema() {
        try {
            if (connectionParameters.schema() != null) {
                return connectionParameters.schema();
            }
            if (connection().getSchema() != null) {
                return connection().getSchema();
            }
            return null;
        } catch (SQLException e) {
            LOGGER.trace(e.toString());
            return null;
        }
    }




}
