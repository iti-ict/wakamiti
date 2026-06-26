package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;
import es.iti.wakamiti.api.imconfig.ConfigurationFactory;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static es.iti.wakamiti.api.WakamitiConfiguration.EXECUTION_ID;
import static es.iti.wakamiti.api.WakamitiConfiguration.OUTPUT_FILE_PER_TEST_CASE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.OUTPUT_FILE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.TREAT_STEPS_AS_TESTS;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class WakamitiJUnit5TestPlan {

    protected static final Logger LOGGER = Wakamiti.LOGGER;
    protected static final ConfigurationFactory CONF_BUILDER = ConfigurationFactory.instance();

    protected boolean profileEnabled;
    protected PlanNodeLogger planNodeLogger;
    protected boolean treatStepsAsTests;
    protected Wakamiti wakamiti;
    protected Configuration configuration;
    protected PlanNode plan;
    protected BackendFactory backendFactory;


    @BeforeAll
    public void initWakamiti() throws Exception {
        profileEnabled = ProfileSelector.isEnabled(getClass());
        if (!profileEnabled) {
            LOGGER.info(
                    "Skipping {} because it does not match active profile(s): {}",
                    getClass().getName(),
                    ProfileSelector.activeProfilesDescription()
            );
            configuration = Configuration.factory().empty();
            return;
        }

        wakamiti = Wakamiti.instance();
        configuration = retrieveConfiguration(getClass());
        plan = wakamiti.createPlanFromConfiguration(configuration);
        backendFactory = wakamiti.newBackendFactory();
        planNodeLogger = new PlanNodeLogger(LOGGER, configuration, plan);
        treatStepsAsTests = configuration.get(TREAT_STEPS_AS_TESTS, Boolean.class).orElse(Boolean.FALSE);

        LOGGER.debug("{}", configuration);
        Wakamiti.contributors().propertyResolvers(configuration);
        wakamiti.configureLogger(configuration);
        wakamiti.configureEventObservers(configuration);
        plan.assignExecutionID(configuration.get(EXECUTION_ID, String.class).orElse(UUID.randomUUID().toString()));
        wakamiti.publishEvent(Event.PLAN_RUN_STARTED, new PlanNodeSnapshot(plan));
        planNodeLogger.logTestPlanHeader(plan);
    }


    @AfterAll
    public void finalizeWakamiti() {
        if (!profileEnabled) {
            return;
        }

        planNodeLogger.logTestPlanResult(plan);
        PlanNodeSnapshot snapshot = new PlanNodeSnapshot(plan);
        wakamiti.publishEvent(Event.PLAN_RUN_FINISHED, snapshot);
        wakamiti.writeOutputFile(plan, configuration);
        wakamiti.generateReports(configuration, snapshot);
    }


    @TestFactory
    public Stream<DynamicNode> wakamitiPlan() {
        if (!profileEnabled) {
            return Stream.empty();
        }
        return createDynamicNodes(plan.children().collect(Collectors.toList()), "0", configuration)
                .stream();
    }


    protected NodeType[] target() {
        return treatStepsAsTests
                ? new NodeType[] {NodeType.STEP, NodeType.VIRTUAL_STEP}
                : new NodeType[] {NodeType.TEST_CASE};
    }


    protected List<DynamicNode> createDynamicNodes(List<PlanNode> nodes, String parentPath, Configuration parentConfiguration) {
        return IntStream.range(0, nodes.size())
                .mapToObj(index -> createDynamicNode(nodes.get(index), childNodePath(parentPath, index), parentConfiguration))
                .collect(Collectors.toList());
    }


    protected DynamicNode createDynamicNode(PlanNode node, String nodePath, Configuration parentConfiguration) {
        Configuration nodeConfiguration = parentConfiguration.append(CONF_BUILDER.fromMap(node.properties()));
        List<PlanNode> children = node.children().collect(Collectors.toList());
        if (children.isEmpty() || node.nodeType().isAnyOf(target())) {
            return DynamicTest.dynamicTest(junitDisplayName(node, nodePath), () ->
                    new JupiterPlanNodeRunner(node, nodeConfiguration, backendFactory, planNodeLogger, nodePath).execute()
            );
        }
        return DynamicContainer.dynamicContainer(
                containerDisplayName(node, nodePath),
                createDynamicNodes(children, nodePath, nodeConfiguration)
        );
    }


    protected String containerDisplayName(PlanNode node, String nodePath) {
        return junitDisplayName(node, nodePath);
    }


    protected String junitDisplayName(PlanNode node, String nodePath) {
        String displayName = Objects.toString(node.displayName(), node.name());
        String source = node.source();
        String discriminator = (source == null || source.isBlank())
                ? nodePath
                : source + " | " + nodePath;
        return String.format("%s [%s]", displayName, discriminator);
    }


    protected String childNodePath(String parentPath, int childIndex) {
        return String.format("%s/%d", parentPath, childIndex);
    }


    private Configuration retrieveConfiguration(Class<?> testedClass) throws ConfigurationException {
        Configuration config = Wakamiti.defaultConfiguration();
        Optional<String> altDir = Optional.ofNullable(getClass().getClassLoader().getResource("."))
                .map(u -> {
                    try {
                        return u.toURI();
                    } catch (URISyntaxException e) {
                        return null;
                    }
                })
                .map(url -> Path.of(url).toString().replace(System.getProperty("user.dir"), ""))
                .map(dir -> dir.replaceAll("^[\\\\/]([^\\\\/]+).*", "$1"));

        if (altDir.isPresent()) {
            config = config.appendFromPairs(
                    OUTPUT_FILE_PATH,
                    String.format("%s/%s", altDir.get(),
                            WakamitiConfiguration.DEFAULTS.get(OUTPUT_FILE_PATH, String.class)
                                    .orElse("wakamiti.json")),
                    OUTPUT_FILE_PER_TEST_CASE_PATH,
                    altDir.get()
            );
        }
        return config.appendFromAnnotation(testedClass);
    }
}
