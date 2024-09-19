/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html.test;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.report.html.HtmlReportGenerator;
import es.iti.wakamiti.report.html.HtmlReportGeneratorConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.assertj.core.api.Assertions;
import org.custommonkey.xmlunit.HTMLDocumentBuilder;
import org.custommonkey.xmlunit.TolerantSaxDocumentBuilder;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.xmlunit.assertj.XmlAssert.assertThat;

public class TestHtmlReportGenerator {

    private static WebDriver driver;
//    private static WebDriverWait wait;

    private static Document xml;
    private static Document xml_2;
    private static Document xml_3;

    @BeforeClass
    public static void setup() throws IOException, ParserConfigurationException, SAXException {
        xml = load("wakamiti",
                "htmlReport.output", "target/wakamiti.html",
                "htmlReport.title", "Test Report Title",
                "htmlReport.extra_info.value1", "Extra info 1",
                "htmlReport.extra_info.value2", "Extra info 2");
        xml_2 = load("wakamiti_2", "htmlReport.output", "target/wakamiti_2.html");
        xml_3 = load("wakamiti_huge", "htmlReport.output", "target/wakamiti_huge.html");

        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless", "--disable-gpu", "--test-type");
        driver = new FirefoxDriver(options);
    }

    @AfterClass
    public static void closeBrowser() {
        driver.quit();
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
    public void test() {
        driver.get(uri("wakamiti_huge"));

        driver.findElement(By.cssSelector("button[title='Menu']")).click();
        driverWait(driver).until(presenceOfElementLocated(By.id("search-input"))).sendKeys("gfewohge");

        List<WebElement> li = driver.findElements(By.className("empty"));

        Assertions.assertThat(li)
                .allMatch(WebElement::isDisplayed)
                .size().isEqualTo(2);
    }

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

    private String uri(String resource) {
        return Path.of("target/" + resource + ".html").toUri().toString();
    }

    private WebDriverWait driverWait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.of(10, ChronoUnit.SECONDS));
    }
}