/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.amqp.client;


import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;

import es.iti.wakamiti.amqp.AmqpConnectionParams;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;


/**
 * AMQP 1.0 client implementation backed by Apache Qpid JMS.
 * <p>
 * Notes for maintainers:
 * <ul>
 *   <li>Queue declaration flags are not fully enforceable via JMS API.</li>
 *   <li>Message bodies are normalized to {@link String} for step assertions.</li>
 *   <li>Connection/session are lazily initialized on first use.</li>
 * </ul>
 */
public class QpidAmqp10Client implements AmqpClient {

    private static final int BYTE_BUFFER_SIZE = 4_096;
    private static final Logger LOGGER = WakamitiLogger.forClass(QpidAmqp10Client.class);

    private final AmqpConnectionParams connectionParams;
    private final Map<String, MessageConsumer> consumers = new HashMap<>();
    private Connection connection;
    private Session session;
    private boolean warnedUnsupportedQueueFlags;

    /**
     * @param connectionParams connection info used to build the JMS connection
     */
    public QpidAmqp10Client(
            AmqpConnectionParams connectionParams
    ) {
        this.connectionParams = connectionParams;
    }

    /**
     * Declares a queue through JMS abstraction.
     * <p>
     * Durable/exclusive/autoDelete are accepted for API compatibility but are broker/JMS specific
     * and may not be fully enforced by the provider.
     */
    @Override
    public synchronized void declareQueue(
            String queueName,
            boolean durable,
            boolean exclusive,
            boolean autoDelete
    ) {
        try {
            if (!warnedUnsupportedQueueFlags && (durable || exclusive || autoDelete)) {
                warnedUnsupportedQueueFlags = true;
                LOGGER.warn("Queue flags durable/exclusive/autoDelete are not enforced by Qpid JMS client");
            }
            session().createQueue(queueName);
        } catch (JMSException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Sends a text message to a queue.
     */
    @Override
    public synchronized void sendText(
            String destination,
            String text,
            String contentType,
            boolean persistent
    ) {
        try {
            var queue = session().createQueue(destination);
            MessageProducer producer = session().createProducer(queue);
            producer.setDeliveryMode(persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
            TextMessage message = session().createTextMessage(text);
            message.setStringProperty("content_type", contentType);
            producer.send(message);
            producer.close();
        } catch (JMSException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Registers an asynchronous JMS listener for a queue.
     */
    @Override
    public synchronized void subscribe(
            String destination,
            Consumer<String> listener
    ) {
        try {
            if (consumers.containsKey(destination)) {
                return;
            }
            var queue = session().createQueue(destination);
            MessageConsumer consumer = session().createConsumer(queue);
            consumer.setMessageListener(message -> listener.accept(extractBody(message)));
            consumers.put(destination, consumer);
        } catch (JMSException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Empties a queue by draining available messages with {@code receiveNoWait()}.
     */
    @Override
    public synchronized void purgeQueue(
            String queueName
    ) {
        try {
            MessageConsumer previousConsumer = consumers.remove(queueName);
            if (previousConsumer != null) {
                previousConsumer.close();
            }
            var queue = session().createQueue(queueName);
            MessageConsumer purgeConsumer = session().createConsumer(queue);
            while (purgeConsumer.receiveNoWait() != null) {
                // Keep consuming until queue is empty.
            }
            purgeConsumer.close();
        } catch (JMSException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Lazily builds and starts JMS connection/session.
     */
    private Session session() throws JMSException {
        if (this.session == null) {
            if (connectionParams == null || connectionParams.uri() == null) {
                throw new WakamitiException("AMQP connection URL is not defined");
            }
            JmsConnectionFactory connectionFactory = new JmsConnectionFactory(connectionParams.uri());
            if (connectionParams.username() != null && !connectionParams.username().isBlank()) {
                this.connection = connectionFactory.createConnection(
                        connectionParams.username(), connectionParams.password());
            } else {
                this.connection = connectionFactory.createConnection();
            }
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        }
        return session;
    }

    /**
     * Converts JMS message to string body for assertions.
     */
    private String extractBody(
            Message message
    ) {
        try {
            if (message instanceof TextMessage) {
                return ((TextMessage) message).getText();
            }
            if (message instanceof BytesMessage) {
                return new String(readBytes((BytesMessage) message), StandardCharsets.UTF_8);
            }
            return message.getBody(String.class);
        } catch (JMSException e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Utility to fully read a JMS {@link BytesMessage}.
     */
    private byte[] readBytes(
            BytesMessage bytesMessage
    ) throws JMSException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[BYTE_BUFFER_SIZE];
        int read = bytesMessage.readBytes(buffer);
        while (read != -1) {
            output.write(buffer, 0, read);
            read = bytesMessage.readBytes(buffer);
        }
        return output.toByteArray();
    }

    /**
     * Closes consumers, session and connection in that order.
     */
    @Override
    public synchronized void close() {
        try {
            for (MessageConsumer consumer : consumers.values()) {
                consumer.close();
            }
            consumers.clear();
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            LOGGER.warn("There were problems releasing the AMQP 1.0 client: {}", e.getMessage());
            LOGGER.debug(e.toString(), e);
        }
    }

}
