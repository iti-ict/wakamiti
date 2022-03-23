/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.examples.spring.junit;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import iti.kukumo.spring.junit.KukumoSpringJUnitRunner;


// Kukumo Configuration
@RunWith(KukumoSpringJUnitRunner.class)
@AnnotatedConfiguration({
    @Property(key="resourceTypes",value="gherkin"),
    @Property(key="resourcePath",value="src/test/resources"),
    @Property(key="outputFilePath",value="target/kukumo/kukumo.json"),
    @Property(key="junit.treatStepsAsTests",value="true"),
    @Property(key="logs.showStepSource",value="false"),
    @Property(key="database.useSpringDataSource",value="true"),
    @Property(key="rest.useSpringLocalServerPort",value="true")
})

// Spring Configuration
@ContextConfiguration(classes = AppTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
public class KukumoTest {

}