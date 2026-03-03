/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp.client;


import java.util.function.Consumer;


/**
 * Protocol-agnostic AMQP client contract used by step support code.
 * <p>
 * Implementations encapsulate transport/protocol details (JMS for AMQP 1.0,
 * RabbitMQ Java API for AMQP 0-9-1) so the rest of the plugin can stay focused
 * on test behavior.
 */
public interface AmqpClient extends AutoCloseable {

    /**
     * Declares a queue if required by the protocol/client.
     *
     * @param queueName queue name
     * @param durable durable flag
     * @param exclusive exclusive flag
     * @param autoDelete auto-delete flag
     */
    void declareQueue(
            String queueName,
            boolean durable,
            boolean exclusive,
            boolean autoDelete
    );

    /**
     * Sends a text payload to a destination queue.
     *
     * @param destination target queue
     * @param text payload
     * @param contentType logical content type (stored as property/header when supported)
     * @param persistent whether message should be persistent
     */
    void sendText(
            String destination,
            String text,
            String contentType,
            boolean persistent
    );

    /**
     * Subscribes asynchronously to a destination queue.
     *
     * @param destination queue name
     * @param listener callback invoked for each received message body
     */
    void subscribe(
            String destination,
            Consumer<String> listener
    );

    /**
     * Removes all pending messages from a queue.
     *
     * @param queueName queue to purge
     */
    void purgeQueue(
            String queueName
    );

    /**
     * Closes all resources owned by this client (connections/channels/consumers).
     */
    @Override
    void close();

}
