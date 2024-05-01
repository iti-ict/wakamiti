/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.amqp;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;


@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
    @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
    @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features")
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