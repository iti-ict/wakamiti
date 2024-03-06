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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.WakamitiConfiguration.EXECUTION_ID;
import static es.iti.wakamiti.api.WakamitiConfiguration.TREAT_STEPS_AS_TESTS;


public class WakamitiJUnitRunner extends ParentRunner<PlanNodeJUnitRunner> {

    protected static final Logger LOGGER = es.iti.wakamiti.core.Wakamiti.LOGGER;
    protected static final ConfigurationFactory CONF_BUILDER = ConfigurationFactory.instance();
    protected final PlanNodeLogger planNodeLogger;
    protected final boolean treatStepsAsTests;
    protected final es.iti.wakamiti.core.Wakamiti wakamiti;
    private final PlanNode plan;
    private List<PlanNodeJUnitRunner> children;

    protected Configuration configuration;

    public WakamitiJUnitRunner(Class<?> configurationClass) throws InitializationError {
        super(configurationClass);
        this.wakamiti = es.iti.wakamiti.core.Wakamiti.instance();
        this.configuration = retrieveConfiguration(configurationClass);
        this.plan = wakamiti.createPlanFromConfiguration(configuration);
        this.planNodeLogger = new PlanNodeLogger(LOGGER, configuration, plan);
        this.treatStepsAsTests = configuration.get(TREAT_STEPS_AS_TESTS, Boolean.class).orElse(Boolean.FALSE);
    }

    private static Configuration retrieveConfiguration(Class<?> testedClass) throws InitializationError {
        try {
            return es.iti.wakamiti.core.Wakamiti.defaultConfiguration().appendFromAnnotation(testedClass);
        } catch (ConfigurationException e) {
            LOGGER.error("Error loading configuration from {}", testedClass);
            throw new InitializationError(e);
        }
    }

    @Override
    protected List<PlanNodeJUnitRunner> getChildren() {
        if (children == null) {
            children = createChildren();
        }
        return children;
    }

    @Override
    protected Description describeChild(PlanNodeJUnitRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(PlanNodeJUnitRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
        validateNoAnnotatedMethod(getTestClass().getJavaClass(), Before.class, errors);
        validateNoAnnotatedMethod(getTestClass().getJavaClass(), After.class, errors);
        validateNoAnnotatedMethod(getTestClass().getJavaClass(), Test.class, errors);
    }

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

    private void initWakamiti() {
        LOGGER.debug("{}", configuration);
        wakamiti.configureLogger(configuration);
        wakamiti.configureEventObservers(configuration);
        plan.assignExecutionID(configuration.get(EXECUTION_ID, String.class).orElse(UUID.randomUUID().toString()));
        wakamiti.publishEvent(Event.PLAN_RUN_STARTED, new PlanNodeSnapshot(plan));
        planNodeLogger.logTestPlanHeader(plan);
    }

    private void finalizeWakamiti() {
        planNodeLogger.logTestPlanResult(plan);
        var snapshot = new PlanNodeSnapshot(plan);
        wakamiti.publishEvent(Event.PLAN_RUN_FINISHED, snapshot);
        wakamiti.writeOutputFile(plan, configuration);
        wakamiti.generateReports(configuration, snapshot);
    }

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
