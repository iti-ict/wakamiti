/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter;


import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.resultsTreeVisualizer;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

import org.hamcrest.Matchers;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.PathUtil;
import es.iti.wakamiti.api.util.http.oauth.Oauth2ProviderConfig;
import us.abstracta.jmeter.javadsl.core.configs.DslCsvDataSet;
import us.abstracta.jmeter.javadsl.core.listeners.GraphiteBackendListener;
import us.abstracta.jmeter.javadsl.core.listeners.InfluxDbBackendListener;


/**
 * Contributes JMeter-specific configuration settings to the Wakamiti platform.
 * This class handles the configuration of various JMeter components such as
 * HTTP samplers, authentication, proxy settings, and reporting tools.
 *
 * @see ConfigContributor
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "jmeter-config",
        version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class JMeterConfigContributor implements ConfigContributor<JMeterStepContributor> {

    /** Configuration key for the base URL prepended to JMeter HTTP sampler paths. */
    public static final String BASE_URL = "jmeter.baseURL";
    /** Configuration key for the default HTTP request content type. */
    public static final String CONTENT_TYPE = "jmeter.contentType";
    /** Configuration key for the response-code boundary considered a failed sample. */
    public static final String HTTP_CODE_THRESHOLD = "jmeter.httpCodeThreshold";
    /** Configuration key for HTTP connection and response timeouts. */
    public static final String TIMEOUT = "jmeter.timeout";
    /** Configuration key controlling use of JMeter's HTTP cookie manager. */
    public static final String COOKIES = "jmeter.cookies";
    /** Configuration key controlling use of JMeter's HTTP cache manager. */
    public static final String CACHE = "jmeter.cache";
    /** Configuration key controlling download of embedded HTTP resources. */
    public static final String RESOURCES_DOWNLOAD = "jmeter.resources.download";
    /** Configuration key for the URL pattern selecting embedded resources to download. */
    public static final String RESOURCES_REGEX = "jmeter.resources.regex";

    /** Configuration key for the delimiter used by CSV data-set inputs. */
    public static final String CSV_DELIMITER = "jmeter.csv.delimiter";
    /** Configuration key for the character encoding of CSV data-set inputs. */
    public static final String CSV_ENCODING = "jmeter.csv.encoding";
    /** Configuration key controlling whether a thread stops at end of a CSV file. */
    public static final String CSV_EOF = "jmeter.csv.eofStop";
    /** Configuration key controlling random selection of CSV records. */
    public static final String CSV_RANDOM = "jmeter.csv.random";
    /** Configuration key selecting how CSV records are shared between threads. */
    public static final String CSV_SHARING = "jmeter.csv.sharing";

    /** Parent configuration section for the HTTP proxy used by samplers. */
    public static final String PROXY = "jmeter.proxy";

    /** Parent configuration section for HTTP authentication. */
    public static final String AUTH = "jmeter.auth";

    /** Configuration key for the OAuth 2 token endpoint. */
    public static final String OAUTH2_URL = "jmeter.oauth2.url";
    /** Configuration key for the OAuth 2 client identifier. */
    public static final String OAUTH2_CLIENT_ID = "jmeter.oauth2.clientId";
    /** Configuration key for the OAuth 2 client secret. */
    public static final String OAUTH2_CLIENT_SECRET = "jmeter.oauth2.clientSecret";
    /** Configuration key for parameters sent with every OAuth 2 token request. */
    public static final String OAUTH2_DEFAULT_PARAMETERS = "jmeter.oauth2.parameters";
    /** Configuration key controlling reuse of a retrieved OAuth 2 token. */
    public static final String OAUTH2_CACHED = "jmeter.oauth2.cached";

    /** Configuration key controlling whether HTTP sampler redirects are followed. */
    public static final String REDIRECT_FOLLOW = "jmeter.redirect.follow";

    /* Reporters */
    /** Enables the tree results reporter. */
    public static final String TREE_ENABLED = "jmeter.report.tree";
    /** Configuration key for the JTL results file written by the test run. */
    public static final String JTL_PATH = "jmeter.report.jtl";
    /** Configuration key for the generated JMeter HTML dashboard directory. */
    public static final String HTML_PATH = "jmeter.report.html";

    /** Parent configuration section for the InfluxDB metrics backend. */
    public static final String INFLUX_BASE = "jmeter.report.influx";
    /** Parent configuration section for the Graphite metrics backend. */
    public static final String GRAPHITE_BASE = "jmeter.report.graphite";

    /** Nested backend-property name for an authentication user name. */
    public static final String USERNAME = "username";
    /** Nested backend-property name for an authentication password. */
    public static final String PASSWORD = "password";
    /** Nested backend-property name for the metrics collector endpoint. */
    public static final String URL = "url";
    /** Nested backend-property name for an API authentication token. */
    public static final String TOKEN = "token";
    /** Nested backend-property name for the displayed test title. */
    public static final String TITLE = "title";
    /** Nested backend-property name used to identify the tested application. */
    public static final String APPLICATION = "application";
    /** Nested backend-property name for the target time-series measurement. */
    public static final String MEASUREMENT = "measurement";
    /** Nested backend-property name for the regular expression selecting samplers. */
    public static final String SAMPLERS_REGEX = "samplersRegex";
    /** Nested backend-property name for custom metric tags. */
    public static final String TAGS = "tags";
    /** Nested backend-property name for the response-time percentiles to publish. */
    public static final String PERCENTILES = "percentiles";
    /** Nested backend-property name for the prefix applied to emitted metric names. */
    public static final String PREFIX = "metricsPrefix";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            BASE_URL, "http://localhost:8080",
            CONTENT_TYPE, "APPLICATION_JSON",
            HTTP_CODE_THRESHOLD, "500",
            COOKIES, Boolean.FALSE.toString(),
            CACHE, Boolean.FALSE.toString(),
            RESOURCES_DOWNLOAD, Boolean.FALSE.toString(),
            CSV_DELIMITER, ",",
            CSV_ENCODING, StandardCharsets.UTF_8.name(),
            CSV_RANDOM, Boolean.FALSE.toString(),
            CSV_EOF, Boolean.FALSE.toString(),
            CSV_SHARING, DslCsvDataSet.Sharing.ALL_THREADS.name(),
            OAUTH2_CACHED, Boolean.FALSE.toString(),
            TIMEOUT, "60000",
            REDIRECT_FOLLOW, Boolean.TRUE.toString(),
            JTL_PATH, "wakamiti.jtl",
            TREE_ENABLED, Boolean.FALSE.toString()
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configurer<JMeterStepContributor> configurer() {
        return this::configure;
    }

    /**
     * Applies the configuration settings to the given JMeter step contributor.
     *
     * @param contributor   The JMeter step contributor.
     * @param configuration The configuration settings.
     */
    private void configure(
            JMeterStepContributor contributor,
            Configuration configuration
    ) {
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

        contributor.csvConfigurer = (csv) -> {
            configuration.get(CSV_DELIMITER, String.class).ifPresent(csv::delimiter);
            configuration.get(CSV_ENCODING, String.class).ifPresent(csv::encoding);
            configuration.get(CSV_RANDOM, Boolean.class).ifPresent(csv::randomOrder);
            configuration.get(CSV_SHARING, DslCsvDataSet.Sharing.class).ifPresent(csv::sharedIn);
            configuration.get(CSV_EOF, Boolean.class).ifPresent(csv::stopThreadOnEOF);
            return csv;
        };

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

    /**
     * Configures an InfluxDB backend listener based on the provided configuration.
     *
     * @param configuration The configuration settings.
     * @return An optional InfluxDB backend listener.
     */
    private Optional<InfluxDbBackendListener> influx(
            Configuration configuration
    ) {
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

    /**
     * Configures a Graphite backend listener based on the provided configuration.
     *
     * @param configuration The configuration settings.
     * @return An optional Graphite backend listener.
     */
    private Optional<GraphiteBackendListener> graphite(
            Configuration configuration
    ) {
        Configuration graphiteConfig = configuration.inner(GRAPHITE_BASE);
        if (!graphiteConfig.isEmpty()) {
            GraphiteBackendListener graphite = graphiteConfig.get(URL, String.class).map(GraphiteBackendListener::new)
                    .orElseThrow(() -> new WakamitiException("Graphite url is needed."));
            graphiteConfig.get(PREFIX, String.class).ifPresent(graphite::metricsPrefix);
            return Optional.of(graphite);
        }
        return Optional.empty();
    }

    /**
     * Configures the proxy settings for the JMeterStepContributor.
     *
     * @param contributor   The JMeterStepContributor to configure.
     * @param configuration The configuration settings to apply.
     */
    private void proxy(
            JMeterStepContributor contributor,
            Configuration configuration
    ) {
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

    /**
     * Retrieves the authentication credentials from the configuration.
     *
     * @param configuration The configuration containing authentication settings.
     * @return An optional pair of username and password if the configuration is valid.
     */
    private Optional<Pair<String, String>> auth(
            Configuration configuration
    ) {
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
