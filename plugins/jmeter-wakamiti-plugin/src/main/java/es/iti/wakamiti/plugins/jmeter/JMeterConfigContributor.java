/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.plugins.jmeter;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.auth.oauth.Oauth2ProviderConfig;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.PathUtil;
import imconfig.Configuration;
import imconfig.Configurer;
import org.hamcrest.Matchers;
import us.abstracta.jmeter.javadsl.core.listeners.GraphiteBackendListener;
import us.abstracta.jmeter.javadsl.core.listeners.InfluxDbBackendListener;

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;


@Extension(provider = "es.iti.wakamiti", name = "jmeter-config", version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor")
public class JMeterConfigContributor implements ConfigContributor<JMeterStepContributor> {

    public static final String BASE_URL = "jmeter.baseURL";
    public static final String CONTENT_TYPE = "jmeter.contentType";
    public static final String HTTP_CODE_THRESHOLD = "jmeter.httpCodeThreshold";
    public static final String TIMEOUT = "jmeter.timeout";
    public static final String COOKIES = "jmeter.cookies";
    public static final String CACHE = "jmeter.cache";
    public static final String RESOURCES_DOWNLOAD = "jmeter.resources.download";
    public static final String RESOURCES_REGEX = "jmeter.resources.regex";

    public static final String PROXY = "jmeter.proxy";

    public static final String AUTH = "jmeter.auth";

    public static final String OAUTH2_URL = "jmeter.oauth2.url";
    public static final String OAUTH2_CLIENT_ID = "jmeter.oauth2.clientId";
    public static final String OAUTH2_CLIENT_SECRET = "jmeter.oauth2.clientSecret";
    public static final String OAUTH2_DEFAULT_PARAMETERS = "jmeter.oauth2.parameters";
    public static final String OAUTH2_CACHED = "jmeter.oauth2.cached";

    public static final String REDIRECT_FOLLOW = "jmeter.redirect.follow";


    /* Reporters */
    public static final String TREE_ENABLED = "jmeter.report.tree";
    public static final String JTL_PATH = "jmeter.report.jlt";
    public static final String HTML_PATH = "jmeter.report.html";

    public static final String INFLUX_BASE = "jmeter.report.influx";
    public static final String GRAPHITE_BASE = "jmeter.report.graphite";

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String URL = "url";
    public static final String TOKEN = "token";
    public static final String TITLE = "title";
    public static final String APPLICATION = "application";
    public static final String MEASUREMENT = "measurement";
    public static final String SAMPLERS_REGEX = "samplersRegex";
    public static final String TAGS = "tags";
    public static final String PERCENTILES = "percentiles";
    public static final String PREFIX = "metricsPrefix";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            BASE_URL, "http://localhost:8080",
            CONTENT_TYPE, "APPLICATION_JSON",
            HTTP_CODE_THRESHOLD, "500",
            OAUTH2_CACHED, Boolean.FALSE.toString(),
            TIMEOUT, "60000",
            REDIRECT_FOLLOW, Boolean.TRUE.toString(),
            JTL_PATH, "wakamiti.jtl",
            TREE_ENABLED, Boolean.FALSE.toString()
    );

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }


    @Override
    public Configurer<JMeterStepContributor> configurer() {
        return this::configure;
    }


    private void configure(JMeterStepContributor contributor, Configuration configuration) {
        Path workingDir = WakamitiAPI.instance().workingDir(configuration);

        configuration.get(BASE_URL, URL.class).ifPresent(contributor::setBaseURL);
        configuration.get(CONTENT_TYPE, String.class).ifPresent(contributor::setContentType);
        configuration.get(HTTP_CODE_THRESHOLD, Integer.class)
                .map(Matchers::lessThan)
                .map(MatcherAssertion<Integer>::new)
                .ifPresent(contributor::setHttpCodeAssertion);
        configuration.get(TIMEOUT, Long.class).map(Duration::ofMillis).ifPresent(contributor::setTimeout);
        configuration.get(COOKIES, Boolean.class).ifPresent(cookie -> {
            if (cookie) {
                contributor.cookiesEnabled();
            } else {
                contributor.cookiesDisabled();
            }
        });
        configuration.get(CACHE, Boolean.class).ifPresent(cookie -> {
            if (cookie) {
                contributor.cacheEnabled();
            } else {
                contributor.cacheDisabled();
            }
        });
        configuration.get(RESOURCES_DOWNLOAD, Boolean.class)
                .ifPresent(down -> contributor.httpDefaults.downloadEmbeddedResources(down));
        configuration.get(RESOURCES_REGEX, String.class).ifPresent(contributor::resourcesMatching);

        proxy(contributor, configuration);
        auth(configuration).ifPresent(credentials -> contributor.setBasicAuth(credentials.key(), credentials.value()));

        Oauth2ProviderConfig oauth2Provider = contributor.oauth2Provider.configuration();
        configuration.get(OAUTH2_URL, URL.class).ifPresent(oauth2Provider::url);
        configuration.get(OAUTH2_CLIENT_ID, String.class).ifPresent(oauth2Provider::clientId);
        configuration.get(OAUTH2_CLIENT_SECRET, String.class).ifPresent(oauth2Provider::clientSecret);
        configuration.get(OAUTH2_CACHED, Boolean.class).ifPresent(oauth2Provider::cacheAuth);
        configuration.inner(OAUTH2_DEFAULT_PARAMETERS).asMap().forEach(oauth2Provider::addParameter);

        configuration.get(REDIRECT_FOLLOW, Boolean.class).ifPresent(contributor.httpDefaults::followRedirects);

        configuration.get(TREE_ENABLED, Boolean.class).filter(x -> x).map(x -> resultsTreeVisualizer())
                .ifPresent(contributor.reporters::add);
        configuration.get(JTL_PATH, Path.class)
                .map(PathUtil::replaceTemporalPlaceholders)
                .map(workingDir::resolve)
                .map(p -> jtlWriter(p.getParent().toString(), p.getFileName().toString()))
                .ifPresent(contributor.reporters::add);
        configuration.get(HTML_PATH, Path.class)
                .map(PathUtil::replaceTemporalPlaceholders)
                .map(workingDir::resolve)
                .map(p -> htmlReporter(p.getParent().toString(), p.getFileName().toString()))
                .ifPresent(contributor.reporters::add);
        influx(configuration).ifPresent(contributor.reporters::add);
        graphite(configuration).ifPresent(contributor.reporters::add);
    }

    private Optional<InfluxDbBackendListener> influx(Configuration configuration) {
        Configuration influxConfig = configuration.inner(INFLUX_BASE);
        if (!influxConfig.isEmpty()) {
            InfluxDbBackendListener influx = influxConfig.get(URL, String.class).map(InfluxDbBackendListener::new)
                    .orElseThrow(() -> new WakamitiException("Influx url is needed."));
            influxConfig.get(TOKEN, String.class).ifPresent(influx::token);
            influxConfig.get(TITLE, String.class).ifPresent(influx::title);
            influxConfig.get(APPLICATION, String.class).ifPresent(influx::application);
            influxConfig.get(MEASUREMENT, String.class).ifPresent(influx::measurement);
            influxConfig.get(SAMPLERS_REGEX, String.class).ifPresent(influx::samplersRegex);
            influxConfig.inner(TAGS).asMap().forEach(influx::tag);
            influxConfig.get(PERCENTILES, float[].class).ifPresent(influx::percentiles);
            return Optional.of(influx);
        }
        return Optional.empty();
    }

    private Optional<GraphiteBackendListener> graphite(Configuration configuration) {
        Configuration graphiteConfig = configuration.inner(GRAPHITE_BASE);
        if (!graphiteConfig.isEmpty()) {
            GraphiteBackendListener graphite = graphiteConfig.get(URL, String.class).map(GraphiteBackendListener::new)
                    .orElseThrow(() -> new WakamitiException("Graphite url is needed."));
            graphiteConfig.get(PREFIX, String.class).ifPresent(graphite::metricsPrefix);
            return Optional.of(graphite);
        }
        return Optional.empty();
    }

    private void proxy(JMeterStepContributor contributor, Configuration configuration) {
        Configuration proxyConfig = configuration.inner(PROXY);
        if (!proxyConfig.isEmpty()) {
            URL url = proxyConfig.get(URL, URL.class)
                    .orElseThrow(() -> new WakamitiException("Proxy url is needed."));
            Optional<String> username = proxyConfig.get(USERNAME, String.class);
            Optional<String> password = proxyConfig.get(PASSWORD, String.class);
            if (username.isPresent() && password.isPresent()) {
                contributor.setProxy(url, username.get(), password.get());
            } else {
                contributor.setProxy(url);
            }
        }
    }

    private Optional<Pair<String, String>> auth(Configuration configuration) {
        Configuration authConfig = configuration.inner(AUTH);
        if (!authConfig.isEmpty()) {
            Optional<String> username = authConfig.get(USERNAME, String.class);
            Optional<String> password = authConfig.get(PASSWORD, String.class);
            if (username.isPresent() && password.isPresent()) {
                return Optional.of(new Pair<>(username.get(), password.get()));
            } else {
                throw new WakamitiException("Auth username and password are needed.");
            }
        }
        return Optional.empty();
    }
}