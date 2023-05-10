/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.test.gherkin;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import iti.wakamiti.core.junit.WakamitiJUnitRunner;
import org.junit.runner.RunWith;


import iti.wakamiti.api.WakamitiConfiguration;
import iti.wakamiti.core.gherkin.GherkinResourceType;



@AnnotatedConfiguration({
    @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
    @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/redefining"),
    @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti.json"),
    @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "iti.wakamiti.test.gherkin.WakamitiSteps"),
    @Property(key = WakamitiJUnitRunner.TREAT_STEPS_AS_TESTS, value = "true")
})
@RunWith(WakamitiJUnitRunner.class)
public class TestWakamitiRunner {

}