/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html;


import ch.simschla.minify.css.CssMin;
import ch.simschla.minify.js.JsMin;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.PathUtil;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.report.html.factory.CountStepsMethod;
import es.iti.wakamiti.report.html.factory.DurationTemplateNumberFormatFactory;
import es.iti.wakamiti.report.html.factory.SumAllMethod;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.UnaryOperator;

import static es.iti.wakamiti.report.html.HtmlReportGeneratorConfig.*;


/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
@Extension(provider = "es.iti.wakamiti", name = "html-report", version = "2.6")
public class HtmlReportGenerator implements Reporter {

    private static final Logger LOGGER = WakamitiLogger.forClass(HtmlReportGenerator.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    private final freemarker.template.Configuration templateConfiguration;
    private String cssFile;
    private String outputFile;
    private String title;
    private Map<String, Object> parameters;

    public HtmlReportGenerator() {
        templateConfiguration = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_29);
        templateConfiguration.setDefaultEncoding("UTF-8");
        templateConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        templateConfiguration.setLogTemplateExceptions(true);
        templateConfiguration.setWrapUncheckedExceptions(true);
        templateConfiguration.setFallbackOnNullLoopVariable(false);
        templateConfiguration.setClassLoaderForTemplateLoading(classLoader(), "/");
        templateConfiguration.setCustomNumberFormats(
                Collections.singletonMap("duration", DurationTemplateNumberFormatFactory.INSTANCE));
    }

    private static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
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

    public void setConfiguration(Configuration configuration) {
        configuration.get(CSS_FILE, String.class).ifPresent(this::setCssFile);
        configuration.get(OUTPUT_FILE, String.class).ifPresent(this::setOutputFile);
        configuration.get(TITLE, String.class).ifPresent(this::setTitle);
        var reportConfiguration = configuration.inner(PREFIX);
        this.parameters = new HashMap<>(reportConfiguration.asMap());
        var reportExtraInfoConfiguration = configuration.inner(EXTRA_INFO);
        this.parameters.put("extra_info", reportExtraInfoConfiguration.asMap());
    }

    @Override
    public void report(PlanNodeSnapshot rootNode) {
        try {
            var resourceLoader = WakamitiAPI.instance().resourceLoader();
            Path output = resourceLoader.absolutePath(PathUtil.replaceTemporalPlaceholders(Path.of(Objects.requireNonNull(
                    this.outputFile,
                    "Output file not configured"
            ))));
            parameters.put("globalStyle", readStyles());
            parameters.put("globalScript", readJavascript("lib/global.js"));
            parameters.put("chartsScript", readJavascript("lib/charts.js"));
            parameters.put("plan", rootNode.withoutChildren());
            parameters.put("title", title);
            parameters.put("version", WakamitiAPI.instance().version());
            parameters.put("plugin_version", version());
            parameters.put("sum", new SumAllMethod());
            parameters.put("countSteps", new CountStepsMethod());
            parameters.put("data", OBJECT_MAPPER.writeValueAsString(FilteredSnapshot.of(rootNode.getChildren()))
                    .replace("\\", "\\\\").replace("'", "\\'"));

            File parent = output.toFile().getCanonicalFile().getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            try (var writer = new FileWriter(output.toFile(), StandardCharsets.UTF_8)) {
                template("report.ftl").process(parameters, writer);
                WakamitiAPI.instance().publishEvent(Event.REPORT_OUTPUT_FILE_WRITTEN, output);

            }
        } catch (IOException | TemplateException e) {
            LOGGER.error("Error generating HTML report: {}", e.getMessage(), e);
        }

    }

    private String readStyles() {
        UnaryOperator<String> readStyle = (resource) -> {
            try (InputStream is = resource(resource)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                CssMin.builder()
                        .inputStream(is)
                        .outputStream(baos)
                        .build()
                        .minify();

                return baos.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        String localCss = readStyle.apply("lib/normalize.css") + readStyle.apply("lib/global.css");
        if (this.cssFile != null) {
            localCss += readStyle.apply(this.cssFile);
        }
        return localCss;
    }

    private String readJavascript(String resource) {
        try (InputStream is = resource(resource)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            JsMin.builder()
                    .inputStream(is)
                    .outputStream(baos)
                    .build()
                    .minify();

            return baos.toString().replaceAll("[\n\r]", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Template template(String resource) throws IOException {
        return templateConfiguration.getTemplate(resource);
    }

    private InputStream resource(String resource) {
        return classLoader().getResourceAsStream(resource);
    }

}