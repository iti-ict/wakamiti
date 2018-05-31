/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.database;



import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.commons.testing.TestingException;

public abstract class DatabaseHelper {

    private static final String DELETE_QUERY = "delete from %s where %s";
    private static final String QUERY_AND = " and ";

    protected static class StatementInfo {
        public final String sql;
        public final List<Object[]> data;
        public StatementInfo(String sql, List<Object[]> data) {
            this.sql = sql;
            this.data = data;
        }
    }


    protected final Logger logger;
    private List<StatementInfo> cleanUpStatements = new ArrayList<>();


    public DatabaseHelper() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    public void addCleanUpScript(String file)  {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream stream = classLoader.getResourceAsStream(file)) {
                for (String sentence : readScript(stream)) {
                    addCleanUpSentence(sentence);
                }
            }
        } catch (IOException e) {
            throw new TestingException(e);
        }
    }



    public void addCleanUpSentence(String sql)  {
        cleanUpStatements.add(new StatementInfo(sql,Arrays.asList()));
    }


    public void addCleanUpTable(String tableName, String criteria)  {
        cleanUpStatements.add(new StatementInfo(String.format(DELETE_QUERY,tableName,criteria),Arrays.asList()));
    }




    public void executeScript(String file) {
        logger.debug("execute script {}",file);
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream stream = classLoader.getResourceAsStream(file)) {
                executeScript(stream);
            }
        } catch (IOException e) {
            throw new TestingException(e);
        }
    }



    public void executeScript(InputStream stream)  {
        try {
            for (String sentence : readScript(stream)) {
                executeSentence(sentence);
            }
        } catch (IOException e) {
            throw new TestingException(e);
        }
    }


    protected List<String> readScript (InputStream stream) throws IOException {
        logger.debug("reading script...");
        List<String> sentences = new ArrayList<>();
        StringBuilder sentence = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(stream)) {
            int character = -1;
            while ((character = reader.read()) != -1) {
                if ((char)character == ';') {
                    sentences.add(sentence.toString());
                    sentence.delete(0, sentence.length());
                } else {
                    sentence.append((char)character);
                }
            }
        }
        logger.debug("end of script");
        return sentences;
    }





    public void insertData(DataLoader dataResolver, Object... arguments) {
        insertData(dataResolver.resolveData(arguments));
    }


    public void insertData (List<DataSet> dataSets) {
        for (DataSet dataSet : dataSets) {
            insertData(dataSet.tableName(), dataSet.columnNames(), dataSet.data());
        }
    }


    public void insertData (String tableName, List<String> columnNames, List<Object[]> data) {
        StatementInfo insertStatement = new StatementInfo(String.format(
                "insert into %s (%s) values (%s)",
                tableName,
                columnNames.stream().map(Object::toString).collect(Collectors.joining(",")),
                columnNames.stream().map(s->"?").collect(Collectors.joining(","))
        ), data);
        StatementInfo deleteStatement = new StatementInfo(String.format(
                DELETE_QUERY,
                tableName,
                columnNames.stream().map(column->"("+column+" = ?)").collect(Collectors.joining(QUERY_AND))
        ), data);
        // perform the inserts
        int[] results = executeStatement(insertStatement);
        logger.debug("inserting data in table {}...", tableName);
        if (logger.isTraceEnabled()) {
            logger.trace("[COLUMNS={}]",columnNames);
            for (Object[] row: data) {
                logger.trace(Arrays.toString(row));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} rows inserted.", StreamSupport.intStream(Arrays.spliterator(results),false).sum());
        }

        // store the delete statement
        cleanUpStatements.add(deleteStatement);

    }




   public void deleteData(String table, String criteria) {
        executeSentence(String.format("delete from %s where %s", table, criteria));
    }






    public void assertIDExistsInTable(Object arrayId, String table) {
        List<String> columns = resolvePrimaryKey(table);
        assertExistsOrNotIDInTable(arrayId,table,columns,true);
    }


    public void assertIDNotExistsInTable(Object arrayId, String table) {
        List<String> columns = resolvePrimaryKey(table);
        assertExistsOrNotIDInTable(arrayId,table,columns,false);
    }


    private String sqlCountWhere(String table, String criteria) {
        return String.format("select count(*) from %s where %s", table, criteria);
    }



    public void assertRowExistsInTable(String table, String criteria)  {
        String sql = sqlCountWhere(table,criteria);
        assertThat(querySingleValue(sql,Integer.class)).isNotZero();
    }

    public void assertRowNotExistsInTable(String table, String criteria)   {
        String sql = sqlCountWhere(table,criteria);
        assertThat(querySingleValue(sql, Integer.class)).isZero();
    }


    public void assertRowCountInTable(String table, String criteria, int expectedCount)  {
        String sql = sqlCountWhere(table,criteria);
        assertThat(querySingleValue(sql, Integer.class)).isEqualTo(expectedCount);
    }


    public void assertTableIsEmpty(String table, String criteria) {
        String sql = sqlCountWhere(table,criteria);
        assertThat(querySingleValue(sql,Integer.class)).isZero();
    }


    public void assertTableIsNotEmpty(String table, String criteria)  {
        String sql = sqlCountWhere(table,criteria);
        assertThat(querySingleValue(sql,Integer.class)).isNotZero();
    }


    public void assertTableCount(String table, String criteria, int expectedSize)  {
        String sql = sqlCountWhere(table,criteria);
        assertThat(querySingleValue(sql,Integer.class)).isEqualTo(expectedSize);
    }


    public void assertDataExists(String table, List<String> headers, List<Object[]> data) {
        assertExistsOrNotData(table,headers,data,true);
    }

    public void assertDataNotExists(String table, List<String> headers, List<Object[]> data) {
        assertExistsOrNotData(table,headers,data,false);
    }


    public void assertDataExists(DataLoader dataLoader, Object...arguments) {
        for (DataSet dataSet : dataLoader.resolveData(arguments)) {
            assertDataExists(dataSet.tableName(),dataSet.columnNames(),dataSet.data());
        }
    }

    public void assertDataNotExists(DataLoader dataLoader, Object...arguments) {
        for (DataSet dataSet : dataLoader.resolveData(arguments)) {
            assertDataNotExists(dataSet.tableName(),dataSet.columnNames(),dataSet.data());
        }
    }


    private void assertExistsOrNotData(String table, List<String> headers, List<Object[]> data, boolean assertExists) {
        String sql = "select * from "+table+" where " +
                headers.stream()
                .map(Object::toString)
                .map(s->s+" = ?")
                .collect(Collectors.joining(QUERY_AND));

        for (int row=0;row<data.size();row++) {
            List<Object> values = Arrays.asList(data.get(row));
            if (assertExists) {
                assertThat( queryList(sql, values) )
                .as("row %d %s in table %s",row,values,table).isNotEmpty();
            } else {
                assertThat( queryList(sql, values) ).as("row %d %s in table %s",row,values,table).isEmpty();
            }
        }
    }






    private void assertExistsOrNotIDInTable(Object arrayId, String table, List<String> idColumns, boolean checkEmpty) {
        List<String> multipleId = Arrays.asList(arrayId.toString().split(","));
        String sql = String.format("select null from %s where %s",
            table,
            idColumns.stream().map(s->s+" = ?").collect(Collectors.joining(QUERY_AND))
        );
        if ( checkEmpty ) {
            assertThat(queryList(sql,multipleId)).as(arrayId+" in table "+table).isNotEmpty();
        } else {
            assertThat(queryList(sql,multipleId)).as(arrayId+" in table "+table).isEmpty();
        }
    }





    public void performCleanUp()  {
        for (int i=cleanUpStatements.size()-1;i>=0;i--) {
            StatementInfo statement = cleanUpStatements.get(i);
            logger.debug("cleaning up data...");
                int[] results = executeStatement(statement);
                if (logger.isDebugEnabled()) {
                    logger.debug("{} rows deleted.", StreamSupport.intStream(Arrays.spliterator(results),false).sum());
                }
        }
    }












    private void prepareUpdateData(List<Object[]> data, List<String> columnIDs, List<String> columnNames) {
        for (int i=0;i<data.size();i++) {
            int count = 0;
            Object[] id = new Object[columnIDs.size()];
            for (int j=0;j<columnNames.size();j++) {
                final int index = j;
                Optional<String> isID = columnIDs.stream().filter(s->s.equalsIgnoreCase(columnNames.get(index))).findFirst();
                if (isID.isPresent()) {
                    id[count] = data.get(i)[j];
                    count++;
                }
            }
            data.set(i, concatArray(data.get(i),id));
        }
    }


    public void updateData (List<DataSet> dataSets) {
        for (DataSet dataSet : dataSets) {
            updateData(dataSet.tableName(), dataSet.columnIDs(), dataSet.columnNames(), dataSet.data());
        }
    }



    public void updateData (DataLoader dataLoader, Object...arguments)  {
        List<DataSet> dataSets = dataLoader.resolveData(arguments);
        for (DataSet dataSet : dataSets) {
            dataSet.setColumnIDs(resolvePrimaryKey(dataSet.tableName()));
        }
        updateData(dataSets);
    }



    public void updateData (String tableName, List<String> columnIDs, List<String> columnNames, List<Object[]> data)  {
        prepareUpdateData(data,columnIDs,columnNames);
        StatementInfo updateStatement = new StatementInfo(String.format(
                "update %s set %s where %s",
                tableName,
                columnNames.stream().map(s->s+" = ?").collect(Collectors.joining(",")),
                columnIDs.stream().map(s->s+" = ?").collect(Collectors.joining(QUERY_AND))
        ), data);
        // perform the updates
        int[] results = executeStatement(updateStatement);

        logger.debug("updating data in table {}...", tableName);
        if (logger.isTraceEnabled()) {
            logger.trace("[COLUMNS={}]",columnNames);
            for (Object[] row: data) {
                logger.trace(Arrays.toString(row));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} rows updated.", StreamSupport.intStream(Arrays.spliterator(results),false).sum());
        }
    }




    public void deleteData (String tableName, List<String> columnIDs, List<Object[]> ids) {
        StatementInfo deleteStatement = new StatementInfo(String.format(
                DELETE_QUERY,
                tableName,
                columnIDs.stream().map(column->"("+column+" = ?)").collect(Collectors.joining(QUERY_AND))
        ), ids);
        int[] results = executeStatement(deleteStatement);
        if (logger.isDebugEnabled()) {
            logger.debug("{} rows deleted.", StreamSupport.intStream(Arrays.spliterator(results),false).sum());
        }
    }



















    private Object[] concatArray(Object[] arrayA, Object[] arrayB) {
        Object[] array = new Object[arrayA.length + arrayB.length];
        for (int i=0;i<array.length;i++) {
            array[i] = (i<arrayA.length ? arrayA[i] : arrayB[i-arrayA.length]);
        }
        return array;
    }



    public List<Object[]> queryList (String sql) {
        return queryList(sql,Arrays.asList());
    }


    public <T> T querySingleValue (String sql, Class<T> type) {
        return querySingleValue(sql,Arrays.asList(),type);
    }





    public abstract void executeSentence(String sentence);
    protected abstract int[] executeStatement (StatementInfo statement);
    public abstract List<String> resolvePrimaryKey(String table);
    public abstract List<Object[]> queryList (String sql, List<? extends Object> arguments);
    public abstract <T> T querySingleValue (String sql, List<? extends Object> arguments, Class<T> type);


}
