package iti.kukumo.plugins.cucumber.test;

import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.api.plan.PlanSerializer;
import iti.kukumo.plugins.cucumber.CucumberExporter;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestCucumberExport {

    @Test
    public void testCucumberExport() throws IOException {

        PlanSerializer serializer = new PlanSerializer();
        PlanNodeSnapshot planOk = serializer.read(Path.of("src/test/resources/kukumo-ok.json"));
        PlanNodeSnapshot planFailed = serializer.read(Path.of("src/test/resources/kukumo-failed.json"));

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


        assertTrue(fileEquals("target/cucumber-ok-inner.json","src/test/resources/cucumber-ok-inner.json"));
        assertTrue(fileEquals("target/cucumber-ok-outer.json","src/test/resources/cucumber-ok-outer.json"));
        assertTrue(fileEquals("target/cucumber-failed-inner.json","src/test/resources/cucumber-failed-inner.json"));
        assertTrue(fileEquals("target/cucumber-failed-outer.json","src/test/resources/cucumber-failed-outer.json"));

    }


    private boolean fileEquals(String file1, String file2) throws IOException {
        return FileUtils.contentEquals(new File(file1), new File(file2));
    }


}