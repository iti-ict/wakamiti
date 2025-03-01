package es.iti.wakamiti.xray;

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
import es.iti.wakamiti.xray.model.*;
import org.slf4j.Logger;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_ENABLED;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Extension(provider = "es.iti.wakamiti", name = "xray-reporter", version = "2.6", priority = 10)
public class XRaySynchronizer implements EventObserver {

    private static final Logger LOGGER = WakamitiLogger.forClass(XRaySynchronizer.class);

    public static final String GHERKIN_TYPE_FEATURE = "feature";
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

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void xRayBaseURL(URL xRayBaseURL) {
        this.xRayBaseURL = xRayBaseURL;
    }

    public void jiraBaseURL(URL jiraBaseURL) {
        this.jiraBaseURL = jiraBaseURL;
    }

    public void xRayclientId(String clientId) {
        this.xRayclientId = clientId;
    }

    public void xRayclientSecret(String clientSecret) {
        this.xRayclientSecret = clientSecret;
    }

    public void jiraCredentials(String jiraCredentials) {
        this.jiraCredentials = jiraCredentials;
    }

    public void project(String project) {
        this.project = project;
    }

    public void testPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    public void testSet(String testSet) {
        this.testSet = testSet;
    }

    public void tag(String tag) {
        this.tag = tag;
    }

    public void createItemsIfAbsent(boolean createItemsIfAbsent) {
        this.createItemsIfAbsent = createItemsIfAbsent;
    }

    public void testCasePerFeature(boolean testCasePerFeature) {
        this.testCasePerFeature = testCasePerFeature;
    }

    public void attachments(Set<String> attachments) {
        this.attachments.addAll(attachments);
    }

    @Override
    public void eventReceived(Event event) {
        if (!enabled) return;

        initializeXRayApi();
        initializeJiraApi();

        if (Event.PLAN_RUN_STARTED.equals(event.type())) {
            try {
                LOGGER.info("Sync plan to XRay...");
                sync((PlanNodeSnapshot) event.data());

            } catch (Exception e) {
                throw new WakamitiException("The test plan could not be synchronized. " +
                        "You can disable the plugin with the '{}' option to continue.", XRAY_ENABLED, e);
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
    public boolean acceptType(String eventType) {
        return List.of(Event.PLAN_RUN_STARTED, Event.PLAN_RUN_FINISHED, Event.REPORT_OUTPUT_FILE_WRITTEN).contains(eventType);
    }

    private void sync(PlanNodeSnapshot plan) {
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
        TestExecution testExecution = xRayApi.createTestExecution("Test Execution ".concat(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))), createdIssuesId, project);
        testPlan.testExecution(testExecution);
        xRayApi.addTestExecutionsToTestPlan(testExecution.getIssueId(), testPlan);
    }

    private void createNewTests(List<TestCase> tests, List<Pair<TestCase, TestCase>> modTests, List<TestSet> remoteTestSets) {
        List<String> remoteTestsSummaries = testPlan.getTestCases().stream()
                .map(TestCase::getJira).map(JiraIssue::getSummary)
                .distinct()
                .collect(Collectors.toList());

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

    private List<Pair<TestCase, TestCase>> updateModifiedTests(List<TestCase> tests) {
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

    private void getRemoteTests(List<TestSet> remoteTestSets) {
        List<TestCase> remoteTests = remoteTestSets.stream().parallel()
                .map(TestSet::getTestCases)
                .flatMap(List::stream)
                .map(testCase -> xRayApi.getTestCase(testCase.getIssueId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        testPlan.testCases(remoteTests);
    }

    private List<TestCase> getTests(PlanNodeSnapshot plan) {
        String gherkinType = testCasePerFeature ? GHERKIN_TYPE_FEATURE : GHERKIN_TYPE_SCENARIO;
        Mapper mapper = Mapper.ofType(gherkinType).instance(testSet);

        return mapper.map(plan)
                .filter(t -> isBlank(tag) || t.getJira().getLabels().contains(tag))
                .collect(Collectors.toList());
    }

    private List<TestSet> createTestSets(List<TestCase> tests) {
        List<TestSet> testSets = tests.stream().map(TestCase::getTestSetList).flatMap(List::stream).collect(Collectors.toList());
        List<TestSet> remoteTestSets = xRayApi.getTestSets();
        List<String> remoteTestSetsSummary = remoteTestSets.stream().map(TestSet::getJira).map(JiraIssue::getSummary).collect(Collectors.toList());
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

    private void updateResults(PlanNodeSnapshot data) {
        data.getChildren().forEach(child ->
                child.getChildren().forEach(results ->
                        testPlan.getTestCases().stream()
                                .filter(testCase -> testCase.getJira().getSummary().equals(results.getName()))
                                .findFirst()
                                .ifPresent(testCase -> testCase.status(results.getResult().name()))
                ));

        xRayApi.updateTestRunStatus(testPlan.getTestCases());
    }


    private void uploadAttachment(Path data) {
        jiraApi.addAttachment(testPlan.getTestExecution().getJira().getKey(), data);
    }

}
