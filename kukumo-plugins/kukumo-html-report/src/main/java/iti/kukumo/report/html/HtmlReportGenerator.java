/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.report.html;

import freemarker.template.*;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.Reporter;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.api.util.KukumoLogger;
import org.slf4j.Logger;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static iti.kukumo.report.html.HtmlReportGeneratorConfig.*;


@Extension(provider = "iti.kukumo", name = "html-report", version = "1.2")
public class HtmlReportGenerator implements Reporter {

    private static final Logger LOGGER = KukumoLogger.forClass(HtmlReportGenerator.class);

    private final Configuration templateConfiguration;
    private String cssFile;
    private String outputFile;
    private String title;
    private Map<String,Object> parameters;

    public HtmlReportGenerator() {
        templateConfiguration = new Configuration(Configuration.VERSION_2_3_29);
        templateConfiguration.setDefaultEncoding("UTF-8");
        templateConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        templateConfiguration.setLogTemplateExceptions(true);
        templateConfiguration.setWrapUncheckedExceptions(true);
        templateConfiguration.setFallbackOnNullLoopVariable(false);
        templateConfiguration.setClassLoaderForTemplateLoading(classLoader(), "/");
    }

    void setCssFile(String cssFile) {
        this.cssFile = cssFile;
    }

    void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public void setConfiguration(imconfig.Configuration configuration) {
        configuration.get(CSS_FILE,String.class).ifPresent(this::setCssFile);
        configuration.get(OUTPUT_FILE,String.class).ifPresent(this::setOutputFile);
        configuration.get(TITLE, String.class).ifPresent(this::setTitle);
        var reportConfiguration = configuration.inner(PREFIX);
        this.parameters = new HashMap<>(reportConfiguration.asMap());
    }


    @Override
    public void report(PlanNodeSnapshot rootNode) {
        try {
            File output = new File(Objects.requireNonNull(
                this.outputFile,
                "Output file not configured"
            ));
            parameters.put("localStyles", readStyles());
            parameters.put("plan", rootNode);
            parameters.put("title",title);
            try (var writer = new FileWriter(output)) {
                template("report.html.ftl").process(parameters, writer);
            }
        } catch (IOException | TemplateException e) {
            LOGGER.error("Error generating HTML report: {}", e.getMessage(), e);
            e.printStackTrace();
        }

    }


    private String readStyles() throws IOException {
        var localStyles = readResource("report-style.css");
        if (this.cssFile == null) {
            return localStyles;
        } else {
            var extraStyles = Files.readString(Paths.get(this.cssFile), StandardCharsets.UTF_8);
            return localStyles + "\n" + extraStyles;
        }
    }




    private Template template(String resource) throws IOException {
        return templateConfiguration.getTemplate(resource);
    }


    private static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }



    private String readResource(String resource) throws IOException {
        try (var reader = new InputStreamReader(
            Objects.requireNonNull(classLoader().getResourceAsStream(resource)),
            StandardCharsets.UTF_8
        )) {
            StringWriter writer = new StringWriter();
            reader.transferTo(writer);
            return writer.toString();
        }
    }

}