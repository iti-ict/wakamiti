/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.amqp;


import iti.commons.configurer.AnnotatedConfiguration;
import iti.commons.configurer.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.junit.KukumoJUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(KukumoJUnitRunner.class)
@AnnotatedConfiguration(properties = {
    @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = "gherkin"),
    @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features")
})
public class AmqpTest {

    static EmbeddedInMemoryQpidBroker broker = new EmbeddedInMemoryQpidBroker();


    @BeforeClass
    public static void setUp() throws Exception {
        broker.start();
    }

    @AfterClass
    public static void tearDown() {
        broker.shutdown();
    }

}