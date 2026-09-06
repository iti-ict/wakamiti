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

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;


public class WakamitiRuntimeConfigurationTest {

    @Test
    public void restoresWorkingDirAfterAnotherDescriptorIsCreated() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(PropertyEvaluatorIT.class), selectClass(SecondPlan.class))
                .filters(EngineFilter.includeEngines(WakamitiTestEngine.ENGINE_ID))
                .build();
        List<TestIdentifier> failures = new ArrayList<>();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(new TestExecutionListener() {
            @Override
            public void executionFinished(
                    TestIdentifier testIdentifier,
                    TestExecutionResult testExecutionResult
            ) {
                if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
                    failures.add(testIdentifier);
                }
            }
        });

        launcher.execute(request);

        assertThat(failures).isEmpty();
    }

    @AnnotatedConfiguration({
            @Property(key = WakamitiConfiguration.WORKING_DIR, value = "src/test/resources"),
            @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
            @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "features/redefining"),
            @Property(key = WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS,
                    value = "es.iti.wakamiti.junit5.WakamitiSteps"),
            @Property(key = WakamitiConfiguration.GENERATE_OUTPUT_FILE, value = "false"),
            @Property(key = WakamitiConfiguration.REPORT_GENERATION, value = "false")
    })
    @WakamitiPlan
    public static class SecondPlan {

    }

}
