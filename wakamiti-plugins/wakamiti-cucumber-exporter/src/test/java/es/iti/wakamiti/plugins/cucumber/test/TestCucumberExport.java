package es.iti.wakamiti.plugins.cucumber.test;

import es.iti.wakamiti.api.WakamitiRunContext;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.plugins.cucumber.CucumberExporter;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


public class TestCucumberExport {

    @Before
    public void prepare() {
        WakamitiRunContext.set(new WakamitiRunContext(Wakamiti.defaultConfiguration()));
    }

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
            new Configuration(new File("target"),"test")
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

}