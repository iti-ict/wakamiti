/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.gherkin;


import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.imconfig.Configuration;
import org.junit.Test;

import java.util.Locale;

import static es.iti.wakamiti.api.WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS;
import static org.assertj.core.api.Assertions.assertThat;


public class TestNonRunnableBackend {

    @Test
    public void testNonRunnableBackend() {

        Configuration configuration = Wakamiti.defaultConfiguration().appendFromPairs(
                NON_REGISTERED_STEP_PROVIDERS, "es.iti.wakamiti.test.gherkin.WakamitiSteps"
        );
        var backend = Wakamiti.instance().newBackendFactory().createNonRunnableBackend(configuration);
        assertThat(backend.getAvailableSteps(Locale.ENGLISH, true)).isNotEmpty();

    }
}