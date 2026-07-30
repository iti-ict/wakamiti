/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray;


import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_ENABLED;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.xray.api.JiraApi;
import es.iti.wakamiti.xray.api.XRayApi;
import es.iti.wakamiti.xray.internal.Mapper;
import es.iti.wakamiti.xray.internal.Util;
import es.iti.wakamiti.xray.internal.WakamitiXRayException;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.TestCase;
import es.iti.wakamiti.xray.model.TestExecution;
import es.iti.wakamiti.xray.model.TestPlan;
import es.iti.wakamiti.xray.model.TestSet;


/**
 * Synchronizes XRay data with the configured external system.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "xray-reporter",
        version = "2.6",
        priority = Extension.NORMAL_PRIORITY * 2
)
public class XRaySynchronizer implements EventObserver {

    private static final Logger LOGGER = WakamitiLogger.forClass(XRaySynchronizer.class);

    /** Gherkin node type used when one Xray test is generated per feature. */
    public static final String GHERKIN_TYPE_FEATURE = "feature";
    /** Gherkin node type used when one Xray test is generated per scenario. */
    public static final String GHERKIN_TYPE_SCENARIO = "scenario";

    private boolean enabled;
    private URL xRayBaseURL;
    private URL jiraBaseURL;
    private String xRayclientId;
    private String xRayclientSecret;
    private String jiraCredentials;
    private String project;
    private TestPlan testPlan;
    private String testSet;
    private String tag;
    private boolean createItemsIfAbsent;
    private boolean testCasePerFeature;
    private final Set<String> attachments = new LinkedHashSet<>();

    private XRayApi xRayApi;
    private JiraApi jiraApi;

    /**
     * Enables or disables every synchronization action handled by this observer.
     *
     * @param enabled {@code true} to synchronize plans, results and attachments
     */
    public void enabled(
            boolean enabled
    ) {
        this.enabled = enabled;
    }

    /**
     * Configures the Xray Cloud API endpoint.
     *
     * @param xRayBaseURL base URL used for authentication and GraphQL operations
     */
    public void xRayBaseURL(
            URL xRayBaseURL
    ) {
        this.xRayBaseURL = xRayBaseURL;
    }

    /**
     * Configures the Jira REST API endpoint used for issue updates and attachments.
     *
     * @param jiraBaseURL base URL of the Jira instance backing Xray
     */
    public void jiraBaseURL(
            URL jiraBaseURL
    ) {
        this.jiraBaseURL = jiraBaseURL;
    }

    /**
     * Configures the client identifier used to authenticate with Xray Cloud.
     *
     * @param clientId Xray API client identifier
     */
    public void xRayclientId(
            String clientId
    ) {
        this.xRayclientId = clientId;
    }

    /**
     * Configures the secret paired with the Xray API client identifier.
     *
     * @param clientSecret Xray API client secret
     */
    public void xRayclientSecret(
            String clientSecret
    ) {
        this.xRayclientSecret = clientSecret;
    }

    /**
     * Configures the Base64-encoded credentials used by Jira REST requests.
     *
     * @param jiraCredentials encoded Jira credentials without the {@code Basic}
     *        authentication scheme
     */
    public void jiraCredentials(
            String jiraCredentials
    ) {
        this.jiraCredentials = jiraCredentials;
    }

    /**
     * Selects the Jira project where missing Xray entities will be created.
     *
     * @param project Jira project key
     */
    public void project(
            String project
    ) {
        this.project = project;
    }

    /**
     * Selects the test plan that receives synchronized tests and executions.
     * <p>
     * Before synchronization, the plan may contain only a Jira summary. It is
     * replaced with the matching remote plan, or with a newly created one when
     * creation is enabled.
     *
     * @param testPlan local test-plan selector
     */
    public void testPlan(
            TestPlan testPlan
    ) {
        this.testPlan = testPlan;
    }

    /**
     * Configures the source-path base removed from generated Xray test-set names.
     *
     * @param testSet base path used by the selected plan mapper
     */
    public void testSet(
            String testSet
    ) {
        this.testSet = testSet;
    }

    /**
     * Restricts synchronization to tests carrying a particular Wakamiti identifier
     * as a Jira label.
     *
     * @param tag required label; a blank value includes all generated tests
     */
    public void tag(
            String tag
    ) {
        this.tag = tag;
    }

    /**
     * Controls whether a missing remote test plan may be created automatically.
     *
     * @param createItemsIfAbsent {@code true} to create a plan when no plan with the
     *        configured summary exists
     */
    public void createItemsIfAbsent(
            boolean createItemsIfAbsent
    ) {
        this.createItemsIfAbsent = createItemsIfAbsent;
    }

    /**
     * Selects the granularity of generated Xray tests.
     *
     * @param testCasePerFeature {@code true} to generate one test per feature;
     *        {@code false} to generate one test per scenario
     */
    public void testCasePerFeature(
            boolean testCasePerFeature
    ) {
        this.testCasePerFeature = testCasePerFeature;
    }

    /**
     * Adds path globs that identify report files to attach to the Jira test
     * execution after they are written.
     *
     * @param attachments glob patterns evaluated against report output paths
     */
    public void attachments(
            Set<String> attachments
    ) {
        this.attachments.addAll(attachments);
    }

    @Override
    public void eventReceived(
            Event event
    ) {
        if (!enabled) {
            return;
        }

        initializeXRayApi();
        initializeJiraApi();

        if (Event.PLAN_RUN_STARTED.equals(event.type())) {
            try {
                LOGGER.info("Sync plan to XRay...");
                sync((PlanNodeSnapshot) event.data());
            } catch (Exception e) {
                throw new WakamitiException("The test plan could not be synchronized. "
                        + "You can disable the plugin with the '{}' option to continue.", XRAY_ENABLED, e);
            }
        }

        if (Event.PLAN_RUN_FINISHED.equals(event.type())) {
            try {
                LOGGER.info("Sync results to XRay...");
                updateResults((PlanNodeSnapshot) event.data());
            } catch (Exception e) {
                throw new WakamitiException("The result of the execution could not be uploaded.", e);
            }
        }

        if (Event.REPORT_OUTPUT_FILE_WRITTEN.equals(event.type())
                && attachments.stream().anyMatch(g -> Util.match((Path) event.data(), g))) {
            try {
                LOGGER.info("Uploading attachments to XRay...");
                uploadAttachment((Path) event.data());
            } catch (Exception e) {
                LOGGER.error("Cannot upload attachment '{}'", event.data(), e);
            }
        }
    }

    private void initializeXRayApi() {
        if (xRayApi == null) {
            xRayApi = new XRayApi(xRayBaseURL, xRayclientId, xRayclientSecret, project, LOGGER);
        }
    }

    private void initializeJiraApi() {
        if (jiraApi == null) {
            jiraApi = new JiraApi(jiraBaseURL, jiraCredentials, LOGGER);
        }
    }

    @Override
    public boolean acceptType(
            String eventType
    ) {
        return List.of(Event.PLAN_RUN_STARTED, Event.PLAN_RUN_FINISHED, Event.REPORT_OUTPUT_FILE_WRITTEN).contains(eventType);
    }

    private void sync(
            PlanNodeSnapshot plan
    ) {
        createTestPlan();

        List<TestCase> tests = getTests(plan);

        List<TestSet> remoteTestSets = createTestSets(tests);

        getRemoteTests(remoteTestSets);

        List<Pair<TestCase, TestCase>> modTests = updateModifiedTests(tests);

        createNewTests(tests, modTests, remoteTestSets);

        createTestExecution();
    }

    private void createTestExecution() {
        List<String> createdIssuesId = testPlan.getTestCases().stream().map(TestCase::getIssueId).collect(Collectors.toList());
        TestExecution testExecution = xRayApi.createTestExecution(
                "Test Execution ".concat(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                ),
                createdIssuesId,
                project
        );
        testPlan.testExecution(testExecution);
        xRayApi.addTestExecutionsToTestPlan(testExecution.getIssueId(), testPlan);
    }

    private void createNewTests(
            List<TestCase> tests,
            List<Pair<TestCase, TestCase>> modTests,
            List<TestSet> remoteTestSets
    ) {
        List<String> remoteTestsSummaries = testPlan.getTestCases().stream()
                .map(TestCase::getJira).map(JiraIssue::getSummary)
                .distinct()
                .toList();

        List<TestCase> newTests = tests.stream().filter(t ->
                        !remoteTestsSummaries.contains(t.getJira().getSummary())
                                && modTests.stream().map(Pair::value).noneMatch(testCase -> testCase.getJira().getSummary().equals(t.getJira().getSummary())))
                .collect(Collectors.toList());

        if (!newTests.isEmpty()) {
            List<TestCase> createdIssues = xRayApi.createTestCases(newTests, project);
            testPlan.getTestCases().addAll(createdIssues);

            List<String> createdIssuesId = createdIssues.stream().map(TestCase::getIssueId).collect(Collectors.toList());
            xRayApi.addTestsToPlan(createdIssuesId, testPlan);
            xRayApi.addTestsToSets(createdIssues, remoteTestSets);

            LOGGER.debug("{} remote test cases created", newTests.size());
        }
    }

    private List<Pair<TestCase, TestCase>> updateModifiedTests(
            List<TestCase> tests
    ) {
        List<Pair<TestCase, TestCase>> modTests = testPlan.getTestCases().stream()
                .filter(t -> tests.stream().anyMatch(c -> t.hasSameLabels(c) && t.isDifferent(c)))
                .map(t -> new Pair<>(t, tests.stream()
                        .filter(t::hasSameLabels)
                        .findFirst()
                        .orElseThrow()))
                .collect(Collectors.toList());

        if (!modTests.isEmpty()) {
            jiraApi.updateTestCases(modTests);
            LOGGER.debug("{} test cases updated", modTests.size());
        }
        return modTests;
    }

    private void getRemoteTests(
            List<TestSet> remoteTestSets
    ) {
        List<TestCase> remoteTests = remoteTestSets.stream().parallel()
                .map(TestSet::getTestCases)
                .flatMap(List::stream)
                .map(testCase -> xRayApi.getTestCase(testCase.getIssueId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        testPlan.testCases(remoteTests);
    }

    private List<TestCase> getTests(
            PlanNodeSnapshot plan
    ) {
        String gherkinType = testCasePerFeature ? GHERKIN_TYPE_FEATURE : GHERKIN_TYPE_SCENARIO;
        Mapper mapper = Mapper.ofType(gherkinType).instance(testSet);

        return mapper.map(plan)
                .filter(t -> isBlank(tag) || t.getJira().getLabels().contains(tag))
                .collect(Collectors.toList());
    }

    private List<TestSet> createTestSets(
            List<TestCase> tests
    ) {
        List<TestSet> testSets = tests.stream().map(TestCase::getTestSetList).flatMap(List::stream).toList();
        List<TestSet> remoteTestSets = xRayApi.getTestSets();
        List<String> remoteTestSetsSummary = remoteTestSets.stream().map(TestSet::getJira).map(JiraIssue::getSummary).toList();
        List<TestSet> newTestSets = testSets.stream()
                .filter(s -> !remoteTestSetsSummary.contains(s.getJira().getSummary()))
                .filter(Util.distinctByKey(xRayTestSet -> xRayTestSet.getJira().getSummary()))
                .collect(Collectors.toList());

        if (!newTestSets.isEmpty()) {
            remoteTestSets.addAll(xRayApi.createTestSets(newTestSets));
            LOGGER.debug("{} remote test sets created", newTestSets.size());
        }
        return remoteTestSets;
    }

    private void createTestPlan() {
        testPlan = xRayApi.getTestPlans().stream().filter(tp -> this.testPlan.getJira().getSummary().equals(tp.getJira().getSummary())).findFirst()
                .orElseGet(() -> {
                    if (createItemsIfAbsent) {
                        return xRayApi.createTestPlan(testPlan.getJira().getSummary());
                    } else {
                        throw new WakamitiXRayException(
                                "Test Plan with name '{}' does not exist in XRay. ",
                                testPlan.getJira().getSummary());
                    }
                });

        LOGGER.debug("Remote plan #{} ready to sync", testPlan.getIssueId());
    }

    private void updateResults(
            PlanNodeSnapshot data
    ) {
        data.getChildren().forEach(child ->
                child.getChildren().forEach(results ->
                        testPlan.getTestCases().stream()
                                .filter(testCase -> testCase.getJira().getSummary().equals(results.getName()))
                                .findFirst()
                                .ifPresent(testCase -> testCase.status(results.getResult().name()))
                ));

        xRayApi.updateTestRunStatus(testPlan.getTestCases());
    }

    private void uploadAttachment(
            Path data
    ) {
        jiraApi.addAttachment(testPlan.getTestExecution().getJira().getKey(), data);
    }

}
