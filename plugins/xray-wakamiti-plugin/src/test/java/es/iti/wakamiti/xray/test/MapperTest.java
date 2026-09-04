/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.xray.XRaySynchronizer;
import es.iti.wakamiti.xray.internal.Mapper;
import es.iti.wakamiti.xray.model.TestCase;


public class MapperTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(MapperTest.class);

    private static PlanNodeSnapshot plan;
    private static PlanNodeSnapshot lifecyclePlan;

    @BeforeClass
    public static void setUp() throws IOException {
        plan = new JsonPlanSerializer().read(resource("wakamiti.json"));
        lifecyclePlan = new JsonPlanSerializer().read(resource("wakamiti_lifecycle.json"));
    }

    private static InputStream resource(
            String resource
    ) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

    @Test
    public void testMapTestsWhenFeatureWithSuccess() {
        List<TestCase> tests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_FEATURE).instance(null)
                .map(plan).toList();

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.getTestSetList().get(0).getJira().getSummary().equals("features/suite1"))
                .allMatch(tc -> tc.getJira().getSummary().equals("XRay integration feature"));
    }

    @Test
    public void testMapTestsWhenFeatureAndSourceBasedWithSuccess() {
        List<TestCase> tests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_FEATURE).instance("features")
                .map(plan).toList();

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.getTestSetList().get(0).getJira().getSummary().equals("suite1"))
                .allMatch(tc -> tc.getJira().getSummary().equals("XRay integration feature"));
    }

    @Test
    public void testMapTestsWhenScenario() {
        List<TestCase> tests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_SCENARIO).instance(null)
                .map(plan).toList();

        assertThat(tests.get(0)).hasFieldOrProperty("jira.summary");
        assertThat(tests.get(1)).hasFieldOrProperty("jira.summary");
        assertThat(tests.get(2)).hasFieldOrProperty("jira.summary");
    }

    @Test
    public void testMapTestsWhenScenarioAndSourceBased() {
        List<TestCase> tests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_SCENARIO).instance("features")
                .map(plan).toList();

        assertThat(tests.get(0)).hasFieldOrProperty("jira.summary");
        assertThat(tests.get(1)).hasFieldOrProperty("jira.summary");
        assertThat(tests.get(2)).hasFieldOrProperty("jira.summary");
    }

    @Test
    public void testLifecycleHooksAreNotMappedOrIncludedInFeatureDefinition() {
        List<TestCase> featureTests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_FEATURE).instance(null)
                .map(lifecyclePlan).toList();
        List<TestCase> scenarioTests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_SCENARIO).instance(null)
                .map(lifecyclePlan).toList();

        assertThat(featureTests).hasSize(1);
        assertThat(featureTests.get(0).getJira().getSummary()).isEqualTo("Lifecycle integration feature");
        assertThat(featureTests.get(0).getGherkin())
                .contains("Given functional group")
                .doesNotContain("fixture-only");
        assertThat(scenarioTests).hasSize(1);
        assertThat(scenarioTests.get(0).getJira().getSummary())
                .isEqualTo("functional scenario remains passed");
        assertThat(scenarioTests.get(0).getGherkin())
                .contains("Given functional-only step")
                .doesNotContain("fixture-only");
    }

}
