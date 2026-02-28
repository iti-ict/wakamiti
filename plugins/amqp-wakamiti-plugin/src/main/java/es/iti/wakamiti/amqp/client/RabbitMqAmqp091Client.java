/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp.client;


import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import es.iti.wakamiti.amqp.AmqpConnectionParams;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.concurrent.TimeoutException;


/**
 * AMQP 0-9-1 implementation based on RabbitMQ Java client.
 * <p>
 * It uses:
 * <ul>
 *   <li>one shared publish channel,</li>
 *   <li>one consume channel per subscribed queue.</li>
 * </ul>
 */
public class RabbitMqAmqp091Client implements AmqpClient {

    private static final Logger LOGGER = WakamitiLogger.forClass(RabbitMqAmqp091Client.class);

    private final ConnectionFactory connectionFactory = new ConnectionFactory();

    private Connection connection;
    private Channel publishChannel;
    private final Map<String, Channel> consumers = new HashMap<>();
    private final Map<String, QueueConfig> declaredQueues = new HashMap<>();

    /**
     * @param connectionParams connection info used to configure {@link ConnectionFactory}
     */
    public RabbitMqAmqp091Client(
            AmqpConnectionParams connectionParams
    ) {
        configureFactory(connectionParams);
    }

    /**
     * Declares the queue with explicit AMQP 0-9-1 flags.
     */
    @Override
    public synchronized void declareQueue(
            String queueName,
            boolean durable,
            boolean exclusive,
            boolean autoDelete
    ) {
        try {
            channel().queueDeclare(queueName, durable, exclusive, autoDelete, null);
            declaredQueues.put(queueName, new QueueConfig(durable, exclusive, autoDelete));
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Publishes a text message to the default exchange using queue name as routing key.
     */
    @Override
    public synchronized void sendText(
            String destination,
            String text,
            String contentType,
            boolean persistent
    ) {
        try {
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType(contentType)
                .deliveryMode(persistent ? 2 : 1)
                .build();
            channel().basicPublish("", destination, properties, text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Starts asynchronous consumption from a queue (auto-ack enabled).
     */
    @Override
    public synchronized void subscribe(
            String destination,
            Consumer<String> listener
    ) {
        if (consumers.containsKey(destination)) {
            return;
        }
        try {
            QueueConfig queueConfig = declaredQueues.get(destination);
            Channel consumeChannel = connection().createChannel();
            if (queueConfig != null) {
                consumeChannel.queueDeclare(
                    destination,
                    queueConfig.durable,
                    queueConfig.exclusive,
                    queueConfig.autoDelete,
                    null
                );
            }
            consumeChannel.basicConsume(
                destination,
                true,
                (consumerTag, delivery) -> listener.accept(new String(delivery.getBody(), StandardCharsets.UTF_8)),
                consumerTag -> {
                    // no-op
                }
            );
            consumers.put(destination, consumeChannel);
        } catch (IOException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Purges queue content and detaches local consume channel cache for that queue.
     */
    @Override
    public synchronized void purgeQueue(
            String queueName
    ) {
        try {
            Channel previousConsumer = consumers.remove(queueName);
            if (previousConsumer != null && previousConsumer.isOpen()) {
                previousConsumer.close();
            }
            channel().queuePurge(queueName);
        } catch (IOException | TimeoutException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Lazily creates and caches a channel for publish operations.
     */
    private Channel channel() throws IOException {
        if (publishChannel == null || !publishChannel.isOpen()) {
            publishChannel = connection().createChannel();
        }
        return publishChannel;
    }

    /**
     * Lazily creates and caches AMQP connection.
     */
    private Connection connection() {
        if (connection == null || !connection.isOpen()) {
            try {
                connection = connectionFactory.newConnection();
            } catch (IOException | TimeoutException e) {
                throw new WakamitiException("Error connecting to AMQP server: {}", e.getMessage(), e);
            }
        }
        return connection;
    }

    /**
     * Applies URI and credentials to {@link ConnectionFactory}.
     */
    private void configureFactory(
            AmqpConnectionParams connectionParams
    ) {
        if (connectionParams == null || connectionParams.uri() == null) {
            throw new WakamitiException("AMQP connection URL is not defined");
        }
        try {
            connectionFactory.setUri(connectionParams.uri());
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new WakamitiException("Invalid AMQP 0-9-1 connection URL: {}", connectionParams.uri(), e);
        }

        if (connectionParams.username() != null && !connectionParams.username().isBlank()) {
            connectionFactory.setUsername(connectionParams.username());
            connectionFactory.setPassword(connectionParams.password());
        }
    }

    /**
     * Closes all consumer channels, publish channel and connection.
     */
    @Override
    public synchronized void close() {
        try {
            for (Channel consumer : consumers.values()) {
                if (consumer.isOpen()) {
                    consumer.close();
                }
            }
            consumers.clear();
            if (publishChannel != null && publishChannel.isOpen()) {
                publishChannel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.warn("There were problems releasing the AMQP 0-9-1 client: {}", e.getMessage());
            LOGGER.debug(e.toString(), e);
        }
    }


    /**
     * Minimal immutable holder for queue declaration flags.
     */
    private static class QueueConfig {
        private final boolean durable;
        private final boolean exclusive;
        private final boolean autoDelete;

        private QueueConfig(boolean durable, boolean exclusive, boolean autoDelete) {
            this.durable = durable;
            this.exclusive = exclusive;
            this.autoDelete = autoDelete;
        }
    }
}
