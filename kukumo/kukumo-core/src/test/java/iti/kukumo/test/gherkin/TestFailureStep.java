/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.test.gherkin;


import imconfig.Configuration;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.api.plan.PlanSerializer;
import iti.kukumo.api.plan.Result;
import iti.kukumo.core.JsonPlanSerializer;
import iti.kukumo.core.Kukumo;
import iti.kukumo.core.gherkin.GherkinResourceType;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class TestFailureStep {

    @Test
    public void testInvalidStep() throws IOException {

        Map<String, String> properties = new HashMap<>();
        properties.put(KukumoConfiguration.RESOURCE_TYPES, GherkinResourceType.NAME);
        properties.put(
            KukumoConfiguration.RESOURCE_PATH,
            "src/test/resources/features/failure/failure.feature"
        );
        properties.put(
            KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS,
            "iti.kukumo.test.gherkin.KukumoSteps"
        );
        Configuration configuration = Kukumo.defaultConfiguration()
            .appendFromMap(properties);
        PlanNode plan = Kukumo.instance().createPlanFromConfiguration(configuration);
        PlanNode executed = Kukumo.instance().executePlan(plan, configuration);

        PlanNodeSnapshot snapshot = new PlanNodeSnapshot(executed);
        PlanSerializer serializer = new JsonPlanSerializer();
        String serial = serializer.serialize(snapshot);

    }

}