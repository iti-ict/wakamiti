/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.junit;


import imconfig.Configuration;
import imconfig.ConfigurationException;
import imconfig.ConfigurationFactory;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.core.Kukumo;
import iti.kukumo.core.runner.PlanNodeLogger;
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


public class KukumoJUnitRunner extends Runner {

    /**
     * Configuration property to set if the steps are treated as tests. False by
     * default
     */
    public static final String TREAT_STEPS_AS_TESTS = "junit.treatStepsAsTests";

    protected static final Logger LOGGER = Kukumo.LOGGER;
    protected static final ConfigurationFactory confBuilder = ConfigurationFactory.instance();

    protected Configuration configuration;
    protected final Class<?> configurationClass;
    protected final PlanNodeLogger planNodeLogger;
    protected final boolean treatStepsAsTests;
    protected final Kukumo kukumo;
    private PlanNode plan;
    private Description description;


    public KukumoJUnitRunner(Class<?> configurationClass) throws InitializationError {
        this.kukumo = Kukumo.instance();
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
    }


    @Override
    public void run(RunNotifier notifier) {
        kukumo.configureLogger(configuration);
        kukumo.configureEventObservers(configuration);
        plan.assignExecutionID(
            configuration.get(KukumoConfiguration.EXECUTION_ID,String.class).orElse(UUID.randomUUID().toString())
        );
        kukumo.publishEvent(Event.PLAN_RUN_STARTED, new PlanNodeSnapshot(plan));
        planNodeLogger.logTestPlanHeader(plan);
        executeAnnotatedMethod(configurationClass, BeforeClass.class);

        for (JUnitPlanNodeRunner child : createChildren()) {
            try {
                child.runNode(notifier);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }

        // refactor this line in the future when multithreading is supported
        executeAnnotatedMethod(configurationClass, AfterClass.class);

        planNodeLogger.logTestPlanResult(plan);
        kukumo.publishEvent(Event.PLAN_RUN_FINISHED, new PlanNodeSnapshot(getPlan()));
        kukumo.writeOutputFile(plan, configuration);
        kukumo.generateReports(configuration);
    }




    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description
                .createSuiteDescription("Kukumo Test Plan", UUID.randomUUID().toString());
            for (JUnitPlanNodeRunner child : createChildren()) {
                description.addChild(child.getDescription());
            }
        }
        return description;
    }


    protected PlanNode getPlan() {
        if (plan == null) {
            plan = kukumo.createPlanFromConfiguration(configuration);
        }
        return plan;
    }


    protected List<JUnitPlanNodeRunner> createChildren() {

        BackendFactory backendFactory = kukumo.newBackendFactory();
        return getPlan().children().map(node -> {
            Configuration featureConfiguration = configuration.append(
                confBuilder.fromMap(node.properties())
            );
            return treatStepsAsTests ? new JUnitPlanNodeStepRunner(
                node, featureConfiguration, backendFactory, planNodeLogger
            ) : new JUnitPlanNodeRunner(node, featureConfiguration, backendFactory, planNodeLogger);
        }).collect(Collectors.toList());

    }


    private static Configuration retrieveConfiguration(
        Class<?> testedClass
    ) throws InitializationError {
        try {
            return Kukumo.defaultConfiguration().appendFromAnnotation(testedClass);
        } catch (ConfigurationException e) {
            LOGGER.error("Error loading configuration from {}", testedClass);
            throw new InitializationError(e);
        }
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
                boolean setUpConfig = (
                  method.getParameterCount() == 1 && method.getParameterTypes()[0] == Configuration.class && method.getReturnType() == Configuration.class
                );
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

            // accepts a setUp method in form of Configuration setUp(Configuration)
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Configuration.class && method.getReturnType() == Configuration.class) {
                try {
                    this.configuration = (Configuration) method.invoke(null,this.configuration);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new KukumoException(e);
                }
            } else {

                try {
                    method.invoke(null);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new KukumoException(e);
                }

            }

        }
    }

}