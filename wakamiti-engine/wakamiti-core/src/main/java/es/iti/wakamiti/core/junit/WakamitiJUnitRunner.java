/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.junit;


import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;
import es.iti.wakamiti.api.imconfig.ConfigurationFactory;
import org.junit.*;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.WakamitiConfiguration.TREAT_STEPS_AS_TESTS;


@Deprecated(since = "2.4.0", forRemoval = true)
public class WakamitiJUnitRunner extends Runner {

    protected static final Logger LOGGER = Wakamiti.LOGGER;
    protected static final ConfigurationFactory confBuilder = ConfigurationFactory.instance();
    protected final Class<?> configurationClass;
    protected final PlanNodeLogger planNodeLogger;
    protected final boolean treatStepsAsTests;
    protected final Wakamiti wakamiti;
    private final List<JUnitPlanNodeRunner> children;
    protected Configuration configuration;
    private PlanNode plan;
    private Description description;


    public WakamitiJUnitRunner(Class<?> configurationClass) throws InitializationError {
        this.wakamiti = Wakamiti.instance();
        this.configurationClass = configurationClass;
        this.configuration = retrieveConfiguration(configurationClass);
        this.planNodeLogger = new PlanNodeLogger(LOGGER, configuration, getPlan());
        this.treatStepsAsTests = configuration.get(TREAT_STEPS_AS_TESTS, Boolean.class)
                .orElse(Boolean.FALSE);
        validateStaticZeroArgumentAnnotatedMethod(configurationClass, BeforeClass.class);
        validateStaticZeroArgumentAnnotatedMethod(configurationClass, AfterClass.class);
        validateNoAnnotatedMethod(configurationClass, Before.class);
        validateNoAnnotatedMethod(configurationClass, After.class);
        validateNoAnnotatedMethod(configurationClass, Test.class);
        wakamiti.configureLogger(configuration);
        wakamiti.configureEventObservers(configuration);
        plan.assignExecutionID(
                configuration.get(WakamitiConfiguration.EXECUTION_ID, String.class).orElse(UUID.randomUUID().toString())
        );
        children = createChildren();
    }

    private static Configuration retrieveConfiguration(
            Class<?> testedClass
    ) throws InitializationError {
        try {
            return Wakamiti.defaultConfiguration().appendFromAnnotation(testedClass);
        } catch (ConfigurationException e) {
            LOGGER.error("Error loading configuration from {}", testedClass);
            throw new InitializationError(e);
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        LOGGER.debug("{}", configuration);
        wakamiti.publishEvent(Event.PLAN_RUN_STARTED, new PlanNodeSnapshot(plan));
        planNodeLogger.logTestPlanHeader(plan);
        executeAnnotatedMethod(configurationClass, BeforeClass.class);

        for (JUnitPlanNodeRunner child : children) {
            try {
                Result result = child.runNode(notifier);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }

        // refactor this line in the future when multithreading is supported
        executeAnnotatedMethod(configurationClass, AfterClass.class);

        planNodeLogger.logTestPlanResult(plan);
        var snapshot = new PlanNodeSnapshot(getPlan());
        wakamiti.publishEvent(Event.PLAN_RUN_FINISHED, snapshot);
        wakamiti.writeOutputFile(plan, configuration);
        wakamiti.generateReports(configuration, snapshot);
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description
                    .createSuiteDescription(configurationClass.getSimpleName(), UUID.randomUUID().toString());
            for (JUnitPlanNodeRunner child : children) {
                description.addChild(child.getDescription());
            }
        }
        return description;
    }

    protected PlanNode getPlan() {
        if (plan == null) {
            plan = wakamiti.createPlanFromConfiguration(configuration);
        }
        return plan;
    }

    protected List<JUnitPlanNodeRunner> createChildren() {
        BackendFactory backendFactory = wakamiti.newBackendFactory();
        return getPlan().children().map(node -> {
            Configuration featureConfiguration = configuration.append(
                    confBuilder.fromMap(node.properties())
            );
            return treatStepsAsTests ? new JUnitPlanNodeStepRunner(
                    node, featureConfiguration, backendFactory, planNodeLogger
            ) : new JUnitPlanNodeRunner(node, featureConfiguration, backendFactory, planNodeLogger);
        }).collect(Collectors.toList());

    }

    private void validateNoAnnotatedMethod(
            Class<?> configurationClass,
            Class<? extends Annotation> annotation
    ) throws InitializationError {
        for (Method method : configurationClass.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                throwInitializationError(method, annotation, "is not allowed");
            }
        }
    }


    private void validateStaticZeroArgumentAnnotatedMethod(
            Class<?> configurationClass,
            Class<? extends Annotation> annotation
    ) throws InitializationError {
        for (Method method : configurationClass.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throwInitializationError(method, annotation, "should be static");
                }
                boolean setUpConfig = method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == Configuration.class
                        && method.getReturnType() == Configuration.class;
                if (method.getParameterCount() > 0 && !setUpConfig) {
                    throwInitializationError(method, annotation, "should have no parameter");
                }
            }
        }
    }


    private void throwInitializationError(
            Method method,
            Class<? extends Annotation> annotation,
            String message
    ) throws InitializationError {
        throw new InitializationError(
                "Method " + method.getName() + " annotated with " + annotation + " " + message
        );
    }


    private void executeAnnotatedMethod(
            Class<?> configurationClass,
            Class<? extends Annotation> annotation
    ) {
        for (Method method : configurationClass.getMethods()) {
            if (!method.isAnnotationPresent(annotation)) {
                continue;
            }

            try {
                method.invoke(null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new WakamitiException(e);
            }
        }
    }

}