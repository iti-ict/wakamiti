/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database.test.dialect;


import iti.commons.configurer.Configurator;
import iti.commons.configurer.Property;
import iti.commons.testing.embeddeddb.EmbeddedDatabase;
import iti.commons.testing.embeddeddb.h2.EmbeddedH2Database;
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
    @Property(key = DatabaseStepConfiguration.DATABASE_CONNECTION_URL, value = "jdbc:h2:tcp://localhost:3316/mem:test"),
    @Property(key = DatabaseStepConfiguration.DATABASE_CONNECTION_USERNAME, value = "sa"),
    @Property(key = DatabaseStepConfiguration.DATABASE_CONNECTION_PASSWORD, value = ""),
    @Property(key = DatabaseStepConfiguration.DATABASE_CASE_SENSITIVITY, value = "upper_cased")
})
public class H2DbLoader {

    private static Logger LOGGER = LoggerFactory.getLogger(H2DbLoader.class);
    private static EmbeddedDatabase database;

    @BeforeClass
    public static void startDatabaseServer() throws Exception {
        LOGGER.info("starting database");
        database = new EmbeddedH2Database(3316,"create-schema.sql");
        database.start();
    }

    @AfterClass
    public static void stopDatabaseServer() throws Exception {
        LOGGER.info("stopping database");
        database.stop();
    }

}
