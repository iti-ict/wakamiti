/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.junit.runner.RunWith;

import static es.iti.wakamiti.api.WakamitiConfiguration.TREAT_STEPS_AS_TESTS;


@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/properties"),
        @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti.json"),
        @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS,
                value = "es.iti.wakamiti.junit.WakamitiSteps"),
        @Property(key = TREAT_STEPS_AS_TESTS, value = "false"),
        @Property(key = "number.integer", value = "6"),
        @Property(key = "number.decimal", value = "3,2"),
        @Property(key = "datetime.today", value = "2023-01-10"),
        @Property(key = "datetime.now", value = "10:05:03"),
        @Property(key = "text", value = "ABC"),
        @Property(key = "url", value = "https://test.es/ABC"),
        @Property(key = "host", value = "test.es")
})
@RunWith(WakamitiJUnitRunner.class)
public class PropertyEvaluatorIT {

}
