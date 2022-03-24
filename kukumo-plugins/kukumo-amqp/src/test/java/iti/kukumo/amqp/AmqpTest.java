/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.amqp;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.junit.KukumoJUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(KukumoJUnitRunner.class)
@AnnotatedConfiguration({
    @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = "gherkin"),
    @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features")
})
public class AmqpTest {

    static EmbeddedInMemoryQpidBroker broker = new EmbeddedInMemoryQpidBroker();


    @BeforeClass
    public static void setUp() throws Exception {
        broker.start();
    }

    @AfterClass
    public static void tearDown() {
        broker.shutdown();
    }

}