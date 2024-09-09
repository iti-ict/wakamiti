/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.examples.spring.junit;


import es.iti.wakamiti.spring.junit.WakamitiSpringJUnitRunner;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


// Wakamiti Configuration
@RunWith(WakamitiSpringJUnitRunner.class)
@AnnotatedConfiguration({
    @Property(key="resourcePath",value="src/test/resources"),
    @Property(key="resourceTypes",value="gherkin"),
    @Property(key="outputFilePath",value="target/wakamiti/wakamiti.json"),
    @Property(key="junit.treatStepsAsTests",value="true"),
    @Property(key="logs.showStepSource",value="false"),
    @Property(key="database.useSpringDataSource",value="true"),
    @Property(key="rest.useSpringLocalServerPort",value="true")
})

// Spring Configuration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AppTestConfig.class)
@ActiveProfiles(profiles = "test")
public class WakamitiTest {

}