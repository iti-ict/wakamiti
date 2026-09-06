/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core;


import static es.iti.wakamiti.api.WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS;
import static es.iti.wakamiti.api.WakamitiConfiguration.REPORT_GENERATION;
import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_TYPES;
import static es.iti.wakamiti.api.WakamitiConfiguration.WORKING_DIR;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Test;

import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import es.iti.wakamiti.core.runner.LifecycleProbeSteps;


public class WakamitiRuntimeConfigurationTest {

    private static final String FEATURE = """
            # language: en
            Feature: Runtime configuration

              Scenario: Execute a plan
                Given an executable lifecycle probe
            """;

    @After
    public void resetProbe() {
        LifecycleProbeSteps.reset();
    }

    @Test
    public void executePlanRestoresItsRuntimeConfiguration() {
        Path firstWorkingDir = Path.of("target", "runtime-configuration", "first");
        Path secondWorkingDir = Path.of("target", "runtime-configuration", "second");
        Configuration firstConfiguration = configuration(firstWorkingDir);
        Configuration secondConfiguration = configuration(secondWorkingDir);
        Wakamiti wakamiti = Wakamiti.instance();
        PlanNode firstPlan = wakamiti.createPlanFromContent(
                firstConfiguration,
                new ByteArrayInputStream(FEATURE.getBytes(StandardCharsets.UTF_8))
        );

        wakamiti.configureRuntime(secondConfiguration);
        wakamiti.executePlan(firstPlan, firstConfiguration);

        assertThat(Wakamiti.resourceLoader().absolutePath(new File("script.sql")).toPath())
                .isEqualTo(firstWorkingDir.toAbsolutePath().resolve("script.sql"));
    }

    private Configuration configuration(
            Path workingDir
    ) {
        return Wakamiti.defaultConfiguration().appendFromPairs(
                WORKING_DIR, workingDir.toString(),
                RESOURCE_TYPES, GherkinResourceType.NAME,
                NON_REGISTERED_STEP_PROVIDERS, LifecycleProbeSteps.class.getName(),
                REPORT_GENERATION, "false"
        );
    }

}
