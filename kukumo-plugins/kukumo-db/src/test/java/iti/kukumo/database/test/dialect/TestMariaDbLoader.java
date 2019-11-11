/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database.test.dialect;


import iti.commons.configurer.Configurator;
import iti.commons.configurer.Property;
import iti.commons.testing.embeddeddb.EmbeddedDatabase;
import iti.commons.testing.embeddeddb.mariadb.EmbeddedMariaDbDatabase;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.database.DatabaseStepConfiguration;
import iti.kukumo.junit.KukumoJUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(KukumoJUnitRunner.class)
@Configurator(properties = {
        @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
        @Property(key = DatabaseStepConfiguration.DATABASE_CONNECTION_URL, value = "jdbc:mysql://localhost:3316/test?serverTimezone=UTC"),
        @Property(key = DatabaseStepConfiguration.DATABASE_CONNECTION_USERNAME, value = "root"),
        @Property(key = DatabaseStepConfiguration.DATABASE_CONNECTION_PASSWORD, value = ""),
        @Property(key = DatabaseStepConfiguration.DATABASE_CASE_SENSITIVITY, value = "lower_cased"),
        @Property(key = DatabaseStepConfiguration.DATABASE_CONNECTION_SCHEMA, value = "def"),
        @Property(key = DatabaseStepConfiguration.DATABASE_CONNECTION_CATALOG, value = "test")
})
public class TestMariaDbLoader {

    private static Logger LOGGER = LoggerFactory.getLogger(H2DbLoader.class);
    private static EmbeddedDatabase database;

    @BeforeClass
    public static void startDatabaseServer() throws Exception {
        LOGGER.info("starting database");
        database = new EmbeddedMariaDbDatabase(3316,"create-schema.sql");
        database.start();
    }

    @AfterClass
    public static void stopDatabaseServer() throws Exception {
        LOGGER.info("stopping database");
        database.stop();
    }

}
