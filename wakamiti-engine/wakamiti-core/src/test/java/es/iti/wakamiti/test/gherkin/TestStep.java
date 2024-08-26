/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.gherkin;


import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static org.assertj.core.api.Assertions.assertThat;


public class TestStep {

    private static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.test");

    @Test
    public void testPropertyStep() throws IOException {
        Map<String, String> properties = new HashMap<>();
        properties.put(RESOURCE_TYPES, GherkinResourceType.NAME);
        properties.put(
                RESOURCE_PATH,
                "src/test/resources/features/properties/test_globalProperties.feature"
        );
        properties.put(
                NON_REGISTERED_STEP_PROVIDERS,
                "es.iti.wakamiti.test.gherkin.WakamitiSteps"
        );
        properties.put(OUTPUT_FILE_PATH, "target/wakamiti.json");

        properties.put("number.integer", "6");
        properties.put("number.decimal", "3,2");
        properties.put("datetime.today", "2023-01-10");
        properties.put("datetime.now", "10:05:03.123000000");
        properties.put("text", "ABC");
        properties.put("text2", "s4_$= A");
        properties.put("url", "https://test.es/ABC");
        properties.put("host", "test.es");

        Configuration configuration = Wakamiti.defaultConfiguration()
                .appendFromMap(properties);
        PlanNode plan = Wakamiti.instance().createPlanFromConfiguration(configuration);
        PlanNode executed = Wakamiti.instance().executePlan(plan, configuration);

        PlanNodeSnapshot snapshot = new PlanNodeSnapshot(executed);

        assertThat(snapshot.getErrorClassifiers()).isEmpty();
        PlanSerializer serializer = new JsonPlanSerializer();
        String serial = serializer.serialize(snapshot);
        if (LOGGER.isDebugEnabled()) {
            System.out.println(serial);
        }
        assertThat(serial).doesNotContain("\"errorClassifier\" : \"test\"");

        List<PlanNode> testCases = plan
                .descendants()
                .filter(node -> node.nodeType() == NodeType.TEST_CASE)
                .filter(node -> node.result().filter(it -> it == Result.FAILED).isEmpty())
                .collect(Collectors.toList());

        for (PlanNode testCase : testCases) {
            String testCaseSerial = serializer.serialize(new PlanNodeSnapshot(testCase).withoutChildren());
            if (LOGGER.isDebugEnabled()) {
                System.out.println("-----------------------------------------\n\n\n");
                System.out.println(testCaseSerial);
            }
            assertThat(testCaseSerial).doesNotContain("\"errorClassifier\" : \"test\"");
        }
    }


}
