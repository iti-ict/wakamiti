/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.auth.oauth.Oauth2Provider;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.jmeter.dsl.ContentTypeUtil;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.configs.BaseConfigElement;
import us.abstracta.jmeter.javadsl.core.configs.DslCsvDataSet;
import us.abstracta.jmeter.javadsl.core.listeners.BaseListener;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;
import us.abstracta.jmeter.javadsl.http.DslHttpDefaults;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;


/**
 * Provides support for JMeter test plan creation and execution within
 * the Wakamiti framework.
 * It includes utilities for configuring HTTP requests, authentication,
 * and various types of listeners and reporters.
 */
public class JMeterSupport {

    protected static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.jmeter");

    /**
     * DslHttpDefaults definition, used in TestPlan
     **/
    protected DslHttpDefaults httpDefaults = new DslHttpDefaults();
    /**
     * Oauth2 specification, used in all DslHttSamplers
     **/
    protected Consumer<DslHttpSampler> authSpecification;
    protected Oauth2Provider oauth2Provider = new Oauth2Provider();
    /**
     * DslDefaultThreadGroup specifications, used in TestPlan
     **/
    protected List<Consumer<DslDefaultThreadGroup>> threadSpecifications = new LinkedList<>();
    /**
     * DslHttpSampler definitions, used in TestPlan
     **/
    protected LinkedList<DslHttpSampler> httpSamplers = new LinkedList<>();
    /**
     * DslHttpSampler specification, used in all DslHttSamplers
     **/
    protected Map<String, Consumer<DslHttpSampler>> httpSpecifications = new LinkedHashMap<>();
    /**
     * Configuration definitions, used in TestPlan
     **/
    protected List<BaseConfigElement> configs = new LinkedList<>();
    /**
     * Reporter definitions, used in TestPlan
     **/
    protected List<BaseListener> reporters = new LinkedList<>();
    /**
     * TestPlan stats result
     **/
    protected TestPlanStats stats;
    /**
     * DslCsvDataSet configurator
     */
    protected UnaryOperator<DslCsvDataSet> csvConfigurer;

    /**
     * Creates a new default thread group and applies any thread
     * specifications defined.
     *
     * @return A new {@link DslDefaultThreadGroup} with applied
     * specifications.
     */
    protected DslDefaultThreadGroup newThreadGroup() {
        DslDefaultThreadGroup thread = threadGroup();
        threadSpecifications.forEach(spec -> spec.accept(thread));
        httpSamplers.forEach(sampler ->
                httpSpecifications.values().forEach(spec -> spec.accept(sampler)));
        httpSamplers.forEach(thread::children);
        return thread;
    }

    /**
     * Creates a new HTTP sampler for the given service path, applying any
     * authentication specifications if present.
     *
     * @param path The service path for the HTTP sampler.
     * @return A new {@link DslHttpSampler} configured with the
     * given path and authentication.
     */
    protected DslHttpSampler newHttpSampler(String path) {
        DslHttpSampler httpSampler = httpSampler(path);
        Optional.ofNullable(authSpecification).ifPresent(spec -> spec.accept(httpSampler));
        httpSamplers.addLast(httpSampler);
        return httpSampler;
    }

    /**
     * Returns the most recently added HTTP sampler.
     *
     * @return The current {@link DslHttpSampler}.
     * @throws WakamitiException if no HTTP sampler has been defined.
     */
    protected DslHttpSampler currentHttpSampler() {
        try {
            return httpSamplers.getLast();
        } catch (NoSuchElementException e) {
            throw new WakamitiException("No http sampler has been defined.");
        }
    }

    /**
     * Creates a new test plan, including a thread group and any
     * defined reporters.
     *
     * @return A new {@link DslTestPlan}.
     */
    private DslTestPlan newTestPlan() {
        stats = null;
        DslTestPlan testPlan = testPlan();
        configs.forEach(testPlan::children);
        Optional.ofNullable(httpDefaults).ifPresent(testPlan::children);
        testPlan.children(newThreadGroup());
        reporters.forEach(testPlan::children);
        return testPlan;
    }


    protected void executePlan() throws IOException {
        stats = newTestPlan().run();
    }

    /**
     * Generates Basic authorization value.
     *
     * @param username The username
     * @param password The password
     * @return the Basic authorization value.
     */
    protected String basic(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("Both username and password are required.");
        }
        return String.format("Basic %s", Base64.getEncoder()
                .encodeToString(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Generates Bearer authorization value.
     *
     * @param token The Bearer token
     * @return the Bearer authorization value.
     */
    protected String bearer(String token) {
        if (isBlank(token)) {
            throw new IllegalArgumentException("The token is required.");
        }
        return String.format("Bearer %s", token);
    }

    /**
     * Checks the validity of the provided URL.
     *
     * @param url The URL to check.
     * @throws WakamitiException If the base URL is missing or if query
     *                           parameters are present.
     */
    protected void checkURL(URL url) {
        if (url == null) {
            throw new WakamitiException("Missing required base URL.");
        }
        if (!isBlank(url.getQuery())) {
            throw new WakamitiException("Query parameters are not allowed here. Please, use the steps provided for this purpose.");
        }
    }

    /**
     * Parses a text expression into the specified data type.
     *
     * @param expression The text expression to parse.
     * @param type       The class of the type into which the expression should be parsed.
     * @param <T>        The type parameter representing the target type.
     * @return The parsed value of the specified type.
     * @throws WakamitiException if no type registry is found for the specified class.
     */
    @SuppressWarnings("unchecked")
    protected <T> T parse(String expression, Class<T> type) {
        WakamitiStepRunContext ctx = WakamitiStepRunContext.current();
        return (T) ctx.typeRegistry().findTypesForJavaType(type).findFirst()
                .orElseThrow(() -> new WakamitiException("No type registry found for Class '{}'", type))
                .parse(ctx.stepLocale(), expression);
    }

    /**
     * Asserts that the test plan has been executed and the stats are
     * available.
     *
     * @throws WakamitiException if the plan has not been executed.
     */
    protected void assertResponseNotNull() {
        if (stats == null) {
            throw new WakamitiException("The plan has not been executed");
        }
    }

    /**
     * Converts a DataTable into a map of name-value pairs.
     *
     * @param dataTable The DataTable to convert.
     * @return A map containing the name-value pairs.
     * @throws WakamitiException if the DataTable does not have exactly two columns.
     */
    protected Map<String, String> tableToMap(DataTable dataTable) {
        if (dataTable.columns() != 2) {
            throw new WakamitiException("Table must have 2 columns [name, value]");
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 1; i < dataTable.rows(); i++) {
            map.put(dataTable.value(i, 0), dataTable.value(i, 1));
        }
        return map;
    }

    /**
     * Converts a DataTable into a list of stretches.
     *
     * @param dataTable The DataTable to convert.
     * @return A list of map containing the name-value pairs.
     * @throws WakamitiException if the DataTable does not have two or three columns.
     */
    protected List<Map<String, String>> tableToStretches(DataTable dataTable) {
        if (dataTable.columns() < 2 || dataTable.columns() > 3) {
            throw new WakamitiException("Table must have 2 or 3 columns [threads, ramp, hold?]");
        }
        List<Map<String, String>> list = new LinkedList<>();
        String[] keys = new String[dataTable.columns()];
        for (int row = 0; row < dataTable.rows(); row++) {
            Map<String, String> map = new LinkedHashMap<>();
            for (int col = 0; col < dataTable.columns(); col++) {
                if (row == 0) {
                    keys[col] = dataTable.value(row, col);
                } else {
                    map.put(keys[col], dataTable.value(row, col));
                }
            }
            if (row != 0)
                list.add(map);
        }
        return list;
    }


    /**
     * Parses the given content type string and returns the corresponding
     * {@link ContentType} object.
     *
     * @param contentType The content type string to parse.
     * @return The parsed {@link ContentType} object.
     * @throws WakamitiException if the content type is not valid.
     */
    protected ContentType parseContentType(String contentType) {
        try {
            return ContentTypeUtil.valueOf(contentType);
        } catch (IllegalArgumentException e) {
            throw new WakamitiException("REST content type must be one of the following: {}",
                    ContentTypeUtil.names(), e);
        }
    }

    /**
     * Reads the content of the specified file and returns it as a string.
     *
     * @param file The file to read.
     * @return The content of the file as a string.
     */
    protected String readFile(File file) {
        if (!file.exists()) {
            throw new WakamitiException("File '{}' not found", file.getAbsolutePath());
        }
        return resourceLoader().readFileAsString(file);
    }

    /**
     * Retrieves the absolute path of the specified file.
     *
     * @param file The file for which the absolute path is required.
     * @return The canonical path of the specified file.
     * @throws WakamitiException If the file does not exist or if the path cannot be retrieved.
     */
    protected String absolutePath(File file) {
        if (!file.exists()) {
            throw new WakamitiException("File '{}' not found", file.getAbsolutePath());
        }
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new WakamitiException("Unable to retrieve the path to file '{}'", file, e);
        }
    }

    /**
     * Returns the {@link ResourceLoader} instance from the Wakamiti API.
     *
     * @return The {@link ResourceLoader}.
     */
    protected ResourceLoader resourceLoader() {
        return WakamitiAPI.instance().resourceLoader();
    }

}
