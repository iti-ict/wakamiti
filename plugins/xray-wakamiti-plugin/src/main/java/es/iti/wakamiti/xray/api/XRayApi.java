/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.api;


import static es.iti.wakamiti.api.util.JsonUtils.read;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.TypeRef;
import es.iti.wakamiti.xray.internal.WakamitiXRayException;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.TestCase;
import es.iti.wakamiti.xray.model.TestExecution;
import es.iti.wakamiti.xray.model.TestPlan;
import es.iti.wakamiti.xray.model.TestRun;
import es.iti.wakamiti.xray.model.TestSet;


/**
 * Provides access to the XRay Api service.
 */
public class XRayApi extends BaseApi {

    private static final String API_GRAPHQL = "/api/v2/graphql";
    private static final String AUTH_URL = "/api/v1/authenticate";
    private static final String QUERY = "query";
    private static final String DELIMITER = "\",\"";

    private final String project;

    /**
     * Creates an authenticated Xray Cloud GraphQL client.
     * <p>
     * The constructor exchanges the supplied client credentials for a Bearer token
     * and stores the default Jira project used when creating Xray entities.
     *
     * @param baseURL Xray Cloud API base URL
     * @param clientId Xray API client identifier
     * @param clientSecret Xray API client secret
     * @param project default Jira project key for newly created entities
     * @param logger logger used to trace GraphQL traffic and synchronization activity
     */
    public XRayApi(
            URL baseURL,
            String clientId,
            String clientSecret,
            String project,
            Logger logger
    ) {
        super(baseURL, AUTH_URL, clientId, clientSecret, logger);
        this.project = project;
    }

    /**
     * Retrieves the test plans visible to the authenticated Xray client.
     *
     * @return up to the first 100 plans, including their Xray issue identifiers and
     *         Jira keys and summaries
     */
    public List<TestPlan> getTestPlans() {
        String query = query("query {"
                + "    getTestPlans( limit: 100) {"
                + "        total"
                + "        start"
                + "        limit"
                + "        results {"
                + "            issueId"
                + "            jira(fields: [\"key\", \"summary\"])"
                + "        }"
                + "    }"
                + "}");

        JsonNode response = post(API_GRAPHQL, query);

        return read(response, "$.data.getTestPlans.results", new TypeRef<>() {
        });
    }

    /**
     * Creates a test plan in the configured Jira project.
     *
     * @param title Jira summary to assign to the new plan
     * @return the created plan, populated with its Xray issue identifier, project
     *         identifier, Jira key and summary
     */
    public TestPlan createTestPlan(
            String title
    ) {
        String mutation = query(
                "mutation {"
                        + "    createTestPlan("
                        + "        jira: {"
                        + getSummaryAndProject(title, project)
                        + "        }"
                        + "    ) {"
                        + "        testPlan {"
                        + "            issueId"
                        + "            projectId"
                        + "            jira(fields: [\"key\", \"summary\"])"
                        + "        }"
                        + "        warnings"
                        + "    }"
                        + "}");

        JsonNode response = post(API_GRAPHQL, mutation);

        return read(response, "$.data.createTestPlan.testPlan", TestPlan.class);
    }

    /**
     * Retrieves an Xray test and the Jira metadata needed for synchronization.
     *
     * @param issueId Xray internal issue identifier of the test
     * @return the test when Xray returned one, or an empty value when no test exists
     *         for the supplied identifier
     */
    public Optional<TestCase> getTestCase(
            String issueId
    ) {
        String query = query("query { "
                + "   getTest(issueId: \"" + issueId + "\") {"
                + "        issueId"
                + "        jira(fields: [\"key\", \"summary\", \"labels\"])"
                + "        folder {"
                + "            name"
                + "        }"
                + "        testType {"
                + "            name"
                + "            kind"
                + "        }"
                + "        gherkin"
                + "    }"
                + "}");

        JsonNode response = post(API_GRAPHQL, query);

        TestCase testCase = read(response, "$.data.getTest", TestCase.class);

        return Optional.ofNullable(testCase);
    }

    /**
     * Associates newly created Xray tests with a test plan.
     *
     * @param createdIssues Xray internal issue identifiers of the tests to add
     * @param remotePlan existing remote plan that will receive the tests
     */
    public void addTestsToPlan(
            List<String> createdIssues,
            TestPlan remotePlan
    ) {
        String mutation = query(
                "mutation {"
                        + "    addTestsToTestPlan("
                        + "        issueId: \"" + remotePlan.getIssueId() + "\", "
                        + "        testIssueIds: [\"" + String.join(DELIMITER, createdIssues) + "\"]"
                        + "    ) {"
                        + "        addedTests"
                        + "        warning"
                        + "    }"
                        + "}");

        post(API_GRAPHQL, mutation);
    }

    /**
     * Associates a test execution with an existing Xray test plan.
     *
     * @param testExecutionIssue Xray internal issue identifier of the execution
     * @param remotePlan existing remote plan that will receive the execution
     */
    public void addTestExecutionsToTestPlan(
            String testExecutionIssue,
            TestPlan remotePlan
    ) {
        String mutation = query(
                "mutation {"
                        + "    addTestExecutionsToTestPlan("
                        + "        issueId: \"" + remotePlan.getIssueId() + "\", "
                        + "        testExecIssueIds: [\"" + testExecutionIssue + "\"]"
                        + "    ) {"
                        + "        addedTestExecutions"
                        + "        warning"
                        + "    }"
                        + "}");

        post(API_GRAPHQL, mutation);
    }

    /**
     * Adds each test to the remote test set with the same Jira summary.
     * <p>
     * Associations are attempted only when a local test-set reference matches one
     * of {@code remoteTestSets}; unmatched references are intentionally ignored.
     *
     * @param tests tests whose test-set memberships must be synchronized
     * @param remoteTestSets available remote sets used to resolve memberships
     */
    public void addTestsToSets(
            List<TestCase> tests,
            List<TestSet> remoteTestSets
    ) {
        tests.forEach(xRayTestCase -> {
            Optional<TestSet> optionalXRayTestSet = remoteTestSets.stream()
                    .filter(xRayTestSet -> xRayTestCase.getTestSetList()
                            .stream().map(TestSet::getJira)
                            .map(JiraIssue::getSummary)
                            .anyMatch(s -> s.equals(xRayTestSet.getJira().getSummary())))
                    .findFirst();

            if (optionalXRayTestSet.isPresent()) {
                String mutation = query(
                        "mutation {"
                                + "    addTestsToTestSet("
                                + "        issueId: \"" + optionalXRayTestSet.get().getIssueId() + "\", "
                                + "        testIssueIds: [\"" + xRayTestCase.getIssueId() + "\"]"
                                + "    ) {"
                                + "        addedTests"
                                + "        warning"
                                + "    }"
                                + "}");

                post(API_GRAPHQL, mutation);
            }
        });
    }

    /**
     * Retrieves Xray test sets together with their current test membership.
     *
     * @return up to the first 100 test sets; an empty list is returned when the API
     *         produces no response
     */
    public List<TestSet> getTestSets() {
        String query = query("query { "
                + "   getTestSets(limit: 100) {"
                + "        total"
                + "        start"
                + "        limit"
                + "        results {"
                + "            issueId"
                + "            jira(fields: [\"key\", \"summary\", \"labels\"])"
                + "            tests(limit: 100) {"
                + "              total"
                + "              results {"
                + "                issueId"
                + "                jira(fields: [\"key\", \"summary\", \"labels\"])"
                + "              }"
                + "            }"
                + "        }"
                + "    }"
                + "}");

        JsonNode response = post(API_GRAPHQL, query);

        if (response == null) {
            return Collections.emptyList();
        }

        List<TestSet> list = read(response, "$.data.getTestSets.results", new TypeRef<List<TestSet>>() {
        });

        if (list == null) {
            return List.of();
        }

        for (int i = 0; i < list.size(); i++) {
            List<TestCase> testCases = read(response, "$.data.getTestSets.results[" + i + "].tests.results", new TypeRef<List<TestCase>>() {
            });
            list.get(i).testCases(testCases);
        }

        return new LinkedList<>(list);
    }

    /**
     * Creates test sets from local definitions.
     * <p>
     * The Jira summary and first label, when present, are copied to each new issue.
     *
     * @param newTestSets local test-set definitions to create
     * @return remote representations containing the generated Xray issue identifiers
     *         and Jira metadata, in input order
     */
    public List<TestSet> createTestSets(
            List<TestSet> newTestSets
    ) {
        return newTestSets.stream().map(xrayTestSet -> {
            StringBuilder jirafields = getJirafields(project, xrayTestSet.getJira());

            String mutation = query(
                    "mutation {"
                            + "    createTestSet("
                            + "        jira: {" + jirafields + "}"
                            + "    ) {"
                            + "        testSet {"
                            + "            issueId"
                            + "            jira(fields: [\"key\", \"summary\"])"
                            + "        }"
                            + "        warnings"
                            + "    }"
                            + "}");

            JsonNode response = post(API_GRAPHQL, mutation);

            return read(response, "$.data.createTestSet.testSet", TestSet.class);
        }).collect(Collectors.toList());
    }

    /**
     * Creates Cucumber tests in Xray from local Gherkin definitions.
     * <p>
     * The returned objects retain the local summary and test-set associations so
     * later synchronization stages can create the corresponding relationships.
     *
     * @param newTests local test definitions to create
     * @param project Jira project key in which the test issues will be created
     * @return created remote tests, in input order
     */
    public List<TestCase> createTestCases(
            List<TestCase> newTests,
            String project
    ) {
        return newTests.stream().map(test -> {
            StringBuilder jirafields = getJirafields(project, test.getJira());

            String mutation = query("mutation {"
                    + "    createTest("
                    + "        testType: { name: \"Cucumber\" },"
                    + "        gherkin: \"" + test.getGherkin() + "\","
                    + "        jira: {" + jirafields + "}"
                    + "    ) {"
                    + "        test {"
                    + "            issueId"
                    + "            testType {"
                    + "                name"
                    + "            }"
                    + "            jira(fields: [\"key\"])"
                    + "        }"
                    + "        warnings"
                    + "    }"
                    + "}");

            JsonNode response = post(API_GRAPHQL, mutation);

            TestCase testCase = read(response, "$.data.createTest.test", TestCase.class);
            testCase.getJira().summary(test.getJira().getSummary());
            testCase.testSetList(test.getTestSetList());
            return testCase;
        }).collect(Collectors.toList());
    }

    /**
     * Creates a test execution containing the supplied Xray tests.
     * <p>
     * The execution is assigned to the {@code Wakamiti} test environment.
     *
     * @param summary Jira summary for the new test execution
     * @param createdIssues Xray internal issue identifiers of the tests to execute
     * @param project Jira project key in which the execution will be created
     * @return the created execution with its Xray identifier and Jira metadata
     */
    public TestExecution createTestExecution(
            String summary,
            List<String> createdIssues,
            String project
    ) {
        String mutation = query(
                "mutation {"
                        + "    createTestExecution("
                        + "        testIssueIds: [\"" + String.join(DELIMITER, createdIssues) + "\"]"
                        + "        testEnvironments: [\"Wakamiti\"]"
                        + "        jira: {"
                        + getSummaryAndProject(summary, project)
                        + "        }"
                        + "    ) {"
                        + "        testExecution {"
                        + "            issueId"
                        + "            jira(fields: [\"key\", \"summary\"])"
                        + "        }"
                        + "        warnings"
                        + "        createdTestEnvironments"
                        + "    }"
                        + "}");

        JsonNode response = post(API_GRAPHQL, mutation);

        TestExecution testExecution = read(
                response,
                "$.data.createTestExecution.testExecution",
                TestExecution.class
        );
        return Optional.ofNullable(testExecution)
                .orElseThrow(() -> new WakamitiXRayException(
                        "XRay did not return the created Test Execution."
                ));
    }

    /**
     * Synchronizes the status of existing Xray test runs with local test results.
     * <p>
     * The method queries runs for all supplied tests and updates only runs whose
     * remote status differs from the status stored in the corresponding test case.
     * Tests without a matching run require no remote change.
     *
     * @param createdIssues tests carrying the desired execution status
     */
    public void updateTestRunStatus(
            List<TestCase> createdIssues
    ) {
        List<String> issues = createdIssues.stream().map(TestCase::getIssueId).collect(Collectors.toList());
        String query = query(
                "query {"
                        + "    getTestRuns( testIssueIds: [\"" + String.join(DELIMITER, issues) + "\"], limit: 100 ) {"
                        + "        total"
                        + "        limit"
                        + "        start"
                        + "        results {"
                        + "            id"
                        + "            status {"
                        + "                name"
                        + "                color"
                        + "                description"
                        + "            }"
                        + "            testExecution {"
                        + "                issueId"
                        + "            }"
                        + "            test {"
                        + "                issueId"
                        + "            }"
                        + "        }"
                        + "    }"
                        + "}");

        JsonNode response = post(API_GRAPHQL, query);

        List<TestRun> testRuns = read(response, "$.data.getTestRuns.results", new TypeRef<List<TestRun>>() {
        });

        createdIssues.forEach(testCase ->
                testRuns.stream()
                        .filter(testRun -> testRun.getTest().getIssueId().equals(testCase.getIssueId())
                                && !testRun.getStatus().getName().equals(testCase.getStatus()))
                        .findAny()
                        .ifPresent(testRun -> {
                            testCase.testRunId(testRun.getId());

                            String mutation = query(
                                    "mutation {"
                                            + "    updateTestRunStatus( id: \"" + testCase.getTestRunId() + "\", status: \"" + testCase.getStatus() + "\")"
                                            + "}");

                            post(API_GRAPHQL, mutation);
                        })
        );
    }

    private String query(
            String query
    ) {
        return toJSON(Map.of(QUERY, query));
    }

    private StringBuilder getJirafields(
            String project,
            JiraIssue issue
    ) {
        StringBuilder jirafields = new StringBuilder(getSummaryAndProject(issue.getSummary(), project));
        if (!issue.getLabels().isEmpty()) {
            jirafields.append(", labels: [\"").append(issue.getLabels().get(0)).append("\"]");
        }
        jirafields.append("}");
        return jirafields;
    }

    private String getSummaryAndProject(
            String summary,
            String project
    ) {
        return "fields: { summary: \"" + summary + "\", project: {key: \"" + project + "\"} }";
    }

}
