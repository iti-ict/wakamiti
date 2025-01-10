package es.iti.wakamiti.xray.test.api;

import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.xray.api.XRayApi;
import es.iti.wakamiti.xray.model.*;
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
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.subString;

public class TestPlanApiTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(TestPlanApiTest.class);

    private static final Integer PORT = 4321;
    private static final String BASE_URL = MessageFormat.format("http://localhost:{0}", String.valueOf(PORT));

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
    public void testAuthenticationWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        requests.forEach(mock::verify);

    }

    @Test
    public void testCreateTestWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("createTest")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/createTest.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        TestCase testCase = new TestCase()
                .issue(new JiraIssue()
                        .summary("Test Summary")
                        .labels(List.of("label1", "label2")))
                .gherkin("Gherkin");

        xRayApi.createTestCases(new TestPlan(), List.of(testCase), "");

        requests.forEach(mock::verify);
    }

    @Test
    public void testCreateTestPlanWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("createTestPlan")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/createTestPlan.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();


        TestPlan testPlan = xRayApi.createTestPlan("Test Summary");

        assertThat(testPlan).isNotNull();
        assertThat(testPlan.getJira().getSummary()).isEqualTo("Test Summary");

        requests.forEach(mock::verify);
    }

    @Test
    public void testCreateTestExecutionWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("createTestExecution")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/createTestExecution.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        TestExecution testExecution = xRayApi.createTestExecution("Test Execution Summary", List.of("1"), "WAK");

        assertThat(testExecution).isNotNull();
        assertThat(testExecution.getJira().getSummary()).isEqualTo("Test Execution Summary");

        requests.forEach(mock::verify);
    }

    @Test
    public void testCreateTestSetsWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("createTestSet")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/createTestSet.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        TestSet testSet = new TestSet()
                .issue(new JiraIssue()
                        .summary("Test Set Summary"));

        List<TestSet> testSets = xRayApi.createTestSets(List.of(testSet));

        assertThat(testSets)
                .isNotNull()
                .hasSize(1);

        assertThat(testSets.get(0).getJira().getSummary()).isEqualTo("Test Set Summary");

        requests.forEach(mock::verify);
    }

    @Test
    public void testGetTestWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("getTest")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/getTest.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        Optional<TestCase> testCase = xRayApi.getTestCase("10070");

        assertThat(testCase).isPresent();

        assertThat(testCase.get().getJira().getSummary()).isEqualTo("Test Case Summary");

        requests.forEach(mock::verify);
    }

    @Test
    public void testGetTestPlansWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("getTestPlans")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/getTestPlans.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        List<TestPlan> testPlans = xRayApi.getTestPlans();

        assertThat(testPlans)
                .isNotNull()
                .hasSize(1);

        assertThat(testPlans.get(0).getJira().getSummary()).isEqualTo("Test Plan Summary");

        requests.forEach(mock::verify);
    }

    @Test
    public void testGetTestSetsWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("getTestSets")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/getTestSets.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        List<TestSet> testSets = xRayApi.getTestSets();

        assertThat(testSets)
                .isNotNull()
                .hasSize(1);

        assertThat(testSets.get(0).getJira().getSummary()).isEqualTo("Test Set Summary");

        requests.forEach(mock::verify);
    }

    @Test
    public void testUpdateTestRunStatusWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("getTestRuns")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/getTestRuns.json"))
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("updateTestRunStatus")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/updateTestRunStatus.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        TestCase testCase = new TestCase()
                .issueId("10070")
                .status("RUNNING")
                .issue(new JiraIssue()
                        .summary("Test Summary")
                        .labels(List.of("label1", "label2")))
                .gherkin("Gherkin");

        xRayApi.updateTestRunStatus(List.of(testCase));

        requests.forEach(mock::verify);
    }

    @Test
    public void testAddTestsToPlanWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("addTestsToTestPlan")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/addTestsToTestPlan.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        TestPlan testPlan = new TestPlan()
                .id("10000");

        xRayApi.addTestsToPlan(List.of("10070"), testPlan);

        requests.forEach(mock::verify);
    }

    @Test
    public void testAddTestsToTestSetWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("addTestsToTestSet")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/addTestsToTestSet.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        TestSet testSet = new TestSet()
                .issue(new JiraIssue()
                        .summary("Test Set Summary"));

        TestCase testCase = new TestCase()
                .issueId("10070")
                .status("RUNNING")
                .issue(new JiraIssue()
                        .summary("Test Summary")
                        .labels(List.of("label1", "label2")))
                .gherkin("Gherkin")
                .testSetList(List.of(testSet));


        xRayApi.addTestsToSets(List.of(testCase), List.of(testSet));

        requests.forEach(mock::verify);
    }

    @Test
    public void testAddTestExecutionsToTestPlanWithSuccess() throws IOException {
        List<HttpRequest> requests = new ArrayList<>();
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/authenticate"),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("\"token\"")
        ).ifPresent(requests::add);
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/api/v2/graphql")
                        .withBody(subString("addTestExecutionsToTestPlan")),
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(resource("server/xray/addTestExecutionsToTestPlan.json"))
        ).ifPresent(requests::add);

        XRayApi xRayApi = new XRayApi(new URL(BASE_URL), "clientId", "clientSecret", "WAK", LOGGER);
        assertThat(xRayApi).isNotNull();

        TestPlan testPlan = new TestPlan()
                .id("10000");

        xRayApi.addTestExecutionsToTestPlan("12345", testPlan);

        requests.forEach(mock::verify);
    }

    private Optional<HttpRequest> mockServer(HttpRequest expected, HttpResponse response) {
        mock.when(expected, Times.once()).respond(response);
        return Optional.of(expected);
    }

    private String resource(String resource) throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(resource), Charset.defaultCharset());
    }
}