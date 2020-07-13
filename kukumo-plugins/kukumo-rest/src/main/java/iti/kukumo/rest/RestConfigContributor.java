/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import java.net.URL;

import org.hamcrest.Matchers;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.Configurer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.util.ThrowableFunction;



@Extension(
    provider = "kukumo",
    name = "rest-configurator",
    extensionPoint = "iti.kukumo.api.extensions.ConfigContributor"
)
public class RestConfigContributor implements ConfigContributor<RestStepContributor> {


    public static final String BASE_URL = "rest.baseURL";
    public static final String CONTENT_TYPE = "rest.contentType";
    public static final String FAILURE_HTTP_CODE_THRESHOLD = "rest.httpCodeThreshold";

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
        configuration.get(BASE_URL,String.class)
            .map(ThrowableFunction.unchecked(URL::new))
            .ifPresent(contributor::setBaseURL);
        configuration.ifPresent(CONTENT_TYPE,String.class,contributor::setContentType);
        configuration.get(FAILURE_HTTP_CODE_THRESHOLD,Integer.class)
            .map(Matchers::lessThan)
            .ifPresent(contributor::setFailureHttpCodeAssertion);
    }

}
