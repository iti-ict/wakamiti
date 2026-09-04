/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.allure;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.awaitility.Awaitility;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.core.Wakamiti;


public class AllureReporterTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DockerImageName ALLURE_DOCKER_SERVICE =
            DockerImageName.parse("frankescobar/allure-docker-service:2.38.1");
    private static final JsonPlanSerializer PLAN_SERIALIZER = new JsonPlanSerializer();

    @Test
    public void shouldGenerateAllureResultsUsingConfiguredOutputDirectory() throws IOException {
        Path resultsDir = moduleDir().resolve("target/allure-config-results");
        Path outputFile = moduleDir().resolve("target/allure-config-wakamiti.json");

        runFeature(moduleDir().resolve("src/test/resources/features/allure.feature").toString(), resultsDir, outputFile);

        List<JsonNode> results = readResults(resultsDir);
        assertThat(results).hasSize(2);

        assertThat(results)
                .extracting(result -> result.path("status").asText())
                .containsExactlyInAnyOrder("passed", "failed");

        JsonNode passedScenario = resultByName(results, "passing scenario");
        assertThat(passedScenario.path("status").asText()).isEqualTo("passed");
        assertThat(passedScenario.path("stage").asText()).isEqualTo("finished");
        assertThat(labelValue(passedScenario, "feature")).isEqualTo("Allure report feature");
        assertThat(labelValue(passedScenario, "framework")).isEqualTo("wakamiti");
        assertThat(labelValue(passedScenario, "language")).isEqualTo("en");
        assertThat(parameterValue(passedScenario, "id")).isEqualTo("ID-1");
        assertThat(parameterValue(passedScenario, "source")).contains("allure.feature");
        assertThat(passedScenario.path("testCaseId").asText()).isEqualTo("ID-1");

        JsonNode failedScenario = resultByName(results, "failing scenario");
        assertThat(failedScenario.path("status").asText()).isEqualTo("failed");
        assertThat(failedScenario.path("statusDetails").path("message").asText())
                .isEqualTo("Synthetic failure for Allure");
        assertThat(failedScenario.path("statusDetails").path("trace").asText())
                .contains("AssertionError");
        assertThat(failedScenario.path("historyId").asText()).isNotBlank();
        assertThat(failedScenario.path("testCaseId").asText()).isNotBlank();
        assertThat(failedScenario.path("uuid").asText()).isNotBlank();

        try (Stream<Path> files = Files.list(resultsDir)) {
            List<JsonNode> containers = files
                    .filter(path -> path.getFileName().toString().endsWith("-container.json"))
                    .map(this::readJson)
                    .collect(Collectors.toList());
            assertThat(containers).hasSize(1);
            assertThat(containers.get(0).path("name").asText()).isEqualTo("Allure report feature");
            assertThat(containers.get(0).path("children")).hasSize(2);
            assertThat(StreamSupport.stream(containers.get(0).path("children").spliterator(), false)
                    .allMatch(child -> !child.asText().isBlank())).isTrue();
        }

        assertThat(Files.isRegularFile(outputFile)).isTrue();
    }

    @Test
    public void shouldRenderGeneratedResultsWithAllure() throws IOException {
        Path resultsDir = moduleDir().resolve("target/allure-render-results");
        Path outputFile = moduleDir().resolve("target/allure-render-wakamiti.json");

        runFeature(moduleDir().resolve("src/test/resources/features/allure.feature").toString(), resultsDir, outputFile);

        verifyRenderedReport(resultsDir);
    }

    @Test
    public void shouldMapUnexpectedErrorsToBrokenStatus() throws IOException {
        Path resultsDir = moduleDir().resolve("target/allure-error-results");
        Path outputFile = moduleDir().resolve("target/allure-error-wakamiti.json");

        runFeature(moduleDir().resolve("src/test/resources/features/error.feature").toString(), resultsDir, outputFile);

        List<JsonNode> results = readResults(resultsDir);
        assertThat(results).hasSize(1);

        JsonNode brokenScenario = resultByName(results, "error scenario");
        assertThat(brokenScenario.path("status").asText()).isEqualTo("broken");
        assertThat(brokenScenario.path("statusDetails").path("message").asText())
                .isEqualTo("Synthetic error for Allure");

        PlanNodeSnapshot plan = PLAN_SERIALIZER.read(outputFile);
        PlanNodeSnapshot brokenPlanNode = plan.flatten(node -> "ID-BROKEN-1".equals(node.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(brokenScenario.path("start").asLong()).isEqualTo(Instant.parse(brokenPlanNode.getStartInstant()).toEpochMilli());
        assertThat(brokenScenario.path("stop").asLong()).isEqualTo(Instant.parse(brokenPlanNode.getFinishInstant()).toEpochMilli());
    }

    @Test
    public void shouldIncludeDescriptionsTagsAndStepParameters() throws IOException {
        Path resultsDir = moduleDir().resolve("target/allure-metadata-results");
        Path outputFile = moduleDir().resolve("target/allure-metadata-wakamiti.json");

        runFeature(moduleDir().resolve("src/test/resources/features/metadata.feature").toString(), resultsDir, outputFile);

        List<JsonNode> results = readResults(resultsDir);
        assertThat(results).hasSize(1);

        JsonNode scenario = results.get(0);
        assertThat(scenario.path("status").asText()).isEqualTo("passed");
        assertThat(scenario.path("name").asText()).isEqualTo("detailed scenario");
        assertThat(scenario.path("fullName").asText()).isEqualTo("[ID-3] Scenario: detailed scenario");
        assertThat(scenario.path("testCaseId").asText()).isEqualTo("ID-3");
        assertThat(scenario.path("description").asText()).isEqualTo("This is a scenario description");
        assertThat(labelValue(scenario, "feature")).isEqualTo("Metadata feature");
        assertThat(labelValue(scenario, "suite")).isNull();
        assertThat(labelValue(scenario, "package")).isNull();
        assertThat(labelValue(scenario, "tags")).contains("smoke", "api");
        assertThat(parameterValue(scenario, "id")).isEqualTo("ID-3");
        assertThat(scenario.path("steps").size()).isEqualTo(1);
        JsonNode step = nodeByName(scenario.path("steps"), "Given a passing step");
        assertThat(step.path("status").asText()).isEqualTo("passed");
        assertThat(step.path("parameters")).isEmpty();
    }

    @Test
    public void shouldIncludeLifecycleHooksInAllureResults() throws IOException {
        Path resultsDir = moduleDir().resolve("target/allure-lifecycle-results");
        Path outputFile = moduleDir().resolve("target/allure-lifecycle-wakamiti.json");

        runFeature(moduleDir().resolve("src/test/resources/features/lifecycle").toString(), resultsDir, outputFile);

        List<JsonNode> results = readResults(resultsDir);
        assertThat(results).hasSize(1);

        JsonNode functionalScenario = resultByName(results, "functional scenario");
        assertThat(functionalScenario.path("status").asText()).isEqualTo("passed");
        assertThat(parameterValue(functionalScenario, "id")).isEqualTo("ID-LC-1");

        try (Stream<Path> files = Files.list(resultsDir)) {
            List<JsonNode> containers = files
                    .filter(path -> path.getFileName().toString().endsWith("-container.json"))
                    .map(this::readJson)
                    .collect(Collectors.toList());
            assertThat(containers).hasSize(1);
            JsonNode container = containers.get(0);
            assertThat(container.path("children")).hasSize(1);
            assertThat(container.path("befores")).hasSize(1);
            assertThat(container.path("befores").get(0).path("name").asText())
                    .isEqualTo("before feature hook");
            assertThat(container.path("afters")).hasSize(1);
            assertThat(container.path("afters").get(0).path("name").asText())
                    .isEqualTo("after feature hook");
        }
    }

    @Test
    public void shouldMapSnapshotSpecificStatusesAndFallbackValues() throws IOException {
        Path resultsDir = moduleDir().resolve("target/allure-status-results");

        cleanDirectory(resultsDir);
        Files.createDirectories(resultsDir);

        PlanNodeSnapshot snapshot = PLAN_SERIALIZER.read(moduleDir().resolve("src/test/resources/snapshots/statuses.json"));
        AllureReporter reporter = new AllureReporter();
        reporter.setOutputDir(resultsDir);
        reporter.report(snapshot);

        List<JsonNode> results = readResults(resultsDir);
        assertThat(results).hasSize(2);

        List<JsonNode> containers = readContainers(resultsDir);
        assertThat(containers).hasSize(1);
        JsonNode container = containers.get(0);
        assertThat(container.path("name").asText()).isEqualTo("Statuses feature");
        assertThat(container.path("children")).hasSize(2);
        assertThat(container.path("befores")).isEmpty();
        assertThat(container.path("afters")).isEmpty();
        assertThat(StreamSupport.stream(container.path("children").spliterator(), false)
                .map(JsonNode::asText)
                .allMatch(uuid -> results.stream()
                        .map(result -> result.path("uuid").asText())
                        .anyMatch(uuid::equals)))
                .isTrue();

        JsonNode skippedScenario = results.stream()
                .filter(result -> "SKIP-1".equals(result.path("testCaseId").asText()))
                .findFirst()
                .orElseThrow();
        assertThat(skippedScenario.path("status").asText()).isEqualTo("skipped");
        assertThat(labelValue(skippedScenario, "tags")).contains("SKIP-1", "focus");
        assertThat(skippedScenario.path("steps")).hasSize(1);

        JsonNode groupedStep = nodeByName(skippedScenario.path("steps"), "Given setup group");
        assertThat(groupedStep.path("status").asText()).isEqualTo("skipped");
        assertThat(groupedStep.path("steps")).hasSize(1);

        JsonNode nestedStep = nodeByName(groupedStep.path("steps"), "And embedded action");
        assertThat(nestedStep.path("status").asText()).isEqualTo("skipped");
        assertThat(nestedStep.path("parameters")).isEmpty();

        JsonNode unknownScenario = results.stream()
                .filter(result -> "unknown".equals(result.path("status").asText()))
                .findFirst()
                .orElseThrow();
        assertThat(unknownScenario.path("status").asText()).isEqualTo("unknown");
        assertThat(unknownScenario.path("name").isNull()).isTrue();
        assertThat(unknownScenario.path("fullName").isNull()).isTrue();
        assertThat(labelValue(unknownScenario, "package")).isNull();
        assertThat(unknownScenario.path("parameters").isMissingNode() || unknownScenario.path("parameters").isEmpty()).isTrue();
    }

    private void runFeature(
            String resourcePath,
            Path resultsDir,
            Path outputFile
    ) throws IOException {
        cleanDirectory(resultsDir);
        Files.createDirectories(resultsDir);
        Files.deleteIfExists(outputFile);

        Configuration configuration = Wakamiti.defaultConfiguration().appendFromPairs(
                WakamitiConfiguration.RESOURCE_TYPES, "gherkin",
                WakamitiConfiguration.RESOURCE_PATH, resourcePath,
                WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, SampleSteps.class.getCanonicalName(),
                WakamitiConfiguration.WORKING_DIR, moduleDir().toString(),
                WakamitiConfiguration.OUTPUT_FILE_PATH, outputFile.toString(),
                WakamitiConfiguration.REPORT_GENERATION, Boolean.FALSE.toString(),
                AllureReporterConfig.OUTPUT, resultsDir.toString()
        );

        Wakamiti wakamiti = Wakamiti.instance();
        PlanNode plan = wakamiti.createPlanFromConfiguration(configuration);
        PlanNode executedPlan = wakamiti.executePlan(plan, configuration);
        generateAllureResults(configuration, executedPlan);

        assertThat(executedPlan.result()).isPresent();
        assertThat(executedPlan.result().orElseThrow()).isIn(Result.PASSED, Result.FAILED, Result.ERROR);
    }

    private void generateAllureResults(
            Configuration configuration,
            PlanNode executedPlan
    ) {
        AllureReporter reporter = new AllureReporter();
        new AllureReporterConfig().configurer().configure(reporter, configuration);
        reporter.report(new PlanNodeSnapshot(executedPlan));
    }

    private void verifyRenderedReport(
            Path resultsDir
    ) throws IOException {
        skipWhenDockerUnavailable();

        GenericContainer<?> container = new GenericContainer<>(ALLURE_DOCKER_SERVICE)
                .withExposedPorts(5050)
                .withEnv("CHECK_RESULTS_EVERY_SECONDS", "1")
                .withEnv("KEEP_HISTORY", "1")
                .waitingFor(Wait.forLogMessage(".*Report successfully generated.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2));
        copyDirectoryToContainer(container, resultsDir, "/app/allure-results");

        try (container) {
            startOrSkipWhenDockerUnavailable(container);

            JsonNode summary = readJsonFromContainer(container,
                    "/app/default-reports/latest/widgets/summary.json", Duration.ofSeconds(30));
            JsonNode suites = readJsonFromContainer(container,
                    "/app/default-reports/latest/widgets/suites.json", Duration.ofSeconds(30));
            JsonNode statusChart = readJsonFromContainer(container,
                    "/app/default-reports/latest/widgets/status-chart.json", Duration.ofSeconds(30));

            assertThat(summary.path("statistic").path("failed").asInt()).isEqualTo(1);
            assertThat(summary.path("statistic").path("broken").asInt()).isZero();
            assertThat(summary.path("statistic").path("skipped").asInt()).isZero();
            assertThat(summary.path("statistic").path("passed").asInt()).isEqualTo(1);
            assertThat(summary.path("statistic").path("total").asInt()).isEqualTo(2);

            assertThat(suites.path("items")).isEmpty();

            assertThat(StreamSupport.stream(statusChart.spliterator(), false)
                    .map(node -> node.path("name").asText()))
                    .contains("failing scenario");
        }
    }

    private void copyDirectoryToContainer(
            GenericContainer<?> container,
            Path sourceDir,
            String targetDir
    ) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            for (Path file : paths.filter(Files::isRegularFile).toList()) {
                byte[] content = Files.readAllBytes(file);
                String targetPath = targetDir + "/" + sourceDir.relativize(file).toString().replace('\\', '/');
                container.withCopyToContainer(Transferable.of(content), targetPath);
            }
        }
    }

    private JsonNode readJsonFromContainer(
            GenericContainer<?> container,
            String containerPath,
            Duration timeout
    ) {
        AtomicReference<JsonNode> json = new AtomicReference<>();
        Awaitility.await()
                .atMost(timeout)
                .ignoreExceptions()
                .until(() -> {
                    json.set(container.copyFileFromContainer(containerPath, OBJECT_MAPPER::readTree));
                    return json.get() != null;
                });
        return json.get();
    }

    private List<JsonNode> readResults(
            Path output
    ) throws IOException {
        return readJsonFiles(output, "-result.json");
    }

    private List<JsonNode> readContainers(
            Path output
    ) throws IOException {
        return readJsonFiles(output, "-container.json");
    }

    private List<JsonNode> readJsonFiles(
            Path output,
            String suffix
    ) throws IOException {
        try (Stream<Path> files = Files.list(output)) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .sorted()
                    .map(this::readJson)
                    .collect(Collectors.toList());
        }
    }

    private JsonNode readJson(
            Path file
    ) {
        try {
            return OBJECT_MAPPER.readTree(file.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read generated allure result " + file, e);
        }
    }

    private JsonNode resultByName(
            List<JsonNode> results,
            String name
    ) {
        return results.stream()
                .filter(result -> name.equals(result.path("name").asText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Result not found: " + name));
    }

    private JsonNode nodeByName(
            JsonNode nodes,
            String name
    ) {
        return StreamSupport.stream(nodes.spliterator(), false)
                .filter(node -> name.equals(node.path("name").asText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Node not found: " + name));
    }

    private String labelValue(
            JsonNode result,
            String labelName
    ) {
        return StreamSupport.stream(result.path("labels").spliterator(), false)
                .filter(label -> labelName.equals(label.path("name").asText()))
                .map(label -> label.path("value").asText())
                .findFirst()
                .orElse(null);
    }

    private List<String> labelValues(
            JsonNode result
    ) {
        return StreamSupport.stream(result.path("labels").spliterator(), false)
                .filter(label -> "tag".equals(label.path("name").asText()))
                .map(label -> label.path("value").asText())
                .collect(Collectors.toList());
    }

    private String parameterValue(
            JsonNode node,
            String parameterName
    ) {
        return StreamSupport.stream(node.path("parameters").spliterator(), false)
                .filter(parameter -> parameterName.equals(parameter.path("name").asText()))
                .map(parameter -> parameter.path("value").asText())
                .findFirst()
                .orElse(null);
    }

    private void cleanDirectory(
            Path directory
    ) throws IOException {
        if (Files.notExists(directory)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Cannot delete " + path, e);
                        }
                    });
        }
    }

    private Path moduleDir() {
        try {
            Path testClassesDir = Path.of(
                    AllureReporterTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );
            return testClassesDir.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cannot resolve module directory", e);
        }
    }

    private void skipWhenDockerUnavailable() {
        try {
            if (!DockerClientFactory.instance().isDockerAvailable()) {
                throw new AssumptionViolatedException(
                        "Skipping Docker-dependent test because no valid Docker environment is available");
            }
        } catch (RuntimeException e) {
            if (hasDockerConnectionError(e)) {
                throw new AssumptionViolatedException(
                        "Skipping Docker-dependent test because no valid Docker environment is available", e);
            }
            throw e;
        }
    }

    private void startOrSkipWhenDockerUnavailable(
            Startable container
    ) {
        try {
            container.start();
        } catch (RuntimeException e) {
            if (hasDockerConnectionError(e)) {
                throw new AssumptionViolatedException(
                        "Skipping Docker-dependent test because no valid Docker environment is available", e);
            }
            throw e;
        }
    }

    private boolean hasDockerConnectionError(
            Throwable throwable
    ) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains("docker")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

}
