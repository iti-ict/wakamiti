package es.iti.wakamiti.xray.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.TypeRef;
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

    private static final String API_GRAPHQL = "/api/v2/graphql";
    private static final String AUTH_URL = "/api/v1/authenticate";
    private static final String QUERY = "query";

    private final String project;
    private final Logger logger;

    public XRayApi(URL baseURL, String clientId, String clientSecret, String project, Logger logger) {
        super(baseURL, AUTH_URL, clientId, clientSecret, logger);
        this.project = project;
        this.logger = logger;
    }

    public Optional<XRayPlan> getTestPlan(String issueId) {

        String query = query("query { " +
                "   getTestPlan(issueId: \"" + issueId + "\" ) {" +
                "        issueId" +
                "        projectId" +
                "        jira(fields: [\"summary\"])" +
                "        }" +
                "    }");

        JsonNode response = post(API_GRAPHQL, query);

        XRayPlan testPlan = read(response, "$.data.getTestPlan", XRayPlan.class);

        return Optional.ofNullable(testPlan);
    }

    public List<XRayPlan> getTestPlans() {

        String query = query("query {" +
                "    getTestPlans( limit: 100) {" +
                "        total" +
                "        start" +
                "        limit" +
                "        results {" +
                "            issueId" +
                "            jira(fields: [\"key\", \"summary\"])" +
                "        }" +
                "    }" +
                "}");

        JsonNode response = post(API_GRAPHQL, query);

        return read(response, "$.data.getTestPlans.results", new TypeRef<>() {
        });
    }

    public XRayPlan createTestPlan(String title) {
        String mutation = query(
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

        JsonNode response = post(API_GRAPHQL, mutation);

        return read(response, "$.data.createTestPlan.testPlan", XRayPlan.class);
    }

    public Optional<XRayTestCase> getTestCase(String issueId) {
        String query = query("query { " +
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

        JsonNode response = post(API_GRAPHQL, query);

        XRayTestCase testCase = read(response, "$.data.getTest", XRayTestCase.class);

        return Optional.ofNullable(testCase);
    }

    public void addTestsToPlan(List<String> createdIssues, XRayPlan remotePlan) {
        String mutation = query(
                "mutation {" +
                        "    addTestsToTestPlan(" +
                        "        issueId: \"" + remotePlan.getIssueId() + "\", " +
                        "        testIssueIds: [\"" + String.join("\",\"", createdIssues) + "\"]" +
                        "    ) {" +
                        "        addedTests" +
                        "        warning" +
                        "    }" +
                        "}");

        post(API_GRAPHQL, mutation);
    }

    public List<XRayTestSet> getTestSets() {
        String query = query("query { " +
                "   getTestSets(limit: 100) {" +
                "        total" +
                "        start" +
                "        limit" +
                "        results {" +
                "            issueId" +
                "            jira(fields: [\"key\", \"summary\", \"labels\"])" +
                "            tests(limit: 100) {" +
                "              total" +
                "              results {" +
                "                issueId" +
                "                jira(fields: [\"key\", \"summary\", \"labels\"])" +
                "              }" +
                "            }" +
                "        }" +
                "    }" +
                "}");

        JsonNode response = post(API_GRAPHQL, query);

        if (response == null) {
            return Collections.emptyList();
        }

        List<XRayTestSet> list = read(response, "$.data.getTestSets.results", new TypeRef<>() {
        });

        for (int i = 0; i < list.size(); i++) {
            List<XRayTestCase> testCases = read(response, "$.data.getTestSets.results[" + i + "].tests.results", new TypeRef<>() {
            });
            list.get(i).testCases(testCases);
        }

        return list;
    }

    public List<XRayTestSet> createTestSets(List<XRayTestSet> newTestSets) {
        return newTestSets.stream().map(xrayTestSet -> {
            String mutation = query(
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

            JsonNode response = post(API_GRAPHQL, mutation);

            return read(response, "$.data.getTest", XRayTestSet.class);

        }).collect(Collectors.toList());
    }

    private String query(String query) {
        return toJSON(Map.of(QUERY, query));
    }

    public List<String> createTestCases(XRayPlan remotePlan, List<XRayTestCase> newTests, String project) {
        return newTests.stream().map(test -> {

            String mutation = query("mutation {" +
                    "    createTest(" +
                    "        testType: { name: \"Generic\" }," +
                    "        unstructured: \"Perform exploratory tests on calculator.\"," +
                    "        jira: {" +
                    "            fields: { summary:\"" + test.getJira().getSummary() + "\", project: {key: \"" + project + "\"} }" +
                    "        }" +
                    "    ) {" +
                    "        test {" +
                    "            issueId" +
                    "            testType {" +
                    "                name" +
                    "            }" +
                    "            unstructured" +
                    "            jira(fields: [\"key\"])" +
                    "        }" +
                    "        warnings" +
                    "    }" +
                    "}");

            JsonNode response = post(API_GRAPHQL, mutation);

            XRayTestCase xRayTestCase = read(response, "$.data.createTest.test", XRayTestCase.class);

            return xRayTestCase.getIssueId();
        }).collect(Collectors.toList());
    }
}
