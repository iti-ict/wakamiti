/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.consum.amqp;


import iti.commons.configurer.Configuration;
import iti.commons.configurer.Configurer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ConfigContributor;


@Extension(
    provider = "kukumo",
    name = "kukumo-consum-amqp-config",
    version = "1.0",
    extensionPoint = "iti.kukumo.api.extensions.ConfigContributor"
)
public class AmqpConfigContributor implements ConfigContributor<AmqpStepContributor> {

    public static final String AMQP_CONNECTION_URL = "amqp.connection.url";
    public static final String AMQP_CONNECTION_USERNAME = "amqp.connection.username";
    public static final String AMQP_CONNECTION_PASSWORD = "amqp.connection.password";


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof AmqpStepContributor;
    }

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.empty();
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
    }

}