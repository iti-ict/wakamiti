package es.iti.wakamiti.xray;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.xray.api.XRayApi;
import es.iti.wakamiti.xray.internal.Mapper;
import es.iti.wakamiti.xray.internal.WakamitiXRayException;
import es.iti.wakamiti.xray.model.XRayPlan;
import es.iti.wakamiti.xray.model.XRayTestCase;
import org.slf4j.Logger;

import java.net.URL;
import java.nio.file.Path;
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

    private final Set<Path> attachments = new LinkedHashSet<>();
    private boolean enabled;
    private URL baseURL;
    private String project;
    private String version;
    private XRayPlan testPlan;
    private String suiteBase;
    private String tag;
    private boolean testCasePerFeature;
    private boolean createItemsIfAbsent;
    private boolean removeOrphans;
    private String idTagPattern;

    private XRayApi api;

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void baseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    public void project(String project) {
        this.project = project;
    }

    public void version(String version) {
        this.version = version;
    }

    public void testPlan(XRayPlan testPlan) {
        this.testPlan = testPlan;
    }

    public void suiteBase(String suiteBase) {
        this.suiteBase = suiteBase;
    }

    public void tag(String tag) {
        this.tag = tag;
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
                LOGGER.info("Sync plan to XRay...");
                sync(data);

            } catch (Exception e) {
                throw new WakamitiException("The test plan could not be synchronized. " +
                        "You can disable the plugin with the '{}' option to continue.", XRAY_ENABLED, e);
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

    private void sync(PlanNodeSnapshot plan) {
        XRayPlan remotePlan = api.getTestPlan(testPlan.getId())
                .orElseGet(() -> {
                    if (createItemsIfAbsent) {
                        return api.createTestPlan(testPlan.getSummary());
                    } else {
                        throw new WakamitiXRayException(
                                "Test Plan with name '{}' does not exist in XRay. ",
                                testPlan.getSummary());
                    }
                });

        LOGGER.debug("Remote plan #{} ready to sync", remotePlan.getId());

        String gherkinType = testCasePerFeature ? GHERKIN_TYPE_FEATURE : GHERKIN_TYPE_SCENARIO;
        Mapper mapper = Mapper.ofType(gherkinType).instance(suiteBase);

        List<XRayTestCase> tests = mapper.map(plan)
                .filter(t -> isBlank(tag) || t.getIssue().getLabels().contains(tag))
                .collect(Collectors.toList());

        List<XRayTestCase> remoteTests = remotePlan.getTestCases().stream().parallel()
                .map(testCase -> api.getTestCase(testCase.getIssueId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<XRayTestCase> newTests = tests.stream().filter(t -> !remoteTests.contains(t)).collect(Collectors.toList());
        List<Pair<XRayTestCase, XRayTestCase>> modSuiteTests = remoteTests.stream()
                .filter(t -> tests.stream().anyMatch(c -> t.tag().equals(c.tag()) && t.isDifferent(c)))
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

    }

}
