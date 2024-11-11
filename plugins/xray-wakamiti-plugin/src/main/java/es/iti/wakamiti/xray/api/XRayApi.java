package es.iti.wakamiti.xray.api;

import es.iti.wakamiti.xray.dto.JiraIssue;
import es.iti.wakamiti.xray.dto.XRayPlan;
import es.iti.wakamiti.xray.dto.XRayTestCase;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public class XRayApi extends BaseApi {

    private static final String BEARER = "Bearer ";
    private static final String API_GRAPHQL = "/api/v2/graphql";

    private final String project;
    private final Logger logger;

    public XRayApi(String urlBase, String token, String project, Logger logger) {
        super(urlBase, BEARER.concat(token), logger);
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
                "        steps {" +
                "            id" +
                "            data" +
                "            action" +
                "            result" +
                "            attachments {" +
                "                id" +
                "                filename" +
                "            }" +
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

}
