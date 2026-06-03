package es.iti.wakamiti.report.allure;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.JsonPlanSerializer;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;


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

        JsonNode passedScenario = resultByName(results, "[ID-1] Scenario: passing scenario");
        assertThat(passedScenario.path("status").asText()).isEqualTo("passed");
        assertThat(passedScenario.path("stage").asText()).isEqualTo("finished");
        assertThat(labelValue(passedScenario, "suite")).isEqualTo("Allure report feature");
        assertThat(labelValue(passedScenario, "feature")).isEqualTo("Allure report feature");
        assertThat(labelValue(passedScenario, "framework")).isEqualTo("wakamiti");
        assertThat(labelValue(passedScenario, "language")).isEqualTo("en");
        assertThat(parameterValue(passedScenario, "id")).isEqualTo("ID-1");
        assertThat(parameterValue(passedScenario, "source")).contains("allure.feature");

        JsonNode failedScenario = resultByName(results, "[ID-2] Scenario: failing scenario");
        assertThat(failedScenario.path("status").asText()).isEqualTo("failed");
        assertThat(failedScenario.path("statusDetails").path("message").asText())
                .isEqualTo("Synthetic failure for Allure");
        assertThat(failedScenario.path("statusDetails").path("trace").asText())
                .contains("AssertionError");
        assertThat(failedScenario.path("historyId").asText()).isNotBlank();
        assertThat(failedScenario.path("testCaseId").asText()).isNotBlank();
        assertThat(failedScenario.path("uuid").asText()).isNotBlank();

        assertThat(Files.isRegularFile(outputFile)).isTrue();
    }

    @Test
    public void shouldRenderGeneratedResultsWithAllure() throws IOException {
        Path resultsDir = moduleDir().resolve("target/allure-render-results");
        Path reportDir = moduleDir().resolve("target/allure-render-report");
        Path outputFile = moduleDir().resolve("target/allure-render-wakamiti.json");

        runFeature(moduleDir().resolve("src/test/resources/features/allure.feature").toString(), resultsDir, outputFile);

        verifyRenderedReport(
                resultsDir,
                reportDir
        );
    }

    @Test
    public void shouldMapUnexpectedErrorsToBrokenStatus() throws IOException {
        Path resultsDir = moduleDir().resolve("target/allure-error-results");
        Path outputFile = moduleDir().resolve("target/allure-error-wakamiti.json");

        runFeature(moduleDir().resolve("src/test/resources/features/error.feature").toString(), resultsDir, outputFile);

        List<JsonNode> results = readResults(resultsDir);
        assertThat(results).hasSize(1);

        JsonNode brokenScenario = resultByName(results, "[ID-BROKEN-1] Scenario: error scenario");
        assertThat(brokenScenario.path("status").asText()).isEqualTo("broken");
        assertThat(brokenScenario.path("statusDetails").path("message").asText())
                .isEqualTo("Synthetic error for Allure");
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
        assertThat(scenario.path("fullName").asText()).endsWith(".ID-3");
        assertThat(scenario.path("description").asText()).isEqualTo("This is a scenario description");
        assertThat(labelValue(scenario, "suite")).isEqualTo("Metadata feature");
        assertThat(labelValues(scenario)).contains("smoke", "api");
        assertThat(labelValues(scenario)).doesNotContain("definition", "implementation");
        assertThat(labelValue(scenario, "package")).contains("metadata");
        assertThat(parameterValue(scenario, "id")).isEqualTo("ID-3");
        assertThat(scenario.path("steps").size()).isEqualTo(1);
        assertThat(nodeByName(scenario.path("steps"), "Given a passing step").path("status").asText())
                .isEqualTo("passed");
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

        JsonNode skippedScenario = resultByName(results, "[SKIP-1] Scenario: skipped case");
        assertThat(skippedScenario.path("status").asText()).isEqualTo("skipped");
        assertThat(labelValues(skippedScenario)).containsExactly("focus");
        assertThat(skippedScenario.path("steps")).hasSize(1);

        JsonNode groupedStep = nodeByName(skippedScenario.path("steps"), "Given setup group");
        assertThat(groupedStep.path("status").asText()).isEqualTo("skipped");
        assertThat(groupedStep.path("steps")).hasSize(1);

        JsonNode nestedStep = nodeByName(groupedStep.path("steps"), "And embedded action");
        assertThat(nestedStep.path("status").asText()).isEqualTo("skipped");
        assertThat(parameterValue(nestedStep, "documentType")).isEqualTo("text/plain");
        assertThat(parameterValue(nestedStep, "document")).isEqualTo("first line\nsecond line");
        assertThat(parameterValue(nestedStep, "dataTable")).isEqualTo("a | b" + System.lineSeparator() + "1 | 2");
        assertThat(labelValue(skippedScenario, "package")).isEqualTo("features.sample");

        JsonNode unknownScenario = resultByName(results, "Unnamed node");
        assertThat(unknownScenario.path("status").asText()).isEqualTo("unknown");
        assertThat(labelValue(unknownScenario, "package")).isEqualTo("wakamiti");
        assertThat(unknownScenario.path("fullName").asText()).isEqualTo("wakamiti.Unnamed node");
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
            Path resultsDir,
            Path reportDir
    ) throws IOException {
        cleanDirectory(reportDir);
        Files.createDirectories(reportDir);
        skipWhenDockerUnavailable();

        try (GenericContainer<?> container = new GenericContainer<>(ALLURE_DOCKER_SERVICE)
                .withExposedPorts(5050)
                .withEnv("CHECK_RESULTS_EVERY_SECONDS", "1")
                .withEnv("KEEP_HISTORY", "1")
                .withFileSystemBind(resultsDir.toAbsolutePath().toString(), "/app/allure-results")
                .withFileSystemBind(reportDir.toAbsolutePath().toString(), "/app/default-reports")
                .waitingFor(Wait.forLogMessage(".*Report successfully generated.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2))) {

            startOrSkipWhenDockerUnavailable(container);

            Path summaryFile = reportDir.resolve("latest/widgets/summary.json");
            Path suitesFile = reportDir.resolve("latest/widgets/suites.json");
            Path statusChartFile = reportDir.resolve("latest/widgets/status-chart.json");

            waitForFile(summaryFile, Duration.ofSeconds(30));
            waitForFile(suitesFile, Duration.ofSeconds(30));
            waitForFile(statusChartFile, Duration.ofSeconds(30));

            JsonNode summary = OBJECT_MAPPER.readTree(summaryFile.toFile());
            JsonNode suites = OBJECT_MAPPER.readTree(suitesFile.toFile());
            JsonNode statusChart = OBJECT_MAPPER.readTree(statusChartFile.toFile());

            assertThat(summary.path("statistic").path("failed").asInt()).isEqualTo(1);
            assertThat(summary.path("statistic").path("broken").asInt()).isEqualTo(0);
            assertThat(summary.path("statistic").path("skipped").asInt()).isEqualTo(0);
            assertThat(summary.path("statistic").path("passed").asInt()).isEqualTo(1);
            assertThat(summary.path("statistic").path("total").asInt()).isEqualTo(2);

            JsonNode suite = suites.path("items").get(0);
            assertThat(suite.path("name").asText()).isEqualTo("Allure report feature");

            assertThat(StreamSupport.stream(statusChart.spliterator(), false)
                    .map(node -> node.path("name").asText()))
                    .contains("[ID-2] Scenario: failing scenario");
        }
    }

    private List<JsonNode> readResults(
            Path output
    ) throws IOException {
        try (Stream<Path> files = Files.list(output)) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith("-result.json"))
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

    private void waitForFile(
            Path file,
            Duration timeout
    ) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(file)) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted waiting for " + file, e);
            }
        }
        throw new AssertionError("Expected file was not generated: " + file);
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
