/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import static org.assertj.core.api.Assertions.assertThat;


public class WakamitiRunnerLifecycleHooksTest {

    private static volatile String currentTestName;

    @AfterEach
    public void cleanup() {
        currentTestName = null;
        HookAwareRunner.clearHooks();
    }

    @Test
    public void beforeAndAfterClassHooksAreReportedInsideLifecycleEntries() {
        runPlan(HookAwareRunner.class, new RecordingListener());

        assertThat(HookAwareRunner.hookContexts).containsExactly(
                "before:beforeClass",
                "after:afterClass"
        );
    }

    private void runPlan(Class<?> planClass, TestExecutionListener listener) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(planClass))
                .filters(EngineFilter.includeEngines(WakamitiTestEngine.ENGINE_ID))
                .build();
        Launcher launcher = LauncherFactory.create();
        if (listener != null) {
            launcher.registerTestExecutionListeners(listener);
        }
        launcher.execute(request);
    }

    @AnnotatedConfiguration({
            @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
            @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features/redefining"),
            @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.junit5.WakamitiSteps")
    })
    @WakamitiPlan
    public static class HookAwareRunner {

        private static final List<String> hookContexts = new ArrayList<>();

        @BeforeAll
        public static void beforeClassHook() {
            hookContexts.add("before:" + currentTestName);
        }

        @AfterAll
        public static void afterClassHook() {
            hookContexts.add("after:" + currentTestName);
        }

        private static void clearHooks() {
            hookContexts.clear();
        }
    }

    private static class RecordingListener implements TestExecutionListener {

        @Override
        public void executionStarted(TestIdentifier testIdentifier) {
            if (testIdentifier.isTest()) {
                currentTestName = testIdentifier.getDisplayName();
            }
        }

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (testIdentifier.isTest()) {
                currentTestName = null;
            }
        }
    }
}
