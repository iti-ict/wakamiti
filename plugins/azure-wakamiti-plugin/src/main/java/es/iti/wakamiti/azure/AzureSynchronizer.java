/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.api.BaseApi;
import es.iti.wakamiti.azure.api.AzureApi;
import es.iti.wakamiti.azure.api.model.*;
import es.iti.wakamiti.azure.internal.Mapper;
import es.iti.wakamiti.azure.internal.Util;
import es.iti.wakamiti.azure.internal.WakamitiAzureException;
import org.slf4j.Logger;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static es.iti.wakamiti.azure.AzureConfigContributor.AZURE_ENABLED;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;


@Extension(provider = "es.iti.wakamiti", name = "azure-reporter", version = "2.7", priority = 10)
public class AzureSynchronizer implements EventObserver {

    public static final String GHERKIN_TYPE_FEATURE = "feature";
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

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void baseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    public void organization(String organization) {
        this.organization = organization;
    }

    public void project(String project) {
        this.project = project;
    }

    public void version(String version) {
        this.version = version;
    }

    public void testPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    public void suiteBase(String suiteBase) {
        this.suiteBase = suiteBase;
    }

    public void setCredentialsAuthenticator(String user, String password) {
        this.authenticator = client -> client.basicAuth(user, password);
    }

    public void setTokenAuthenticator(String token) {
        this.authenticator = client -> client.tokenAuth(token);
    }

    public void configuration(String configuration) {
        this.configuration = configuration;
    }

    public void testCasePerFeature(boolean testCasePerFeature) {
        this.testCasePerFeature = testCasePerFeature;
    }

    public void createItemsIfAbsent(boolean createItemsIfAbsent) {
        this.createItemsIfAbsent = createItemsIfAbsent;
    }

    public void removeOrphans(boolean removeOrphans) {
        this.removeOrphans = removeOrphans;
    }

    public void attachments(Set<String> attachments) {
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

    @Override
    public void eventReceived(Event event) {
        if (!enabled) return;

        if (Event.PLAN_RUN_STARTED.equals(event.type())) {
            try {
                LOGGER.info("Synchronising test plan with Azure...");
                syncAndStart((PlanNodeSnapshot) event.data());
            } catch (Exception e) {
                throw new WakamitiException("The test plan could not be synchronized. " +
                        "You can disable the plugin with the '{}' option to continue.", AZURE_ENABLED, e);
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

    @Override
    public boolean acceptType(String eventType) {
        return List.of(Event.PLAN_RUN_STARTED, Event.PLAN_RUN_FINISHED, Event.REPORT_OUTPUT_FILE_WRITTEN)
                .contains(eventType);
    }


    private void syncAndStart(PlanNodeSnapshot plan) {
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

    private void uploadExecution(PlanNodeSnapshot plan) {
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

    private void uploadAttachment(Path file) {
        api().attachFile(run, file);
        LOGGER.debug("Attachment '{}' uploaded", file.getFileName());
    }

}
