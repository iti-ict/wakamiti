/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.spring.rest;


import java.net.MalformedURLException;
import java.net.URL;

import es.iti.wakamiti.spring.db.SpringConnectionProvider;
import imconfig.Configuration;
import imconfig.ConfigurationException;
import imconfig.Configurer;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.rest.RestStepContributor;



@Extension(
    provider =  "es.iti.wakamiti",
    name = "rest-configurator-springboot",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor",
    externallyManaged = true,
    priority = Integer.MAX_VALUE // to ensure it will executed last
)
@Component
@ConditionalOnProperty(SpringLocalPortConfigurer.USE_SPRING_LOCAL_SERVER_PORT)
public class SpringLocalPortConfigurer implements ConfigContributor<RestStepContributor> {

    private static final Logger LOGGER = WakamitiLogger.forClass(SpringLocalPortConfigurer.class);
    public static final String USE_SPRING_LOCAL_SERVER_PORT = "wakamiti.rest.useSpringLocalServerPort";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
        USE_SPRING_LOCAL_SERVER_PORT, "false",
        SpringConnectionProvider.USE_SPRING_DATASOURCE, "false"
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