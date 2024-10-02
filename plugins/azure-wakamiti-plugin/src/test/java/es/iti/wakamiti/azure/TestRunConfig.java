/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;


import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static es.iti.wakamiti.azure.AzureConfigContributor.*;


@AnnotatedConfiguration({
        @Property(key = NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.azure.MockSteps"),
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources"),
        @Property(key = AZURE_BASE_URL, value = "https://azure-devops.iti.upv.es"),
        @Property(key = AZURE_ORGANIZATION, value = "ST"),
        @Property(key = AZURE_PROJECT, value = "ACS"),
        @Property(key = AZURE_PLAN_NAME, value = "Wakamiti Test Plan A"),
        @Property(key = AZURE_PLAN_AREA, value = "ACS"),
        @Property(key = AZURE_PLAN_ITERATION, value = "ACS/Iteración 1"),
        @Property(key = AZURE_AUTH_TOKEN, value = "XXX"),
        @Property(key = AZURE_API_VERSION, value = "6.0-preview"),
        @Property(key = AZURE_ATTACHMENTS, value = "wakamiti.html")
})
//@RunWith(WakamitiJUnitRunner.class)
public class TestRunConfig {

}

