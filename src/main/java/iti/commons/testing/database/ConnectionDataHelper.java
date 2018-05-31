/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import iti.commons.testing.TestingException;

public class ConnectionDataHelper extends DatabaseHelper {

    private Connection connection;

    public ConnectionDataHelper(Connection connection) {
        super();
        this.connection = connection;
    }

    @Override
    protected int[] executeStatement(StatementInfo statement) {
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement.sql)) {
                for (Object[] row : statement.data) {
                    for (int i=0;i<row.length;i++) {
                        preparedStatement.setObject(i+1, row[i]);
                    }
                    preparedStatement.addBatch();
                }
                return preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            throw new TestingException(e);
        }
    }



    @Override
    public void executeSentence(String sql)  {
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new TestingException(e);
        }
    }


    /*
     * Obtain the primary key columns of the specified table
     */
    public List<String> resolvePrimaryKey(String table) {
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            ArrayList<String> primaryKeys = new ArrayList<>();
            ResultSet resultSet = metadata.getPrimaryKeys(null, null, table);
            while (resultSet.next()) {
                primaryKeys.add(resultSet.getString("column_name"));
            }
            if (primaryKeys.isEmpty()) {
                // try to use show columns
                List<Object[]> columns = queryList("show columns from "+table,Arrays.asList());
                for (Object[] column : columns) {
                    if ("PRI".equals(column[3])) {
                        primaryKeys.add((String)column[0]);
                    }
                }
            }
            if (primaryKeys.isEmpty()) {
                throw new SQLException("cannot determine primary key for table "+table);
            }
            return primaryKeys;
        } catch (SQLException e) {
            throw new TestingException(e);
        }
    }



    @Override
    public List<Object[]> queryList (String sql, List<? extends Object> params)  {
        try {
            List<Object[]> results = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i=0;i<params.size();i++) {
                    statement.setObject(i+1, params.get(i));
                }
                logger.debug("query {} [{}]", sql, params);
                ResultSet resultSet = statement.executeQuery();
                while(resultSet.next()) {
                    Object[] row = new Object[resultSet.getMetaData().getColumnCount()];
                    for (int i=0;i<row.length;i++) {
                        row[i] = resultSet.getObject(i+1);
                    }
                    results.add(row);
                }
            }
            return results;
        } catch (SQLException e) {
            throw new TestingException(e);
        }
    }





   @Override
   public <T> T querySingleValue (String sql, List<? extends Object> params, Class<T> type)  {
       try {
           try (PreparedStatement statement = connection.prepareStatement(sql)) {
               for (int i=0;i<params.size();i++) {
                   statement.setObject(i+1, params.get(i));
               }
               logger.debug("query {} [{}]", sql, params);
               ResultSet resultSet = statement.executeQuery();
               resultSet.next();
               return resultSet.getObject(1, type);
           }
       } catch (SQLException e) {
           throw new TestingException(e);
       }
   }





}
