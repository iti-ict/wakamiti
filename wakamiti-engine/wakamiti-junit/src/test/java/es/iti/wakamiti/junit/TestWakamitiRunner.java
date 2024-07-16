/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import org.junit.runner.RunWith;

import static es.iti.wakamiti.api.WakamitiConfiguration.TREAT_STEPS_AS_TESTS;


@AnnotatedConfiguration({
    @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
    @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/redefining"),
    @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti-%execID%.json"),
    @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.junit.WakamitiSteps"),
    @Property(key = TREAT_STEPS_AS_TESTS, value = "true")
})
@RunWith(WakamitiJUnitRunner.class)
public class TestWakamitiRunner {

}