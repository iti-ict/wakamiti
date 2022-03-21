/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.examples.spring.junit;


import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import iti.commons.configurer.AnnotatedConfiguration;
import iti.kukumo.spring.junit.KukumoSpringJUnitRunner;


// Kukumo Configuration
@RunWith(KukumoSpringJUnitRunner.class)
@AnnotatedConfiguration(path = "classpath:application-test.yaml", pathPrefix = "kukumo")

// Spring Configuration
@ContextConfiguration(classes = AppTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
public class KukumoTest {

}