/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.report.html.test;

import iti.kukumo.api.KukumoAPI;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.report.html.HtmlReportGenerator;
import iti.kukumo.report.html.HtmlReportGeneratorConfig;
import org.custommonkey.xmlunit.HTMLDocumentBuilder;
import org.custommonkey.xmlunit.TolerantSaxDocumentBuilder;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.xmlunit.assertj.XmlAssert.assertThat;

public class TestHtmlReportGenerator {

    private static Document xml;

    @BeforeClass
    public static void setup() throws IOException, ParserConfigurationException, SAXException {
        try (Reader reader = Files
                .newBufferedReader(Paths.get("src/test/resources/kukumo.json"), StandardCharsets.UTF_8)
        ) {
            PlanNodeSnapshot plan = KukumoAPI.instance().planSerializer().read(reader);
            HtmlReportGenerator generator = new HtmlReportGenerator();
            generator.setConfiguration(new HtmlReportGeneratorConfig()
                    .defaultConfiguration()
                    .appendProperty("htmlReport.output", "target/kukumo.html")
                    .appendProperty("htmlReport.title", "Test Report Title")
            );
            generator.report(plan);

            TolerantSaxDocumentBuilder tolerantSaxDocumentBuilder =
                    new TolerantSaxDocumentBuilder(XMLUnit.newTestParser());
            HTMLDocumentBuilder htmlDocumentBuilder = new HTMLDocumentBuilder(tolerantSaxDocumentBuilder);
            xml = htmlDocumentBuilder.parse(Files.readString(Path.of("target/kukumo.html")));
        }
    }

    @Test
    public void testReportTitle() {
        String elem = "//div[@id='report']/div[@class='navbar']/div[@class='report-info']/h1";

        assertThat(xml)
                .valueByXPath(elem + "/@title").isEqualTo("Test Report Title");
        assertThat(xml)
                .valueByXPath(elem + "/text()").isEqualTo("Test Report Title");
    }

    @Test
    public void testReportMetadata() {
        String elem = "//li[@class='details--item'][//*[@class='test--title']/text()='Run info']";

        assertThat(xml)
                .valueByXPath(elem + "//li[2]")
                .isEqualTo("Execution start Sep 27, 2019, 10:00:00 AM");
        assertThat(xml)
                .valueByXPath(elem + "//li[3]")
                .isEqualTo("Execution end Sep 27, 2019, 10:00:00 AM");
        assertThat(xml)
                .valueByXPath(elem + "//li[4]")
                .isEqualTo("Total duration 2h 24m 8s 410ms");
    }

    @Test
    public void testReportMenuToggles() {
        String elem = "//*[contains(@class,\"nav-menu--control\")]";

        assertThat(xml).valueByXPath("count(" + elem + ")").isEqualTo("6");

        boolean[] tests = new boolean[]{false, true, true, true, false, false};

        for (int i = 0; i < 6; i++) {
            assertThat(xml)
                    .valueByXPath("boolean(" + elem + "[" + (i + 1) + "][contains(@class,'toggle-switch--disabled')])")
                    .isEqualTo(tests[i]);
            assertThat(xml)
                    .valueByXPath("boolean(" + elem + "[" + (i + 1) + "]/input/@checked)")
                    .isEqualTo(!tests[i]);
            assertThat(xml)
                    .valueByXPath("boolean(" + elem + "[" + (i + 1) + "]/input/@disabled)")
                    .isEqualTo(tests[i]);
        }

    }

    @Test
    public void testReportMenuIndex() {
        String elem = "//*[@class='nav-menu--section']//a/@href";
        assertThat(xml).nodesByXPath(elem)
                .extractingText()
                .containsExactly("#a327ycn3219c", "#CP-A", "#CP-B",
                        "#jt9043uv30", "#CP-C", "#CP-D1", "#CP-D2");
    }

}