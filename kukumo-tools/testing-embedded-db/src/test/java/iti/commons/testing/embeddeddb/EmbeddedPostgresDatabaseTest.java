package iti.commons.testing.embeddeddb;

import iti.commons.testing.embeddeddb.postgres.EmbeddedPostgresDatabaseRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.SQLException;

/**
 * @author ITI
 * Created by ITI on 23/10/19
 */
public class EmbeddedPostgresDatabaseTest {

    @Rule
    public EmbeddedDatabaseRule mariaDbRandomPort = new EmbeddedPostgresDatabaseRule("create-schema.sql");


    @Test
    public void createAndPopulateDatabasePostgresRandomPort() throws SQLException {
        DatabaseTestUtil.createAndPopulateDatabase(mariaDbRandomPort);
    }


}
