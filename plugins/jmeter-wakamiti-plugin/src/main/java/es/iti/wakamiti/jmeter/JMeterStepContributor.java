/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.util.http.oauth.GrantType;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.ResourceLoader;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.assertions.AssertionResult;
import us.abstracta.jmeter.javadsl.core.configs.DslVariables;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Map;

import static es.iti.wakamiti.jmeter.JMeterConfigContributor.PASSWORD;
import static es.iti.wakamiti.jmeter.JMeterConfigContributor.USERNAME;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;


/**
 * Contributes JMeter-related steps for defining and executing
 * HTTP requests and configurations.
 */
@Extension(provider = "es.iti.wakamiti", name = "jmeter-steps", version = "2.6")
@I18nResource("iti_wakamiti_wakamiti-jmeter")
public class JMeterStepContributor extends JMeterSupport implements StepContributor {

    /**
     * Sets the content type for all http requests.
     *
     * @param contentType The content type
     * @see ContentType
     */
    @Step(value = "jmeter.define.contentType", args = "word")
    public void setContentType(String contentType) {
        httpSpecifications.put("contentType",
                req -> req.contentType(parseContentType(contentType)));
    }

    /**
     * Sets the default base url.
     *
     * @param url The base url
     */
    @Step(value = "jmeter.define.baseURL", args = "url")
    public void setBaseURL(URL url) {
        this.checkURL(url);
        this.httpDefaults.url(url.toString());
    }

    /**
     * Sets the range of http codes that are valid for all http requests.
     *
     * @param httpCodeAssertion The valid http codes
     */
    @Step(value = "jmeter.define.http.code.assertion", args = "integer-assertion")
    public void setHttpCodeAssertion(Assertion<Integer> httpCodeAssertion) {
        this.httpSpecifications.put("code.assertion", httpSampler -> httpSampler.children(
                jsr223PostProcessor(s -> {
                    try {
                        Assertion.assertThat(Integer.parseInt(s.prev.getResponseCode()), httpCodeAssertion);
                        s.prev.setSuccessful(true);
                    } catch (Exception | Error e) {
                        AssertionResult result = new AssertionResult("HTTP code");
                        result.setResultForFailure("HTTP code " + e.getMessage());
                        s.prev.addAssertionResult(result);
                        s.prev.setSuccessful(false);
                    }
                })
        ));
    }

    /**
     * Sets the default connection and response timeout.
     *
     * @param duration The duration timeout
     */
    @Step(value = "jmeter.define.timeout", args = {"duration"})
    public void setTimeout(Duration duration) {
        this.httpDefaults
                .connectionTimeout(duration)
                .responseTimeout(duration);
    }

    /**
     * Enables cookies for all http requests.
     */
    @Step("jmeter.define.cookies.enable")
    public void cookiesEnabled() {
        this.configs.add(httpCookies());
    }

    /**
     * Disables cookies for all http requests.
     */
    @Step("jmeter.define.cookies.disable")
    public void cookiesDisabled() {
        this.configs.add(httpCookies().disable());
    }

    /**
     * Enables caching for all http requests.
     */
    @Step("jmeter.define.cache.enable")
    public void cacheEnabled() {
        this.configs.add(httpCache());
    }

    /**
     * Disables caching for all http requests.
     */
    @Step("jmeter.define.cache.disable")
    public void cacheDisabled() {
        this.configs.add(httpCache().disable());
    }

    /**
     * Enables download of embedded resources on all http requests.
     */
    @Step("jmeter.define.resources.enable")
    public void resourcesEnabled() {
        this.httpDefaults.downloadEmbeddedResources(true);
    }

    /**
     * Disables download of embedded resources on all http requests.
     */
    @Step("jmeter.define.resources.disable")
    public void resourcesDisabled() {
        this.httpDefaults.downloadEmbeddedResources(false);
    }

    /**
     * Specifies the regular expression of the embedded resources to
     * download in response to all requests.
     *
     * @param regex The regex pattern
     */
    @Step(value = "jmeter.define.resources.matching", args = {"pattern:text"})
    public void resourcesMatching(String regex) {
        this.httpDefaults.downloadEmbeddedResourcesMatching(regex);
    }

    /**
     * Specifies the regular expression of the embedded resources that
     * will not be downloaded in response to all requests.
     *
     * @param regex The regex pattern
     */
    @Step(value = "jmeter.define.resources.not.matching", args = {"pattern:text"})
    public void resourcesNotMatching(String regex) {
        this.httpDefaults.downloadEmbeddedResourcesNotMatching(regex);
    }

    /**
     * Specifies the URL of a proxy server through which HTTP requests
     * are sent to their final destination.
     *
     * @param url The URL of the server proxy
     */
    @Step(value = "jmeter.define.proxy", args = {"url"})
    public void setProxy(URL url) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Setting proxy [url: {}]", url);
        }
        this.httpDefaults.proxy(url.toString());
    }

    /**
     * Specifies the URL and credentials of a proxy server through which
     * HTTP requests are sent to their final destination.
     *
     * @param url      The URL of the server proxy
     * @param username The username
     * @param password The password
     */
    @Step(value = "jmeter.define.proxy.auth", args = {"url", "username:text", "password:text"})
    public void setProxy(URL url, String username, String password) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Setting proxy [url: {}, credentials: {}:{}]", url, username, "*".repeat(password.length()));
        }
        this.httpDefaults.proxy(url.toString(), username, password);
    }

    /**
     * Sets basic authentication for HTTP requests.
     *
     * @param username The username
     * @param password The password
     */
    @Step(value = "jmeter.define.auth.basic", args = {"username:text", "password:text"})
    public void setBasicAuth(String username, String password) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Setting header [Authorization: {}:{}]", username, "*".repeat(password.length()));
        }
        this.authSpecification = httpSampler -> httpSampler.header(AUTHORIZATION, basic(username, password));
    }

    /**
     * Sets bearer token authentication for HTTP requests.
     *
     * @param token The bearer token
     */
    @Step("jmeter.define.auth.bearer.token")
    public void setBearerAuth(String token) {
        LOGGER.trace("Setting header [Authorization: Bearer {}]", token);
        this.authSpecification = httpSampler -> httpSampler.header(AUTHORIZATION, bearer(token));
    }

    /**
     * Sets bearer token authentication for HTTP requests from a file.
     *
     * @param file The file containing the bearer token
     */
    @Step("jmeter.define.auth.bearer.token.file")
    public void setBearerAuthFile(File file) {
        this.setBearerAuth(this.readFile(file).trim());
    }

    /**
     * Disables authentication for HTTP requests.
     */
    @Step("jmeter.define.auth.none")
    public void setNoneAuth() {
        this.authSpecification = null;
    }

    /**
     * Sets default bearer token authentication using OAuth2 provider.
     */
    @Step("jmeter.define.auth.bearer.default")
    public void setBearerDefault() {
        this.authSpecification = httpSampler ->
                httpSampler.header(AUTHORIZATION, bearer(oauth2Provider.getAccessToken()));
    }

    /**
     * Sets bearer token authentication using password grant type.
     *
     * @param username The username
     * @param password The password
     */
    @Step(value = "jmeter.define.auth.bearer.password", args = {"username:text", "password:text"})
    public void setBearerAuthPassword(String username, String password) {
        this.oauth2Provider.configuration().type(GrantType.PASSWORD)
                .addParameter(USERNAME, username)
                .addParameter(PASSWORD, password);
        this.setBearerDefault();
    }

    /**
     * Sets bearer token authentication using password grant type
     * and additional parameters.
     *
     * @param username The username
     * @param password The password
     * @param params   Additional parameters
     */
    @Step(value = "jmeter.define.auth.bearer.password.parameters", args = {"username:text", "password:text"})
    public void setBearerAuthPassword(String username, String password, DataTable params) {
        this.oauth2Provider.configuration().type(GrantType.PASSWORD)
                .addParameter(USERNAME, username)
                .addParameter(PASSWORD, password);
        this.tableToMap(params).forEach(this.oauth2Provider.configuration()::addParameter);
        this.setBearerDefault();
    }

    /**
     * Sets bearer token authentication using client credentials grant
     * type.
     */
    @Step("jmeter.define.auth.bearer.client")
    public void setBearerAuthClient() {
        this.oauth2Provider.configuration().type(GrantType.CLIENT_CREDENTIALS);
        this.setBearerDefault();
    }

    /**
     * Sets bearer token authentication using client credentials grant
     * type and additional parameters.
     *
     * @param params Additional parameters
     */
    @Step("jmeter.define.auth.bearer.client.parameters")
    public void setBearerAuthClient(DataTable params) {
        this.oauth2Provider.configuration().type(GrantType.CLIENT_CREDENTIALS);
        this.tableToMap(params).forEach(this.oauth2Provider.configuration()::addParameter);
        this.setBearerDefault();
    }

    /**
     * Sets the dataset from a CSV file for HTTP requests.
     *
     * @param csv The CSV file
     */
    @Step("jmeter.define.dataset.file")
    public void setDatasetFile(File csv) {
        this.configs.add(csvConfigurer.apply(csvDataSet(absolutePath(csv))));
    }

    /**
     * Sets the variables for HTTP requests.
     *
     * @param vars The variables
     */
    @Step("jmeter.define.dataset.vars")
    public void setVariables(DataTable vars) {
        DslVariables variables = vars();
        tableToMap(vars).forEach(variables::set);
        this.configs.add(variables);
    }

    /**
     * Sets a single variable for HTTP requests.
     *
     * @param name  The variable name
     * @param value The variable value
     */
    @Step(value = "jmeter.define.dataset.var", args = {"name:text", "value:text"})
    public void setVariable(String name, String value) {
        this.configs.add(vars().set(name, value));
    }

    /**
     * Sets the HTTP request with method and service.
     *
     * @param method  The HTTP method
     * @param service The service URL
     */
    @Step(value = "jmeter.define.request", args = {"method:word", "service:text"})
    public void setRequest(String method, String service) {
        this.newHttpSampler(service.replace("{", "${"))
                .method(method);
    }

    /**
     * Sets the body content for the HTTP request.
     *
     * @param body The document body
     */
    @Step("jmeter.define.request.body.data")
    public void setBody(Document body) {
        this.currentHttpSampler().body(body.getContent().replace("{", "${"));
    }

    /**
     * Sets the body content for the HTTP request using a file.
     *
     * @param file The file containing the body content
     */
    @Step(value = "jmeter.define.request.body.file", args = {"file"})
    public void setBody(File file) {
        this.currentHttpSampler().body(readFile(file).replace("{", "${"));
    }

    /**
     * Sets parameters for the HTTP request using a data table.
     *
     * @param params The table of parameters
     */
    @Step("jmeter.define.request.parameters")
    public void setParameters(DataTable params) {
        tableToMap(params).forEach((k, v) ->
                this.currentHttpSampler().param(k.replace("{", "${"), v.replace("{", "${")));
    }

    /**
     * Sets a single parameter for the HTTP request.
     *
     * @param name  The name of the parameter
     * @param value The value of the parameter
     */
    @Step(value = "jmeter.define.request.parameter", args = {"name:text", "value:text"})
    public void setParameter(String name, String value) {
        this.currentHttpSampler().param(name.replace("{", "${"), value.replace("{", "${"));
    }

    /**
     * Sets headers for the HTTP request using a data table.
     *
     * @param params The table of headers
     */
    @Step("jmeter.define.request.headers")
    public void setHeaders(DataTable params) {
        tableToMap(params).forEach((k, v) ->
                this.currentHttpSampler().header(k.replace("{", "${"), v.replace("{", "${")));
    }

    /**
     * Sets a single header for the HTTP request.
     *
     * @param name  The name of the header
     * @param value The value of the header
     */
    @Step(value = "jmeter.define.request.header", args = {"name:text", "value:text"})
    public void setHeader(String name, String value) {
        this.currentHttpSampler().header(name.replace("{", "${"), value.replace("{", "${"));
    }

    /**
     * Sets form parameters for the HTTP request.
     *
     * @param params The form parameters
     */
    @Step("jmeter.define.request.form.parameters")
    public void setFormParameters(DataTable params) {
        tableToMap(params).forEach((k, v) ->
                this.currentHttpSampler().bodyPart(k.replace("{", "${"), v.replace("{", "${"), ContentType.TEXT_PLAIN));
    }

    /**
     * Sets a single form parameter for the HTTP request.
     *
     * @param name  The parameter name
     * @param value The parameter value
     */
    @Step(value = "jmeter.define.request.form.parameter", args = {"name:text", "value:text"})
    public void setFormParameter(String name, String value) {
        this.currentHttpSampler().bodyPart(name.replace("{", "${"), value.replace("{", "${"), ContentType.TEXT_PLAIN);
    }

    /**
     * Sets a file to be attached to the HTTP request.
     *
     * @param file The file to attach
     * @param name The parameter name
     */
    @Step(value = "jmeter.define.request.attached.file", args = {"file", "name:text"})
    public void setAttachedFile(File file, String name) {
        ContentType mimeType = ResourceLoader.getContentType(file);
        this.currentHttpSampler()
                .bodyFilePart(name.replace("{", "${"), absolutePath(file).replace("{", "${"), mimeType);
    }

    /**
     * Sets a regex extractor for the HTTP request.
     *
     * @param regex The regex pattern
     * @param name  The name for the extracted value
     */
    @Step(value = "jmeter.define.request.extractor.regex", args = {"regex:text", "name:text"})
    public void setRegexExtractor(String regex, String name) {
        this.currentHttpSampler().children(regexExtractor(name, regex));
    }

    /**
     * Sets a JSON extractor for the HTTP request.
     *
     * @param query The JSON query to extract data
     * @param name  The name for the extracted value
     */
    @Step(value = "jmeter.define.request.extractor.json", args = {"query:text", "name:text"})
    public void setJsonExtractor(String query, String name) {
        this.currentHttpSampler().children(jsonExtractor(name, query)
                .queryLanguage(query.startsWith("$") ?
                        DslJsonExtractor.JsonQueryLanguage.JSON_PATH :
                        DslJsonExtractor.JsonQueryLanguage.JMES_PATH));
    }

    /**
     * Sets a boundary extractor for the HTTP request.
     *
     * @param leftBoundary  The left boundary for extraction
     * @param rightBoundary The right boundary for extraction
     * @param name          The name for the extracted value
     */
    @Step(value = "jmeter.define.request.extractor.boundary", args = {"leftBoundary:text", "rightBoundary:text", "name:text"})
    public void setBoundaryExtractor(String leftBoundary, String rightBoundary, String name) {
        this.currentHttpSampler().children(boundaryExtractor(name, leftBoundary, rightBoundary));
    }


    /**
     * Executes a thread group the specified number of times.
     *
     * @param threads The number of threads
     * @throws IOException if an I/O error occurs
     */
    @Step(value = "jmeter.execute.simple", args = "threads:int")
    public void executeSimple(Integer threads) throws IOException {
        this.threadSpecifications.add(group -> group.rampTo(threads, Duration.ZERO).holdIterating(1));
        this.executePlan();
    }

    /**
     * Executes a thread group for the specified number of threads, ramp-up
     * time and hold time.
     *
     * @param threads The number of threads
     * @param ramp    The ramp-up duration
     * @param hold    The hold duration
     * @throws IOException if an I/O error occurs
     */
    @Step(value = "jmeter.execute.load.duration", args = {"threads:int", "ramp:duration", "hold:duration"})
    public void executeLoad(Integer threads, Duration ramp, Duration hold) throws IOException {
        this.threadSpecifications.add(group -> group.rampToAndHold(threads, ramp, hold).rampTo(0, ramp));
        this.executePlan();
    }

    /**
     * Executes a thread group for the specified number of threads, ramp-up,
     * for the specified number of times.
     *
     * @param threads   The number of threads
     * @param ramp      The ramp-up duration
     * @param iterations The number of iterations
     * @throws IOException if an I/O error occurs
     */
    @Step(value = "jmeter.execute.load.iterations", args = {"threads:int", "ramp:duration", "iterations:int"})
    public void executeLoad(Integer threads, Duration ramp, Integer iterations) throws IOException {
        for (int i = 0; i < iterations; i++) {
            this.threadSpecifications.add(group -> group.rampTo(threads, ramp).rampTo(0, ramp));
        }
        this.executePlan();
    }

    @Step(value = "jmeter.execute.increase.iterations",
            args = {"threads:int", "ramp:duration", "hold:duration", "iterations:int"})
    public void executeIncrease(Integer threads, Duration ramp, Duration hold, Integer iterations) throws IOException {
        for (int i = 0; i < iterations; i++) {
            final int it = i + 1;
            this.threadSpecifications.add(group -> group.rampToAndHold(threads * it, ramp, hold));
        }
        this.executePlan();
    }

    @Step("jmeter.execute.stretches")
    public void executeStretches(DataTable stretches) throws IOException {
        tableToStretches(stretches).stream().map(Map::values).map(LinkedList::new).forEach(stretch -> {
            int threads = parse(stretch.get(0), Integer.class);
            Duration ramp = parse(stretch.get(1), Duration.class);
            if (stretch.size() > 2) {
                this.threadSpecifications.add(group ->
                        group.rampToAndHold(threads, ramp, parse(stretch.get(2), Duration.class)));
            } else {
                this.threadSpecifications.add(group -> group.rampTo(threads, ramp));
            }
        });
        this.executePlan();
    }


    @Step(value = "jmeter.assert.metric.duration", args = {"metric:duration-metric", "matcher:duration-assertion"})
    public void assertDurationMetric(Metric<Duration> metric, Assertion<Duration> matcher) {
        assertResponseNotNull();
        Assertion.assertThat(metric.apply(stats.overall()), matcher);
    }

    @Step(value = "jmeter.assert.metric.long", args = {"metric:long-metric", "matcher:long-assertion"})
    public void assertLongMetric(Metric<Long> metric, Assertion<Long> matcher) {
        assertResponseNotNull();
        Assertion.assertThat(metric.apply(stats.overall()), matcher);
    }

    @Step(value = "jmeter.assert.metric.double", args = {"metric:double-metric", "matcher:double-assertion"})
    public void assertDoubleMetric(Metric<Double> metric, Assertion<Double> matcher) {
        assertResponseNotNull();
        Assertion.assertThat(metric.apply(stats.overall()), matcher);
    }
}
