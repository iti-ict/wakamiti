/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api;


import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.api.model.Settings;
import es.iti.wakamiti.azure.api.model.TestPlan;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPlanApiTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(TestPlanApiTest.class);

    private static final String BASE_URL = "https://azure-devops.iti.upv.es";

    private TestPlanApi client;

//    @Before
    public void beforeEach() throws IOException {
        client = new TestPlanApi(new URL(BASE_URL), Function.identity())
                .tokenAuth(token())
                .organization("ST")
                .project("ACS")
                .version("6.0-preview");
    }

//    @Test
    public void testSettingsWhenDefaultWithSuccess() throws InterruptedException {
        Settings settings = client.settings();
        Thread.sleep(5000);
        logResult(settings);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.of("UTC+01:00"));
    }

//    @Test
    public void testSettingsWhenCustomWithSuccess() throws InterruptedException {
        // TODO
    }

//    @Test
    public void testSettingsWhenErrorWithSuccess() throws InterruptedException {
        // TODO
    }

//    @Test
    public void testSearchTestPlan() {
        TestPlan testPlan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"));

        Optional<TestPlan> remotePlan = client.searchTestPlan(testPlan);
        logResult(remotePlan);

        assertThat(remotePlan).isPresent();

        client.searchTestSuites(remotePlan.get())
                .forEach(this::logResult);


        client.searchTestCases(remotePlan.get())
                .forEach(this::logResult);

    }

    public String token() throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("token"));
    }

    private void logResult(Object o) {
        LOGGER.debug("Result: {}", o);
    }
}
