package iti.commons.testing.embeddeddb;

import iti.commons.testing.embeddeddb.mariadb.EmbeddedMariaDbDatabaseRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.SQLException;

/**
 * @author ITI
 * Created by ITI on 23/10/19
 */
public class EmbeddedMariaDbDatabaseTest {

    @Rule
    public EmbeddedDatabaseRule mariaDbRandomPort = new EmbeddedMariaDbDatabaseRule("create-schema.sql");


    @Test
    public void createAndPopulateDatabaseMariaDbRandomPort() throws SQLException {
        DatabaseTestUtil.createAndPopulateDatabase(mariaDbRandomPort);
    }


}
