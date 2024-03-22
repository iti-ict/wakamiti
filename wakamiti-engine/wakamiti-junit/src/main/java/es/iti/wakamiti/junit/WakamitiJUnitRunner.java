/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import imconfig.Configuration;
import imconfig.ConfigurationException;
import imconfig.ConfigurationFactory;
import org.junit.*;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.WakamitiConfiguration.EXECUTION_ID;
import static es.iti.wakamiti.api.WakamitiConfiguration.TREAT_STEPS_AS_TESTS;


/**
 * JUnit Runner for executing Wakamiti plan.
 *
 * <p>This custom JUnit runner integrates Wakamiti plan nodes with JUnit for test execution. It extends
 * {@link ParentRunner} and manages the execution of Wakamiti test plan nodes within a JUnit framework. The
 * runner supports the execution of test suites and test cases, providing descriptions and handling child nodes
 * accordingly.</p>
 *
 * <p>The runner ensures proper initialization and finalization of the Wakamiti framework, captures test plan
 * results, and supports the configuration of Wakamiti settings through annotations on the test class.</p>
 *
 * <p>Annotations such as {@link BeforeClass}, {@link AfterClass}, and {@link Test} are not allowed on the
 * test class, as Wakamiti manages its own lifecycle and execution flow.</p>
 *
 * @author María Galbis Calomarde - mgalbis@iti.es
 */
public class WakamitiJUnitRunner extends ParentRunner<PlanNodeJUnitRunner> {

    protected static final Logger LOGGER = es.iti.wakamiti.core.Wakamiti.LOGGER;
    protected static final ConfigurationFactory CONF_BUILDER = ConfigurationFactory.instance();
    protected final PlanNodeLogger planNodeLogger;
    protected final boolean treatStepsAsTests;
    protected final es.iti.wakamiti.core.Wakamiti wakamiti;
    private final PlanNode plan;
    protected Configuration configuration;
    private List<PlanNodeJUnitRunner> children;

    /**
     * Constructs a WakamitiJUnitRunner for the specified test class.
     *
     * <p>This constructor initializes the Wakamiti framework, creates a test plan based on the configuration,
     * and configures the Wakamiti logger and event observers.</p>
     *
     * @param configurationClass The test class containing the Wakamiti configuration annotations.
     * @throws InitializationError If there is an error initializing the runner.
     */
    public WakamitiJUnitRunner(Class<?> configurationClass) throws InitializationError {
        super(configurationClass);
        this.wakamiti = es.iti.wakamiti.core.Wakamiti.instance();
        this.configuration = retrieveConfiguration(configurationClass);
        this.plan = wakamiti.createPlanFromConfiguration(configuration);
        this.planNodeLogger = new PlanNodeLogger(LOGGER, configuration, plan);
        this.treatStepsAsTests = configuration.get(TREAT_STEPS_AS_TESTS, Boolean.class).orElse(Boolean.FALSE);
    }

    /**
     * Retrieves the Wakamiti configuration for the specified test class.
     *
     * <p>This method is responsible for fetching the Wakamiti configuration for a given test class.
     * It uses Wakamiti's default configuration and appends additional configuration obtained from
     * annotations present on the test class. If there is an error loading the configuration,
     * it logs an error message and throws an InitializationError.</p>
     *
     * @param testedClass The test class for which to retrieve the configuration.
     * @return The Wakamiti configuration for the specified test class.
     * @throws InitializationError If an error occurs during configuration retrieval.
     */
    private static Configuration retrieveConfiguration(Class<?> testedClass) throws InitializationError {
        try {
            return es.iti.wakamiti.core.Wakamiti.defaultConfiguration().appendFromAnnotation(testedClass);
        } catch (ConfigurationException e) {
            LOGGER.error("Error loading configuration from {}", testedClass);
            throw new InitializationError(e);
        }
    }

    /**
     * Retrieves the child nodes representing test suites or test cases.
     *
     * <p>This method creates the child runners based on the Wakamiti plan and configuration settings.</p>
     *
     * @return A list of PlanNodeJUnitRunner instances representing the child nodes.
     */
    @Override
    protected List<PlanNodeJUnitRunner> getChildren() {
        if (children == null) {
            children = createChildren();
        }
        return children;
    }

    /**
     * Describes a child node for reporting purposes.
     *
     * <p>This method returns a JUnit Description for the specified child runner.</p>
     *
     * @param child The child runner.
     * @return A Description object representing the child node.
     */
    @Override
    protected Description describeChild(PlanNodeJUnitRunner child) {
        return child.getDescription();
    }

    /**
     * Runs a child node with the provided RunNotifier.
     *
     * <p>This method executes the specified child runner, capturing the results using the provided RunNotifier.</p>
     *
     * @param child    The child runner representing a test suite or test case.
     * @param notifier The RunNotifier for reporting test execution events.
     */
    @Override
    protected void runChild(PlanNodeJUnitRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    /**
     * Collects initialization errors for the test class.
     *
     * <p>This method validates that no annotated methods (e.g., BeforeClass, AfterClass, Test) are present in the
     * test class. Any violations are added to the list of errors.</p>
     *
     * @param errors The list to which validation errors are added.
     */
    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        validateNoAnnotatedMethod(getTestClass().getJavaClass(), Before.class, errors);
        validateNoAnnotatedMethod(getTestClass().getJavaClass(), After.class, errors);
        validateNoAnnotatedMethod(getTestClass().getJavaClass(), Test.class, errors);
    }

    /**
     * Creates child runners for the test class.
     *
     * @return The list of child runners.
     */
    protected List<PlanNodeJUnitRunner> createChildren() {
        BackendFactory backendFactory = wakamiti.newBackendFactory();
        return plan.children().map(node -> {
            Configuration featureConfiguration = configuration.append(
                    CONF_BUILDER.fromMap(node.properties())
            );
            return treatStepsAsTests ? new PlanNodeStepJUnitRunner(
                    node, featureConfiguration, backendFactory, planNodeLogger
            ) : new PlanNodeJUnitRunner(node, featureConfiguration, backendFactory, planNodeLogger);
        }).collect(Collectors.toList());
    }

    /**
     * Initializes Wakamiti before the test plan execution.
     */
    public void initWakamiti() {
        LOGGER.debug("{}", configuration);
        wakamiti.configureLogger(configuration);
        wakamiti.configureEventObservers(configuration);
        plan.assignExecutionID(configuration.get(EXECUTION_ID, String.class).orElse(UUID.randomUUID().toString()));
        wakamiti.publishEvent(Event.PLAN_RUN_STARTED, new PlanNodeSnapshot(plan));
        planNodeLogger.logTestPlanHeader(plan);
    }

    /**
     * Finalizes Wakamiti after the test plan execution.
     */
    public void finalizeWakamiti() {
        planNodeLogger.logTestPlanResult(plan);
        var snapshot = new PlanNodeSnapshot(plan);
        wakamiti.publishEvent(Event.PLAN_RUN_FINISHED, snapshot);
        wakamiti.writeOutputFile(plan, configuration);
        wakamiti.generateReports(configuration, snapshot);
    }

    /**
     * Overrides the execution of setup methods annotated with {@code @BeforeClass} for WakamitiJUnitRunner.
     *
     * <p>This method intercepts the execution of setup methods annotated with {@code @BeforeClass} for the
     * WakamitiJUnitRunner. It prepares the initialization of Wakamiti before executing these setup methods.
     * If the method to initialize Wakamiti is not found, it throws a WakamitiException indicating the failure.</p>
     *
     * @param statement The statement to be executed, which includes the setup methods annotated with {@code @BeforeClass}.
     * @return The statement with the intercepted execution of Wakamiti initialization.
     * @throws WakamitiException If the method to initialize Wakamiti is not found.
     */
    @Override
    protected Statement withBeforeClasses(Statement statement) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(BeforeClass.class);
        try {
            Method initWakamiti = this.getClass().getDeclaredMethod("initWakamiti");
            initWakamiti.setAccessible(true);
            statement = new RunBefores(statement, List.of(new FrameworkMethod(initWakamiti)), this);
        } catch (NoSuchMethodException e) {
            throw new WakamitiException("Cannot initialize wakamiti runner", e);
        }
        return (befores.isEmpty() ? statement : new RunBefores(statement, befores, null));
    }

    /**
     * Overrides the execution of teardown methods annotated with {@code @AfterClass} for WakamitiJUnitRunner.
     *
     * <p>This method intercepts the execution of teardown methods annotated with {@code @AfterClass} for the
     * WakamitiJUnitRunner. It prepares the finalization of Wakamiti after executing these teardown methods.
     * If the method to finalize Wakamiti is not found, it throws a WakamitiException indicating the failure.</p>
     *
     * @param statement The statement to be executed, which includes the teardown methods annotated with {@code @AfterClass}.
     * @return The statement with the intercepted execution of Wakamiti finalization.
     * @throws WakamitiException If the method to finalize Wakamiti is not found.
     */
    @Override
    protected Statement withAfterClasses(Statement statement) {
        List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterClass.class);
        try {
            Method finalizeWakamiti = this.getClass().getDeclaredMethod("finalizeWakamiti");
            finalizeWakamiti.setAccessible(true);
            statement = new RunAfters(statement, List.of(new FrameworkMethod(finalizeWakamiti)), this);
        } catch (NoSuchMethodException e) {
            throw new WakamitiException("Cannot finalize wakamiti runner", e);
        }
        return (afters.isEmpty() ? statement : new RunAfters(statement, afters, null));
    }

    /**
     * Validates that no annotated methods are present in the test class.
     *
     * @param configurationClass The test class.
     * @param annotation         The annotation to check for.
     * @param errors             The list to collect errors.
     */
    private void validateNoAnnotatedMethod(
            Class<?> configurationClass,
            Class<? extends Annotation> annotation,
            List<Throwable> errors
    ) {
        for (Method method : configurationClass.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                String message = String.format("Method %s annotated with %s is not allowed",
                        method.getName(), annotation.getName());
                errors.add(new InitializationError(message));
            }
        }
    }

}
