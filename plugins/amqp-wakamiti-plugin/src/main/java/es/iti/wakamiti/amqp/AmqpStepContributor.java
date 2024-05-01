/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.amqp;

import com.rabbitmq.client.*;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.awaitility.Durations;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;
import static org.awaitility.Awaitility.await;


@Extension(provider = "es.iti.wakamiti", name = "amqp-steps", version = "2.6")
@I18nResource("iti_wakamiti_wakamiti-amqp")
public class AmqpStepContributor implements StepContributor {

    private final Logger logger = WakamitiLogger.forClass(AmqpStepContributor.class);

    private static final String FORMAT = "[d' days 'H' hours 'm' minutes 's' seconds']";

    private AmqpConnectionParams connectionParams;
    private Connection connection;
    private Channel channel;
    private final Map<String, List<String>> receivedMessages = new HashMap<>();
    private String destination;

    private boolean durable;
    private boolean exclusive;
    private boolean autoDelete;


    public void setConnectionParams(AmqpConnectionParams connectionParams) {
        this.connectionParams = connectionParams;
    }


    protected Channel channel() {
        if (this.channel == null) {
            try {
                var connectionFactory = new ConnectionFactory();
                connectionFactory.setUri(connectionParams.host());
                connectionFactory.setUsername(connectionParams.username());
                connectionFactory.setPassword(connectionParams.password());
                this.connection = connectionFactory.newConnection();
                this.channel = connection.createChannel();
            } catch (URISyntaxException | GeneralSecurityException | IOException | TimeoutException e) {
                throw new WakamitiException("Error connecting to AMQP server: {}", e.getMessage(), e);
            }
        }
        return channel;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }


    @TearDown
    public void releaseConnection() {
        try {
            if (this.channel != null) {
                this.channel.close();
            }
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (IOException | TimeoutException e) {
            logger.warn("There were problems releasing the connection: {}", e.getMessage());
            logger.debug(e.toString(), e);
        }
    }


    void sendJsonMessageToQueue(String queueName, String text) {
        sendTextMessageToQueue(queueName, text, "application/json");
    }


    void sendTextMessageToQueue(String queueName, String text, String contentType) {
        try {
            declareQueue(queueName);
            var bytes = text.getBytes(StandardCharsets.UTF_8);
            AMQP.BasicProperties props = new AMQP.BasicProperties(
                    contentType,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            channel().basicPublish("", queueName, props, bytes);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }


    void consumeQueue(String queueName) {
        try {
            declareQueue(queueName);
            boolean ack = false;
            Consumer callback = new DefaultConsumer(channel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    receivedMessages
                            .computeIfAbsent(queueName, x -> new ArrayList<>())
                            .add(new String(body, StandardCharsets.UTF_8))
                    ;
                    channel().basicAck(envelope.getDeliveryTag(), true);
                }
            };
            channel().basicConsume(queueName, ack, callback);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }


    private boolean messageExistsInReceived(String message) {
        Objects.requireNonNull(destination, "Destination queue is not defined");
        return receivedMessages
                .computeIfAbsent(destination, x -> new ArrayList<>())
                .stream()
                .anyMatch(receivedMessage -> receivedMessage.equals(message));
    }


    private void checkMessageExistsInReceived(String message, Duration duration) {
        try {
            await()
                    .atMost(duration)
                    .pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS)
                    .until(() -> messageExistsInReceived(message));
        } catch (ConditionTimeoutException e) {
            throw new AssertionError("Message not received in " + formatDuration(duration.toMillis(), FORMAT));
        }
    }


    private void declareQueue(String queueName) throws IOException {
        Map<String, Object> arguments = Map.of();
        channel().queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
    }


    private String readFile(File file) {
        return WakamitiAPI.instance().resourceLoader().readFileAsString(file);
    }


    @Step(value = "amqp.define.connection.parameters", args = {"url:text", "username:text", "password:text"})
    public void defineConnectionParameters(String url, String username, String password) {
        this.connectionParams = new AmqpConnectionParams(url, username, password);
    }


    @Step(value = "amqp.define.destination.queue", args = {"word"})
    public void defineDestinationQueue(String queueName) {
        this.destination = queueName;
        consumeQueue(queueName);
    }


    @Step(value = "amqp.send.json.from.string", args = {"word"})
    public void sendJSONFromString(String queueName, Document document) {
        sendJsonMessageToQueue(queueName, document.getContent());
    }


    @Step(value = "amqp.send.json.from.file", args = {"queue:word", "file:file"})
    public void sendJSONFromFile(String queueName, File file) {
        sendJsonMessageToQueue(queueName, readFile(file));
    }


    @Step("amqp.send.await")
    public void awaitFor(Duration duration) {
        await().timeout(duration.plusSeconds(1))
                .pollDelay(duration).until(() -> true);
    }


    @Step(value = "amqp.check.received.json.from.string", args = {"duration:duration"})
    public void checkReceivedJSONFromString(Duration duration, Document json) {
        checkMessageExistsInReceived(json.getContent(), duration);
    }


    @Step(value = "amqp.check.received.json.from.file", args = {"duration:duration", "file:file"})
    public void checkReceivedJSONFromString(Duration duration, File file) {
        checkMessageExistsInReceived(readFile(file), duration);
    }


}