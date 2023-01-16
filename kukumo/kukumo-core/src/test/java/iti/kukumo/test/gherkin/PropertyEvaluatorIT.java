/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.test.gherkin;

import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.core.gherkin.GherkinResourceType;
import iti.kukumo.core.junit.KukumoJUnitRunner;
import org.junit.runner.RunWith;

@AnnotatedConfiguration({
        @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
        @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features/properties/test_globalProperties.feature"),
        @Property(key = KukumoConfiguration.OUTPUT_FILE_PATH, value = "target/kukumo.json"),
        @Property(key = KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "iti.kukumo.test.gherkin.KukumoSteps"),
        @Property(key = KukumoJUnitRunner.TREAT_STEPS_AS_TESTS, value = "true"),
        @Property(key = "number.integer", value = "6"),
        @Property(key = "number.decimal", value = "3,2"),
        @Property(key = "datetime.today", value = "2023-01-10"),
        @Property(key = "datetime.now", value = "10:05:03"),
        @Property(key = "text", value = "ABC"),
        @Property(key = "url", value = "https://test.es/ABC"),
        @Property(key = "host", value = "test.es")
})
@RunWith(KukumoJUnitRunner.class)
public class PropertyEvaluatorIT {
}
