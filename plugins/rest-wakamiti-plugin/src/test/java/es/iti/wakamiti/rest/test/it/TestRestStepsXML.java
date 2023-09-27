/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.rest.test.it;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import es.iti.wakamiti.core.junit.WakamitiJUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import java.io.IOException;

import static es.iti.wakamiti.rest.test.TestUtil.prepare;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/en/test-rest-xml.feature"),
        @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti.json")
})
@RunWith(WakamitiJUnitRunner.class)
public class TestRestStepsXML {

    private static final ClientAndServer client = startClientAndServer(8887);


    @BeforeClass
    public static void setupServer() throws IOException {
        ConfigurationProperties.logLevel("OFF");
        prepare(client, "server", MediaType::isXml);
    }


    @AfterClass
    public static void teardownServer() {
        client.close();
    }
}