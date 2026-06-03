/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.allure;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Extension(provider = "es.iti.wakamiti", name = "allure-report", version = "2.6")
public class AllureReporter implements Reporter {

    private static final Logger LOGGER = WakamitiLogger.forClass(AllureReporter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private Path outputDir = Path.of("allure-results");

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

            List<TestCaseContext> testCases = new ArrayList<>();
            collectTestCases(rootNode, null, testCases);

            for (TestCaseContext testCase : testCases) {
                writeResult(output, testCase);
            }
        } catch (IOException e) {
            LOGGER.error("Error generating Allure results: {}", e.getMessage(), e);
        }
    }

    private void collectTestCases(
            PlanNodeSnapshot node,
            PlanNodeSnapshot currentFeature,
            List<TestCaseContext> testCases
    ) {
        PlanNodeSnapshot activeFeature = isFeature(node) ? node : currentFeature;
        if (node.getNodeType() == NodeType.TEST_CASE) {
            testCases.add(new TestCaseContext(activeFeature, node));
        }
        if (node.getChildren() != null) {
            for (PlanNodeSnapshot child : node.getChildren()) {
                collectTestCases(child, activeFeature, testCases);
            }
        }
    }

    private void writeResult(
            Path output,
            TestCaseContext context
    ) throws IOException {
        Map<String, Object> result = mapTestCase(context);
        String uuid = Objects.toString(result.get("uuid"));
        Path file = output.resolve(uuid + "-result.json");
        OBJECT_MAPPER.writeValue(file.toFile(), result);
        WakamitiAPI.instance().publishEvent(Event.REPORT_OUTPUT_FILE_WRITTEN, file);
    }

    private Map<String, Object> mapTestCase(
            TestCaseContext context
    ) {
        PlanNodeSnapshot testCase = context.testCase;
        String fullName = fullName(context.feature, testCase);
        String deterministicId = hash(fullName);
        String uuid = UUID.nameUUIDFromBytes(
                (Objects.toString(testCase.getExecutionID(), "") + ":" + fullName).getBytes(StandardCharsets.UTF_8)
        ).toString();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uuid", uuid);
        result.put("historyId", deterministicId);
        result.put("testCaseId", deterministicId);
        result.put("fullName", fullName);
        result.put("name", displayName(testCase));
        putIfNotNull(result, "description", description(testCase));
        putIfNotNull(result, "status", status(testCase.getResult()));
        putIfNotNull(result, "statusDetails", statusDetails(testCase));
        result.put("stage", "finished");
        putIfNotNull(result, "start", toEpochMillis(testCase.getStartInstant()));
        putIfNotNull(result, "stop", toEpochMillis(testCase.getFinishInstant()));

        List<Map<String, Object>> steps = mapSteps(testCase.getChildren());
        if (!steps.isEmpty()) {
            result.put("steps", steps);
        }

        List<Map<String, String>> parameters = testParameters(testCase);
        if (!parameters.isEmpty()) {
            result.put("parameters", parameters);
        }

        result.put("labels", labels(context));
        return result;
    }

    private List<Map<String, Object>> mapSteps(
            List<PlanNodeSnapshot> children
    ) {
        List<Map<String, Object>> steps = new LinkedList<>();
        if (children == null) {
            return steps;
        }

        for (PlanNodeSnapshot child : children) {
            if (child.getNodeType().isAnyOf(NodeType.STEP, NodeType.STEP_AGGREGATOR, NodeType.VIRTUAL_STEP)) {
                steps.add(mapStep(child));
            }
        }
        return steps;
    }

    private Map<String, Object> mapStep(
            PlanNodeSnapshot step
    ) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        mapped.put("name", displayName(step));
        putIfNotNull(mapped, "description", description(step));
        putIfNotNull(mapped, "status", status(step.getResult()));
        putIfNotNull(mapped, "statusDetails", statusDetails(step));
        mapped.put("stage", "finished");
        putIfNotNull(mapped, "start", toEpochMillis(step.getStartInstant()));
        putIfNotNull(mapped, "stop", toEpochMillis(step.getFinishInstant()));

        List<Map<String, String>> parameters = dataParameters(step);
        if (!parameters.isEmpty()) {
            mapped.put("parameters", parameters);
        }

        List<Map<String, Object>> substeps = mapSteps(step.getChildren());
        if (!substeps.isEmpty()) {
            mapped.put("steps", substeps);
        }
        return mapped;
    }

    private List<Map<String, String>> testParameters(
            PlanNodeSnapshot testCase
    ) {
        List<Map<String, String>> parameters = new ArrayList<>();
        addParameter(parameters, "id", testCase.getId());
        addParameter(parameters, "source", testCase.getSource());
        return parameters;
    }

    private List<Map<String, String>> dataParameters(
            PlanNodeSnapshot node
    ) {
        List<Map<String, String>> parameters = new ArrayList<>();
        addParameter(parameters, "source", node.getSource());
        if (node.getDocument() != null) {
            addParameter(parameters, "documentType", node.getDocumentType());
            addParameter(parameters, "document", node.getDocument());
        }
        if (node.getDataTable() != null) {
            addParameter(parameters, "dataTable", serializeDataTable(node.getDataTable()));
        }
        return parameters;
    }

    private void addParameter(
            List<Map<String, String>> parameters,
            String name,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            parameters.add(Map.of("name", name, "value", value));
        }
    }

    private List<Map<String, String>> labels(
            TestCaseContext context
    ) {
        List<Map<String, String>> labels = new ArrayList<>();
        addLabel(labels, "framework", "wakamiti");
        addLabel(labels, "language", Optional.ofNullable(context.testCase.getLanguage()).orElse("gherkin"));
        addLabel(labels, "host", hostName());
        addLabel(labels, "thread", Optional.ofNullable(context.testCase.getExecutionID()).orElse("wakamiti"));
        addLabel(labels, "feature", context.feature == null ? null : context.feature.getName());
        addLabel(labels, "suite", context.feature == null ? null : context.feature.getName());
        addLabel(labels, "package", packageName(context.feature, context.testCase));

        if (context.testCase.getTags() != null) {
            for (String tag : context.testCase.getTags()) {
                if (!tag.equals(context.testCase.getId()) && !"definition".equals(tag) && !"implementation".equals(tag)) {
                    addLabel(labels, "tag", tag);
                }
            }
        }
        return labels;
    }

    private void addLabel(
            List<Map<String, String>> labels,
            String name,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            labels.add(Map.of("name", name, "value", value));
        }
    }

    private String description(
            PlanNodeSnapshot node
    ) {
        if (node.getDescription() == null || node.getDescription().isEmpty()) {
            return null;
        }
        return String.join(System.lineSeparator(), node.getDescription());
    }

    private String displayName(
            PlanNodeSnapshot node
    ) {
        if (node.getDisplayName() != null && !node.getDisplayName().isBlank()) {
            return node.getDisplayName();
        }
        String keyword = Optional.ofNullable(node.getKeyword()).orElse("");
        String name = Optional.ofNullable(node.getName()).orElse("");
        String joined = (keyword + " " + name).trim();
        return joined.isEmpty() ? "Unnamed node" : joined;
    }

    private Map<String, Object> statusDetails(
            PlanNodeSnapshot node
    ) {
        if (node.getErrorMessage() == null && node.getErrorTrace() == null) {
            return null;
        }
        Map<String, Object> details = new LinkedHashMap<>();
        putIfNotNull(details, "message", node.getErrorMessage());
        putIfNotNull(details, "trace", node.getErrorTrace());
        return details;
    }

    private String status(
            Result result
    ) {
        if (result == null) {
            return "unknown";
        }
        switch (result) {
            case PASSED:
                return "passed";
            case FAILED:
                return "failed";
            case ERROR:
            case UNDEFINED:
                return "broken";
            case SKIPPED:
            case NOT_IMPLEMENTED:
                return "skipped";
            default:
                return "unknown";
        }
    }

    private Long toEpochMillis(
            String instant
    ) {
        if (instant == null || instant.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(instant)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    private boolean isFeature(
            PlanNodeSnapshot node
    ) {
        return node.getProperties() != null && "feature".equals(node.getProperties().get("gherkinType"));
    }

    private String fullName(
            PlanNodeSnapshot feature,
            PlanNodeSnapshot testCase
    ) {
        return packageName(feature, testCase) + "." + Optional.ofNullable(testCase.getId()).orElse(displayName(testCase));
    }

    private String packageName(
            PlanNodeSnapshot feature,
            PlanNodeSnapshot testCase
    ) {
        String source = Optional.ofNullable(feature)
                .map(PlanNodeSnapshot::getSource)
                .orElse(testCase.getSource());
        if (source == null || source.isBlank()) {
            return "wakamiti";
        }

        String normalized = source.replace('\\', '/');
        int bracketIndex = normalized.indexOf('[');
        if (bracketIndex >= 0) {
            normalized = normalized.substring(0, bracketIndex);
        }
        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex > 0) {
            normalized = normalized.substring(0, dotIndex);
        }
        normalized = normalized.replace('/', '.').replace(' ', '_');
        return normalized.isBlank() ? "wakamiti" : normalized;
    }

    private String serializeDataTable(
            String[][] dataTable
    ) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dataTable.length; i++) {
            if (i > 0) {
                builder.append(System.lineSeparator());
            }
            builder.append(String.join(" | ", dataTable[i]));
        }
        return builder.toString();
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return "localhost";
        }
    }

    private String hash(
            String value
    ) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
    }

    private void putIfNotNull(
            Map<String, Object> target,
            String key,
            Object value
    ) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private static final class TestCaseContext {
        private final PlanNodeSnapshot feature;
        private final PlanNodeSnapshot testCase;

        private TestCaseContext(
                PlanNodeSnapshot feature,
                PlanNodeSnapshot testCase
        ) {
            this.feature = feature;
            this.testCase = testCase;
        }
    }
}
