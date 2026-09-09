/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.allure;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.report.allure.internal.AllureMapper;
import es.iti.wakamiti.report.allure.internal.EnumValueSerializer;
import es.iti.wakamiti.report.allure.internal.WakamitiTestResult;
import es.iti.wakamiti.report.allure.internal.WakamitiTestResultContainer;
import es.iti.wakamiti.report.allure.internal.WithUuid;


/**
 * Reports Wakamiti execution information in Allure's result format.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "allure-report",
        version = "3.0"
)
public class AllureReporter implements Reporter {

    private static final Logger LOGGER = WakamitiLogger.forClass(AllureReporter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .registerModule(new SimpleModule()
                    .addSerializer(io.qameta.allure.model.Status.class,
                            new EnumValueSerializer<>(io.qameta.allure.model.Status::value))
                    .addSerializer(io.qameta.allure.model.Stage.class,
                            new EnumValueSerializer<>(io.qameta.allure.model.Stage::value)));
    private static final AllureMapper MAPPER = new AllureMapper();

    private Path outputDir;

    /**
     * Sets the directory in which Allure result files are written.
     *
     * @param outputDir result directory
     */
    public void setOutputDir(
            Path outputDir
    ) {
        this.outputDir = outputDir;
    }

    @Override
    public void report(
            PlanNodeSnapshot rootNode
    ) {
        Path output = WakamitiAPI.instance().resourceLoader().absolutePath(outputDir);
        try {
            Files.createDirectories(output);
            for (WithUuid result : MAPPER.map(rootNode)) {
                writeResult(output, result);
            }
        } catch (IOException e) {
            LOGGER.error("Error generating Allure results: {}", e.getMessage(), e);
        }
    }

    private void writeResult(
            Path output,
            WithUuid result
    ) throws IOException {
        String suffix = result instanceof WakamitiTestResultContainer
                ? "-container.json" : "-result.json";
        Path file = output.resolve(result.getUuid() + suffix);
        JsonNode json = OBJECT_MAPPER.valueToTree(result);
        if (result instanceof WakamitiTestResult testResult
                && testResult.getStatus() == null
                && json instanceof ObjectNode objectNode) {
            objectNode.put("status", "unknown");
        }
        OBJECT_MAPPER.writeValue(file.toFile(), json);
        WakamitiAPI.instance().publishEvent(Event.REPORT_OUTPUT_FILE_WRITTEN, file);
    }

}
