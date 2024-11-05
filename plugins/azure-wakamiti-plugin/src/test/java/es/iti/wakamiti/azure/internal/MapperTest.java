/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.AzureSynchronizer;
import es.iti.wakamiti.azure.api.model.TestCase;
import es.iti.wakamiti.core.JsonPlanSerializer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class MapperTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(MapperTest.class);

    private static PlanNodeSnapshot plan;
    private static PlanNodeSnapshot planSuite;

    @BeforeClass
    public static void setUp() throws IOException {
        plan = new JsonPlanSerializer().read(resource("wakamiti.json"));
        planSuite = new JsonPlanSerializer().read(resource("wakamiti_suite.json"));
    }

    private static InputStream resource(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

    @Test
    public void testMapWhenFeatureWithSuccess() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_FEATURE).instance(null)
                .map(plan).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("features/suite")))
                .allMatch(tc -> tc.name().equals("Azure integration feature"));
    }

    @Test
    public void testMapWhenFeatureAndSourceBasedWithSuccess() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_FEATURE).instance("features")
                .map(plan).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("suite")))
                .allMatch(tc -> tc.name().equals("Azure integration feature"));
    }

    @Test(expected = WakamitiException.class)
    public void testMapWhenFeatureAndInvalidSourceBaseWithError() {
        try {
            Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_FEATURE).instance("feature").map(plan);
        } catch (WakamitiException e) {
            assertThat(e).hasMessageContaining("Invalid suiteBase: feature");
            throw e;
        }
    }

    @Test
    public void testMapWhenFeatureAndSourceBasedAndAzureSuite() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_FEATURE).instance("features")
                .map(planSuite).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("api/suite/azure")))
                .allMatch(tc -> tc.name().equals("Azure integration feature"));
    }

    @Test
    public void testMapWhenScenario() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_SCENARIO).instance(null)
                .map(plan).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("features/suite/Azure integration feature")));
        assertThat(tests.get(0)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario B");
        assertThat(tests.get(0)).hasFieldOrPropertyWithValue("order", 0);
        assertThat(tests.get(1)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario A");
        assertThat(tests.get(1)).hasFieldOrPropertyWithValue("order", 1);
        assertThat(tests.get(2)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario C");
        assertThat(tests.get(2)).hasFieldOrPropertyWithValue("order", 2);
    }

    @Test
    public void testMapWhenScenarioAndSourceBased() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_SCENARIO).instance("features")
                .map(plan).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("suite/Azure integration feature")));
        assertThat(tests.get(0)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario B");
        assertThat(tests.get(0)).hasFieldOrPropertyWithValue("order", 0);
        assertThat(tests.get(1)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario A");
        assertThat(tests.get(1)).hasFieldOrPropertyWithValue("order", 1);
        assertThat(tests.get(2)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario C");
        assertThat(tests.get(2)).hasFieldOrPropertyWithValue("order", 2);
    }

    @Test(expected = WakamitiException.class)
    public void testMapWhenScenarioAndInvalidSourceBaseWithError() {
        try {
            Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_SCENARIO).instance("feature").map(plan);
        } catch (WakamitiException e) {
            assertThat(e).hasMessageContaining("Invalid suiteBase: feature");
            throw e;
        }
    }

    @Test
    public void testMapWhenScenarioAndSourceBasedAndAzureSuite() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_SCENARIO).instance("features")
                .map(planSuite).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("api/suite/azure")));
        assertThat(tests.get(0)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario B");
        assertThat(tests.get(0)).hasFieldOrPropertyWithValue("order", 0);
        assertThat(tests.get(1)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario A");
        assertThat(tests.get(1)).hasFieldOrPropertyWithValue("order", 1);
        assertThat(tests.get(2)).hasFieldOrPropertyWithValue("name", "Wakamiti Scenario C");
        assertThat(tests.get(2)).hasFieldOrPropertyWithValue("order", 2);
    }

    private void logResult(Object o) {
        LOGGER.debug("Result: {}", o);
    }
}
