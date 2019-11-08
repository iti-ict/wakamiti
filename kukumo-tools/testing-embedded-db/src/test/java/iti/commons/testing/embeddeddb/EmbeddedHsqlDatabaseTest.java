package iti.commons.testing.embeddeddb;

import iti.commons.testing.embeddeddb.hsql.EmbeddedHsqlDatabaseRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.SQLException;

/**
 * @author ITI
 * Created by ITI on 23/10/19
 */
public class EmbeddedHsqlDatabaseTest {

    @Rule
    public EmbeddedDatabaseRule hsqlRandomPort = new EmbeddedHsqlDatabaseRule("create-schema.sql");


    @Test
    public void createAndPopulateDatabaseMariaDbRandomPort() throws SQLException {
        DatabaseTestUtil.createAndPopulateDatabase(hsqlRandomPort);
    }


}
