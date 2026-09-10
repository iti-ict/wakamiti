/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Level;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;


/** Test contributor that exposes lifecycle activity as observable events. */
@I18nResource("steps/lifecycle-probe")
public class LifecycleProbeSteps implements StepContributor {

    public enum FailurePoint {

        NONE,
        PLAN_SET_UP,
        PLAN_TEAR_DOWN,
        FEATURE_SET_UP,
        FEATURE_TEAR_DOWN

    }

    private static final List<String> EVENTS = new CopyOnWriteArrayList<>();
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    private static final List<Integer> FEATURE_SET_UP_INSTANCES = new CopyOnWriteArrayList<>();
    private static final List<Integer> FEATURE_TEAR_DOWN_INSTANCES = new CopyOnWriteArrayList<>();
    private static final List<Boolean> SCENARIO_CONTEXTS = new CopyOnWriteArrayList<>();
    private static volatile FailurePoint failurePoint = FailurePoint.NONE;
    private static volatile Integer planSetUpInstance;
    private static volatile Integer planTearDownInstance;

    private final int instanceId = INSTANCE_COUNTER.incrementAndGet();

    public static void reset() {
        EVENTS.clear();
        FEATURE_SET_UP_INSTANCES.clear();
        FEATURE_TEAR_DOWN_INSTANCES.clear();
        SCENARIO_CONTEXTS.clear();
        failurePoint = FailurePoint.NONE;
        planSetUpInstance = null;
        planTearDownInstance = null;
    }

    public static void failAt(
            FailurePoint point
    ) {
        failurePoint = point;
    }

    public static List<String> events() {
        return List.copyOf(EVENTS);
    }

    public static Integer planSetUpInstance() {
        return planSetUpInstance;
    }

    public static Integer planTearDownInstance() {
        return planTearDownInstance;
    }

    public static List<Integer> featureSetUpInstances() {
        return List.copyOf(FEATURE_SET_UP_INSTANCES);
    }

    public static List<Integer> featureTearDownInstances() {
        return List.copyOf(FEATURE_TEAR_DOWN_INSTANCES);
    }

    public static List<Boolean> scenarioContexts() {
        return List.copyOf(SCENARIO_CONTEXTS);
    }

    @Override
    public String info() {
        return "lifecycle-probe";
    }

    @SetUp(level = Level.PLAN)
    public void setUpPlan() {
        planSetUpInstance = instanceId;
        record("PLAN_SET_UP", FailurePoint.PLAN_SET_UP);
    }

    @TearDown(level = Level.PLAN)
    public void tearDownPlan() {
        planTearDownInstance = instanceId;
        record("PLAN_TEAR_DOWN", FailurePoint.PLAN_TEAR_DOWN);
    }

    @SetUp(level = Level.FEATURE)
    public void setUpFeature() {
        FEATURE_SET_UP_INSTANCES.add(instanceId);
        record("FEATURE_SET_UP", FailurePoint.FEATURE_SET_UP);
    }

    @TearDown(level = Level.FEATURE)
    public void tearDownFeature() {
        FEATURE_TEAR_DOWN_INSTANCES.add(instanceId);
        record("FEATURE_TEAR_DOWN", FailurePoint.FEATURE_TEAR_DOWN);
    }

    @SetUp(level = Level.SCENARIO)
    public void setUpScenario() {
        SCENARIO_CONTEXTS.add(WakamitiStepRunContext.current() != null);
        EVENTS.add("SCENARIO_SET_UP");
    }

    @TearDown(level = Level.SCENARIO)
    public void tearDownScenario() {
        SCENARIO_CONTEXTS.add(WakamitiStepRunContext.current() != null);
        EVENTS.add("SCENARIO_TEAR_DOWN");
    }

    @Step("given.lifecycle.probe")
    public void executeProbeStep() {
        EVENTS.add("STEP");
    }

    @Step("given.lifecycle.probe.failure")
    public void executeFailingProbeStep() {
        EVENTS.add("FAILING_STEP");
        throw new WakamitiException("forced lifecycle hook error");
    }

    private void record(
            String event,
            FailurePoint point
    ) {
        EVENTS.add(event);
        if (failurePoint == point) {
            throw new WakamitiException("forced {} error", event);
        }
    }

}
