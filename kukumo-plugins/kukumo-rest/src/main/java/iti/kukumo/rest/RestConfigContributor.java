/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import java.net.URL;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import iti.kukumo.rest.log.RestAssuredLogger;
import org.hamcrest.Matchers;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.Configurer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.util.ThrowableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Extension(
    provider = "kukumo",
    name = "rest-configurator",
    extensionPoint = "iti.kukumo.api.extensions.ConfigContributor"
)
public class RestConfigContributor implements ConfigContributor<RestStepContributor> {


    public static final String BASE_URL = "rest.baseURL";
    public static final String CONTENT_TYPE = "rest.contentType";
    public static final String FAILURE_HTTP_CODE_THRESHOLD = "rest.httpCodeThreshold";
    public static final String OAUTH2_URL = "rest.oauth2.url";
    public static final String OAUTH2_CLIENT_ID = "rest.oauth2.clientId";
    public static final String OAUTH2_CLIENT_SECRET = "rest.oauth2.clientSecret";

    private static final Configuration DEFAULTS = Configuration.fromPairs(
        BASE_URL, "http://localhost:8080",
        CONTENT_TYPE, "JSON",
        FAILURE_HTTP_CODE_THRESHOLD, "500"
    );


    @Override
    public boolean accepts(Object contributor) {
        return RestStepContributor.class.isAssignableFrom(contributor.getClass());
    }

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    @Override
    public Configurer<RestStepContributor> configurer() {
        return this::configure;
    }

    private void configure(RestStepContributor contributor, Configuration configuration) {
        RestAssured.config = RestAssured.config().logConfig(
                new LogConfig().defaultStream(RestAssuredLogger.getPrintStream())
        );
        RestAssured.useRelaxedHTTPSValidation();

        configuration.get(BASE_URL,String.class)
            .map(ThrowableFunction.unchecked(URL::new))
            .ifPresent(contributor::setBaseURL);
        configuration.ifPresent(CONTENT_TYPE,String.class,contributor::setContentType);
        configuration.get(FAILURE_HTTP_CODE_THRESHOLD,Integer.class)
            .map(Matchers::lessThan)
            .ifPresent(contributor::setFailureHttpCodeAssertion);

        Oauth2ProviderConfiguration oauth2ProviderConfiguration = contributor.oauth2ProviderConfiguration;
        configuration.ifPresent(OAUTH2_URL, URL.class, oauth2ProviderConfiguration::url)
                .ifPresent(OAUTH2_CLIENT_ID, String.class, oauth2ProviderConfiguration::clientId)
                .ifPresent(OAUTH2_CLIENT_SECRET, String.class, oauth2ProviderConfiguration::clientSecret)
        ;
    }

}
