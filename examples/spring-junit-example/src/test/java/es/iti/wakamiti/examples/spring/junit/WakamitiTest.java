/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.examples.spring.junit;


import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


// Wakamiti Configuration
@RunWith(WakamitiSpringJUnitRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = {App.class, AppTestConfig.class}
)
@ActiveProfiles("test")
@AnnotatedConfiguration(path = "classpath:application-test.yaml", pathPrefix = "wakamiti")
public class WakamitiTest {

}
