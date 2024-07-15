/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.rest.log.RestAssuredLogger;
import es.iti.wakamiti.api.auth.oauth.Oauth2ProviderConfig;
import imconfig.Configuration;
import imconfig.Configurer;
import io.restassured.RestAssured;
import io.restassured.config.Config;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import org.hamcrest.Matchers;

import java.lang.reflect.Field;
import java.net.URL;
import java.time.Duration;
import java.util.Map;


/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
@Extension(provider = "es.iti.wakamiti", name = "rest-configurator", version = "2.5",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor")
public class RestConfigContributor implements ConfigContributor<RestStepContributor> {

    public static final String BASE_URL = "rest.baseURL";
    public static final String CONTENT_TYPE = "rest.contentType";
    public static final String FAILURE_HTTP_CODE_THRESHOLD = "rest.httpCodeThreshold";
    public static final String TIMEOUT = "rest.timeout";
    public static final String OAUTH2_URL = "rest.oauth2.url";
    public static final String OAUTH2_CLIENT_ID = "rest.oauth2.clientId";
    public static final String OAUTH2_CLIENT_SECRET = "rest.oauth2.clientSecret";
    public static final String OAUTH2_DEFAULT_PARAMETERS = "rest.oauth2.parameters";
    public static final String OAUTH2_CACHED = "rest.oauth2.cached";

    // RestAssured config
    public static final String MULTIPART_SUBTYPE = "rest.config.multipart.subtype";
    public static final String MULTIPART_FILENAME = "rest.config.multipart.filename";

    public static final String REDIRECT_FOLLOW = "rest.config.redirect.follow";
    public static final String REDIRECT_ALLOW_CIRCULAR = "rest.config.redirect.allowCircular";
    public static final String REDIRECT_REJECT_RELATIVE = "rest.config.redirect.rejectRelative";
    public static final String REDIRECT_MAX = "rest.config.redirect.max";

    private static void config(RestAssuredConfig config) {
        RestAssured.config = config;
    }

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                BASE_URL, "http://localhost:8080",
                CONTENT_TYPE, "JSON",
                FAILURE_HTTP_CODE_THRESHOLD, "500",
                OAUTH2_CACHED, "false",
                TIMEOUT, "60000"
        );
    }

    @Override
    public Configurer<RestStepContributor> configurer() {
        return this::configure;
    }

    private void configure(RestStepContributor contributor, Configuration configuration) {
        restassuredConfigure();

        configuration.get(BASE_URL, String.class)
                .map(ThrowableFunction.unchecked(URL::new))
                .ifPresent(contributor::setBaseURL);
        configuration.get(CONTENT_TYPE, String.class)
                .ifPresent(contributor::setContentType);
        configuration.get(FAILURE_HTTP_CODE_THRESHOLD, Integer.class)
                .map(Matchers::lessThan)
                .map(MatcherAssertion<Integer>::new)
                .ifPresent(contributor::setHttpCodeAssertion);
        configuration.get(TIMEOUT, Integer.class).map(Duration::ofMillis)
                .ifPresent(contributor::setTimeout);

        Oauth2ProviderConfig oauth2Provider = contributor.oauth2Provider.configuration();
        configuration.get(OAUTH2_URL, URL.class).ifPresent(oauth2Provider::url);
        configuration.get(OAUTH2_CLIENT_ID, String.class).ifPresent(oauth2Provider::clientId);
        configuration.get(OAUTH2_CLIENT_SECRET, String.class).ifPresent(oauth2Provider::clientSecret);
        configuration.get(OAUTH2_CACHED, Boolean.class).ifPresent(oauth2Provider::cacheAuth);
        configuration.inner(OAUTH2_DEFAULT_PARAMETERS).asMap().forEach(oauth2Provider::addParameter);

        configuration.get(MULTIPART_SUBTYPE, String.class).ifPresent(contributor::setMultipartSubtype);
        configuration.get(MULTIPART_FILENAME, String.class).ifPresent(contributor::setFilename);
        configuration.get(REDIRECT_FOLLOW, Boolean.class)
                .map(RestAssured.config().getRedirectConfig()::followRedirects)
                .ifPresent(this::config);
        configuration.get(REDIRECT_ALLOW_CIRCULAR, Boolean.class)
                .map(RestAssured.config().getRedirectConfig()::allowCircularRedirects)
                .ifPresent(this::config);
        configuration.get(REDIRECT_REJECT_RELATIVE, Boolean.class)
                .map(RestAssured.config().getRedirectConfig()::rejectRelativeRedirect)
                .ifPresent(this::config);
        configuration.get(REDIRECT_MAX, Integer.class)
                .map(RestAssured.config().getRedirectConfig()::maxRedirects)
                .ifPresent(this::config);
    }

    private void restassuredConfigure() {
        RestAssured.reset();
        config(RestAssured.config().logConfig(new LogConfig().defaultStream(RestAssuredLogger.getPrintStream())));
        RestAssured.useRelaxedHTTPSValidation();
    }

    @SuppressWarnings(value = "unchecked")
    private void config(Config config) {
        try {
            Field field = RestAssuredConfig.class.getDeclaredField("configs");
            field.setAccessible(true);
            ((Map<Class<? extends Config>, Config>) field.get(RestAssured.config)).put(config.getClass(), config);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WakamitiException("Error configuring RestAssured", e);
        }
    }
}