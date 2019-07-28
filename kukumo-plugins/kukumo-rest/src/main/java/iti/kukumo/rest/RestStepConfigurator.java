package iti.kukumo.rest;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.Configurator;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * @author ITI
 *         Created by ITI on 12/03/19
 */
@Extension(
    provider = "iti.kukumo", 
    name ="rest-configurator", 
    extensionPoint = "iti.kukumo.api.extensions.Configurator"
)
public class RestStepConfigurator implements Configurator<RestStepContributor> {

    @Override
    public boolean accepts(Object contributor) {
        return RestStepContributor.class.isAssignableFrom(contributor.getClass());
    }

    @Override
    public void configure(RestStepContributor contributor, Configuration configuration) {

        try {
            Logger logger = LoggerFactory.getLogger("iti.kukumo.rest");
            RestAssured.config().logConfig(new LogConfig().defaultStream(new RestAssuredLogger(logger).getPrintStream()));


            contributor.setBaseURL(new URL(configuration.getString(
                    RestConfiguration.BASE_URL).orElse(
                    RestConfiguration.DefaultValues.BASE_URL)
            ));
            contributor.setContentType(configuration.getString(
                    RestConfiguration.CONTENT_TYPE).orElse(
                    RestConfiguration.DefaultValues.CONTENT_TYPE)
            );

            contributor.setFailureHttpCodeAssertion(Matchers.lessThan(configuration.getInteger(
                    RestConfiguration.FAILURE_HTTP_CODE_THRESHOLD).orElse(
                    RestConfiguration.DefaultValues.FAILURE_HTTP_CODE_THRESHOLD)
            ));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
