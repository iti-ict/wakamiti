/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import static es.iti.wakamiti.api.WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS;
import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_TYPES;
import static es.iti.wakamiti.api.WakamitiConfiguration.STOP_EXECUTION_ON_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;


public class PlanRunnerLifecycleHooksIntegrationTest {

    private static final String FEATURES_ROOT = "src/test/resources/features/lifecycleProbe/";

    @Before
    @After
    public void resetProbe() {
        LifecycleProbeSteps.reset();
    }

    @Test
    public void executesPlanAndFeatureHooksAroundImplementedFeatures() {
        run("implemented", Configuration.factory().empty());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "PLAN_TEAR_DOWN"
        );
    }

    @Test
    public void preservesLifecycleBackendStateAndScenarioContext() {
        run("implemented", Configuration.factory().empty());

        assertThat(LifecycleProbeSteps.planSetUpInstance())
                .isEqualTo(LifecycleProbeSteps.planTearDownInstance());
        assertThat(LifecycleProbeSteps.featureSetUpInstances())
                .containsExactlyElementsOf(LifecycleProbeSteps.featureTearDownInstances());
        assertThat(LifecycleProbeSteps.scenarioContexts()).containsOnly(true);
        assertThat(es.iti.wakamiti.api.WakamitiStepRunContext.current()).isNull();
    }

    @Test
    public void skipsPlanAndFeatureHooksWhenDefinitionsHaveNoImplementation() {
        PlanNode plan = run("definitionOnly", Configuration.factory().empty());

        assertThat(LifecycleProbeSteps.events()).isEmpty();
        assertThat(plan.result()).contains(Result.NOT_IMPLEMENTED);
        assertThat(feature(plan, "Definition only").result()).contains(Result.NOT_IMPLEMENTED);
    }

    @Test
    public void runsPlanHooksButSkipsFeatureHooksForDefinitionOnlyFeatureInMixedPlan() {
        PlanNode plan = run("mixed", Configuration.factory().empty());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "PLAN_TEAR_DOWN"
        );
        assertThat(feature(plan, "Definition only").result()).contains(Result.NOT_IMPLEMENTED);
        assertThat(feature(plan, "Implemented feature").result()).contains(Result.PASSED);
    }

    @Test
    public void doesNotRunLifecycleOperationsInDryRun() {
        Configuration configuration = Configuration.factory().fromPairs(WakamitiConfiguration.DRY_RUN, "true");

        run("implemented", configuration);

        assertThat(LifecycleProbeSteps.events()).isEmpty();
    }

    @Test
    public void continuesAfterPlanSetUpErrorAndRunsPlanTearDownWhenStopOnErrorIsDisabled() {
        LifecycleProbeSteps.failAt(LifecycleProbeSteps.FailurePoint.PLAN_SET_UP);

        PlanNode plan = run("implemented", Configuration.factory().empty());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "PLAN_TEAR_DOWN"
        );
        assertThat(plan.result()).contains(Result.ERROR);
        assertThat(feature(plan, "First feature").result()).contains(Result.PASSED);
        assertThat(feature(plan, "Second feature").result()).contains(Result.PASSED);
    }

    @Test
    public void stopsAfterPlanSetUpErrorAndRunsPlanTearDownWhenStopOnErrorIsEnabled() {
        LifecycleProbeSteps.failAt(LifecycleProbeSteps.FailurePoint.PLAN_SET_UP);

        PlanNode plan = run("implemented", stopOnError());

        assertThat(LifecycleProbeSteps.events()).containsExactly("PLAN_SET_UP", "PLAN_TEAR_DOWN");
        assertThat(plan.result()).contains(Result.ERROR);
        assertThat(feature(plan, "First feature").result()).contains(Result.SKIPPED);
        assertThat(feature(plan, "Second feature").result()).contains(Result.SKIPPED);
    }

    @Test
    public void marksPlanAsErrorWhenPlanTearDownFails() {
        LifecycleProbeSteps.failAt(LifecycleProbeSteps.FailurePoint.PLAN_TEAR_DOWN);

        PlanNode plan = run("implemented", Configuration.factory().empty());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "PLAN_TEAR_DOWN"
        );
        assertThat(plan.result()).contains(Result.ERROR);
        assertThat(feature(plan, "First feature").result()).contains(Result.PASSED);
        assertThat(feature(plan, "Second feature").result()).contains(Result.PASSED);
    }

    @Test
    public void continuesAfterFeatureSetUpErrorWhenStopOnErrorIsDisabled() {
        LifecycleProbeSteps.failAt(LifecycleProbeSteps.FailurePoint.FEATURE_SET_UP);

        PlanNode plan = run("implemented", Configuration.factory().empty());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "PLAN_TEAR_DOWN"
        );
        assertThat(feature(plan, "First feature").result()).contains(Result.ERROR);
        assertThat(feature(plan, "Second feature").result()).contains(Result.ERROR);
    }

    @Test
    public void stopsAfterFeatureSetUpErrorAndStillRunsScopeTeardownsWhenEnabled() {
        LifecycleProbeSteps.failAt(LifecycleProbeSteps.FailurePoint.FEATURE_SET_UP);

        PlanNode plan = run("implemented", stopOnError());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP", "FEATURE_SET_UP", "FEATURE_TEAR_DOWN", "PLAN_TEAR_DOWN"
        );
        assertThat(feature(plan, "First feature").result()).contains(Result.ERROR);
        assertThat(feature(plan, "Second feature").result()).contains(Result.SKIPPED);
    }

    @Test
    public void stopsAfterFeatureTearDownErrorWhenEnabled() {
        LifecycleProbeSteps.failAt(LifecycleProbeSteps.FailurePoint.FEATURE_TEAR_DOWN);

        PlanNode plan = run("implemented", stopOnError());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN",
                "PLAN_TEAR_DOWN"
        );
        assertThat(feature(plan, "First feature").result()).contains(Result.ERROR);
        assertThat(feature(plan, "Second feature").result()).contains(Result.SKIPPED);
    }

    @Test
    public void propagatesBeforeHookErrorToFeatureWhileContinuingWhenStopOnErrorIsDisabled() {
        PlanNode plan = run("beforeFails", Configuration.factory().empty());

        assertThat(LifecycleProbeSteps.events()).containsSubsequence(
                "FAILING_STEP", "SCENARIO_TEAR_DOWN", "SCENARIO_SET_UP", "STEP"
        );
        assertThat(feature(plan, "Feature with failing before hook").result()).contains(Result.ERROR);
        assertThat(feature(plan, "Second feature").result()).contains(Result.PASSED);
        assertThat(new PlanNodeSnapshot(plan).getTestCaseResults()).doesNotContainKey(Result.ERROR);
    }

    @Test
    public void stopsAfterBeforeHookErrorWhenStopOnErrorIsEnabled() {
        PlanNode plan = run("beforeFails", stopOnError());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP", "FEATURE_SET_UP", "SCENARIO_SET_UP", "FAILING_STEP", "SCENARIO_TEAR_DOWN",
                "FEATURE_TEAR_DOWN", "PLAN_TEAR_DOWN"
        );
        assertThat(feature(plan, "Feature with failing before hook").result()).contains(Result.ERROR);
        assertThat(feature(plan, "Second feature").result()).contains(Result.SKIPPED);
    }

    @Test
    public void propagatesAfterHookErrorToFeatureAndStopsFollowingFeaturesWhenEnabled() {
        PlanNode plan = run("afterFails", stopOnError());

        assertThat(LifecycleProbeSteps.events()).containsExactly(
                "PLAN_SET_UP",
                "FEATURE_SET_UP", "SCENARIO_SET_UP", "STEP", "SCENARIO_TEAR_DOWN",
                "SCENARIO_SET_UP", "FAILING_STEP", "SCENARIO_TEAR_DOWN", "FEATURE_TEAR_DOWN", "PLAN_TEAR_DOWN"
        );
        assertThat(feature(plan, "Feature with failing after hook").result()).contains(Result.ERROR);
        assertThat(feature(plan, "Second feature").result()).contains(Result.SKIPPED);
    }

    private PlanNode run(
            String resourcePath,
            Configuration extraConfiguration
    ) {
        Configuration configuration = Wakamiti.defaultConfiguration().appendFromPairs(
                RESOURCE_TYPES, GherkinResourceType.NAME,
                RESOURCE_PATH, FEATURES_ROOT + resourcePath,
                NON_REGISTERED_STEP_PROVIDERS, LifecycleProbeSteps.class.getName()
        ).append(extraConfiguration);
        Wakamiti wakamiti = Wakamiti.instance();
        PlanNode plan = wakamiti.createPlanFromConfiguration(configuration);
        return wakamiti.executePlan(plan, configuration);
    }

    private Configuration stopOnError() {
        return Configuration.factory().fromPairs(STOP_EXECUTION_ON_ERROR, "true");
    }

    private PlanNode feature(
            PlanNode plan,
            String name
    ) {
        return plan.children()
                .filter(node -> node.nodeType() == NodeType.AGGREGATOR && name.equals(node.name()))
                .findFirst()
                .orElseThrow();
    }

}
