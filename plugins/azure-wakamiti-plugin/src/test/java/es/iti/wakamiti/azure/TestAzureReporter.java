/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.core.JsonPlanSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;


public class TestAzureReporter {

//    @Test
    public void test() throws IOException {
        AzureSynchronizer reporter = new AzureSynchronizer();
        configure(reporter, "wakamiti.yaml");

        Event event = event(Event.PLAN_CREATED, new JsonPlanSerializer().read(resource("wakamiti.json")));
        reporter.eventReceived(event);

    }

    private void configure(AzureSynchronizer reporter, String resource) {
        AzureConfigContributor azureConfig = new AzureConfigContributor();
        Configuration config = WakamitiConfiguration.DEFAULTS
                .append(azureConfig.defaultConfiguration())
                .append(Configuration.factory()
                        .fromResource(resource, classLoader())
                        .inner("wakamiti"));
        azureConfig.configurer().configure(reporter, config);
    }

    private ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private InputStream resource(String resource) {
        return classLoader().getResourceAsStream(resource);
    }

    private Event event(String type, Object data) {
        return new Event(type, Instant.now(), data);
    }

}
