/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.groovy.it;

import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import iti.wakamiti.api.WakamitiConfiguration;
import iti.wakamiti.core.gherkin.GherkinResourceType;
import iti.wakamiti.core.junit.WakamitiJUnitRunner;
import org.junit.runner.RunWith;

@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/test.feature"),
        @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti.json")
})
@RunWith(WakamitiJUnitRunner.class)
public class TestGroovySteps {
}
