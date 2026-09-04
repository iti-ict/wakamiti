/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.plugins.cucumber.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.plugins.cucumber.CucumberExporter;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;


public class TestCucumberExport {

    @Test
    public void testCucumberExport() throws IOException {
        JsonPlanSerializer serializer = new JsonPlanSerializer();
        PlanNodeSnapshot planOk = serializer.read(Path.of("src/test/resources/wakamiti-ok.json"));
        PlanNodeSnapshot planFailed = serializer.read(Path.of("src/test/resources/wakamiti-failed.json"));

        CucumberExporter exporter = new CucumberExporter();

        exporter.setStrategy(CucumberExporter.Strategy.INNERSTEPS);
        exporter.setOutputFile("target/cucumber-ok-inner.json");
        exporter.report(planOk);

        exporter.setStrategy(CucumberExporter.Strategy.OUTERSTEPS);
        exporter.setOutputFile("target/cucumber-ok-outer.json");
        exporter.report(planOk);

        exporter.setStrategy(CucumberExporter.Strategy.INNERSTEPS);
        exporter.setOutputFile("target/cucumber-failed-inner.json");
        exporter.report(planFailed);

        exporter.setStrategy(CucumberExporter.Strategy.OUTERSTEPS);
        exporter.setOutputFile("target/cucumber-failed-outer.json");
        exporter.report(planFailed);

        ReportBuilder cucumberReportBuilder = new ReportBuilder(
                List.of(
                        "target/cucumber-ok-inner.json",
                        "target/cucumber-ok-outer.json",
                        "target/cucumber-failed-inner.json",
                        "target/cucumber-failed-outer.json"
                ),
                new Configuration(new File("target"), "test")
        );
        cucumberReportBuilder.generateReports();

        Stream.of("cucumber-ok-inner.json",
                "cucumber-ok-outer.json",
                "cucumber-failed-inner.json",
                "cucumber-failed-outer.json"
        ).forEach(file -> {
            assertThat(new File(String.format("target/%s", file)))
                    .hasSameTextualContentAs(new File(String.format("src/test/resources/%s", file)));
        });
    }

    @Test
    public void testCucumberExportWithLifecycleHooks() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonPlanSerializer serializer = new JsonPlanSerializer();
        PlanNodeSnapshot planLifecycle = serializer.read(Path.of("src/test/resources/wakamiti-lifecycle.json"));

        CucumberExporter exporter = new CucumberExporter();

        exporter.setStrategy(CucumberExporter.Strategy.INNERSTEPS);
        exporter.setOutputFile("target/cucumber-lifecycle-inner.json");
        exporter.report(planLifecycle);

        exporter.setStrategy(CucumberExporter.Strategy.OUTERSTEPS);
        exporter.setOutputFile("target/cucumber-lifecycle-outer.json");
        exporter.report(planLifecycle);

        JsonNode innerExpected = mapper.readTree(new File("src/test/resources/cucumber-lifecycle-inner.json"));
        JsonNode innerActual = mapper.readTree(new File("target/cucumber-lifecycle-inner.json"));
        assertThat(innerActual).isEqualTo(innerExpected);

        JsonNode outerExpected = mapper.readTree(new File("src/test/resources/cucumber-lifecycle-outer.json"));
        JsonNode outerActual = mapper.readTree(new File("target/cucumber-lifecycle-outer.json"));
        assertThat(outerActual).isEqualTo(outerExpected);

        // Feature hooks have no faithful representation in Cucumber legacy JSON.
        JsonNode elements = innerActual.get(0).path("elements");
        assertThat(elements).hasSize(1);
        assertThat(elements.get(0).path("id").asText()).isEqualTo("LC-1");
        assertThat(elements.get(0).path("name").asText()).isEqualTo("a functional scenario");
        assertThat(elements.get(0).has("before")).isFalse();
        assertThat(elements.get(0).has("after")).isFalse();
        assertThat(innerActual.toString())
                .doesNotContain("before feature hook")
                .doesNotContain("after feature hook")
                .doesNotContain("unexecuted after hook");
    }

}
