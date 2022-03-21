/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.test.gherkin;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.Result;
import iti.kukumo.gherkin.GherkinResourceType;


public class TestInvalidStep {

    @Test
    public void testInvalidStep() {

        Map<String, String> properties = new HashMap<>();
        properties.put(KukumoConfiguration.RESOURCE_TYPES, GherkinResourceType.NAME);
        properties.put(
            KukumoConfiguration.RESOURCE_PATH,
            "src/test/resources/features/test5_invalidStep.feature"
        );
        properties.put(
            KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS,
            "iti.kukumo.test.gherkin.KukumoSteps"
        );
        Configuration configuration = Kukumo.defaultConfiguration()
            .appendFromMap(properties);
        PlanNode plan = Kukumo.instance().createPlanFromConfiguration(configuration);
        Kukumo.instance().executePlan(plan, configuration);

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