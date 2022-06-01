/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.report.html.test;


import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
;
import iti.kukumo.report.html.HtmlReportGeneratorConfig;
import org.junit.Test;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.report.html.HtmlReportGenerator;

import static org.junit.Assert.assertEquals;


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
                .appendProperty("htmlReport.title", "Test Report Title")
            );
            generator.report(plan);

            assertEquals(
                Files.readString(Path.of("src/test/resources/kukumo.html")),
                Files.readString(Path.of("target/kukumo.html"))
            );
        }
    }
}