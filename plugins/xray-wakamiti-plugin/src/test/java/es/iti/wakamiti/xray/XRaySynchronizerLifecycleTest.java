/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.JsonPlanSerializer;


public class XRaySynchronizerLifecycleTest {

    @Test
    public void resultNodesUseConfiguredLevelWithoutPropagatingFixtureFailure() throws IOException {
        try (InputStream resource = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("wakamiti_lifecycle.json")) {
            PlanNodeSnapshot plan = new JsonPlanSerializer().read(resource);

            List<PlanNodeSnapshot> featureResults = XRaySynchronizer.resultNodes(
                    plan, XRaySynchronizer.GHERKIN_TYPE_FEATURE).toList();
            List<PlanNodeSnapshot> scenarioResults = XRaySynchronizer.resultNodes(
                    plan, XRaySynchronizer.GHERKIN_TYPE_SCENARIO).toList();

            assertThat(featureResults).extracting(PlanNodeSnapshot::getResult)
                    .containsExactly(Result.ERROR);
            assertThat(scenarioResults).extracting(PlanNodeSnapshot::getResult)
                    .containsExactly(Result.PASSED);
            assertThat(scenarioResults).extracting(PlanNodeSnapshot::getName)
                    .containsExactly("functional scenario remains passed");
        }
    }

}
