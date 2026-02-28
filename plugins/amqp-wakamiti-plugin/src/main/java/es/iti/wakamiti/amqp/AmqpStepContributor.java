/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.Document;

import java.io.File;
import java.net.URI;
import java.time.Duration;

import static org.awaitility.Awaitility.await;


/**
 * Wakamiti step contributor that exposes AMQP steps to feature files.
 * <p>
 * This class is the public API layer of the plugin:
 * <ul>
 *   <li>step annotations map Gherkin sentences to Java methods,</li>
 *   <li>each method delegates to reusable logic in {@link AmqpSupport},</li>
 *   <li>teardown methods guarantee cleanup and connection release.</li>
 * </ul>
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "amqp-steps",
        version = "2.6"
)
@I18nResource("iti_wakamiti_wakamiti-amqp")
public class AmqpStepContributor extends AmqpSupport implements StepContributor {

    private static final AmqpProtocol DEFAULT_PROTOCOL = AmqpProtocol.AMQP_1_0;


    /**
     * Executes deferred cleanup operations registered during scenario execution.
     */
    @TearDown(order = 1)
    public void cleanUp() {
        cleanUpOperations.forEach(Runnable::run);
        cleanUpOperations.clear();
    }

    /**
     * Releases runtime resources used by this contributor instance.
     */
    @TearDown(order = 2)
    public void releaseConnection() {
        receivedMessages.clear();
        closeClient();
    }


    /**
     * Registers a queue purge operation that will run at teardown.
     *
     * @param queue queue to purge after scenario ends
     */
    @Step(
            value = "amqp.define.cleanup.purge.queue",
            args = { "word" }
    )
    public void setCleanupQueue(
            String queue
    ) {
        cleanUpOperations.add(() -> purgeQueue(queue));
    }


    /**
     * Defines broker URL and credentials.
     *
     * @param url broker URI
     * @param username broker username (optional)
     * @param password broker password (optional)
     */
    @Step(
            value = "amqp.define.connection.parameters",
            args = { "url:text", "username:text", "password:text" }
    )
    public void defineConnectionParameters(
            String url,
            String username,
            String password
    ) {
        this.connectionParams = new AmqpConnectionParams(
                URI.create(url),
                username,
                password
        );
        closeClient();
    }


    /**
     * Defines protocol to use for future operations.
     *
     * @param protocol protocol name or alias
     */
    @Step(
            value = "amqp.define.connection.protocol",
            args = { "protocol:word" }
    )
    public void defineProtocol(
        String protocol
    ) {
        try {
            this.protocol = AmqpProtocol.parseOrDefault(protocol, DEFAULT_PROTOCOL);
        } catch (RuntimeException e) {
            throw new WakamitiException("Unsupported AMQP protocol '{}'", protocol, e);
        }
        closeClient();
    }


    /**
     * Sets destination queue used by assertion steps and subscribes to it.
     *
     * @param queueName queue to observe
     */
    @Step(
            value = "amqp.define.destination.queue",
            args = { "word" }
    )
    public void defineDestinationQueue(
            String queueName
    ) {
        this.destination = queueName;
        consumeQueue(queueName);
    }


    /**
     * Sends inline JSON document content to queue.
     *
     * @param queueName target queue
     * @param document inline JSON document
     */
    @Step(
            value = "amqp.send.json.from.string",
            args = { "word" }
    )
    public void sendJSONFromString(
            String queueName,
            Document document
    ) {
        sendTextMessageToQueue(queueName, document.getContent());
    }


    /**
     * Sends JSON content loaded from file.
     *
     * @param queueName target queue
     * @param file JSON file
     */
    @Step(
            value = "amqp.send.json.from.file",
            args = { "queue:word", "file:file" }
    )
    public void sendJSONFromFile(
            String queueName,
            File file
    ) {
        assertFileExists(file);
        sendTextMessageToQueue(queueName, readFile(file));
    }

    /**
     * Purges all pending messages from queue.
     *
     * @param queueName queue to purge
     */
    @Step(
            value = "amqp.purge.queue",
            args = { "word" }
    )
    public void purgeQueueStep(
            String queueName
    ) {
        purgeQueue(queueName);
    }


    /**
     * Adds a deterministic delay in a scenario.
     *
     * @param duration amount of time to wait
     */
    @Step("amqp.send.await")
    public void awaitFor(
            Duration duration
    ) {
        await().timeout(duration.plusSeconds(1))
                .pollDelay(duration).until(() -> true);
    }


    /**
     * Validates that a JSON message appears within timeout.
     *
     * @param duration timeout window
     * @param json expected JSON payload
     */
    @Step(
            value = "amqp.check.received.json.from.string",
            args = { "duration:duration" }
    )
    public void checkReceivedJSONFromString(
            Duration duration,
            Document json
    ) {
        checkMessageExistsInReceived(json.getContent(), duration);
    }


    /**
     * Validates that expected JSON from file appears within timeout.
     *
     * @param duration timeout window
     * @param file file containing expected JSON payload
     */
    @Step(
            value = "amqp.check.received.json.from.file",
            args = { "duration:duration", "file:file" }
    )
    public void checkReceivedJSONFromFile(
            Duration duration,
            File file
    ) {
        assertFileExists(file);
        checkMessageExistsInReceived(readFile(file), duration);
    }

    /**
     * Validates that no message is received during the timeout window.
     *
     * @param duration timeout window
     */
    @Step(
            value = "amqp.check.received.none",
            args = { "duration:duration" }
    )
    public void checkNoMessageReceived(
            Duration duration
    ) {
        checkNoMessageInReceived(duration);
    }

}
