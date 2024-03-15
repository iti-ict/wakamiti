/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest.it;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import es.iti.wakamiti.rest.RestConfigContributor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static es.iti.wakamiti.rest.RestConfigContributor.BASE_URL;
import static es.iti.wakamiti.rest.TestUtil.prepare;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;


@AnnotatedConfiguration({
        @Property(key = RESOURCE_TYPES, value = GherkinResourceType.NAME),
        @Property(key = RESOURCE_PATH, value = "src/test/resources/features/en"),
        @Property(key = OUTPUT_FILE_PATH, value = "target/wakamiti.json"),
        @Property(key = TREAT_STEPS_AS_TESTS, value = "true"),
        @Property(key = BASE_URL, value = "http://localhost:8888"),
        @Property(key = "data.dir", value = "src/test/resources"),
        @Property(key = RestConfigContributor.TIMEOUT, value = "5000")
})
@RunWith(WakamitiJUnitRunner.class)
public class TestRestSteps {

    private static final ClientAndServer client = startClientAndServer(8888);


    @BeforeClass
    public static void setupServer() throws IOException {
        ConfigurationProperties.logLevel("OFF");
        prepare(client, "server", media -> true);
    }


    @AfterClass
    public static void teardownServer() {
        client.close();
    }

}