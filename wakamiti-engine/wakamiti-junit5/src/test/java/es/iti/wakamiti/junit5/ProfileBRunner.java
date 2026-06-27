/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;


@Profile("B")
@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/redefining"),
        @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.junit5.WakamitiSteps")
})
@WakamitiPlan
public class ProfileBRunner {

}
