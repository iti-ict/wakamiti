package iti.kukumo.rest.test;

import iti.commons.configurer.Configurator;
import iti.commons.configurer.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.gherkin.GherkinResourceType;
import iti.kukumo.junit.KukumoJUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ITI
 *         Created by ITI on 6/03/19
 */
@Configurator(properties = {
        @Property(key= KukumoConfiguration.RESOURCE_TYPE, value= GherkinResourceType.NAME),
        @Property(key=KukumoConfiguration.RESOURCE_PATH, value="src/test/resources/test-rest-xml.feature"),
        @Property(key=KukumoConfiguration.OUTPUT_FILE_PATH, value="target/kukumo.json")
})
@RunWith(KukumoJUnitRunner.class)
public class TestRestStepsXML {

    
    private static MockServer server;
    
    @BeforeClass
    public static void setupServer() throws IOException {
        server = new MockServer(MockServer.Format.XML, StandardCharsets.UTF_8,8888, "src/test/resources/data.json", MockServer.Format.JSON);
    }
    
    @AfterClass
    public static void teardownServer() {
        server.stop();
    }
}
