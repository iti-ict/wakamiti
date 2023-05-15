/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.test.gherkin;


import imconfig.Configuration;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class TestFailureStep {

    @Test
    public void testInvalidStep() throws IOException {

        Map<String, String> properties = new HashMap<>();
        properties.put(WakamitiConfiguration.RESOURCE_TYPES, GherkinResourceType.NAME);
        properties.put(
            WakamitiConfiguration.RESOURCE_PATH,
            "src/test/resources/features/failure/failure.feature"
        );
        properties.put(
            WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS,
             "es.iti.wakamiti.test.gherkin.WakamitiSteps"
        );
        Configuration configuration = Wakamiti.defaultConfiguration()
            .appendFromMap(properties);
        PlanNode plan = Wakamiti.instance().createPlanFromConfiguration(configuration);
        PlanNode executed = Wakamiti.instance().executePlan(plan, configuration);

        PlanNodeSnapshot snapshot = new PlanNodeSnapshot(executed);

        assertThat(snapshot.getErrorClassifiers()).containsEntry("test",2L);
        PlanSerializer serializer = new JsonPlanSerializer();
        String serial = serializer.serialize(snapshot);
        System.out.println(serial);
        assertThat(serial).contains("\"errorClassifier\" : \"test\"");

    }

}