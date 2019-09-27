package iti.kukumo.report.html.test;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.PlanNodeDescriptor;
import iti.kukumo.report.html.HtmlReportGenerator;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * @author ITI
 *         Created by ITI on 14/03/19
 */
public class TestHtmlReportGenerator {

    @Test
    public void testHtmlReport() throws IOException {

        try (Reader reader = Files.newBufferedReader(Paths.get("src/test/resources/kukumo.json"), StandardCharsets.UTF_8);
             Writer writer = Files.newBufferedWriter(Paths.get("target/kukumo.html"), StandardCharsets.UTF_8);
        ) {
            PlanNodeDescriptor plan = Kukumo.instance().getPlanSerializer().read(reader);
            HtmlReportGenerator generator = new HtmlReportGenerator();
            generator.setReportLocale(Locale.ENGLISH);
            generator.generate(plan,writer);
        }
    }
}
