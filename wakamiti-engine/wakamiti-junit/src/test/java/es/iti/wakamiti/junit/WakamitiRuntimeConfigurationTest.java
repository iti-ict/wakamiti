/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;


public class WakamitiRuntimeConfigurationTest {

    @Test
    public void restoresWorkingDirAfterAnotherRunnerIsCreated() throws Exception {
        WakamitiJUnitRunner firstRunner = new WakamitiJUnitRunner(PropertyEvaluatorIT.class);
        new WakamitiJUnitRunner(SecondPlan.class);
        List<Failure> failures = new ArrayList<>();
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(new RunListener() {
            @Override
            public void testFailure(
                    Failure failure
            ) {
                failures.add(failure);
            }
        });

        firstRunner.run(notifier);

        assertThat(failures).isEmpty();
    }

    @AnnotatedConfiguration({
            @Property(key = WakamitiConfiguration.WORKING_DIR, value = "src/test/resources"),
            @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
            @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "features/redefining"),
            @Property(key = WakamitiConfiguration.GENERATE_OUTPUT_FILE, value = "false"),
            @Property(key = WakamitiConfiguration.REPORT_GENERATION, value = "false")
    })
    public static class SecondPlan {

    }

}
