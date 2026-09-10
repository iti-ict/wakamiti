/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;


public class WakamitiRunnerFeatureLifecycleHooksTest {

    @Test
    public void lifecycleFeatureRunsSuccessfully() {
        ExecutionProbe probe = runPlan(TestWakamitiLifecycleRunner.class);

        assertThat(probe.summary.getTestsFailedCount()).isZero();
        assertThat(probe.summary.getTestsSucceededCount()).isGreaterThan(0);
        assertThat(probe.startedDisplayNames)
                .anyMatch(name -> name.contains("Setup feature execution"))
                .anyMatch(name -> name.contains("Functional scenario"))
                .anyMatch(name -> name.contains("Teardown feature execution"));
    }

    @Test
    public void lifecycleFeatureRunsSuccessfullyWithPerTestCaseOutput() {
        ExecutionProbe probe = runPlan(TestWakamitiLifecycleRunnerMultipleOutput.class);

        assertThat(probe.summary.getTestsFailedCount()).isZero();
        assertThat(probe.summary.getTestsSucceededCount()).isGreaterThan(0);
        assertThat(probe.startedDisplayNames)
                .anyMatch(name -> name.contains("Setup feature execution"))
                .anyMatch(name -> name.contains("Functional scenario"))
                .anyMatch(name -> name.contains("Teardown feature execution"));
    }

    private ExecutionProbe runPlan(
            Class<?> planClass
    ) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(planClass))
                .filters(EngineFilter.includeEngines(WakamitiTestEngine.ENGINE_ID))
                .build();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        RecordingListener recordingListener = new RecordingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(summaryListener, recordingListener);
        launcher.execute(request);
        return new ExecutionProbe(summaryListener.getSummary(), recordingListener.startedDisplayNames);
    }

    private record ExecutionProbe(TestExecutionSummary summary, List<String> startedDisplayNames) {

    }

    private static final class RecordingListener implements TestExecutionListener {

        private final List<String> startedDisplayNames = new ArrayList<>();

        @Override
        public void executionStarted(
                TestIdentifier testIdentifier
        ) {
            startedDisplayNames.add(testIdentifier.getDisplayName());
        }

        @Override
        public void executionFinished(
                TestIdentifier testIdentifier,
                TestExecutionResult testExecutionResult
        ) {
            // no-op
        }

    }

}
