/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.groovy.it;

import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.core.gherkin.GherkinResourceType;
import iti.kukumo.core.junit.KukumoJUnitRunner;
import org.junit.runner.RunWith;

@AnnotatedConfiguration({
        @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
        @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features/test.feature"),
        @Property(key = KukumoConfiguration.OUTPUT_FILE_PATH, value = "target/kukumo.json")
})
@RunWith(KukumoJUnitRunner.class)
public class TestGroovySteps {
}
