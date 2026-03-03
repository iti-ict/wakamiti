/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.amqp;


import es.iti.wakamiti.amqp.client.AmqpClient;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.Document;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class AmqpStepContributorTest {

    @Test
    public void defineConnectionParametersShouldCloseExistingClient() throws Exception {
        AmqpStepContributor contributor = new AmqpStepContributor();
        AmqpClient client = mock(AmqpClient.class);
        setClient(contributor, client);

        contributor.defineConnectionParameters("amqp://127.0.0.1:5672", "user", "pass");

        verify(client).close();
        assertNull(getClient(contributor));
        assertEquals("amqp://127.0.0.1:5672", contributor.connectionParams.uri().toString());
    }

    @Test(expected = WakamitiException.class)
    public void defineProtocolShouldThrowForUnsupportedValue() {
        AmqpStepContributor contributor = new AmqpStepContributor();

        contributor.defineProtocol("invalid_protocol");
    }

    @Test
    public void defineDestinationQueueShouldPropagateDeclareQueueErrors() throws Exception {
        AmqpStepContributor contributor = new AmqpStepContributor();
        AmqpClient client = mock(AmqpClient.class);
        setClient(contributor, client);
        doThrow(new WakamitiException("declare failed")).when(client).declareQueue(anyString(), anyBoolean(), anyBoolean(), anyBoolean());

        try {
            contributor.defineDestinationQueue("ERR.Q");
            fail("Expected WakamitiException");
        } catch (WakamitiException e) {
            assertTrue(e.getMessage().contains("declare failed"));
        }
    }

    @Test
    public void sendJsonFromStringShouldPropagateDeclareQueueErrors() throws Exception {
        AmqpStepContributor contributor = new AmqpStepContributor();
        AmqpClient client = mock(AmqpClient.class);
        setClient(contributor, client);
        doThrow(new WakamitiException("cannot declare")).when(client).declareQueue(anyString(), anyBoolean(), anyBoolean(), anyBoolean());

        Document document = new Document("{\"k\":1}");
        assertThrows(WakamitiException.class, () ->
                contributor.sendJSONFromString("SEND.Q", document));
    }

    @Test
    public void sendJsonFromStringShouldPropagateSendErrors() throws Exception {
        AmqpStepContributor contributor = new AmqpStepContributor();
        AmqpClient client = mock(AmqpClient.class);
        setClient(contributor, client);
        doThrow(new WakamitiException("send failed")).when(client)
                .sendText("SEND.Q", "{\"k\":1}", "application/json", true);

        Document document = new Document("{\"k\":1}");
        assertThrows(WakamitiException.class, () ->
                contributor.sendJSONFromString("SEND.Q", document));
    }

    @Test(expected = WakamitiException.class)
    public void sendJsonFromFileShouldFailWhenFileDoesNotExist() {
        AmqpStepContributor contributor = new AmqpStepContributor();

        contributor.sendJSONFromFile("FILE.Q", new File("target/missing-message.json"));
    }

    @Test
    public void purgeQueueStepShouldPropagateErrorsAndKeepRuntimeState() throws Exception {
        AmqpStepContributor contributor = new AmqpStepContributor();
        AmqpClient client = mock(AmqpClient.class);
        setClient(contributor, client);
        contributor.receivedMessages.put("PURGE.Q", new ArrayList<>(List.of("msg")));
        doThrow(new WakamitiException("purge failed")).when(client).purgeQueue("PURGE.Q");

        try {
            contributor.purgeQueueStep("PURGE.Q");
            fail("Expected WakamitiException");
        } catch (WakamitiException e) {
            assertTrue(e.getMessage().contains("purge failed"));
        }
        assertTrue(contributor.receivedMessages.containsKey("PURGE.Q"));
    }

    @Test
    public void cleanUpShouldRunRegisteredPurgeAndClearOperations() throws Exception {
        AmqpStepContributor contributor = new AmqpStepContributor();
        AmqpClient client = mock(AmqpClient.class);
        setClient(contributor, client);
        contributor.setCleanupQueue("Q1");
        contributor.setCleanupQueue("Q2");

        contributor.cleanUp();

        verify(client).purgeQueue("Q1");
        verify(client).purgeQueue("Q2");
        assertTrue(contributor.cleanUpOperations.isEmpty());
    }

    @Test
    public void releaseConnectionShouldClearBuffersAndCloseClient() throws Exception {
        AmqpStepContributor contributor = new AmqpStepContributor();
        AmqpClient client = mock(AmqpClient.class);
        setClient(contributor, client);
        contributor.receivedMessages.put("Q", new ArrayList<>(List.of("m")));

        contributor.releaseConnection();

        verify(client).close();
        assertNull(getClient(contributor));
        assertTrue(contributor.receivedMessages.isEmpty());
    }

    @Test
    public void checkNoMessageReceivedShouldFailWhenMessageAlreadyExists() {
        AmqpStepContributor contributor = new AmqpStepContributor();
        contributor.destination = "DEST";
        contributor.receivedMessages.put("DEST", new ArrayList<>(List.of("already-there")));

        Duration duration = Duration.ofSeconds(1);
        assertThrows(AssertionError.class, () ->
                contributor.checkNoMessageReceived(duration));
    }

    private static void setClient(AmqpStepContributor steps, AmqpClient client) throws Exception {
        Field field = AmqpSupport.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(steps, client);
    }

    private static AmqpClient getClient(AmqpStepContributor steps) throws Exception {
        Field field = AmqpSupport.class.getDeclaredField("client");
        field.setAccessible(true);
        return (AmqpClient) field.get(steps);
    }
}
