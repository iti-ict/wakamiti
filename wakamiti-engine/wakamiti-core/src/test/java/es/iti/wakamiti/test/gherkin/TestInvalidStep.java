/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.gherkin;


import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import imconfig.Configuration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static org.assertj.core.api.Assertions.assertThat;


public class TestInvalidStep {

    @Test
    public void testInvalidStep() {

        Map<String, String> properties = new HashMap<>();
        properties.put(RESOURCE_TYPES, GherkinResourceType.NAME);
        properties.put(
                RESOURCE_PATH,
                "src/test/resources/features/test5_invalidStep.feature"
        );
        properties.put(
                NON_REGISTERED_STEP_PROVIDERS,
                "es.iti.wakamiti.test.gherkin.WakamitiSteps"
        );
        properties.put(OUTPUT_FILE_PATH, "target/wakamiti.json");
        Configuration configuration = Wakamiti.defaultConfiguration()
                .appendFromMap(properties);
        PlanNode plan = Wakamiti.instance().createPlanFromConfiguration(configuration);
        Wakamiti.instance().executePlan(plan, configuration);

        PlanNode feature = plan.children().findFirst().orElse(null);
        PlanNode scenarioOutline = feature.children().findFirst().orElse(null);
        PlanNode[] scenarios = scenarioOutline.children().toArray(PlanNode[]::new);
        for (PlanNode scenario : scenarios) {
            PlanNode[] steps = scenario.children().toArray(PlanNode[]::new);
            assertThat(feature.result()).contains(Result.UNDEFINED);
            assertThat(scenario.result()).contains(Result.UNDEFINED);
            assertThat(steps[0].result()).contains(Result.SKIPPED);
            assertThat(steps[1].result()).contains(Result.SKIPPED);
            assertThat(steps[2].result()).contains(Result.UNDEFINED);
            assertThat(steps[3].result()).contains(Result.SKIPPED);
        }

    }

}