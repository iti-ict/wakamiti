package es.iti.wakamiti.xray;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.xray.api.JiraApi;
import es.iti.wakamiti.xray.api.XRayApi;
import es.iti.wakamiti.xray.internal.Mapper;
import es.iti.wakamiti.xray.internal.Util;
import es.iti.wakamiti.xray.internal.WakamitiXRayException;
import es.iti.wakamiti.xray.model.*;
import org.slf4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.Optional;
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
    private String idTagPattern;
    private boolean testCasePerFeature;

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

    public void idTagPattern(String idTagPattern) {
        this.idTagPattern = idTagPattern;
    }

    public void testCasePerFeature(boolean testCasePerFeature) {
        this.testCasePerFeature = testCasePerFeature;
    }

    @Override
    public void eventReceived(Event event) {
        if (!enabled) return;
        if (!(event.data() instanceof PlanNodeSnapshot)) {
            LOGGER.warn("No event data found");
            return;
        }
        PlanNodeSnapshot data = (PlanNodeSnapshot) event.data();

        xRayApi = new XRayApi(xRayBaseURL, xRayclientId, xRayclientSecret, project, LOGGER);
//        jiraApi = new JiraApi(jiraBaseURL, jiraCredentials, project, LOGGER);

        if (Event.PLAN_RUN_STARTED.equals(event.type())) {
            try {
                LOGGER.info("Sync plan to XRay...");
                sync(data);

            } catch (Exception e) {
                throw new WakamitiException("The test plan could not be synchronized. " +
                        "You can disable the plugin with the '{}' option to continue.", XRAY_ENABLED, e);
            }
        }

        if (Event.PLAN_RUN_FINISHED.equals(event.type())) {
            try {
                LOGGER.info("Sync plan to XRay...");
//                updateResults(data);
            } catch (Exception e) {
                throw new WakamitiException("The result of the execution could not be uploaded.", e);
            }
        }
    }

    private void updateResults(PlanNodeSnapshot data) {
        data.getChildren().forEach(child ->
                child.getChildren().forEach(results ->
                        testPlan.getTestCases().stream()
                                .filter(testCase -> testCase.getJira().getSummary().equals(results.getName()) && results.getResult().isPassed())
                                .findFirst()
                                .ifPresent(testCase -> testCase.status(results.getResult().name()))
                ));

        xRayApi.updateTestRunStatus(testPlan.getTestCases());
    }

    @Override
    public boolean acceptType(String eventType) {
        return List.of(Event.PLAN_RUN_STARTED, Event.PLAN_RUN_FINISHED).contains(eventType);
    }

    private void sync(PlanNodeSnapshot plan) {
        TestPlan remotePlan = xRayApi.getTestPlans().stream().filter(testPlan -> this.testPlan.getJira().getSummary().equals(testPlan.getJira().getSummary())).findFirst()
                .orElseGet(() -> {
                    if (createItemsIfAbsent) {
                        return xRayApi.createTestPlan(testPlan.getJira().getSummary());
                    } else {
                        throw new WakamitiXRayException(
                                "Test Plan with name '{}' does not exist in XRay. ",
                                testPlan.getJira().getSummary());
                    }
                });

        LOGGER.debug("Remote plan #{} ready to sync", remotePlan.getIssueId());

        String gherkinType = testCasePerFeature ? GHERKIN_TYPE_FEATURE : GHERKIN_TYPE_SCENARIO;
        Mapper mapper = Mapper.ofType(gherkinType).instance(testSet);

        List<TestCase> tests = mapper.map(plan)
                .filter(t -> isBlank(tag) || t.getJira().getLabels().contains(tag))
                .collect(Collectors.toList());

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

        List<TestCase> remoteTests = remoteTestSets.stream().parallel()
                .map(TestSet::getTestCases)
                .flatMap(List::stream)
                .map(testCase -> xRayApi.getTestCase(testCase.getIssueId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<String> remoteTestsSummaries = remoteTests.stream().map(TestCase::getJira).map(JiraIssue::getSummary).distinct().collect(Collectors.toList());

        List<TestCase> newTests = tests.stream().filter(t -> !remoteTestsSummaries.contains(t.getJira().getSummary())).collect(Collectors.toList());
//        List<Pair<XRayTestCase, XRayTestCase>> modTests = remoteTests.stream()
//                .filter(t -> tests.stream().anyMatch(c -> t.hasSameLabels(c) && t.isDifferent(c)))
//                .map(t -> new Pair<>(t, tests.stream()
//                        .filter(t::hasSameLabels)
//                        .findFirst()
//                        .orElseThrow()))
//                .collect(Collectors.toList());
//
//        if (!modTests.isEmpty()) {
////            jiraApi.updateTestCases(modTests);
//            LOGGER.debug("{} test cases updated", modTests.size());
//        }

        if (!newTests.isEmpty()) {
            List<TestCase> createdIssues = xRayApi.createTestCases(remotePlan, newTests, project);
            List<String> createdIssuesId = createdIssues.stream().map(TestCase::getIssueId).collect(Collectors.toList());
            xRayApi.addTestsToPlan(createdIssuesId, remotePlan);
            xRayApi.addTestsToSets(createdIssues, remoteTestSets);

            TestExecution testExecution = xRayApi.createTestExecution(remotePlan.getJira().getSummary() + " Test Execution", createdIssuesId, project);
            xRayApi.addTestExecutionsToTestPlan(testExecution.getIssueId(), remotePlan);

            LOGGER.debug("{} remote test cases created", newTests.size());
        }

//        List<XRayTestSet> nonSuites = remoteTestSets.stream().filter(s -> !testSets.contains(s)).collect(Collectors.toList());
//        // azure contiene suites que no constan en local
//        remoteTestSets.removeAll(nonSuites);

    }

}
