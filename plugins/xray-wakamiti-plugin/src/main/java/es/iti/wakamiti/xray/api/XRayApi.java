package es.iti.wakamiti.xray.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.TypeRef;
import es.iti.wakamiti.xray.model.*;
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

    public Optional<TestPlan> getTestPlan(String issueId) {

        String query = query("query { " +
                "   getTestPlan(issueId: \"" + issueId + "\" ) {" +
                "        issueId" +
                "        projectId" +
                "        jira(fields: [\"summary\"])" +
                "        }" +
                "    }");

        JsonNode response = post(API_GRAPHQL, query);

        TestPlan testPlan = read(response, "$.data.getTestPlan", TestPlan.class);

        return Optional.ofNullable(testPlan);
    }

    public List<TestPlan> getTestPlans() {

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

    public TestPlan createTestPlan(String title) {
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

        return read(response, "$.data.createTestPlan.testPlan", TestPlan.class);
    }

    public Optional<TestCase> getTestCase(String issueId) {
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

        TestCase testCase = read(response, "$.data.getTest", TestCase.class);

        return Optional.ofNullable(testCase);
    }

    public void addTestsToPlan(List<String> createdIssues, TestPlan remotePlan) {
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

    public void addTestExecutionsToTestPlan(String testExecutionIssue, TestPlan remotePlan) {
        String mutation = query(
                "mutation {" +
                        "    addTestExecutionsToTestPlan(" +
                        "        issueId: \"" + remotePlan.getIssueId() + "\", " +
                        "        testExecIssueIds: [\"" + testExecutionIssue + "\"]" +
                        "    ) {" +
                        "        addedTestExecutions" +
                        "        warning" +
                        "    }" +
                        "}");

        post(API_GRAPHQL, mutation);
    }

    public void addTestsToSets(List<TestCase> tests, List<TestSet> remoteTestSets) {
        tests.forEach(xRayTestCase -> {

            Optional<TestSet> optionalXRayTestSet = remoteTestSets.stream()
                    .filter(xRayTestSet -> xRayTestCase.getTestSetList()
                            .stream().map(TestSet::getJira)
                            .map(JiraIssue::getSummary)
                            .anyMatch(s -> s.equals(xRayTestSet.getJira().getSummary())))
                    .findFirst();

            if (optionalXRayTestSet.isPresent()) {
                String mutation = query(
                        "mutation {" +
                                "    addTestsToTestSet(" +
                                "        issueId: \"" + optionalXRayTestSet.get().getIssueId() + "\", " +
                                "        testIssueIds: [\"" + xRayTestCase.getIssueId() + "\"]" +
                                "    ) {" +
                                "        addedTests" +
                                "        warning" +
                                "    }" +
                                "}");

                post(API_GRAPHQL, mutation);
            }

        });
    }

    public List<TestSet> getTestSets() {
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

        List<TestSet> list = read(response, "$.data.getTestSets.results", new TypeRef<>() {
        });

        for (int i = 0; i < list.size(); i++) {
            List<TestCase> testCases = read(response, "$.data.getTestSets.results[" + i + "].tests.results", new TypeRef<>() {
            });
            list.get(i).testCases(testCases);
        }

        return list;
    }

    public List<TestSet> createTestSets(List<TestSet> newTestSets) {
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

            return read(response, "$.data.createTestSet.testSet", TestSet.class);

        }).collect(Collectors.toList());
    }

    private String query(String query) {
        return toJSON(Map.of(QUERY, query));
    }

    public List<TestCase> createTestCases(TestPlan remotePlan, List<TestCase> newTests, String project) {
        return newTests.stream().map(test -> {

            StringBuilder jirafields = new StringBuilder("fields: { summary:\"" + test.getJira().getSummary() + "\", project: {key: \"" + project + "\"}");
            if (!test.getJira().getLabels().isEmpty()) {
                jirafields.append(", labels: [\"").append(test.getJira().getLabels().get(0)).append("\"]");
            }
            jirafields.append("}");

            String mutation = query("mutation {" +
                    "    createTest(" +
                    "        testType: { name: \"Cucumber\" }," +
                    "        gherkin: \"" + test.getGherkin() + "\"," +
//                    "        unstructured: \"Perform exploratory tests on calculator.\"," +
                    "        jira: {" + jirafields + "}" +
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

            TestCase testCase = read(response, "$.data.createTest.test", TestCase.class);
            testCase.getJira().summary(test.getJira().getSummary());
            testCase.testSetList(test.getTestSetList());
            return testCase;
        }).collect(Collectors.toList());
    }

    public TestExecution createTestExecution(String summary, List<String> createdIssues, String project) {
        String mutation = query(
                "mutation {" +
                        "    createTestExecution(" +
                        "        testIssueIds: [\"" + String.join("\",\"", createdIssues) + "\"]" +
                        "        testEnvironments: [\"Wakamiti\"]" +
                        "        jira: {" +
                        "            fields: { summary: \"" + summary + "\", project: {key: \"" + project + "\"} }" +
                        "        }" +
                        "    ) {" +
                        "        testExecution {" +
                        "            issueId" +
                        "            jira(fields: [\"key\", \"summary\"])" +
                        "        }" +
                        "        warnings" +
                        "        createdTestEnvironments" +
                        "    }" +
                        "}");

        JsonNode response = post(API_GRAPHQL, mutation);

        return read(response, "$.data.createTestExecution.testExecution", TestExecution.class);

    }

    public void updateTestRunStatus(List<TestCase> createdIssues) {

        List<String> issues = createdIssues.stream().map(TestCase::getIssueId).collect(Collectors.toList());
        String query = query(
                "query {" +
                        "    getTestRuns( testIssueIds: [\"" + String.join("\",\"", issues) + "\"], limit: 100 ) {" +
                        "        total" +
                        "        limit" +
                        "        start" +
                        "        results {" +
                        "            id" +
                        "            status {" +
                        "                name" +
                        "                color" +
                        "                description" +
                        "            }" +
                        "            testExecution {" +
                        "                issueId" +
                        "            }" +
                        "            test {" +
                        "                issueId" +
                        "            }" +
                        "        }" +
                        "    }" +
                        "}");

        JsonNode response = post(API_GRAPHQL, query);

        List<TestRun> testRuns = read(response, "$.data.getTestRuns.results", new TypeRef<>() {
        });

        createdIssues.forEach(testCase ->
                testRuns.stream()
                        .filter(testRun -> testRun.getTest().getIssueId().equals(testCase.getIssueId())
                                && !testRun.getStatus().getName().equals(testCase.getStatus()))
                        .findAny()
                        .ifPresent(testRun -> {
                            testCase.testRunId(testRun.getId());

                            String mutation = query(
                                    "mutation {" +
                                            "    updateTestRunStatus( id: \"" + testCase.getTestRunId() + "\", status: \"" + testCase.getStatus() + "\")" +
                                            "}");

                            post(API_GRAPHQL, mutation);
                        })
        );
    }
}
