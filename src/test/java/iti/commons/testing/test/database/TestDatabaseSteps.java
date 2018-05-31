/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.test.database;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;

import iti.commons.testing.database.ConnectionDataHelper;
import iti.commons.testing.database.DatabaseSteps;

public class TestDatabaseSteps  extends DatabaseSteps {


    public TestDatabaseSteps() throws SQLException {
        super(new ConnectionDataHelper(DriverManager.getConnection("jdbc:h2:mem:", "sa", null)),
              classLoader(),
              Locale.forLanguageTag("es"),
              "databaseSteps");
     }


    @cucumber.api.java.Before(order=10)
    public void createSchema() {
        helper.executeScript("create_schema.sql");
    }


    @cucumber.api.java.After(order=10)
    public void dropSchema() {
        helper.executeScript("drop_schema.sql");
    }


    public static final ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
