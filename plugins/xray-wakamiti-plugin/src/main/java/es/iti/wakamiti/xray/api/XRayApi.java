package es.iti.wakamiti.xray.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.TypeRef;
import es.iti.wakamiti.xray.internal.WakamitiXRayException;
import es.iti.wakamiti.xray.internal.XRayRequestType;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.XRayPlan;
import es.iti.wakamiti.xray.model.XRayTestCase;
import es.iti.wakamiti.xray.model.XRayTestSet;
import org.slf4j.Logger;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.JsonUtils.read;

public class XRayApi extends BaseApi {

    private static final String BEARER = "Bearer ";
    private static final String API_GRAPHQL = "/api/v2/graphql";
    private static final String AUTH_URL = "/api/v1/authenticate";

    private final String project;
    private final Logger logger;

    private static final ObjectMapper mapper = new ObjectMapper();

    public XRayApi(URL baseURL, String clientId, String clientSecret, String project, Logger logger) {
        super(baseURL, AUTH_URL, clientId, clientSecret, logger);
        this.project = project;
        this.logger = logger;
    }

    public Optional<XRayPlan> getTestPlan(String issueId) {

        String query = request(XRayRequestType.QUERY, "query { " +
                "   getTestPlan(issueId: \"" + issueId + "\" ) {" +
                "        issueId" +
                "        projectId" +
                "        jira(fields: [\"summary\"])" +
                "        }" +
                "    }");

        String response = post(API_GRAPHQL, query);

        String isPresent = extract(response, "$.data.getTestPlan", "Cannot find the test plan");
        if (isPresent == null) {
            return Optional.empty();
        }

        String key = extract(response, "$.data.getTestPlan.issueId", "Cannot find the attribute 'id' of the test plan");
        String summary = extract(response, "$.data.getTestPlan.jira.summary", "Cannot find the attribute 'summary' of the test plan");
        String projectId = extract(response, "$.data.getTestPlan.projectId", "Cannot find the attribute 'projectId' of the test plan");
//        List<XRayTestCase> tests = extractList(response, "$.data.getTestPlan.tests.results[*]", "Cannot find the attribute 'tests' of the test plan");

        return Optional.of(new XRayPlan(key, summary, projectId, Collections.emptyList()));
    }

    public XRayPlan createTestPlan(String title) {
        String mutation = request(XRayRequestType.MUTATION,
                "mutation {" +
                        "    createTestPlan(" +
                        "        jira: {" +
                        "            fields: { summary: \"" + title + "\", project: {key: \"" + project + "\"} }" +
                        "        }" +
                        "    ) {" +
                        "        testPlan {" +
                        "            issueId" +
                        "            projectId" +
                        "            jira(fields: [\"key\", \"summary\"])" +
                        "        }" +
                        "        warnings" +
                        "    }" +
                        "}");

        String response = post(API_GRAPHQL, mutation);

        String key = extract(response, "$.data.createTestPlan.testPlan.issueId", "Cannot find the attribute 'id' of the test plan");
        String summary = extract(response, "$.data.createTestPlan.testPlan.jira.summary", "Cannot find the attribute 'summary' of the test plan");
        String projectId = extract(response, "$.data.createTestPlan.testPlan.projectId", "Cannot find the attribute 'projectId' of the test plan");
//        List<XRayTestCase> tests = extractList(response, "$.data.getTestPlan.tests.results[*]", "Cannot find the attribute 'tests' of the test plan");

        return new XRayPlan(key, summary, projectId, Collections.emptyList());

    }

    public Optional<XRayTestCase> getTestCase(String issueId) {
        String query = request(XRayRequestType.QUERY, "query { " +
                "   getTest(issueId: \"" + issueId + "\") {" +
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
                "    }" +
                "}");

        String response = post(API_GRAPHQL, query);

        try {
            JsonNode node = mapper.readTree(response);

            XRayTestCase testCase = read(node, "$.data.getTest", XRayTestCase.class);

            return Optional.ofNullable(testCase);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void addTestsToPlan(List<String> createdIssues, XRayPlan remotePlan) {
        String mutation = request(XRayRequestType.MUTATION,
                "mutation {" +
                        "    addTestsToTestPlan(" +
                        "        issueId: " + remotePlan.getId() + "," +
                        "        testIssueIds: [" + String.join(",", createdIssues) + "]" +
                        "    ) {" +
                        "        addedTests" +
                        "        warning" +
                        "    }" +
                        "}");

        post(API_GRAPHQL, mutation);
    }

    public List<XRayTestSet> getTestSets() {
        String query = request(XRayRequestType.QUERY, "query { " +
                "   getTestSets(limit: 100) {" +
                "        total" +
                "        start" +
                "        limit" +
                "        results {" +
                "            issueId" +
                "            jira(fields: [\"key\", \"summary\", \"labels\"])" +
                "        }" +
                "    }" +
                "}");

        String response = post(API_GRAPHQL, query);

        String isPresent = extract(response, "$.data.getTestSets", "Cannot find the test");
        if (isPresent == null) {
            return Collections.emptyList();
        }


        try {
            return read(mapper.readTree(response), "$.data.getTestSets.results", new TypeRef<>() {
            });
        } catch (JsonProcessingException e) {
            throw new WakamitiXRayException(e.getMessage());
        }
    }

    public List<XRayTestSet> createTestSets(List<XRayTestSet> newTestSets) {
        return newTestSets.stream().map(xrayTestSet -> {
            String mutation = request(XRayRequestType.MUTATION,
                    "mutation {" +
                            "    createTestSet(" +
//                            "        testIssueIds: [\"54321\"]" +
                            "        jira: {" +
                            "            fields: { summary: \"" + xrayTestSet.getJira().getSummary() + "\", project: {key: \"" + project + "\" } }" +
                            "        }" +
                            "    ) {" +
                            "        testSet {" +
                            "            issueId" +
                            "            jira(fields: [\"key\", \"summary\"])" +
                            "        }" +
                            "        warnings" +
                            "    }" +
                            "}");

            String response = post(API_GRAPHQL, mutation);

            String issueId = extract(response, "$.data.getTest.issueId", "Cannot find the attribute 'id' of the test plan");
            JiraIssue issue = extractList(response, "$.data.getTest.jira", "Cannot find the attribute 'jira' of the test plan");

            return new XRayTestSet()
                    .issueId(issueId)
                    .issue(issue);

        }).collect(Collectors.toList());
    }

    private String request(XRayRequestType type, String query) {
        logger.debug("Sending {} with request:\n {}", type.getName(), query);
        return toJSON(Map.of(type.getName(), query));
    }

}
