/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp;


import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;

/**
 * Wakamiti config contributor for AMQP steps.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>declaring the set of supported config keys,</li>
 *   <li>providing safe defaults,</li>
 *   <li>mapping configuration values to runtime state in {@link AmqpStepContributor}.</li>
 * </ul>
 */
@Extension(
    provider =  "es.iti.wakamiti",
    name = "amqp-config",
    version = "2.6",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class AmqpConfigContributor implements ConfigContributor<AmqpStepContributor> {

    /** Protocol selection key ({@code AMQP_1_0} / {@code AMQP_0_9_1}). */
    public static final String AMQP_CONNECTION_PROTOCOL = "amqp.connection.protocol";
    /** Broker URL. */
    public static final String AMQP_CONNECTION_URL = "amqp.connection.url";
    /** Broker username. */
    public static final String AMQP_CONNECTION_USERNAME = "amqp.connection.username";
    /** Broker password. */
    public static final String AMQP_CONNECTION_PASSWORD = "amqp.connection.password";
    /** Message delivery mode flag (persistent vs non-persistent). */
    public static final String AMQP_MESSAGE_PERSISTENT = "amqp.message.persistent";
    /** Queue declaration durable flag. */
    public static final String AMQP_QUEUE_DURABLE = "amqp.queue.durable";
    /** Queue declaration exclusive flag. */
    public static final String AMQP_QUEUE_EXCLUSIVE = "amqp.queue.exclusive";
    /** Queue declaration auto-delete flag. */
    public static final String AMQP_QUEUE_AUTODELETE = "amqp.queue.autodelete";

    /**
     * Default values used when users omit explicit configuration.
     *
     * @return default AMQP configuration
     */
    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                AMQP_CONNECTION_PROTOCOL, AmqpProtocol.AMQP_1_0.name(),
                AMQP_MESSAGE_PERSISTENT, Boolean.TRUE.toString(),
                AMQP_QUEUE_DURABLE,  Boolean.FALSE.toString(),
                AMQP_QUEUE_EXCLUSIVE,  Boolean.FALSE.toString(),
                AMQP_QUEUE_AUTODELETE, Boolean.FALSE.toString()
        );
    }

    /**
     * @return function that applies configuration into the step contributor
     */
    @Override
    public Configurer<AmqpStepContributor> configurer() {
        return this::configure;
    }

    /**
     * Maps raw configuration values into the contributor runtime state.
     *
     * @param contributor step contributor instance created by Wakamiti
     * @param configuration resolved configuration object
     */
    private void configure(AmqpStepContributor contributor, Configuration configuration) {
        configuration.get(AMQP_CONNECTION_URL, String.class).ifPresent(url ->
            contributor.defineConnectionParameters(
                    url,
                    configuration.get(AMQP_CONNECTION_USERNAME, String.class).orElse(null),
                    configuration.get(AMQP_CONNECTION_PASSWORD, String.class).orElse(null)
            )
        );

        configuration.get(AMQP_CONNECTION_PROTOCOL, String.class).ifPresent(contributor::defineProtocol);

        configuration.get(AMQP_QUEUE_DURABLE, Boolean.class).ifPresent(contributor::setDurable);
        configuration.get(AMQP_QUEUE_EXCLUSIVE, Boolean.class).ifPresent(contributor::setExclusive);
        configuration.get(AMQP_QUEUE_AUTODELETE, Boolean.class).ifPresent(contributor::setAutoDelete);
        configuration.get(AMQP_MESSAGE_PERSISTENT, Boolean.class).ifPresent(contributor::setMessagePersistent);
    }

}
