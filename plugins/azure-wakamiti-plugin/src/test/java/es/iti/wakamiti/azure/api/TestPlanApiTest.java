/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api;


import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.api.model.*;
import es.iti.wakamiti.azure.internal.WakamitiAzureException;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.MatchType;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.*;

import static es.iti.wakamiti.api.util.MapUtils.map;
import static es.iti.wakamiti.api.util.StringUtils.format;
import static es.iti.wakamiti.azure.api.model.query.Field.TAGS;
import static es.iti.wakamiti.azure.api.model.query.Field.TITLE;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.groupBy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.RegexBody.regex;


public class TestPlanApiTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(TestPlanApiTest.class);

    private static final Integer PORT = 4321;
    private static final String BASE_URL = MessageFormat.format("http://localhost:{0}", PORT.toString());

    private static final ClientAndServer mock = startClientAndServer(PORT);

    @BeforeClass
    public static void beforeEach() {
        ConfigurationProperties.logLevel("TRACE");
    }

    @AfterClass
    public static void shutdown() {
        mock.close();
    }

    @Test
    public void testSettingsWhenDefaultWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/_api/_common/GetUserProfile")
                        .withQueryStringParameter("api-version", "6.0-preview"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/settings/default.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/wit/workitemtypecategories/Microsoft.TestCaseCategory"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/categories/single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/configurations")
                        .withQueryStringParameter("api-version", "6.0-preview"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("x-ms-continuationtoken", "123")
                        .withBody("{}")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/configurations")
                        .withQueryStringParameter("api-version", "6.0-preview")
                        .withQueryStringParameter("continuationToken", "123"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, "wakamiti")
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        Settings settings = client.settings();
        logResult(settings);

        // check
        requests.forEach(mock::verify);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.of("UTC+01:00"));
        assertThat(settings.testCaseType()).isEqualTo("Caso de prueba");
        assertThat(settings.configuration()).isEqualTo("29");
    }

    @Test
    public void testSettingsWhenCustomWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("GET").withPath("/ST/_api/_common/GetUserProfile"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/settings/by_id.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/wit/workitemtypecategories/Microsoft.TestCaseCategory"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/categories/single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        Settings settings = client.settings();
        logResult(settings);

        // check
        requests.forEach(mock::verify);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.of("UTC-12:00"));
        assertThat(settings.testCaseType()).isEqualTo("Caso de prueba");
        assertThat(settings.configuration()).isEqualTo("13");
    }

    @Test
    public void testSettingsWhenNoTZWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("GET").withPath("/ST/_api/_common/GetUserProfile"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/settings/none.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/wit/workitemtypecategories/Microsoft.TestCaseCategory"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/categories/single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        Settings settings = client.settings();
        logResult(settings);

        // check
        requests.forEach(mock::verify);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(settings.testCaseType()).isEqualTo("Caso de prueba");
        assertThat(settings.configuration()).isEqualTo("13");
    }

    @Test
    public void testSettingsWhenTZEndpointNotFoundWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/wit/workitemtypecategories/Microsoft.TestCaseCategory"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/categories/single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        Settings settings = client.settings();
        logResult(settings);

        // check
        requests.forEach(mock::verify);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(settings.testCaseType()).isEqualTo("Caso de prueba");
        assertThat(settings.configuration()).isEqualTo("13");
    }

    @Test(expected = WakamitiAzureException.class)
    public void testSettingsWhenNoConfigurationWithError() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("GET").withPath("/ST/_api/_common/GetUserProfile"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/settings/none.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/wit/workitemtypecategories/Microsoft.TestCaseCategory"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/categories/single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/single.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, "wakamiti")
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        try {
            client.settings();

            //check
        } catch (WakamitiAzureException e) {
            requests.forEach(mock::verify);
            assertThat(e).hasMessage("There is no configuration with name 'wakamiti' available. "
                    + System.lineSeparator() + "Please try to fix this problem in azure.");
            throw e;
        }
    }

    @Test(expected = WakamitiAzureException.class)
    public void testSettingsWhenNoConfigurationDefaultWithError() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("GET").withPath("/ST/_api/_common/GetUserProfile"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/settings/none.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/wit/workitemtypecategories/Microsoft.TestCaseCategory"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/categories/single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{}")
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        try {
            client.settings();

            //check
        } catch (WakamitiAzureException e) {
            requests.forEach(mock::verify);
            assertThat(e).hasMessage("There is no default configuration available. " + System.lineSeparator() +
                    "Please try to fix this problem in azure.");
            throw e;
        }
    }

    @Test(expected = WakamitiAzureException.class)
    public void testSettingsWhenNoDefaultCategoryWithError() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/_api/_common/GetUserProfile")
                        .withQueryStringParameter("api-version", "6.0-preview"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/settings/default.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/wit/workitemtypecategories/Microsoft.TestCaseCategory"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/categories/none.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/configurations")
                        .withQueryStringParameter("api-version", "6.0-preview"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, "wakamiti")
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        try {
            client.settings();

            //check
        } catch (WakamitiAzureException e) {
            requests.forEach(mock::verify);
            assertThat(e).hasMessage("There is no test case category available. " + System.lineSeparator() +
                    "Please try to fix this problem in azure.");
            assertThat(e.getCause()).isNotNull().hasMessage("Default test case category");
            throw e;
        }
    }

    @Test(expected = WakamitiAzureException.class)
    public void testSettingsWhenNoCategoryWithError() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/_api/_common/GetUserProfile")
                        .withQueryStringParameter("api-version", "6.0-preview"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/settings/default.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/configurations")
                        .withQueryStringParameter("api-version", "6.0-preview"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, "wakamiti")
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        try {
            client.settings();

            //check
        } catch (Exception e) {
            requests.forEach(mock::verify);
            assertThat(e).hasMessage("There is no test case category available. " + System.lineSeparator() +
                    "Please try to fix this problem in azure.");
            assertThat(e.getCause()).isNotNull().hasMessage("The Azure API returned a non-OK response");
            throw e;
        }
    }


    @Test
    public void testGetTestPlanWhenExistsWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("POST").withPath("/ST/ACS/_apis/wit/wiql"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/plans/search/single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/plans/56983"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/plans/get/single.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        TestPlan testPlan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"));

        // act
        TestPlan newTestPlan = client.getTestPlan(testPlan, true);
        logResult(newTestPlan);

        // check
        requests.forEach(mock::verify);
        assertThat(newTestPlan).isNotNull()
                .hasFieldOrPropertyWithValue("id", "56983")
                .hasFieldOrPropertyWithValue("name", "Wakamiti Test Plan")
                .hasFieldOrPropertyWithValue("area", "ACS")
                .hasFieldOrPropertyWithValue("iteration", "ACS\\Iteración 1")
                .hasFieldOrPropertyWithValue("rootSuite", new TestSuite().id("56984").name("Wakamiti Test Plan"));
    }

    @Test(expected = WakamitiAzureException.class)
    public void testGetTestPlanWhenExistsMultipleWithError() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("POST").withPath("/ST/ACS/_apis/wit/wiql"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/plans/search/multiple.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        TestPlan testPlan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"));

        // act
        try {
            client.getTestPlan(testPlan, true);

            // check
        } catch (WakamitiAzureException e) {
            requests.forEach(mock::verify);
            assertThat(e).hasMessage("Too many test plans with same 'name', 'area' and 'iteration': [56983, 56984]. "
                    + System.lineSeparator() + "Please try to fix this problem in azure.");
            throw e;
        }
    }

    @Test
    public void testGetTestPlanWhenNotExistsAndCreateIfAbsentWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("POST").withPath("/ST/ACS/_apis/wit/wiql"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/plans/search/none.json"))
        ).ifPresent(requests::add);
        mockServer(
                request().withMethod("POST").withPath("/ST/ACS/_apis/testplan/plans")
                        .withBody(json("{" +
                                                "\"name\":\"Wakamiti Test Plan\"," +
                                                "\"areaPath\":\"ACS\"," +
                                                "\"iteration\":\"ACS\\\\Iteración 1\"" +
                                        "}", MatchType.ONLY_MATCHING_FIELDS)
                        ),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/plans/get/single.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        TestPlan testPlan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"));

        // act
        TestPlan newTestPlan = client.getTestPlan(testPlan, true);
        logResult(newTestPlan);

        // check
        requests.forEach(mock::verify);
        assertThat(newTestPlan).isNotNull()
                .hasFieldOrPropertyWithValue("id", "56983")
                .hasFieldOrPropertyWithValue("name", "Wakamiti Test Plan")
                .hasFieldOrPropertyWithValue("area", "ACS")
                .hasFieldOrPropertyWithValue("iteration", "ACS\\Iteración 1")
                .hasFieldOrPropertyWithValue("rootSuite", new TestSuite().id("56984").name("Wakamiti Test Plan"));
    }

    @Test(expected = WakamitiAzureException.class)
    public void testGetTestPlanWhenNotExistsAndNoCreateIfAbsentWithError() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("POST").withPath("/ST/ACS/_apis/wit/wiql"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/plans/search/none.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        TestPlan testPlan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"));

        // act
        try {
            client.getTestPlan(testPlan, false);

            // check
        } catch (WakamitiAzureException e) {
            requests.forEach(mock::verify);
            assertThat(e).hasMessage("Test Plan with name 'Wakamiti Test Plan', area 'ACS' and iteration "
                    + "'ACS\\Iteración 1' does not exist in Azure. " + System.lineSeparator()
                    + "Please try to fix this problem in azure.");
            throw e;
        }
    }

    @Test
    public void testGetSuitesWhenExistsWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/suites")
                        .withQueryStringParameter("asTreeView", "true"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("x-ms-continuationtoken", "123")
                        .withBody("{\"value\":[],\"count\":0}")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/suites")
                        .withQueryStringParameter("asTreeView", "true")
                        .withQueryStringParameter("continuationToken", "123"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/suites/tree_list.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        TestPlan plan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"))
                .id("56983").rootSuite(new TestSuite().id("56984").name("Wakamiti Test Plan"));

        List<TestSuite> suites = new LinkedList<>(List.of(new TestSuite().name("Wakamiti Test Plan")));
        suites.add(new TestSuite().name("Feature 1").parent(suites.get(0)));
        suites.add(new TestSuite().name("AAA").parent(suites.get(1)));
        suites.add(new TestSuite().name("BBB").parent(suites.get(1)));

        // act
        List<TestSuite> remoteSuites = client.getTestSuites(plan, suites, true);
        logResult(remoteSuites);

        // check
        requests.forEach(mock::verify);
        assertThat(remoteSuites).hasSize(4);
        assertThat(remoteSuites.get(0))
                .hasFieldOrPropertyWithValue("id", "56984")
                .hasFieldOrPropertyWithValue("name", "Wakamiti Test Plan");
        assertThat(remoteSuites.get(1))
                .hasFieldOrPropertyWithValue("id", "56985")
                .hasFieldOrPropertyWithValue("name", "Feature 1")
                .hasFieldOrPropertyWithValue("parent", remoteSuites.get(0));
        assertThat(remoteSuites.get(2))
                .hasFieldOrPropertyWithValue("id", "56986")
                .hasFieldOrPropertyWithValue("name", "AAA")
                .hasFieldOrPropertyWithValue("parent", remoteSuites.get(1));
        assertThat(remoteSuites.get(3))
                .hasFieldOrPropertyWithValue("id", "56987")
                .hasFieldOrPropertyWithValue("name", "BBB")
                .hasFieldOrPropertyWithValue("parent", remoteSuites.get(1));
    }

    @Test
    public void testGetSuitesWhenNotExistsAndCreateIfAbsentWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/suites")
                        .withQueryStringParameter("asTreeView", "true"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/suites/tree_single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/suites")
                        .withBody(regex(".*\"name\":\"Feature 1/abc\".+" +
                                "\"suiteType\":\"staticTestSuite\".+" +
                                "\"parentSuite\":\\{\"id\":\"56984\".*")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/suites/single.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        TestPlan plan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"))
                .id("56983").rootSuite(new TestSuite().id("56984").name("Wakamiti Test Plan"));

        List<TestSuite> suites = List.of(
                new TestSuite().name("Wakamiti Test Plan"),
                new TestSuite().name("Feature 1/abc").parent(new TestSuite().name("Wakamiti Test Plan"))
        );

        // act
        List<TestSuite> remoteSuites = client.getTestSuites(plan, suites, true);
        logResult(remoteSuites);

        // check
        requests.forEach(mock::verify);
        assertThat(remoteSuites).hasSize(2);
        assertThat(remoteSuites.get(0))
                .hasFieldOrPropertyWithValue("id", "56984")
                .hasFieldOrPropertyWithValue("name", "Wakamiti Test Plan");
        assertThat(remoteSuites.get(1))
                .hasFieldOrPropertyWithValue("id", "56985")
                .hasFieldOrPropertyWithValue("name", "Feature 1")
                .hasFieldOrPropertyWithValue("parent", remoteSuites.get(0));
    }

    @Test
    public void testGetSuitesWhenNotExistsAndNoCreateIfAbsentWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/suites")
                        .withQueryStringParameter("asTreeView", "true"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/suites/tree_single.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        TestPlan plan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"))
                .id("56983").rootSuite(new TestSuite().id("56984").name("Wakamiti Test Plan"));

        List<TestSuite> suites = List.of(
                new TestSuite().name("Wakamiti Test Plan"),
                new TestSuite().name("Feature 1").parent(new TestSuite().name("Wakamiti Test Plan"))
        );

        // act
        List<TestSuite> remoteSuites = client.getTestSuites(plan, suites, false);
        logResult(remoteSuites);

        // check
        requests.forEach(mock::verify);
        assertThat(remoteSuites).hasSize(1);
        assertThat(remoteSuites.get(0))
                .hasFieldOrPropertyWithValue("id", "56984")
                .hasFieldOrPropertyWithValue("name", "Wakamiti Test Plan");
    }


    @Test
    public void testGetTestCasesWhenExistsWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        for (String suite : List.of("56984", "56985")) {
            mockServer(
                    request()
                            .withMethod("GET")
                            .withPath(format("/ST/ACS/_apis/testplan/Plans/56983/suites/{}/TestCase", suite))
                            .withQueryStringParameter("witFields", join(List.of(TITLE, TAGS), ",")),
                    response()
                            .withStatusCode(200)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withBody(resource(format("server/testcases/list_{}.json", suite)))
            ).ifPresent(requests::add);
        }

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");


        TestSuite root = new TestSuite().id("56984").name("Wakamiti Test Plan");
        TestPlan plan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"))
                .id("56983").rootSuite(root);

        List<TestSuite> suites = List.of(root, new TestSuite().id("56985").name("Feature 1").parent(root));

        List<TestCase> testCases = List.of(
                new TestCase().name("Scenario A").suite(suites.get(1)).order(0).tag("ID-1"),
                new TestCase().name("Scenario B").suite(suites.get(1)).order(1).tag("ID-2"),
                new TestCase().name("Scenario C").suite(suites.get(1)).order(2).tag("ID-3")
        );

        // act
        List<TestCase> remoteTestCases = client.getTestCases(plan, suites, testCases, true);
        logResult(remoteTestCases);

        // check
        requests.forEach(mock::verify);
        assertThat(remoteTestCases).hasSize(3);
        assertThat(remoteTestCases.get(0))
                .hasFieldOrPropertyWithValue("id", "56977")
                .hasFieldOrPropertyWithValue("name", "Scenario A");
        assertThat(remoteTestCases.get(1))
                .hasFieldOrPropertyWithValue("id", "56978")
                .hasFieldOrPropertyWithValue("name", "Scenario B");
        assertThat(remoteTestCases.get(2))
                .hasFieldOrPropertyWithValue("id", "56979")
                .hasFieldOrPropertyWithValue("name", "Scenario C");
        remoteTestCases.forEach(t -> {
            assertThat(t.pointAssignments()).hasSize(1);
            assertThat(t.pointAssignments().get(0))
                    .hasFieldOrPropertyWithValue("configurationId", "29");
        });
    }

    @Test
    public void testGetTestCasesWhenNotExistsAndCreateIfAbsentWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/wit/workitemtypecategories/Microsoft.TestCaseCategory"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/categories/single.json"))
        ).ifPresent(requests::add);
        mockServer(
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        for (String suite : List.of("56984", "56985", "56986")) {
            mockServer(
                    request()
                            .withMethod("GET")
                            .withPath(format("/ST/ACS/_apis/testplan/Plans/56983/suites/{}/TestCase", suite))
                            .withQueryStringParameter("witFields", join(List.of(TITLE, TAGS), ",")),
                    response()
                            .withStatusCode(200)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withBody("{\"value\":[],\"count\":0}")
            ).ifPresent(requests::add);
        }

        TestSuite root = new TestSuite().id("56984").name("Wakamiti Test Plan");
        TestPlan plan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"))
                .id("56983").rootSuite(root);
        List<TestSuite> suites = List.of(root,
                new TestSuite().id("56985").name("Feature 1").parent(root),
                new TestSuite().id("56986").name("Feature 2").parent(root)
        );
        List<TestCase> testCases = List.of(
                new TestCase().name("Scenario A").suite(suites.get(1)).order(0).tag("ID-1"),
                new TestCase().name("Scenario B").suite(suites.get(1)).order(1).tag("ID-2"),
                new TestCase().name("Scenario C").suite(suites.get(1)).order(2).tag("ID-3"),
                new TestCase().name("Scenario X").suite(suites.get(2)).order(0).tag("ID-1A"),
                new TestCase().name("Scenario Y").suite(suites.get(2)).order(1).tag("ID-2A"),
                new TestCase().name("Scenario Z").suite(suites.get(2)).order(2).tag("ID-3A")
        );

        for (TestCase t : testCases) {
            mockServer(
                    request()
                            .withMethod("POST")
                            .withContentType(MediaType.APPLICATION_JSON_PATCH_JSON)
                            .withPath("/ST/ACS/_apis/wit/workitems/.+")
                            .withBody(regex(format(".+" +
                                            "\\{\"op\":\"add\",\"path\":\"/fields/System\\.Title\",\"value\":\"{}\"}.+" +
                                            "\\{\"op\":\"add\",\"path\":\"/fields/System\\.Tags\",\"value\":\"{}\"}.+" +
                                            "\\{\"op\":\"add\",\"path\":\"/fields/System\\.AreaPath\",\"value\":\"ACS\"}.+" +
                                            "\\{\"op\":\"add\",\"path\":\"/fields/System\\.IterationPath\",\"value\":\"ACS\\\\\\\\Iteración 1\"}.+",
                                    t.name(), t.tag()))),
                    response()
                            .withStatusCode(200)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withBody(resource(format("server/workitems/post_{}.json", t.tag())))
            ).ifPresent(requests::add);
        }

        for (Map.Entry<String, List<String>> e : map(
                "56985", List.of("56977", "56978", "56979"),
                "56986", List.of("56980", "56981", "56982")
        ).entrySet()) {
            String json = e.getValue().stream().map(t -> format("{\"workItem\":{\"id\":\"{}\"}}", t))
                    .collect(joining(",", "[", "]"));
            mockServer(
                    request()
                            .withMethod("POST")
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withPath(format("/ST/ACS/_apis/testplan/Plans/56983/Suites/{}/TestCase", e.getKey()))
                            .withBody(json(json, MatchType.ONLY_MATCHING_FIELDS)),
                    response()
                            .withStatusCode(200)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withBody(resource(format("server/testcases/list_{}.json", e.getKey())))
            ).ifPresent(requests::add);
        }


        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");
        client.settings();

        // act
        List<TestCase> remoteTestCases = client.getTestCases(plan, suites, testCases, true);
        logResult(remoteTestCases);

        // check
        requests.forEach(mock::verify);
        assertThat(remoteTestCases).hasSize(6);
        assertThat(remoteTestCases.get(0))
                .hasFieldOrPropertyWithValue("id", "56977")
                .hasFieldOrPropertyWithValue("name", "Scenario A")
                .hasFieldOrPropertyWithValue("tag", "ID-1");
        assertThat(remoteTestCases.get(1))
                .hasFieldOrPropertyWithValue("id", "56978")
                .hasFieldOrPropertyWithValue("name", "Scenario B")
                .hasFieldOrPropertyWithValue("tag", "ID-2");
        assertThat(remoteTestCases.get(2))
                .hasFieldOrPropertyWithValue("id", "56979")
                .hasFieldOrPropertyWithValue("name", "Scenario C")
                .hasFieldOrPropertyWithValue("tag", "ID-3");
        assertThat(remoteTestCases.get(3))
                .hasFieldOrPropertyWithValue("id", "56980")
                .hasFieldOrPropertyWithValue("name", "Scenario X")
                .hasFieldOrPropertyWithValue("tag", "ID-1A");
        assertThat(remoteTestCases.get(4))
                .hasFieldOrPropertyWithValue("id", "56981")
                .hasFieldOrPropertyWithValue("name", "Scenario Y")
                .hasFieldOrPropertyWithValue("tag", "ID-2A");
        assertThat(remoteTestCases.get(5))
                .hasFieldOrPropertyWithValue("id", "56982")
                .hasFieldOrPropertyWithValue("name", "Scenario Z")
                .hasFieldOrPropertyWithValue("tag", "ID-3A");
        remoteTestCases.forEach(t -> {
            assertThat(t.pointAssignments()).hasSize(1);
            assertThat(t.pointAssignments().get(0))
                    .hasFieldOrPropertyWithValue("configurationId", "29");
        });
    }

    @Test
    public void testGetTestCasesWhenNotExistsAndNotCreateIfAbsentWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/suites/56984/TestCase")
                        .withQueryStringParameter("witFields", join(List.of(TITLE, TAGS), ",")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/testcases/list_56984.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/suites/56985/TestCase")
                        .withQueryStringParameter("witFields", join(List.of(TITLE, TAGS), ",")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/testcases/list_56985.json"))
        ).ifPresent(requests::add);

        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");


        TestSuite root = new TestSuite().id("56984").name("Wakamiti Test Plan");
        TestPlan plan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"))
                .id("56983").rootSuite(root);

        List<TestSuite> suites = List.of(root, new TestSuite().id("56985").name("Feature 1").parent(root));

        List<TestCase> testCases = List.of(
                new TestCase().name("Scenario A").suite(suites.get(1)).order(0).tag("ID-1"),
                new TestCase().name("Scenario B").suite(suites.get(1)).order(1).tag("ID-2"),
                new TestCase().name("Scenario C").suite(suites.get(1)).order(2).tag("ID-3"),
                new TestCase().name("Scenario D").suite(suites.get(1)).order(3).tag("ID-4")
        );

        // act
        List<TestCase> remoteTestCases = client.getTestCases(plan, suites, testCases, false);
        logResult(remoteTestCases);

        // check
        requests.forEach(mock::verify);
        assertThat(remoteTestCases).hasSize(3);
        assertThat(remoteTestCases.get(0))
                .hasFieldOrPropertyWithValue("id", "56977")
                .hasFieldOrPropertyWithValue("name", "Scenario A");
        assertThat(remoteTestCases.get(1))
                .hasFieldOrPropertyWithValue("id", "56978")
                .hasFieldOrPropertyWithValue("name", "Scenario B");
        assertThat(remoteTestCases.get(2))
                .hasFieldOrPropertyWithValue("id", "56979")
                .hasFieldOrPropertyWithValue("name", "Scenario C");
    }

    @Test
    public void testGetTestCasesWhenExistsAndHasChangedWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();

        for (String suite : List.of("56984", "56985", "56986")) {
            mockServer(
                    request()
                            .withMethod("GET")
                            .withPath(format("/ST/ACS/_apis/testplan/Plans/56983/Suites/{}/TestCase", suite))
                            .withQueryStringParameter("witFields", join(List.of(TITLE, TAGS), ",")),
                    response()
                            .withStatusCode(200)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withBody(resource(format("server/testcases/list_{}.json", suite)))
            ).ifPresent(requests::add);
        }

        TestSuite root = new TestSuite().id("56984").name("Wakamiti Test Plan");
        TestPlan plan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"))
                .id("56983").rootSuite(root);

        List<TestSuite> suites = List.of(root,
                new TestSuite().id("56985").name("Feature 1").parent(root),
                new TestSuite().id("56986").name("Feature 2").parent(root)
        );
        List<Pair<String, TestCase>> testCases = List.of(
                new Pair<>("56977", new TestCase().name("Scenario AA").suite(suites.get(2)).order(0).tag("ID-1")),
                new Pair<>("56978", new TestCase().name("Scenario BB").suite(suites.get(2)).order(1).tag("ID-2")),
                new Pair<>("56979", new TestCase().name("Scenario CC").suite(suites.get(2)).order(4).tag("ID-3")),
                new Pair<>("56980", new TestCase().name("Scenario XX").suite(suites.get(1)).order(0).tag("ID-1A")),
                new Pair<>("56981", new TestCase().name("Scenario YY").suite(suites.get(1)).order(1).tag("ID-2A")),
                new Pair<>("56982", new TestCase().name("Scenario ZZ").suite(suites.get(1)).order(2).tag("ID-3A"))
        );

        for (Pair<String, TestCase> p : testCases) {
            mockServer(
                    request()
                            .withMethod("PATCH")
                            .withContentType(MediaType.APPLICATION_JSON_PATCH_JSON)
                            .withPath(format("/ST/ACS/_apis/wit/workitems/{}", p.key()))
                            .withBody(regex(format(".+" +
                                            "\\{\"op\":\"replace\",\"path\":\"/fields/System\\.Title\",\"value\":\"{}\"}.+",
                                    p.value().name()))),
                    response()
                            .withStatusCode(200)
                            .withContentType(MediaType.APPLICATION_JSON)
                            .withBody(resource(format("server/workitems/patch_{}.json", p.value().tag())))
            ).ifPresent(requests::add);
        }

        mockServer(
                request()
                        .withMethod("DELETE")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/Suites/56985/TestCase/56977,56978,56979"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("[]")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("DELETE")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/Suites/56986/TestCase/56980,56981,56982"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("[]")
        ).ifPresent(requests::add);

        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/Suites/56985/TestCase/56980,56981,56982"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("[]")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/ST/ACS/_apis/testplan/Plans/56983/Suites/56986/TestCase/56977,56978,56979"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("[]")
        ).ifPresent(requests::add);


        AzureApi client = new AzureApi(new URL(BASE_URL), x -> x, null)
                .organization("ST").projectBase("ACS").version("6.0-preview");

        // act
        List<TestCase> remoteTestCases = client.getTestCases(plan, suites,
                testCases.stream().map(Pair::value).collect(toList()), true);
        logResult(remoteTestCases);

        // check
        requests.forEach(mock::verify);
        assertThat(remoteTestCases).hasSize(6);
        assertThat(remoteTestCases.get(0))
                .hasFieldOrPropertyWithValue("id", "56977")
                .hasFieldOrPropertyWithValue("name", "Scenario AA")
                .hasFieldOrPropertyWithValue("tag", "ID-1")
                .extracting(t -> t.suite().id()).isEqualTo("56986");
        assertThat(remoteTestCases.get(1))
                .hasFieldOrPropertyWithValue("id", "56978")
                .hasFieldOrPropertyWithValue("name", "Scenario BB")
                .hasFieldOrPropertyWithValue("tag", "ID-2")
                .extracting(t -> t.suite().id()).isEqualTo("56986");
        assertThat(remoteTestCases.get(2))
                .hasFieldOrPropertyWithValue("id", "56979")
                .hasFieldOrPropertyWithValue("name", "Scenario CC")
                .hasFieldOrPropertyWithValue("tag", "ID-3")
                .extracting(t -> t.suite().id()).isEqualTo("56986");
        assertThat(remoteTestCases.get(3))
                .hasFieldOrPropertyWithValue("id", "56980")
                .hasFieldOrPropertyWithValue("name", "Scenario XX")
                .hasFieldOrPropertyWithValue("tag", "ID-1A")
                .extracting(t -> t.suite().id()).isEqualTo("56985");
        assertThat(remoteTestCases.get(4))
                .hasFieldOrPropertyWithValue("id", "56981")
                .hasFieldOrPropertyWithValue("name", "Scenario YY")
                .hasFieldOrPropertyWithValue("tag", "ID-2A")
                .extracting(t -> t.suite().id()).isEqualTo("56985");
        assertThat(remoteTestCases.get(5))
                .hasFieldOrPropertyWithValue("id", "56982")
                .hasFieldOrPropertyWithValue("name", "Scenario ZZ")
                .hasFieldOrPropertyWithValue("tag", "ID-3A")
                .extracting(t -> t.suite().id()).isEqualTo("56985");
    }


    private void logResult(Object o) {
        LOGGER.debug("Result: {}", o);
    }

    private Optional<HttpRequest> mockServer(HttpRequest expected, HttpResponse response) {
        mock.when(expected, Times.once()).respond(response);
        return Optional.of(expected);
    }

    private String resource(String resource) throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource));
    }

}
