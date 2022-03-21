/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.test.gherkin;


import org.junit.runner.RunWith;

import iti.commons.configurer.AnnotatedConfiguration;
import iti.commons.configurer.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.gherkin.GherkinResourceType;
import iti.kukumo.junit.KukumoJUnitRunner;


@AnnotatedConfiguration(properties = {
    @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
    @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features/redefining"),
    @Property(key = KukumoConfiguration.OUTPUT_FILE_PATH, value = "target/kukumo.json"),
    @Property(key = KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "iti.kukumo.test.gherkin.KukumoSteps"),
    @Property(key = KukumoJUnitRunner.TREAT_STEPS_AS_TESTS, value = "true")
})
@RunWith(KukumoJUnitRunner.class)
public class TestKukumoRunner {

}