/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;
import es.iti.wakamiti.api.imconfig.ConfigurationFactory;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static es.iti.wakamiti.api.WakamitiConfiguration.EXECUTION_ID;
import static es.iti.wakamiti.api.WakamitiConfiguration.OUTPUT_FILE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.OUTPUT_FILE_PER_TEST_CASE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.TREAT_STEPS_AS_TESTS;


/**
 * JUnit Platform descriptor representing a Wakamiti test plan class. It builds
 * the plan during discovery and drives its whole lifecycle (initialization,
 * {@code @BeforeAll}/{@code @AfterAll} hooks, node execution and finalization)
 * during execution.
 */
class WakamitiClassDescriptor extends AbstractTestDescriptor {

    private static final Logger LOGGER = Wakamiti.LOGGER;
    private static final ConfigurationFactory CONF_BUILDER = ConfigurationFactory.instance();

    private final Class<?> testClass;
    private final boolean profileEnabled;
    private final List<NodeExecution> featureRunners = new ArrayList<>();

    private Wakamiti wakamiti;
    private Configuration configuration;
    private PlanNode plan;
    private PlanNodeLogger planNodeLogger;
    private WakamitiNodeDescriptor beforeClassDescriptor;
    private WakamitiNodeDescriptor afterClassDescriptor;

    WakamitiClassDescriptor(UniqueId uniqueId, Class<?> testClass) {
        super(uniqueId, testClass.getSimpleName(), ClassSource.from(testClass));
        this.testClass = testClass;
        this.profileEnabled = ProfileSelector.isEnabled(testClass);
        if (profileEnabled) {
            buildPlan();
        }
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    private void buildPlan() {
        this.wakamiti = Wakamiti.instance();
        this.configuration = retrieveConfiguration(testClass);
        this.plan = wakamiti.createPlanFromConfiguration(configuration);
        this.planNodeLogger = new PlanNodeLogger(LOGGER, configuration, plan);
        this.beforeClassDescriptor = lifecycleDescriptor("beforeClass");
        this.afterClassDescriptor = lifecycleDescriptor("afterClass");
        addChild(beforeClassDescriptor);
        boolean treatStepsAsTests = configuration.get(TREAT_STEPS_AS_TESTS, Boolean.class).orElse(Boolean.FALSE);
        BackendFactory backendFactory = wakamiti.newBackendFactory();
        List<String> resourceRoots = configuration.getList(WakamitiConfiguration.RESOURCE_PATH, String.class);

        List<PlanNode> planChildren = plan.children().collect(Collectors.toCollection(ArrayList::new));
        IntStream.range(0, planChildren.size()).forEach(index -> {
            PlanNode node = planChildren.get(index);
            Configuration featureConfiguration = configuration.append(CONF_BUILDER.fromMap(node.properties()));
            String nodePath = String.format("0/%d", index);
            PlanNodeJUnitRunner runner = treatStepsAsTests
                    ? new PlanNodeStepJUnitRunner(
                            node, featureConfiguration, backendFactory, Optional.empty(),
                            planNodeLogger, nodePath, getUniqueId(), resourceRoots)
                    : new PlanNodeJUnitRunner(
                            node, featureConfiguration, backendFactory, Optional.empty(),
                            planNodeLogger, nodePath, getUniqueId(), resourceRoots);
            featureRunners.add(runner);
            addChild(runner.descriptor());
        });
        addChild(afterClassDescriptor);
    }

    void execute(EngineExecutionListener listener) {
        if (!profileEnabled) {
            listener.executionSkipped(this, profileSkipReason());
            return;
        }

        listener.executionStarted(this);
        TestExecutionResult result = TestExecutionResult.successful();
        try {
            executeLifecyclePhase(listener, beforeClassDescriptor, () -> {
                initWakamiti();
                invokeLifecycleHooks(BeforeAll.class);
            });
            featureRunners.forEach(runner -> runner.execute(listener));
            executeLifecyclePhase(listener, afterClassDescriptor, () -> {
                finalizeWakamiti();
                invokeLifecycleHooks(AfterAll.class);
            });
        } catch (Throwable error) {
            result = TestExecutionResult.failed(error);
        }
        listener.executionFinished(this, result);
    }

    private WakamitiNodeDescriptor lifecycleDescriptor(String name) {
        return new WakamitiNodeDescriptor(
                getUniqueId().append("lifecycle", name),
                name,
                null,
                Type.TEST
        );
    }

    private void executeLifecyclePhase(
            EngineExecutionListener listener,
            WakamitiNodeDescriptor descriptor,
            LifecycleAction action
    ) throws Throwable {
        listener.executionStarted(descriptor);
        try {
            action.run();
            listener.executionFinished(descriptor, TestExecutionResult.successful());
        } catch (Throwable error) {
            listener.executionFinished(descriptor, TestExecutionResult.failed(error));
            throw error;
        }
    }

    private void initWakamiti() {
        LOGGER.debug("{}", configuration);
        Wakamiti.contributors().propertyResolvers(configuration);
        wakamiti.configureLogger(configuration);
        wakamiti.configureEventObservers(configuration);
        plan.assignExecutionID(configuration.get(EXECUTION_ID, String.class).orElse(UUID.randomUUID().toString()));
        wakamiti.publishEvent(Event.PLAN_RUN_STARTED, new PlanNodeSnapshot(plan));
        planNodeLogger.logTestPlanHeader(plan);
    }

    private void finalizeWakamiti() {
        planNodeLogger.logTestPlanResult(plan);
        PlanNodeSnapshot snapshot = new PlanNodeSnapshot(plan);
        wakamiti.publishEvent(Event.PLAN_RUN_FINISHED, snapshot);
        wakamiti.writeOutputFile(plan, configuration);
        wakamiti.generateReports(configuration, snapshot);
    }

    private void invokeLifecycleHooks(Class<? extends Annotation> annotation) {
        for (Method method : testClass.getMethods()) {
            if (method.isAnnotationPresent(annotation) && Modifier.isStatic(method.getModifiers())) {
                try {
                    method.setAccessible(true);
                    method.invoke(null);
                } catch (ReflectiveOperationException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    }
                    throw new IllegalStateException(
                            "Could not invoke lifecycle method " + method.getName(), cause
                    );
                }
            }
        }
    }

    private Configuration retrieveConfiguration(Class<?> testedClass) throws ConfigurationException {
        Configuration config = Wakamiti.defaultConfiguration();
        Optional<String> altDir = Optional.ofNullable(testedClass.getClassLoader().getResource("."))
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

    private String profileSkipReason() {
        return String.format(
                "Skipping %s because it does not match active profile(s): %s",
                testClass.getName(),
                ProfileSelector.activeProfilesDescription()
        );
    }

    @FunctionalInterface
    private interface LifecycleAction {
        void run() throws Throwable;
    }

}
