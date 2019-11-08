package iti.commons.testing.embeddeddb;
import iti.commons.testing.embeddeddb.h2.EmbeddedH2DatabaseRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.SQLException;

/**
 * @author ITI
 * Created by ITI on 23/10/19
 */
public class EmbeddedH2DatabaseTest {

    @Rule
    public EmbeddedDatabaseRule h2RandomPort = new EmbeddedH2DatabaseRule(3396,"create-schema.sql");

    @Test
    public void createAndPopulateDatabaseH2RandomPort() throws SQLException {
        DatabaseTestUtil.createAndPopulateDatabase(h2RandomPort);
    }

    @Test
    public void createAndPopulateDatabaseH2RandomPort2() throws SQLException {
        DatabaseTestUtil.createAndPopulateDatabase(h2RandomPort);
    }

}
