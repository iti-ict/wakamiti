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

import static es.iti.wakamiti.azure.api.model.TestSuite.SLASH_CODE;
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
    public void testMapTestsWhenFeatureWithSuccess() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_FEATURE).instance(null)
                .mapTests(plan).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("features/suite")))
                .allMatch(tc -> tc.name().equals("Azure integration feature"));
    }

    @Test
    public void testMapTestsWhenFeatureAndSourceBasedWithSuccess() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_FEATURE).instance("features")
                .mapTests(plan).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("suite")))
                .allMatch(tc -> tc.name().equals("Azure integration feature"));
    }

    @Test(expected = WakamitiException.class)
    public void testMapTestsWhenFeatureAndInvalidSourceBaseWithError() {
        try {
            Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_FEATURE).instance("feature").mapTests(plan);
        } catch (WakamitiException e) {
            assertThat(e).hasMessageContaining("Invalid suiteBase: feature");
            throw e;
        }
    }

    @Test
    public void testMapTestsWhenFeatureAndSourceBasedAndAzureSuite() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_FEATURE).instance("features")
                .mapTests(planSuite).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("api"+SLASH_CODE+"suite/azure"))
                        && tc.suite().name().equals("azure")
                        && tc.suite().parent().name().equals("api/suite")
                )
                .allMatch(tc -> tc.name().equals("Azure integration feature"));
    }

    @Test
    public void testMapTestsWhenScenario() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_SCENARIO).instance(null)
                .mapTests(plan).collect(Collectors.toList());
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
    public void testMapTestsWhenScenarioAndSourceBased() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_SCENARIO).instance("features")
                .mapTests(plan).collect(Collectors.toList());
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
    public void testMapTestsWhenScenarioAndInvalidSourceBaseWithError() {
        try {
            Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_SCENARIO).instance("feature").mapTests(plan);
        } catch (WakamitiException e) {
            assertThat(e).hasMessageContaining("Invalid suiteBase: feature");
            throw e;
        }
    }

    @Test
    public void testMapTestsWhenScenarioAndSourceBasedAndAzureSuite() {
        List<TestCase> tests = Mapper.ofType(AzureSynchronizer.GHERKIN_TYPE_SCENARIO).instance("features")
                .mapTests(planSuite).collect(Collectors.toList());
        logResult(tests);

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .allMatch(tc -> tc.suite().asPath().equals(Path.of("api"+SLASH_CODE+"suite/azure"))
                        && tc.suite().name().equals("azure")
                        && tc.suite().parent().name().equals("api/suite")
                );
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
