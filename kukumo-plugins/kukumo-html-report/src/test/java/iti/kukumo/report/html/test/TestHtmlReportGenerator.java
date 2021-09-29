/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.report.html.test;


import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import iti.commons.configurer.Configuration;
import iti.kukumo.report.html.HtmlReportGeneratorConfig;
import org.junit.Test;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.report.html.HtmlReportGenerator;



public class TestHtmlReportGenerator {

    @Test
    public void testHtmlReport() throws IOException {

        try (Reader reader = Files
            .newBufferedReader(Paths.get("src/test/resources/kukumo.json"), StandardCharsets.UTF_8);
        ) {
            Kukumo.instance();
            PlanNodeSnapshot plan = Kukumo.planSerializer().read(reader);
            HtmlReportGenerator generator = new HtmlReportGenerator();
            generator.setConfiguration(new HtmlReportGeneratorConfig()
                .defaultConfiguration()
                .appendProperty("htmlReport.output","target/kukumo.html")
            );
            generator.report(plan);
        }
    }
}