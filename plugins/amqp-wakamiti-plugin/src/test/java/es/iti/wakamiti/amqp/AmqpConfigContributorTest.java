/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.amqp;


import es.iti.wakamiti.amqp.client.AmqpClient;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.Document;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class AmqpConfigContributorTest {

    @Test
    public void defaultConfigurationShouldExposeExpectedDefaults() {
        AmqpConfigContributor contributor = new AmqpConfigContributor();

        Configuration defaults = contributor.defaultConfiguration();

        assertEquals(AmqpProtocol.AMQP_1_0.name(),
                defaults.get(AmqpConfigContributor.AMQP_CONNECTION_PROTOCOL, String.class).orElse(null));
        assertEquals(Boolean.TRUE,
                defaults.get(AmqpConfigContributor.AMQP_MESSAGE_PERSISTENT, Boolean.class).orElse(null));
        assertEquals(Boolean.FALSE,
                defaults.get(AmqpConfigContributor.AMQP_QUEUE_DURABLE, Boolean.class).orElse(null));
        assertEquals(Boolean.FALSE,
                defaults.get(AmqpConfigContributor.AMQP_QUEUE_EXCLUSIVE, Boolean.class).orElse(null));
        assertEquals(Boolean.FALSE,
                defaults.get(AmqpConfigContributor.AMQP_QUEUE_AUTODELETE, Boolean.class).orElse(null));
    }

    @Test
    public void configurerShouldApplySettingsToAmqpSupportBehavior() throws Exception {
        AmqpConfigContributor contributor = new AmqpConfigContributor();
        AmqpStepContributor steps = new AmqpStepContributor();
        AmqpClient client = mock(AmqpClient.class);
        setClient(steps, client);

        Configuration configuration = Configuration.factory().fromPairs(
                AmqpConfigContributor.AMQP_CONNECTION_URL, "amqp://127.0.0.1:5671",
                AmqpConfigContributor.AMQP_CONNECTION_USERNAME, "guest",
                AmqpConfigContributor.AMQP_CONNECTION_PASSWORD, "guest",
                AmqpConfigContributor.AMQP_CONNECTION_PROTOCOL, "amqp-0.9.1",
                AmqpConfigContributor.AMQP_QUEUE_DURABLE, "true",
                AmqpConfigContributor.AMQP_QUEUE_EXCLUSIVE, "true",
                AmqpConfigContributor.AMQP_QUEUE_AUTODELETE, "true",
                AmqpConfigContributor.AMQP_MESSAGE_PERSISTENT, "false"
        );

        contributor.configurer().configure(steps, configuration);
        setClient(steps, client);
        steps.sendJSONFromString("QUEUE.A", new Document("{\"ok\":true}"));

        assertEquals("amqp://127.0.0.1:5671", steps.connectionParams.uri().toString());
        assertEquals("guest", steps.connectionParams.username());
        assertEquals("guest", steps.connectionParams.password());
        assertEquals(AmqpProtocol.AMQP_0_9_1, steps.protocol);

        verify(client).declareQueue("QUEUE.A", true, true, true);
        verify(client).sendText("QUEUE.A", "{\"ok\":true}", "application/json", false);
    }

    @Test
    public void configurerShouldNotSetConnectionParamsWhenUrlIsMissing() {
        AmqpConfigContributor contributor = new AmqpConfigContributor();
        AmqpStepContributor steps = new AmqpStepContributor();

        Configuration configuration = Configuration.factory().fromPairs(
                AmqpConfigContributor.AMQP_CONNECTION_PROTOCOL, "AMQP_1_0"
        );

        contributor.configurer().configure(steps, configuration);

        assertNull(steps.connectionParams);
        assertEquals(AmqpProtocol.AMQP_1_0, steps.protocol);
    }

    private static void setClient(AmqpStepContributor steps, AmqpClient client) throws Exception {
        Field field = AmqpSupport.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(steps, client);
    }
}

