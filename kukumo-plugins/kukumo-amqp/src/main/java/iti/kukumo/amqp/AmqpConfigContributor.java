/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.amqp;


import iti.commons.configurer.Configuration;
import iti.commons.configurer.Configurer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ConfigContributor;


@Extension(
    provider = "kukumo",
    name = "kukumo-amqp-config",
    version = "1.0",
    extensionPoint = "iti.kukumo.api.extensions.ConfigContributor"
)
public class AmqpConfigContributor implements ConfigContributor<AmqpStepContributor> {

    public static final String AMQP_CONNECTION_URL = "amqp.connection.url";
    public static final String AMQP_CONNECTION_USERNAME = "amqp.connection.username";
    public static final String AMQP_CONNECTION_PASSWORD = "amqp.connection.password";

    public static final String AMQP_QUEUE_DURABLE = "amqp.queue.durable";
    public static final String AMQP_QUEUE_EXCLUSIVE = "amqp.queue.exclusive";
    public static final String AMQP_QUEUE_AUTODELETE = "amqp.queue.autodelete";

    private static final Configuration DEFAULTS = Configuration.fromPairs(
            AMQP_QUEUE_DURABLE,  "false",
            AMQP_QUEUE_EXCLUSIVE,  "false",
            AMQP_QUEUE_AUTODELETE, "false"
    );


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof AmqpStepContributor;
    }

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
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

        configuration
                .ifPresent(AMQP_QUEUE_DURABLE, Boolean.class, contributor::setDurable)
                .ifPresent(AMQP_QUEUE_EXCLUSIVE, Boolean.class, contributor::setExclusive)
                .ifPresent(AMQP_QUEUE_AUTODELETE, Boolean.class, contributor::setAutoDelete);
    }

}