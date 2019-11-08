package iti.commons.testing.embeddeddb;

import org.assertj.core.util.Arrays;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author ITI
 * Created by ITI on 23/10/19
 */
public class DatabaseTestUtil {


    public static void createAndPopulateDatabase(EmbeddedDatabaseRule databaseRule) throws SQLException {
        System.out.println(databaseRule.urlConnection());
        try (Connection connection = databaseRule.openConnection()) {
            execute(connection,"insert into USER_ values (1,'John','Bow',1,'1985-04-05')");
            assertQueryResult(query(connection,"select * from USER_"),
                1,
                "John",
                "Bow",
                1,
                java.sql.Date.valueOf("1985-04-05")
            );
        }
    }

    private static void assertQueryResult(List<List<Object>> results, Object... values) {
        assertEquals(results.size(),1);
        for (int i=0;i<values.length;i++) {
            assertTrue("Value "+i,compare(results.get(0).get(i),values[i]));
        }
    }

    private static boolean compare(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return ((Number)a).longValue() == ((Number)b).longValue();
        }
        if (a instanceof java.sql.Date && b instanceof java.sql.Date) {
            return ((java.sql.Date)a).toLocalDate().equals(((java.sql.Date)b).toLocalDate());
        }
        return a.equals(b);
    }


    private static void execute(Connection connection, String sql) throws SQLException {
         try (Statement statement = connection.createStatement()) {
             statement.execute(sql);
        }
    }


    private static List<List<Object>> query(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                List<List<Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    Object[] row = new Object[resultSet.getMetaData().getColumnCount()];
                    for (int i=0;i<row.length;i++) {
                        row[i] = resultSet.getObject(i+1);
                    }
                    results.add(Arrays.asList(row));
                }
                return results;
            }
        }
    }

}
