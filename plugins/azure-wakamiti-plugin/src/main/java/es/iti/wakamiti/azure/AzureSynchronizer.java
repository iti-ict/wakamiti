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
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.api.BaseApi;
import es.iti.wakamiti.azure.api.TestPlanApi;
import es.iti.wakamiti.azure.api.model.TestCase;
import es.iti.wakamiti.azure.api.model.TestPlan;
import es.iti.wakamiti.azure.api.model.TestSuite;
import es.iti.wakamiti.azure.internal.Mapper;
import es.iti.wakamiti.azure.internal.Util;
import es.iti.wakamiti.azure.internal.WakamitiAzureException;
import org.slf4j.Logger;

import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.iti.wakamiti.azure.AzureConfigContributor.AZURE_ENABLED;
import static org.apache.commons.lang3.StringUtils.isBlank;


@Extension(provider = "es.iti.wakamiti", name = "azure-reporter", version = "2.6", priority = 10)
public class AzureSynchronizer implements EventObserver {

    private static final Logger LOGGER = WakamitiLogger.forClass(AzureSynchronizer.class);

    public static final String GHERKIN_TYPE_FEATURE = "feature";
    public static final String GHERKIN_TYPE_SCENARIO = "scenario";

    private final Set<Path> attachments = new LinkedHashSet<>();
    private boolean enabled;
    private URL baseURL;
    private String organization;
    private String project;
    private String version;
    private TestPlan testPlan;
    private String suiteBase;
    private String tag;
    private boolean testCasePerFeature;
    private boolean createItemsIfAbsent;
    private boolean removeOrphans;
    private String idTagPattern;
    private String configuration;

    private TestPlanApi api;

    private Consumer<BaseApi<?>> authenticator = (client) -> {
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

    public void tag(String tag) {
        this.tag = tag;
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

    public void attachments(Set<Path> attachments) {
        this.attachments.addAll(attachments);
    }

    public void idTagPattern(String idTagPattern) {
        this.idTagPattern = idTagPattern;
    }

    private TestPlanApi api() {
        if (api == null) {
            Function<String, String> tagIdExtractor = tags -> {
                List<String> t = Stream.of(tags.split(";"))
                        .map(String::trim)
                        .filter(tag -> tag.matches(idTagPattern)).collect(Collectors.toList());
                if (t.size() > 1) {
                    throw new WakamitiAzureException("Too many tags match the id pattern. ");
                } else if (t.isEmpty()) {
                    throw new WakamitiAzureException("No tag matches the id pattern. ");
                }
                return t.get(0);
            };

            api = new TestPlanApi(baseURL, tagIdExtractor, configuration)
                    .organization(organization).project(project).version(version);
            authenticator.accept(api);
        }
        return api;
    }

    @Override
    public void eventReceived(Event event) {
        if (!enabled) return;
        if (!(event.data() instanceof PlanNodeSnapshot)) {
            LOGGER.warn("No event data found");
            return;
        }
        PlanNodeSnapshot data = (PlanNodeSnapshot) event.data();

        if (Event.PLAN_CREATED.equals(event.type())) {
            try {
                LOGGER.info("Synchronise test plan with Azure...");
                sync(data);
            } catch (Exception e) {
                throw new WakamitiException("The test plan could not be synchronized. " +
                        "You can disable the plugin with the '{}' option to continue.", AZURE_ENABLED, e);
            }
        }

        if (Event.PLAN_RUN_FINISHED.equals(event.type())) {
            try {
                // Upload runs...
            } catch (Exception e) {
                throw new WakamitiException("The result of the execution could not be uploaded.", e);
            }
        }
    }

    @Override
    public boolean acceptType(String eventType) {
        return List.of(Event.PLAN_CREATED, Event.PLAN_RUN_FINISHED).contains(eventType);
    }

    /**
     *
     *
     * @param plan
     */
    private void sync(PlanNodeSnapshot plan) {
        TestPlan remotePlan = api().getTestPlan(testPlan, createItemsIfAbsent);
        LOGGER.debug("Remote plan #{} ready to sync", remotePlan.id());

        String gherkinType = testCasePerFeature ? GHERKIN_TYPE_FEATURE : GHERKIN_TYPE_SCENARIO;
        Mapper mapper = Mapper.ofType(gherkinType).instance(suiteBase);

        List<TestCase> tests = mapper.map(plan)
                .filter(t -> isBlank(tag) || t.metadata().getTags().contains(tag))
                .peek(t -> LOGGER.trace("Load test case: {}", t))
                .collect(Collectors.toList());
        LOGGER.debug("{} local test cases ready to sync", tests.size());

        List<TestSuite> suites = tests.stream().map(TestCase::suite).flatMap(Util::flatten).collect(Collectors.toList());
        List<TestSuite> remoteSuites = api().searchTestSuites(remotePlan)
                .flatMap(Util::flatten).collect(Collectors.toList());
        List<TestSuite> newSuites = suites.stream().filter(s -> !remoteSuites.contains(s)).collect(Collectors.toList());

        if (!newSuites.isEmpty()) {
            remoteSuites.addAll(api().createTestSuites(remotePlan, newSuites));
            LOGGER.debug("{} remote test suites created", newSuites.size());
        }

        List<TestCase> remoteTests = remoteSuites.stream().parallel()
                .flatMap(suite -> api().getTestCases(remotePlan, suite))
                .collect(Collectors.toList());
        List<TestCase> newTests = tests.stream().filter(t -> !remoteTests.contains(t)).collect(Collectors.toList());
        List<Pair<TestCase, TestCase>> modSuiteTests = remoteTests.stream()
                .filter(t -> tests.stream().anyMatch(c -> hasChanged(t, c)))
                .map(t -> new Pair<>(t, tests.stream().filter(x -> x.tag().equals(t.tag())).findFirst().get()))
                .collect(Collectors.toList());

        if (!modSuiteTests.isEmpty()) {
            api().updateTestCases(remotePlan, modSuiteTests);
            LOGGER.debug("{} test cases updated", modSuiteTests.size());
        }

        if (!newTests.isEmpty()) {
            remoteTests.addAll(api().createTestCases(remotePlan, newTests));
            LOGGER.debug("{} remote test cases created", newTests.size());
        }


        List<TestSuite> nonSuites = remoteSuites.stream().filter(s -> !suites.contains(s)).collect(Collectors.toList());
        // azure contiene suites que no constan en local
        remoteSuites.removeAll(nonSuites);
    }

    private boolean hasChanged(TestCase test, TestCase newTest) {
        return test.tag().equals(newTest.tag()) && test.isDifferent(newTest);
    }

//    private TestCase getTest(List<TestCase> tests, TestCase test) {
//
//    }
}
