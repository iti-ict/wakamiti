/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import static org.assertj.core.api.Assertions.assertThat;


public class WakamitiRunnerProfileReportingTest {

    @AfterEach
    public void cleanupProperties() {
        System.clearProperty(ProfileSelector.PROFILE_PROPERTY);
        System.clearProperty(ProfileSelector.PROFILE_FALLBACK_PROPERTY);
        System.clearProperty(ProfileSelector.STRICT_PROPERTY);
        System.clearProperty(ProfileSelector.STRICT_FALLBACK_PROPERTY);
    }

    @Test
    public void profileMismatchIsReportedAsSkippedWithReason() {
        System.setProperty(ProfileSelector.PROFILE_PROPERTY, "A");
        System.setProperty(ProfileSelector.STRICT_PROPERTY, "true");

        TestExecutionSummary summary = runPlan(ProfileBRunner.class);

        assertThat(summary.getTestsFoundCount()).isZero();
        assertThat(summary.getTestsSucceededCount()).isZero();
        assertThat(summary.getTestsFailedCount()).isZero();
    }

    private TestExecutionSummary runPlan(Class<?> planClass) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(planClass))
                .filters(EngineFilter.includeEngines(WakamitiTestEngine.ENGINE_ID))
                .build();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(summaryListener);
        launcher.execute(request);
        return summaryListener.getSummary();
    }
}
