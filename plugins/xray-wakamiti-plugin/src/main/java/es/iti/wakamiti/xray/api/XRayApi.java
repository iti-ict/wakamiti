package es.iti.wakamiti.xray.api;

import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.XRayPlan;
import es.iti.wakamiti.xray.model.XRayTestCase;
import org.slf4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class XRayApi extends BaseApi {

    private static final String BEARER = "Bearer ";
    private static final String API_GRAPHQL = "/api/v2/graphql";
    private static final String AUTH_URL = "/api/v1/authenticate";

    private final String project;
    private final Logger logger;

    public XRayApi(URL baseURL, String clientId, String clientSecret, String project, Logger logger) {
        super(baseURL, AUTH_URL, clientId, clientSecret, logger);
        this.project = project;
        this.logger = logger;
    }

    public Optional<XRayPlan> getTestPlan(String issueId) {
        String query = "getTestPlan(issueId: " + issueId + " ) {" +
                "        issueId" +
                "        projectId" +
                "        jira(fields: [\"summary\"])" +
                "        }" +
                "    }";

        String response = post(API_GRAPHQL, query);

        String isPresent = extract(response, "$.data.getTestPlan", "Cannot find the test plan");
        if (isPresent == null) {
            return Optional.empty();
        }

        String key = extract(response, "$.data.getTestPlan.issueId", "Cannot find the attribute 'id' of the test plan");
        String summary = extract(response, "$.data.getTestPlan.jira.summary", "Cannot find the attribute 'summary' of the test plan");
        String projectId = extract(response, "$.data.getTestPlan.projectId", "Cannot find the attribute 'projectId' of the test plan");
        List<XRayTestCase> tests = extractList(response, "$.data.getTestPlan.tests.results[*]", "Cannot find the attribute 'tests' of the test plan");

        return Optional.of(new XRayPlan(key, summary, projectId, tests));
    }

    public XRayPlan createTestPlan(String title) {
        String mutation =
                "mutation {" +
                        "    createTestPlan(" +
                        "        jira: {" +
                        "            fields: { summary: " + title + ", project: {key: " + project + "} }" +
                        "        }" +
                        "    ) {" +
                        "        testPlan {" +
                        "            issueId" +
                        "            projectId" +
                        "            jira(fields: [\"key\", \"summary\"])" +
                        "        }" +
                        "        warnings" +
                        "    }" +
                        "}";

        String response = post(API_GRAPHQL, mutation);

        String key = extract(response, "$.data.createTestPlan.testPlan.issueId", "Cannot find the attribute 'id' of the test plan");
        String summary = extract(response, "$.data.createTestPlan.testPlan.jira.summary", "Cannot find the attribute 'summary' of the test plan");
        String projectId = extract(response, "$.data.createTestPlan.testPlan.projectId", "Cannot find the attribute 'projectId' of the test plan");
        List<XRayTestCase> tests = extractList(response, "$.data.getTestPlan.tests.results[*]", "Cannot find the attribute 'tests' of the test plan");

        return new XRayPlan(key, summary, projectId, tests);

    }

    public Optional<XRayTestCase> getTestCase(String issueId) {
        String query = "getTest(issueId: " + issueId + ") {" +
                "        issueId" +
                "        jira(fields: [\"key\", \"summary\", \"labels\"])" +
                "        folder {" +
                "            name" +
                "        }" +
                "        testType {" +
                "            name" +
                "            kind" +
                "        }" +
                "        gherkin" +
                "    }";

        String response = post(API_GRAPHQL, query);

        String isPresent = extract(response, "$.data.getTest", "Cannot find the test");
        if (isPresent == null) {
            return Optional.empty();
        }

        String key = extract(response, "$.data.getTest.issueId", "Cannot find the attribute 'id' of the test plan");
        JiraIssue issue = extractList(response, "$.data.getTest.jira", "Cannot find the attribute 'jira' of the test plan");
        String gherkin = extract(response, "$.data.getTest.gherkin", "Cannot find the attribute 'gherkin' of the test plan");

        return Optional.of(new XRayTestCase(key, issue, gherkin));
    }

    public void addTestsToPlan(List<String> createdIssues, XRayPlan remotePlan) {
        String mutation =
                "mutation {" +
                        "    addTestsToTestPlan(" +
                        "        issueId: " + remotePlan.getId() + "," +
                        "        testIssueIds: [" + String.join(",", createdIssues) + "]" +
                        "    ) {" +
                        "        addedTests" +
                        "        warning" +
                        "    }" +
                        "}";

        post(API_GRAPHQL, mutation);
    }
}
