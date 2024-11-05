/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api;


import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.api.model.Settings;
import es.iti.wakamiti.azure.api.model.TestPlan;
import es.iti.wakamiti.azure.api.model.TestSuite;
import es.iti.wakamiti.azure.internal.WakamitiAzureException;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
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
                        .withPath("/ST/ACS/_apis/testplan/configurations")
                        .withQueryStringParameter("api-version", "6.0-preview"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("x-ms-continuationtoken", "123")
                        .withBody(resource("server/configurations/none.json"))
        ).ifPresent(requests::add);;
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

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), "wakamiti")
                .organization("ST").project("ACS").version("6.0-preview");

        // act
        Settings settings = client.settings();
        logResult(settings);

        // check
        requests.forEach(mock::verify);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.of("UTC+01:00"));
        assertThat(settings.configuration()).isEqualTo(29);
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
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

        // act
        Settings settings = client.settings();
        logResult(settings);

        // check
        requests.forEach(mock::verify);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.of("UTC-12:00"));
        assertThat(settings.configuration()).isEqualTo(13);
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
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

        // act
        Settings settings = client.settings();
        logResult(settings);

        // check
        requests.forEach(mock::verify);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(settings.configuration()).isEqualTo(13);
    }

    @Test
    public void testSettingsWhenTZEndpointNotFoundWithSuccess() throws IOException {
        // prepare
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/multiple.json"))
        ).ifPresent(requests::add);

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

        // act
        Settings settings = client.settings();
        logResult(settings);

        // check
        requests.forEach(mock::verify);
        assertThat(settings).isNotNull();
        assertThat(settings.zoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(settings.configuration()).isEqualTo(13);
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
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/single.json"))
        ).ifPresent(requests::add);

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), "wakamiti")
                .organization("ST").project("ACS").version("6.0-preview");

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
                request().withMethod("GET").withPath("/ST/ACS/_apis/testplan/configurations"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/configurations/none.json"))
        ).ifPresent(requests::add);

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

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

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

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

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

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
                        .withBody(regex(".*\"name\":\"Wakamiti Test Plan\".*"))
                        .withBody(regex(".*\"areaPath\":\"ACS\".*"))
                        .withBody(regex(".*\"iteration\":\"ACS\\\\\\\\Iteración 1\".*")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/plans/get/single.json"))
        ).ifPresent(requests::add);

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

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

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

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
                        .withBody(resource("server/suites/none.json"))
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

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

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
                        .withBody(regex(".*\"name\":\"Feature 1\".*"))
                        .withBody(regex(".*\"suiteType\":\"staticTestSuite\".*"))
                        .withBody(regex(".*\"parentSuite\":\\{\"id\":\"56984\",\"name\":\"Wakamiti Test Plan\".*")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/suites/single.json"))
        ).ifPresent(requests::add);

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

        TestPlan plan = new TestPlan("Wakamiti Test Plan", Path.of("ACS"), Path.of("ACS/Iteración 1"))
                .id("56983").rootSuite(new TestSuite().id("56984").name("Wakamiti Test Plan"));

        List<TestSuite> suites = List.of(
                new TestSuite().name("Wakamiti Test Plan"),
                new TestSuite().name("Feature 1").parent(new TestSuite().name("Wakamiti Test Plan"))
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

        TestPlanApi client = new TestPlanApi(new URL(BASE_URL), Function.identity(), null)
                .organization("ST").project("ACS").version("6.0-preview");

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
