/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.imconfig.Configuration;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


public class TestAzureReporter {

    @Test
    public void test() throws IOException {
        AzureSynchronizer reporter = new AzureSynchronizer();
        configure(reporter, "wakamiti.yaml");

        assertThat(reporter)
                .hasFieldOrPropertyWithValue("enabled", true)
                .hasFieldOrPropertyWithValue("baseURL", new URL("http://localhost:8888/"))
                .hasFieldOrPropertyWithValue("organization", "ST")
                .hasFieldOrPropertyWithValue("project", "ACS")
                .hasFieldOrPropertyWithValue("version", "6.0-preview")
                .hasFieldOrPropertyWithValue("tag", "Azure")
                .hasFieldOrPropertyWithValue("testCasePerFeature", false)
                .hasFieldOrPropertyWithValue("createItemsIfAbsent", true)
                .hasFieldOrPropertyWithValue("idTagPattern", "ID([\\w-]+)");
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
