/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.amqp;



import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;


@Extension(
    provider =  "es.iti.wakamiti",
    name = "amqp-config",
    version = "2.5",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class AmqpConfigContributor implements ConfigContributor<AmqpStepContributor> {

    public static final String AMQP_CONNECTION_URL = "amqp.connection.url";
    public static final String AMQP_CONNECTION_USERNAME = "amqp.connection.username";
    public static final String AMQP_CONNECTION_PASSWORD = "amqp.connection.password";
    public static final String AMQP_QUEUE_DURABLE = "amqp.queue.durable";
    public static final String AMQP_QUEUE_EXCLUSIVE = "amqp.queue.exclusive";
    public static final String AMQP_QUEUE_AUTODELETE = "amqp.queue.autodelete";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                AMQP_QUEUE_DURABLE,  Boolean.FALSE.toString(),
                AMQP_QUEUE_EXCLUSIVE,  Boolean.FALSE.toString(),
                AMQP_QUEUE_AUTODELETE, Boolean.FALSE.toString()
        );
    }

    @Override
    public Configurer<AmqpStepContributor> configurer() {
        return this::configure;
    }

    private void configure(AmqpStepContributor contributor, Configuration configuration) {
        contributor.setConnectionParams(new AmqpConnectionParams(
            configuration.get(AMQP_CONNECTION_URL, String.class).orElse(null),
            configuration.get(AMQP_CONNECTION_USERNAME, String.class).orElse(null),
            configuration.get(AMQP_CONNECTION_PASSWORD, String.class).orElse(null)
        ));

        configuration.get(AMQP_QUEUE_DURABLE, Boolean.class).ifPresent(contributor::setDurable);
        configuration.get(AMQP_QUEUE_EXCLUSIVE, Boolean.class).ifPresent(contributor::setExclusive);
        configuration.get(AMQP_QUEUE_AUTODELETE, Boolean.class).ifPresent(contributor::setAutoDelete);
    }

}