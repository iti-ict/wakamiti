package es.iti.wakamiti.plugins.jmeter;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;

import static es.iti.wakamiti.plugins.jmeter.TestUtil.prepare;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
        @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti.json"),
        @Property(key = "jmeter.report.jlt", value = "target/wakamiti-it.jtl"),
        @Property(key = JMeterConfigContributor.BASE_URL, value = "http://localhost:8888")
})
@RunWith(WakamitiJUnitRunner.class)
public class JMeterTest {

    public static final ClientAndServer client = startClientAndServer(8888);


    @BeforeClass
    public static void setupServer() throws IOException {
        ConfigurationProperties.logLevel("TRACE");
        prepare(client, "server", media -> true);
    }


    @AfterClass
    public static void teardownServer() {
        client.close();
    }


}