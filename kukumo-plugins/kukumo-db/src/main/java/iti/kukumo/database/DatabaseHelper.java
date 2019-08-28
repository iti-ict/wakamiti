package iti.kukumo.database;

import iti.kukumo.api.KukumoException;
import iti.kukumo.database.dataset.*;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class DatabaseHelper {


    public interface ConnectionProvider {
        Connection obtainConnection() throws SQLException;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.database");

    private final Map<String,List<String>> primaryKeyCache = new HashMap<>();
    private final Map<String,Map<String,Integer>> nonNullabeColumnCache = new HashMap<>();
    private final ConnectionProvider connectionProvider;
    private final Deque<DataSet> cleanUpOperations = new LinkedList<>();



    public DatabaseHelper(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }


    protected Connection connection() throws SQLException {
        return connectionProvider.obtainConnection();
    }


    private StringBuilder sqlSelectFrom(String table) {
        return new StringBuilder("select * from ").append(table);
    }


    private StringBuilder sqlSelectCountFrom(String table) {
        return new StringBuilder("select count(*) from ").append(table);
    }

    private StringBuilder sqlDeleteFrom(String table) {
        return new StringBuilder("delete from ").append(table);
    }


    private String sqlInsertIntoValues(DataSet dataSet) {
        return new StringBuilder("insert into ").append(dataSet.table())
        .append(" (").append(dataSet.collectColumns(",")).append(") values (")
        .append(dataSet.collectColumns(x->"?",",")).append(")")
        .toString();
    }

    private String sqlWhereColumnsEquals(DataSet dataSet) {
        return sqlWhereColumnsEquals(dataSet.columns());
    }

    private String sqlWhereColumnsEquals(String... columns) {
        return " where " + Stream.of(columns)
        .map(column -> "("+column+ "=? or ("+column+" is null and ? is null))")
        .collect(Collectors.joining(" and "));
    }


    private String sqlWhereClause(String clause) {
        return " where " + clause;
    }


    public void bindRowValues(PreparedStatement statement, DataSet dataSet, boolean nullControl) throws SQLException {
        bindRowValues(statement,dataSet,dataSet.columns(),nullControl);
    }


    public void bindRowValues(PreparedStatement statement, DataSet dataSet, String[] columns, boolean nullControl)
    throws SQLException {
        // if nullControl = true, the statement will contain 2 entries of the same parameters
        int factor = nullControl ? 2 : 1;
        for (int i = 0; i < columns.length; i++) {
            statement.setObject(i*factor + 1, dataSet.rowValue(columns[i]));
            if (nullControl) {
                statement.setObject(i*factor +2, dataSet.rowValue(columns[i]));
            }
        }
    }


    private PreparedStatement createRowStatement(CharSequence sql, DataSet dataSet, boolean nullControl)
    throws SQLException {
        PreparedStatement statement = connection().prepareStatement(sql.toString());
        bindRowValues(statement, dataSet, nullControl);
        return statement;
    }



    private <T> T extractSingleResult(PreparedStatement statement, Class<T> type) throws SQLException {
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
            ResultSet resultSet = metadata.getPrimaryKeys(null, null, table.toUpperCase());
            while (resultSet != null && resultSet.next()) {
                primaryKeys.add(resultSet.getString("COLUMN_NAME"));
            }
            if (primaryKeys.isEmpty() && throwIfAbsent) {
                throw new KukumoException("Cannot determine primary key for table "+table);
            }
            return primaryKeys;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Map<String,Integer> collectNonNullableColumns(String table) {
        try {
            DatabaseMetaData metadata = connection().getMetaData();
            Map<String,Integer> nonNullableColumns = new LinkedHashMap<>();
            ResultSet resultSet = metadata.getColumns(null, null, table.toUpperCase(), null);
            while (resultSet != null && resultSet.next()) {
                if (resultSet.getInt("NULLABLE") == DatabaseMetaData.attributeNoNulls) {
                    nonNullableColumns.put(resultSet.getString("COLUMN_NAME"),resultSet.getInt("DATA_TYPE"));
                }
            }
            return nonNullableColumns;
        } catch (Exception e) {
            throw new KukumoException(e);
        }
    }


    private void assertCount(PreparedStatement statement, Matcher<Long> matcher) throws SQLException {
        MatcherAssert.assertThat(extractSingleResult(statement,Long.class),matcher);
    }


    public void assertCountRowsInTableByColumns(Matcher<Long> matcher, String table, String[] columns, Object[] values)
    throws SQLException {
        StringBuilder sql = sqlSelectCountFrom(table).append(sqlWhereColumnsEquals(columns));
        try (PreparedStatement statement = createRowStatement(sql,new InlineDataSet(table,columns,values),true)) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("[SQL] {}  | {}", sql, mapValues(columns,values));
            }
            assertCount(statement, matcher);
        }
    }


    public void assertCountRowsInTableByClause(Matcher<Long> matcher, String table, String clause) throws SQLException {
        StringBuilder sql = sqlSelectCountFrom(table).append(sqlWhereClause(clause));
        try (PreparedStatement statement = createRowStatement(sql, new EmptyDataSet(table), false)) {
            LOGGER.trace("[SQL] {}", sql);
            assertCount(statement, matcher);
        }
    }

    public long executeSQLStatements(List<String> statements) throws SQLException {
        return executeSQLStatements(statements,null);
    }


    public long executeSQLStatements(List<String> statements, String scriptFileName) throws SQLException {
        if (scriptFileName != null) {
            LOGGER.debug("Executing SQL script from '{}'...",scriptFileName);
        } else {
            LOGGER.debug("Executing SQL script...");
        }
        try(Statement statement = connection().createStatement()) {
            for (String statementLine : statements) {
                statement.addBatch(statementLine);
                LOGGER.trace("[SQL] {}", statementLine);
            }
            long count = countResults( statement.executeLargeBatch() );
            if (scriptFileName != null) {
                LOGGER.debug("Executed SQL script '{}'; {} rows affected", scriptFileName, count);
            } else {
                LOGGER.debug("Executed SQL script; {} rows affected", count);
            }
            return count;
        }

    }


    public long insertDataSet(DataSet dataSet, boolean addCleanUpOperation) throws SQLException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Inserting rows in table {} from {}...", dataSet.table(), dataSet.origin());
        }
        Map<String,Integer> nonNullableColumns = nonNullableColumns(dataSet.table());
        if (!dataSet.containColumns(nonNullableColumns.keySet())) {
            dataSet = new CompletableDataSet(dataSet,nonNullableColumns);
        }
        if (addCleanUpOperation) {
            cleanUpOperations.addFirst(dataSet.copy());
        }
        String sql = sqlInsertIntoValues(dataSet);
        try(PreparedStatement statement = connection().prepareStatement(sql)) {
            while (dataSet.nextRow()) {
                bindRowValues(statement,dataSet,false);
                statement.addBatch();
                traceSQL(sql,dataSet);
            }
            long count = countResults( statement.executeLargeBatch() );
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Inserted {} rows in table {} from {}", count, dataSet.table(), dataSet.origin());
            }
            return count;
        }

    }



    public long deleteDataSet(DataSet dataSet) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Deleting rows in table {} from {}...", dataSet.table(), dataSet.origin());
        }
        String sql = null;
        String[] primaryKey = primaryKey(dataSet.table(), false);
        boolean deleteByPrimaryKey;
        if (primaryKey.length > 0 && dataSet.containColumns(primaryKey)) {
            sql = sqlDeleteFrom(dataSet.table()).append(sqlWhereColumnsEquals(primaryKey)).toString();
            deleteByPrimaryKey = true;
        } else {
            sql = sqlDeleteFrom(dataSet.table()).append(sqlWhereColumnsEquals(dataSet)).toString();
            deleteByPrimaryKey = false;
        }

        try(PreparedStatement statement = connection().prepareStatement(sql)) {
            while (dataSet.nextRow()) {
                if (deleteByPrimaryKey) {
                    bindRowValues(statement,dataSet,primaryKey,true);
                } else {
                    bindRowValues(statement,dataSet,true);
                }
                statement.addBatch();
                traceSQL(sql,dataSet);
            }
            long count = countResults( statement.executeLargeBatch() );
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleted {} rows in table {} from {}", count, dataSet.table(), dataSet.origin());
            }
            return count;

        }
    }


    public void truncateTable(String table) throws SQLException {
        LOGGER.debug("Deleting all rows in table {}...", table);
        try(Statement truncate = connection().createStatement()) {
            truncate.execute("truncate table "+table);
            LOGGER.debug("Deleted all rows in table {}", table);
        } catch (SQLException e) {
            // truncate may not work in every situation, try to do a delete instead
            try (Statement delete = connection().createStatement()) {
                delete.execute("delete from "+table);
                LOGGER.debug("Deleted all rows in table {}", table);
            }
        }
    }


    public long insertMultiDataSet(MultiDataSet multiDataSet, boolean addCleanUpOperation) throws SQLException, IOException {
        long count = 0;
        for (DataSet dataSet : multiDataSet) {
            count += insertDataSet(dataSet,false); //adding the complete multidataset as cleanup operation
        }
        if (addCleanUpOperation) {
            multiDataSet.copy().forEach(cleanUpOperations::addFirst);
        }
        return count;
    }


    public long deleteMultiDataSet(MultiDataSet multiDataSet) throws SQLException {
        long count = 0;
        for (DataSet dataSet : multiDataSet) {
            count += deleteDataSet(dataSet);
        }
        return count;
    }


    public void assertDataSetExists(DataSet dataSet) throws SQLException {
        String sql = sqlSelectFrom(dataSet.table()).append(sqlWhereColumnsEquals(dataSet)).toString();
        try(PreparedStatement statement = connection().prepareStatement(sql)) {
            while (dataSet.nextRow()) {
                bindRowValues(statement,dataSet,true);
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    Assertions.fail(logRowNotFound(dataSet));
                }
            }
        }
    }


    public void assertDataSetNotExists(DataSet dataSet) throws SQLException {
        String sql = sqlSelectFrom(dataSet.table()).append(sqlWhereColumnsEquals(dataSet)).toString();
        try(PreparedStatement statement = connection().prepareStatement(sql)) {
            while (dataSet.nextRow()) {
                bindRowValues(statement,dataSet,true);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    Assertions.fail("Expected row "+dataSet.rowAsMap()+" to not exists in table "+dataSet.table()+" but it does");
                }
            }
        }
    }


    public void assertCountRowsInTableByDataSet(DataSet dataSet, Matcher<Long> matcher) throws SQLException {
        String sql = sqlSelectCountFrom(dataSet.table()).append(sqlWhereColumnsEquals(dataSet)).toString();
        long count = 0;
        try(PreparedStatement statement = connection().prepareStatement(sql)) {
            while (dataSet.nextRow()) {
                bindRowValues(statement, dataSet, true);
                count += extractSingleResult(statement, Long.class);
            }
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
        String message = "Expected row "+dataSet.rowAsMap()+" existed in table "+dataSet.table();
        // try to locate the actual row values according the primary keys
        String[] primaryKey = primaryKey(dataSet.table(), false);
        if (primaryKey == null || primaryKey.length == 0 || !dataSet.containColumns(primaryKey)){
            message += " but was not found";
        } else {
            String sql = sqlSelectFrom(dataSet.table()).append(sqlWhereColumnsEquals(primaryKey)).toString();
            try (PreparedStatement statement = connection().prepareStatement(sql)) {
                bindRowValues(statement,dataSet,primaryKey,true);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    message += " but was actually "+mapValues(dataSet.columns(),resultSet);
                }
            }
        }
        return message;
    }



    public void cleanUp() {
        LOGGER.debug("Performing clean-up operations...");
        for (DataSet cleanUpDataSet : cleanUpOperations) {
            try {
                deleteDataSet(cleanUpDataSet);
            } catch (SQLException e) {
                LOGGER.debug("Error on clean-up operation: {}", e.getMessage(), e);
            }
        }
        LOGGER.debug("Clean-up finished");
    }


    private long countResults(long[] results) {
        return LongStream.of(results).filter(count->count>0).count();
    }


    public String[] primaryKey(String table, boolean throwIfAbsent) {
        return primaryKeyCache.computeIfAbsent(table, t->detectPrimaryKey(t,throwIfAbsent)).toArray(new String[0]);
    }

    public Map<String,Integer> nonNullableColumns(String table) {
        return nonNullabeColumnCache.computeIfAbsent(table, this::collectNonNullableColumns);
    }


    private Map<String,Object> mapValues(String[] columns, Object[] values) {
        Map<String,Object> log = new LinkedHashMap<>();
        for (int i=0;i<columns.length;i++) {
            log.put(columns[i],values[i]);
        }
        return log;
    }

    private Map<String,Object> mapValues(String[] columns, ResultSet resultSet) throws SQLException {
        Map<String,Object> log = new LinkedHashMap<>();
        for (int i=0;i<columns.length;i++) {
            log.put(columns[i],resultSet.getObject(columns[i]));
        }
        return log;
    }


    private void traceSQL(String sql, DataSet dataSet) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[SQL] {} | {}",sql,dataSet.rowAsMap());
        }
    }

}
