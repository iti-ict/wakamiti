/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html.test;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.report.html.FilteredSnapshot;
import es.iti.wakamiti.report.html.HtmlReportGenerator;
import es.iti.wakamiti.report.html.HtmlReportGeneratorConfig;
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
    private static Document xml_2;
    private static Document xml_3;
    private static Document xml_noExecution;
    private static String html_2;
    private static String templateSource;
    private static String scriptSource;

    @BeforeClass
    public static void setup() throws IOException, ParserConfigurationException, SAXException {
        xml = load("wakamiti",
                "htmlReport.output", "target/wakamiti.html",
                "htmlReport.title", "Test Report Title",
                "htmlReport.extra_info.value1", "Extra info 1",
                "htmlReport.extra_info.value2", "Extra info 2");
        xml_2 = load("wakamiti_2", "htmlReport.output", "target/wakamiti_2.html");
        xml_3 = load("wakamiti_huge", "htmlReport.output", "target/wakamiti_huge.html");
        xml_noExecution = load("wakamiti_noExecution", "htmlReport.output", "target/wakamiti_noExecution.html");
        html_2 = Files.readString(Path.of("target/wakamiti_2.html"));
        templateSource = Files.readString(Path.of("src/main/resources/report.ftl"));
        scriptSource = Files.readString(Path.of("src/main/resources/lib/global.js"));
    }

    private static Document load(String name, String... properties) throws IOException, ParserConfigurationException, SAXException {
        try (Reader reader = Files
                .newBufferedReader(Paths.get("src/test/resources/" + name + ".json"), StandardCharsets.UTF_8)
        ) {
            PlanNodeSnapshot plan = WakamitiAPI.instance().planSerializer().read(reader);
            HtmlReportGenerator generator = new HtmlReportGenerator();
            generator.setConfiguration(new HtmlReportGeneratorConfig()
                    .defaultConfiguration()
                    .appendFromPairs(properties)
            );
            generator.report(plan);

            TolerantSaxDocumentBuilder tolerantSaxDocumentBuilder =
                    new TolerantSaxDocumentBuilder(XMLUnit.newTestParser());
            HTMLDocumentBuilder htmlDocumentBuilder = new HTMLDocumentBuilder(tolerantSaxDocumentBuilder);
            return htmlDocumentBuilder.parse(Files.readString(Path.of("target/" + name + ".html")));
        }
    }

    @Test
    public void testReportTitle() {
        String elem = "//div[@id='report']/div[@class='navbar']/div[@class='report-info']/h1";

        assertThat(xml)
                .valueByXPath(elem + "/@title").isEqualTo("Test Report Title");
        assertThat(xml)
                .valueByXPath(elem + "/text()").isEqualTo("Test Report Title");

        assertThat(xml_2)
                .valueByXPath(elem + "/@title").isEqualTo("Test Plan B");
        assertThat(xml_2)
                .valueByXPath(elem + "/text()").isEqualTo("Test Plan B");
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

//    @Test
//    public void testReportMenuToggles() {
//        String elem = "//*[contains(@class,\"nav-menu--control\")]";
//
//        assertThat(xml).valueByXPath("count(" + elem + ")").isEqualTo("6");
//
//        boolean[] tests = new boolean[]{false, true, true, true, false, false};
//
//        for (int i = 0; i < 6; i++) {
//            assertThat(xml)
//                    .valueByXPath("boolean(" + elem + "[" + (i + 1) + "][contains(@class,'toggle-switch--disabled')])")
//                    .isEqualTo(tests[i]);
//            assertThat(xml)
//                    .valueByXPath("boolean(" + elem + "[" + (i + 1) + "]/input/@checked)")
//                    .isEqualTo(!tests[i]);
//            assertThat(xml)
//                    .valueByXPath("boolean(" + elem + "[" + (i + 1) + "]/input/@disabled)")
//                    .isEqualTo(tests[i]);
//        }
//
//    }
//
//    @Test
//    public void testReportMenuIndex() {
//        String elem = "//*[@class='nav-menu--section']//a/@href";
//        assertThat(xml).nodesByXPath(elem)
//                .extractingText()
//                .containsExactly("#a327ycn3219c", "#CP-A", "#CP-B",
//                        "#jt9043uv30", "#CP-C", "#CP-D1", "#CP-D2");
//    }

    @Test
    public void testReportExtraInfo() {
        assertThat(xml)
                .nodesByXPath("//*[text()='Extra info 1']")
                .exist();
        assertThat(xml)
                .nodesByXPath("//*[text()='Extra info 2']")
                .exist();

        assertThat(xml_2)
                .nodesByXPath("//*[text()='Extra info 1']")
                .doNotExist();
        assertThat(xml_2)
                .nodesByXPath("//*[text()='Extra info 2']")
                .doNotExist();
    }

    @Test
    public void testFilteredSnapshotIncludesReturnedValue() throws IOException {
        try (Reader reader = Files
                .newBufferedReader(Paths.get("src/test/resources/wakamiti_2.json"), StandardCharsets.UTF_8)
        ) {
            PlanNodeSnapshot plan = WakamitiAPI.instance().planSerializer().read(reader);
            FilteredSnapshot snapshot = FilteredSnapshot.of(plan.getChildren().get(0).getChildren().get(0)
                    .getChildren().get(2).getChildren().get(0));

            assertThat(snapshot.getResponse()).isEqualTo("{\"alpha\":1,\"nested\":{\"beta\":2}}");
        }
    }

    @Test
    public void testReportTemplateIncludesPrettyToggleForStructuredResponses() {
        org.assertj.core.api.Assertions.assertThat(templateSource)
                .contains("<%#response%>")
                .contains("<%#prettyResponse%>")
                .contains("step--body-heading\">Response</div>")
                .contains("class=\"step--format-button\"")
                .contains("data-action=\"toggle-response-format\"")
                .contains(">{}</button>")
                .contains("class=\"step--response\"");
        org.assertj.core.api.Assertions.assertThat(html_2)
                .contains("class=\"step--response\"");
    }

    @Test
    public void testPrettyResponseIsOnlyEnabledForStructuredResponses() {
        org.assertj.core.api.Assertions.assertThat(scriptSource)
                .contains("function canPrettifyResponse(text)")
                .contains("function decorateNode(node)")
                .contains("node.prettyResponse = canPrettifyResponse(node.response);")
                .contains("aux?.forEach(decorateNode);")
                .contains("$(document).on('click', '.step--format-button[data-action=\"toggle-response-format\"]'")
                .contains("$(document).on('change', '.nav-menu--control input[type=\"checkbox\"]'");
    }

    private String uri(String resource) {
        return Path.of("target/" + resource + ".html").toUri().toString();
    }

}
