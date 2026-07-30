/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;


import static es.iti.wakamiti.azure.AzureConfigContributor.AZURE_ENABLED;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.api.AzureApi;
import es.iti.wakamiti.azure.api.BaseApi;
import es.iti.wakamiti.azure.api.model.PointAssignment;
import es.iti.wakamiti.azure.api.model.TestCase;
import es.iti.wakamiti.azure.api.model.TestPlan;
import es.iti.wakamiti.azure.api.model.TestResult;
import es.iti.wakamiti.azure.api.model.TestRun;
import es.iti.wakamiti.azure.api.model.TestSuite;
import es.iti.wakamiti.azure.internal.Mapper;
import es.iti.wakamiti.azure.internal.Util;
import es.iti.wakamiti.azure.internal.WakamitiAzureException;


/**
 * Synchronizes Azure data with the configured external system.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "azure-reporter",
        version = "2.13",
        priority = Extension.NORMAL_PRIORITY * 2
)
public class AzureSynchronizer implements EventObserver {

    /** Mapper mode that publishes one Azure Test Case per Gherkin feature. */
    public static final String GHERKIN_TYPE_FEATURE = "feature";
    /** Mapper mode that publishes one Azure Test Case per Gherkin scenario. */
    public static final String GHERKIN_TYPE_SCENARIO = "scenario";

    private static final Logger LOGGER = WakamitiLogger.forClass(AzureSynchronizer.class);
    private final Set<String> attachments = new LinkedHashSet<>();

    private boolean enabled;
    private URL baseURL;
    private String organization;
    private String project;
    private String version;
    private TestPlan testPlan;
    private String suiteBase;
    private boolean testCasePerFeature;
    private boolean createItemsIfAbsent;
    private boolean removeOrphans;
    private String configuration;

    private AzureApi api;
    private TestRun run;
    private List<TestResult> testResults;

    private Consumer<BaseApi<?>> authenticator = client -> {
        throw new WakamitiException("Authentication is needed");
    };

    /**
     * @param enabled whether synchronization reacts to execution events
     */
    public void enabled(
            boolean enabled
    ) {
        this.enabled = enabled;
    }

    /**
     * @param baseURL Azure DevOps service base URL
     */
    public void baseURL(
            URL baseURL
    ) {
        this.baseURL = baseURL;
    }

    /**
     * @param organization Azure DevOps organization name
     */
    public void organization(
            String organization
    ) {
        this.organization = organization;
    }

    /**
     * @param project Azure DevOps project containing the test plan
     */
    public void project(
            String project
    ) {
        this.project = project;
    }

    /**
     * @param version Azure DevOps REST API version
     */
    public void version(
            String version
    ) {
        this.version = version;
    }

    /**
     * @param testPlan plan identity and classification paths to synchronize
     */
    public void testPlan(
            TestPlan testPlan
    ) {
        this.testPlan = testPlan;
    }

    /**
     * @param suiteBase base suite path beneath which Wakamiti creates suites
     */
    public void suiteBase(
            String suiteBase
    ) {
        this.suiteBase = suiteBase;
    }

    /**
     * Configures HTTP Basic authentication for subsequent Azure API clients.
     *
     * @param user     Azure DevOps user name
     * @param password password or compatible personal access token
     */
    public void setCredentialsAuthenticator(
            String user,
            String password
    ) {
        this.authenticator = client -> client.basicAuth(user, password);
    }

    /**
     * Configures bearer-token authentication for subsequent Azure API clients.
     *
     * @param token access token sent to Azure DevOps
     */
    public void setTokenAuthenticator(
            String token
    ) {
        this.authenticator = client -> client.tokenAuth(token);
    }

    /**
     * @param configuration Azure test-configuration name assigned to test points
     */
    public void configuration(
            String configuration
    ) {
        this.configuration = configuration;
    }

    /**
     * Selects synchronization granularity.
     *
     * @param testCasePerFeature {@code true} for one Test Case per feature;
     *                           {@code false} for one per scenario
     */
    public void testCasePerFeature(
            boolean testCasePerFeature
    ) {
        this.testCasePerFeature = testCasePerFeature;
    }

    /**
     * @param createItemsIfAbsent whether missing plans, suites and cases may be created
     */
    public void createItemsIfAbsent(
            boolean createItemsIfAbsent
    ) {
        this.createItemsIfAbsent = createItemsIfAbsent;
    }

    /**
     * Sets whether remote items without a corresponding Wakamiti node should
     * be removed during synchronization.
     *
     * @param removeOrphans orphan-removal policy
     */
    public void removeOrphans(
            boolean removeOrphans
    ) {
        this.removeOrphans = removeOrphans;
    }

    /**
     * Adds path glob patterns for report files uploaded to the current run.
     * Existing patterns are retained.
     *
     * @param attachments patterns matched against report output paths
     */
    public void attachments(
            Set<String> attachments
    ) {
        this.attachments.addAll(attachments);
    }

    private AzureApi api() {
        if (api == null) {
            api = new AzureApi(baseURL, configuration)
                    .organization(organization).projectBase(project).version(version);
            authenticator.accept(api);
        }
        return api;
    }

    /**
     * Handles Azure synchronization lifecycle events.
     * <p>
     * On {@link Event#PLAN_RUN_STARTED}, the local plan is synchronized and a
     * remote run is opened. On {@link Event#PLAN_RUN_FINISHED}, results are
     * pushed and the run is completed. Matching report files are uploaded as
     * attachments when {@link Event#REPORT_OUTPUT_FILE_WRITTEN} is received.
     * The Azure API client is closed after each handled event.
     * </p>
     *
     * @param event received runtime event
     */
    @Override
    public void eventReceived(
            Event event
    ) {
        if (!enabled) {
            return;
        }

        if (Event.PLAN_RUN_STARTED.equals(event.type())) {
            try {
                LOGGER.info("Synchronising test plan with Azure...");
                syncAndStart((PlanNodeSnapshot) event.data());
            } catch (Exception e) {
                throw new WakamitiException("The test plan could not be synchronized. "
                        + "You can disable the plugin with the '{}' option to continue.", AZURE_ENABLED, e);
            }
        }

        if (Event.PLAN_RUN_FINISHED.equals(event.type())) {
            try {
                LOGGER.info("Uploading test plan results to Azure...");
                uploadExecution((PlanNodeSnapshot) event.data());
            } catch (Exception e) {
                throw new WakamitiException("The result of the execution could not be uploaded.", e);
            }
        }

        if (Event.REPORT_OUTPUT_FILE_WRITTEN.equals(event.type())
                && attachments.stream().anyMatch(g -> Util.match((Path) event.data(), g))) {
            try {
                LOGGER.info("Uploading attachments to Azure...");
                uploadAttachment((Path) event.data());
            } catch (Exception e) {
                LOGGER.error("Cannot upload attachment '{}'", event.data(), e);
            }
        }
        api().close();
    }

    /**
     * Declares the event types consumed by this observer.
     *
     * @param eventType event type identifier
     * @return {@code true} for plan start/finish and report file events
     */
    @Override
    public boolean acceptType(
            String eventType
    ) {
        return List.of(Event.PLAN_RUN_STARTED, Event.PLAN_RUN_FINISHED, Event.REPORT_OUTPUT_FILE_WRITTEN)
                .contains(eventType);
    }

    /**
     * Synchronizes plan metadata and starts a remote Azure run.
     *
     * @param plan executed plan snapshot used to map suites/tests
     */
    private void syncAndStart(
            PlanNodeSnapshot plan
    ) {
        testPlan = api().getTestPlan(testPlan, createItemsIfAbsent);
        LOGGER.debug("Remote plan #{} ready to sync", testPlan.id());

        Mapper mapper = Mapper.ofType(testCasePerFeature ? GHERKIN_TYPE_FEATURE : GHERKIN_TYPE_SCENARIO)
                .instance(suiteBase);
        List<TestCase> tests = mapper.mapTests(plan)
                .peek(t -> LOGGER.trace("Load test case: {}", t))
                .peek(t -> t.suite().root(testPlan.rootSuite()))
                .collect(Collectors.toList());
        LOGGER.debug("{} local test cases ready to sync", tests.size());

        List<TestSuite> suites = tests.stream().map(TestCase::suite).distinct().collect(Collectors.toList());

        List<TestSuite> remoteSuites = api().getTestSuites(testPlan, suites, createItemsIfAbsent);
        LOGGER.debug("{} remote suites ready to sync", remoteSuites.size());

        List<TestCase> testCases = api().getTestCases(testPlan, remoteSuites, tests, createItemsIfAbsent);
        LOGGER.debug("{} remote tests ready to sync", testCases.size());

        Function<String, TestCase> findTestCase = id -> testCases.stream()
                .filter(t -> t.id().equals(id)).findFirst()
                .orElseThrow(() -> new WakamitiAzureException("No such test case '{}'", id));

        run = new TestRun()
                .plan(testPlan)
                .name(testPlan.name() + " - run by Wakamiti")
                .state(TestRun.Status.IN_PROGRESS)
                .pointIds(testCases.stream().flatMap(t -> t.pointAssignments().stream().map(PointAssignment::id))
                        .collect(Collectors.toList()));
        api().createRun(run);
        LOGGER.debug("Test run #{} ready to sync", run.id());
        testResults = api().getResults(run, testCases.size())
                .peek(r -> r.testCase(findTestCase.apply(r.testCase().id())))
                .collect(Collectors.toList());
        LOGGER.debug("{} remote test results ready to sync", testResults.size());
    }

    /**
     * Maps local execution results to Azure test results and completes the run.
     *
     * @param plan executed plan snapshot containing final outcomes
     */
    private void uploadExecution(
            PlanNodeSnapshot plan
    ) {
        if (isEmpty(testResults)) {
            return;
        }
        Function<String, TestResult> findResult = t -> testResults.stream()
                .filter(e -> e.testCase().identifier().equals(t)).findFirst()
                .orElseThrow(() -> new WakamitiAzureException("No such test result '{}'", t));
        testResults = Mapper.ofType(testCasePerFeature ? GHERKIN_TYPE_FEATURE : GHERKIN_TYPE_SCENARIO)
                .instance(suiteBase)
                .mapResults(plan)
                .map(r -> findResult.apply(r.testCase().identifier()).merge(r))
                .collect(Collectors.toList());

        api().updateResults(run, testResults);
        api().updateRun(run.errorMessage(plan.getErrorMessage()).state(TestRun.Status.COMPLETED));
    }

    private void uploadAttachment(
            Path file
    ) {
        api().attachFile(run, file);
        LOGGER.debug("Attachment '{}' uploaded", file.getFileName());
    }

}
