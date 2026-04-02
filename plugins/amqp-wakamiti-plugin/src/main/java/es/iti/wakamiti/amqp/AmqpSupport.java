/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp;


import es.iti.wakamiti.amqp.client.AmqpClient;
import es.iti.wakamiti.amqp.client.QpidAmqp10Client;
import es.iti.wakamiti.amqp.client.RabbitMqAmqp091Client;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.awaitility.Durations;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;

import java.io.File;
import java.time.Duration;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;
import static org.awaitility.Awaitility.await;


/**
 * Shared internal support for AMQP step execution.
 * <p>
 * This class centralizes:
 * <ul>
 *   <li>client lifecycle,</li>
 *   <li>queue declaration and consumption helpers,</li>
 *   <li>received message buffering used by assertions,</li>
 *   <li>common polling/assertion utilities.</li>
 * </ul>
 * <p>
 * Keeping this logic here avoids duplicating it across step methods.
 */
public class AmqpSupport {

    private static final Logger LOGGER = WakamitiLogger.forClass(AmqpSupport.class);
    private static final String FORMAT = "[d' days 'H' hours 'm' minutes 's' seconds']";
    private static final String CONTENT_TYPE = "application/json";
    private static final String DESTINATION_QUEUE_NOT_DEFINED = "Destination queue is not defined";
    protected final Deque<Runnable> cleanUpOperations = new LinkedList<>();
    protected final Map<String, List<String>> receivedMessages = new ConcurrentHashMap<>();
    protected AmqpConnectionParams connectionParams;
    protected AmqpProtocol protocol = AmqpProtocol.AMQP_1_0;
    protected String destination;
    private final AmqpJsonDiff jsonDiff = new AmqpJsonDiff();
    private AmqpClient client;
    private boolean durable;
    private boolean exclusive;
    private boolean autoDelete;
    private boolean messagePersistent = true;


    /**
     * Configures queue declaration durable flag.
     */
    public void setDurable(
            boolean durable
    ) {
        this.durable = durable;
    }

    /**
     * Configures queue declaration exclusive flag.
     */
    public void setExclusive(
            boolean exclusive
    ) {
        this.exclusive = exclusive;
    }

    /**
     * Configures queue declaration auto-delete flag.
     */
    public void setAutoDelete(
            boolean autoDelete
    ) {
        this.autoDelete = autoDelete;
    }

    /**
     * Configures message persistence mode for sends.
     */
    public void setMessagePersistent(
            boolean messagePersistent
    ) {
        this.messagePersistent = messagePersistent;
    }

    /**
     * Declares queue according to configured declaration flags.
     */
    protected void declareQueue(
            String queue
    ) {
        client().declareQueue(queue, durable, exclusive, autoDelete);
    }

    /**
     * Sends JSON text payload to queue using configured persistence mode.
     */
    protected void sendTextMessageToQueue(
            String queue,
            String text
    ) {
        declareQueue(queue);
        client().sendText(queue, text, CONTENT_TYPE, messagePersistent);
    }


    /**
     * Lazily creates protocol-specific client.
     */
    protected AmqpClient client() {
        if (this.client == null) {
            if (connectionParams == null || connectionParams.uri() == null) {
                throw new WakamitiException("AMQP connection URL is not defined");
            }
            switch (protocol) {
                case AMQP_1_0:
                    this.client = new QpidAmqp10Client(connectionParams);
                    break;
                case AMQP_0_9_1:
                    this.client = new RabbitMqAmqp091Client(connectionParams);
                    break;
                default:
                    throw new WakamitiException("Unsupported AMQP protocol {}", protocol);
            }
        }
        return client;
    }

    /**
     * Closes current client and resets cache.
     */
    protected void closeClient() {
        if (this.client != null) {
            this.client.close();
            this.client = null;
        }
    }

    /**
     * Formats durations for assertion error messages.
     */
    protected String format(
            Duration duration
    ) {
        return formatDuration(duration.toMillis(), FORMAT);
    }

    /**
     * Reads full file content from Wakamiti resource loader.
     */
    protected String readFile(
            File file
    ) {
        return WakamitiAPI.instance().resourceLoader().readFileAsString(file);
    }

    /**
     * Validates that file exists before using it in steps.
     */
    protected void assertFileExists(
            File file
    ) {
        if (!file.exists()) {
            throw new WakamitiException("File '{}' not found", file.getAbsolutePath());
        }
    }


    /**
     * Starts asynchronous consumption on a queue if not already subscribed.
     */
    protected void consumeQueue(
            String queueName
    ) {
        declareQueue(queueName);
            client().subscribe(
                    queueName,
                    message -> {
                        receivedMessages.computeIfAbsent(queueName, x -> new CopyOnWriteArrayList<>()).add(message);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Received AMQP message in queue '{}': {}", queueName, message);
                        }
                    }
            );
    }

    /**
     * Purges queue and resets local cached state for that queue.
     */
    protected void purgeQueue(
            String queueName
    ) {
        declareQueue(queueName);
        client().purgeQueue(queueName);
        receivedMessages.remove(queueName);
    }


    /**
     * Checks if an exact message exists in current destination queue buffer.
     */
    protected boolean messageExistsInReceived(
            String message
    ) {
        Objects.requireNonNull(destination, DESTINATION_QUEUE_NOT_DEFINED);
        return receivedMessages.computeIfAbsent(destination, x -> new CopyOnWriteArrayList<>()).stream().anyMatch(
                receivedMessage -> receivedMessage.equals(message));
    }

    /**
     * Checks if at least one received message matches expected JSON using mode.
     */
    protected boolean messageExistsInReceived(
            String message,
            MatchMode matchMode
    ) {
        Objects.requireNonNull(destination, DESTINATION_QUEUE_NOT_DEFINED);
        return receivedMessages.computeIfAbsent(destination, x -> new CopyOnWriteArrayList<>()).stream().anyMatch(
                receivedMessage -> jsonDiff.matches(message, receivedMessage, matchMode));
    }

    /**
     * Checks if at least one message arrived for current destination queue.
     */
    protected boolean anyMessageReceivedInDestination() {
        Objects.requireNonNull(destination, DESTINATION_QUEUE_NOT_DEFINED);
        return !receivedMessages.computeIfAbsent(destination, x -> new CopyOnWriteArrayList<>()).isEmpty();
    }


    /**
     * Waits until expected message appears or throws assertion timeout error.
     */
    protected void checkMessageExistsInReceived(
            String message,
            Duration duration
    ) {
        try {
            await().atMost(duration).pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS).until(
                    () -> messageExistsInReceived(message));
        } catch (ConditionTimeoutException e) {
            logBufferedMessagesAtDebug();
            throw new AssertionError("Message not received in " + format(duration));
        }
    }

    /**
     * Waits until expected message appears according to JSON match mode.
     */
    protected void checkMessageExistsInReceived(
            String message,
            Duration duration,
            MatchMode matchMode
    ) {
        jsonDiff.assertValidExpected(message);
        try {
            await().atMost(duration).pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS).until(
                    () -> messageExistsInReceived(message, matchMode));
        } catch (ConditionTimeoutException e) {
            logBufferedMessagesAtDebug();
            throw new AssertionError("Message not received in " + format(duration) + " using " + matchMode + " mode");
        }
    }

    /**
     * Logs buffered messages for current destination queue to help debug assertion failures.
     */
    protected void logBufferedMessagesAtDebug() {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        if (destination == null) {
            LOGGER.debug("No destination queue configured. No buffered AMQP messages to log");
            return;
        }
        List<String> buffered = receivedMessages.computeIfAbsent(destination, x -> new CopyOnWriteArrayList<>());
        if (buffered.isEmpty()) {
            LOGGER.debug("No buffered AMQP messages found in queue '{}'", destination);
            return;
        }
        LOGGER.debug("Buffered AMQP messages in queue '{}': {}", destination, buffered.size());
        for (int i = 0; i < buffered.size(); i++) {
            LOGGER.debug("AMQP buffered message [{}] in queue '{}': {}", i + 1, destination, buffered.get(i));
        }
    }

    /**
     * Asserts that no message is received during the full timeout window.
     */
    protected void checkNoMessageInReceived(
            Duration duration
    ) {
        try {
            await().atMost(duration).pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS).until(
                    this::anyMessageReceivedInDestination);
            throw new AssertionError("Unexpected message received in " + format(duration));
        } catch (ConditionTimeoutException e) {
            // Expected path: no message was received during timeout.
        }
    }
}
