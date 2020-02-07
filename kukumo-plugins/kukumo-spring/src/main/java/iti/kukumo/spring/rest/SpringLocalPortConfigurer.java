/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.spring.rest;


import static iti.kukumo.spring.db.SpringConnectionProvider.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.commons.configurer.Configurer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.rest.RestStepContributor;
import iti.kukumo.util.KukumoLogger;


@Extension(
    provider = "kukumo",
    name = "rest-configurator-springboot",
    extensionPoint = "iti.kukumo.api.extensions.ConfigContributor",
    externallyManaged = true,
    priority = Integer.MAX_VALUE // to ensure it will executed last
)
@Component
@ConditionalOnProperty(SpringLocalPortConfigurer.USE_SPRING_LOCAL_SERVER_PORT)
public class SpringLocalPortConfigurer implements ConfigContributor<RestStepContributor> {

    private static final Logger LOGGER = KukumoLogger.forClass(SpringLocalPortConfigurer.class);
    public static final String USE_SPRING_LOCAL_SERVER_PORT = "kukumo.rest.useSpringLocalServerPort";

    private static final Configuration DEFAULTS = Configuration.fromPairs(
        USE_SPRING_LOCAL_SERVER_PORT, "false",
        USE_SPRING_DATASOURCE, "false"
    );

    @Autowired
    private Environment environment;

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    @Override
    public boolean accepts(Object contributor) {
        return RestStepContributor.class.isAssignableFrom(contributor.getClass());
    }

    @Override
    public Configurer<RestStepContributor> configurer() {
        return this::configure;
    }

    private void configure(RestStepContributor contributor, Configuration configuration) {
        try {
            String localServerPort = environment.getProperty("local.server.port");
            URL baseURL = new URL("http://localhost:" + localServerPort);
            contributor.setBaseURL(baseURL);
            LOGGER.debug("Using Spring local server URL: {uri}", baseURL);
        } catch (MalformedURLException e) {
            throw new ConfigurationException(e);
        }
    }

}
