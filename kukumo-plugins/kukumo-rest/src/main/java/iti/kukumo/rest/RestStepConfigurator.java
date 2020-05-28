/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import java.net.URL;

import iti.commons.configurer.ConfigurationConsumer;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.extensions.Configurator;



@Extension(provider = "iti.kukumo", name = "rest-configurator", extensionPoint = "iti.kukumo.api.extensions.Configurator")
public class RestStepConfigurator implements Configurator<RestStepContributor> {

    @Override
    public boolean accepts(Object contributor) {
        return RestStepContributor.class.isAssignableFrom(contributor.getClass());
    }


    @Override
    public void configure(RestStepContributor contributor, Configuration configuration) {

        try {
            Logger logger = LoggerFactory.getLogger("iti.kukumo.rest");
            RestAssured.config = RestAssured.config().logConfig(
                new LogConfig().defaultStream(new RestAssuredLogger(logger).getPrintStream())
            );
            RestAssured.useRelaxedHTTPSValidation();

            contributor.setBaseURL(
                new URL(
                    configuration.get(
                        RestConfiguration.BASE_URL,
                        String.class
                    ).orElse(
                        RestConfiguration.DefaultValues.BASE_URL
                    )
                )
            );
            contributor.setContentType(
                configuration.get(
                    RestConfiguration.CONTENT_TYPE,
                    String.class
                ).orElse(
                    RestConfiguration.DefaultValues.CONTENT_TYPE
                )
            );

            contributor.setFailureHttpCodeAssertion(
                Matchers.lessThan(
                    configuration.get(
                        RestConfiguration.FAILURE_HTTP_CODE_THRESHOLD,
                        Integer.class
                    ).orElse(
                        RestConfiguration.DefaultValues.FAILURE_HTTP_CODE_THRESHOLD
                    )
                )
            );

        } catch (Exception e) {
            throw new KukumoException(e);
        }
    }

}
