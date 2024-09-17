package es.iti.wakamiti.jmeter;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;

import static es.iti.wakamiti.jmeter.TestUtil.prepare;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
        @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti.json"),
        @Property(key = "data.dir", value = "src/test/resources"),
        @Property(key = "jmeter.report.jlt", value = "target/wakamiti-it.jtl"),
        @Property(key = JMeterConfigContributor.BASE_URL, value = "http://localhost:8888/api"),
        @Property(key = JMeterConfigContributor.OAUTH2_URL, value = "http://localhost:8888/token"),
        @Property(key = JMeterConfigContributor.OAUTH2_CLIENT_ID, value = "WEB"),
        @Property(key = JMeterConfigContributor.OAUTH2_CLIENT_SECRET, value = "s3cr3t"),
        @Property(key = JMeterConfigContributor.OAUTH2_DEFAULT_PARAMETERS + ".grant_type", value = "password"),
        @Property(key = JMeterConfigContributor.OAUTH2_DEFAULT_PARAMETERS + ".username", value = "abc"),
        @Property(key = JMeterConfigContributor.OAUTH2_DEFAULT_PARAMETERS + ".password", value = "123"),
        @Property(key = JMeterConfigContributor.CSV_EOF, value = "true")
})
@RunWith(WakamitiJUnitRunner.class)
public class JMeterTest {

    public static final ClientAndServer client = startClientAndServer(8888);


    @BeforeClass
    public static void setupServer() throws IOException {
        ConfigurationProperties.logLevel("TRACE");
        TestUtil.prepare(client, "server", media -> true);
    }


    @AfterClass
    public static void teardownServer() {
        client.close();
    }


}