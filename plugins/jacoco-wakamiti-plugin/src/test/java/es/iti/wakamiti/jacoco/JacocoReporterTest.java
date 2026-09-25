/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.ISourceFileLocator;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.WakamitiLogger;


public class JacocoReporterTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(JacocoReporter.class);

    private final List<Path> temporaries = new ArrayList<>();

    @After
    public void clean() {
        temporaries.forEach(p -> {
            try {
                if (Files.isDirectory(p)) {
                    FileUtils.cleanDirectory(p.toFile());
                }
                Files.deleteIfExists(p);
            } catch (IOException e) {
                LOGGER.warn("Cannot delete file '{}'", p, e);
            }
        });
        temporaries.clear();
    }

    @Test
    public void acceptTypeRecognizesExpectedEventsOnly() {
        JacocoReporter reporter = new JacocoReporter();
        assertThat(reporter.acceptType(Event.NODE_RUN_FINISHED)).isTrue();
        assertThat(reporter.acceptType(Event.AFTER_WRITE_OUTPUT_FILES)).isTrue();

        // Some negative checks
        assertThat(reporter.acceptType(Event.NODE_RUN_STARTED)).isFalse();
        assertThat(reporter.acceptType(Event.PLAN_RUN_STARTED)).isFalse();
        assertThat(reporter.acceptType("SOME_UNKNOWN_EVENT")).isFalse();
    }

    @Test
    public void eventReceivedDoesNothingWhenDataIsNull() {
        assertThatNoException().isThrownBy(() -> LOGGER.info("OK"));
        JacocoReporter reporter = new JacocoReporter();
        // Should not throw on null data

        reporter.eventReceived(new Event(Event.NODE_RUN_FINISHED, Instant.now(), null));
    }

    @Test
    public void eventWithTestCaseDumpsAndMergesAllHostsWhenNoXmlOrCsv() throws Exception {
        // Arrange reporter
        JacocoReporter reporter = new JacocoReporter();
        reporter.setHosts(List.of("localhost:6300", "jacoco-agent:6301"));
        reporter.setRetries(5);
        Path out = Files.createTempDirectory("jacoco-out");
        temporaries.add(out);
        reporter.setOutput(out);
        reporter.setClasses(List.of(Files.createTempDirectory("classes")));
        reporter.setSources(List.of(Files.createTempDirectory("sources")));
        reporter.setName("Report");

        // Create mocks
        ExecDumpClient dumpClient = mock(ExecDumpClient.class);
        ExecFileLoader firstDumpLoader = executionDataLoader(
                new ExecutionData(1L, "covered/First", new boolean[]{true}),
                new ExecutionData(2L, "uncovered/First", new boolean[]{false})
        );
        ExecFileLoader secondDumpLoader = executionDataLoader(
                new ExecutionData(3L, "covered/Second", new boolean[]{true}),
                new ExecutionData(4L, "uncovered/Second", new boolean[]{false})
        );
        when(dumpClient.dump(anyString(), anyInt())).thenReturn(firstDumpLoader, secondDumpLoader);

        // Inject mock dump client
        setPrivate(reporter, "dumpClient", dumpClient);

        // Prepare event data
        PlanNodeSnapshot snapshot = mock(PlanNodeSnapshot.class);
        when(snapshot.getNodeType()).thenReturn(NodeType.TEST_CASE);
        when(snapshot.getId()).thenReturn("TC-1");

        // Act
        reporter.eventReceived(new Event(Event.NODE_RUN_FINISHED, Instant.now(), snapshot));

        // Assert dump interactions
        verify(dumpClient).setReset(true);
        verify(dumpClient).setRetryCount(5);
        verify(dumpClient).dump("localhost", 6300);
        verify(dumpClient).dump("jacoco-agent", 6301);
        assertThat(executionDataNames(out.resolve("TC-1.exec").toFile()))
                .containsExactlyInAnyOrder("covered/First", "covered/Second");
        // And ensure per-test loader (distinct field) was not used (kept null)
        Object fileLoaderField = getPrivate(reporter, "fileLoader");
        assertThat(fileLoaderField).isNull();
        assertThat(Files.exists(Path.of(out.toString() + ".exec"))).isFalse();
    }

    @Test
    public void eventWithLifecycleHooksDoesNotDumpWhenMergeIsDisabled() throws Exception {
        JacocoReporter reporter = new JacocoReporter();
        reporter.setHosts(List.of("localhost:6300"));
        Path output = Files.createTempDirectory("jacoco-hooks");
        temporaries.add(output);
        reporter.setOutput(output);

        ExecDumpClient dumpClient = mock(ExecDumpClient.class);
        setPrivate(reporter, "dumpClient", dumpClient);

        reporter.eventReceived(hookEvent("#setup", Result.PASSED));
        reporter.eventReceived(hookEvent("#teardown", Result.ERROR));
        reporter.eventReceived(hookEvent("#ignored", Result.SKIPPED));

        verifyNoInteractions(dumpClient);
    }

    @Test
    public void eventWithTestCaseAndXmlTriggersExecuteSingleAndProducesXml() throws Exception {
        // Arrange temporary filesystem
        Path reportsParent = Files.createTempDirectory("jacoco-reports");
        Path out = reportsParent.resolve("out");
        Path xml = reportsParent.resolve("xml");
        Path classes = Files.createTempDirectory("jacoco-classes");
        Path generatedClasses = Files.createTempDirectory("jacoco-generated-classes");
        Path sources = Files.createTempDirectory("jacoco-sources");
        copyClass(classes, JacocoReporter.class);
        copyClass(generatedClasses, JacocoConfig.class);
        temporaries.addAll(List.of(reportsParent, out, xml, classes, generatedClasses, sources));

        // Reporter with configuration
        JacocoReporter reporter = new JacocoReporter();
        reporter.setHosts(List.of("127.0.0.1:6300"));
        reporter.setRetries(1);
        reporter.setOutput(out);
        reporter.setXml(xml);
        reporter.setClasses(List.of(classes, generatedClasses));
        reporter.setSources(List.of(sources));
        reporter.setTabwidth(4);
        reporter.setName("Report");

        // Mock dump client with empty execution data
        ExecDumpClient dumpClient = mock(ExecDumpClient.class);
        ExecFileLoader dumpLoader = new ExecFileLoader();
        when(dumpClient.dump(anyString(), anyInt())).thenReturn(dumpLoader);
        setPrivate(reporter, "dumpClient", dumpClient);

        // Mock fileLoader used by executeSingle
        ExecFileLoader fileLoader = mock(ExecFileLoader.class);
        doNothing().when(fileLoader).load(any(File.class));
        when(fileLoader.getExecutionDataStore()).thenReturn(new ExecutionDataStore());
        when(fileLoader.getSessionInfoStore()).thenReturn(new SessionInfoStore());
        setPrivate(reporter, "fileLoader", fileLoader);

        // Event data
        PlanNodeSnapshot snapshot = mock(PlanNodeSnapshot.class);
        when(snapshot.getNodeType()).thenReturn(NodeType.TEST_CASE);
        when(snapshot.getId()).thenReturn("TC-2");

        // Act
        reporter.eventReceived(new Event(Event.NODE_RUN_FINISHED, Instant.now(), snapshot));

        // Assert that per-test file was loaded
        File expectedExec = out.resolve("TC-2.exec").toFile();
        verify(fileLoader).load(expectedExec);
        // And XML output was created
        Path producedXml = xml.resolve("TC-2.xml");
        assertThat(out).isDirectory();
        assertThat(xml).isDirectory();
        assertThat(Files.exists(producedXml)).isTrue();
        assertThat(Files.readString(producedXml))
                .doesNotContain("es/iti/wakamiti/jacoco/JacocoReporter", "es/iti/wakamiti/jacoco/JacocoConfig");
    }

    @Test
    public void mergeGeneratesOnlyAggregateReportsAndIncludesLifecycleHooks() throws Exception {
        Path reportsParent = Files.createTempDirectory("jacoco-merged-reports");
        Path out = reportsParent.resolve("out");
        Path xml = reportsParent.resolve("xml");
        Path csv = reportsParent.resolve("csv");
        Path html = reportsParent.resolve("html");
        Path classes = Files.createTempDirectory("jacoco-merged-classes");
        Path aggregateExec = Path.of(out + ".exec");
        Path aggregateXml = Path.of(xml + ".xml");
        Path aggregateCsv = Path.of(csv + ".csv");
        temporaries.addAll(List.of(
                reportsParent, out, xml, csv, html, classes, aggregateExec, aggregateXml, aggregateCsv
        ));
        Files.writeString(aggregateExec, "stale execution data");

        JacocoReporter reporter = new JacocoReporter();
        reporter.setHosts(List.of("first:6300", "second:6301"));
        reporter.setOutput(out);
        reporter.setXml(xml);
        reporter.setCsv(csv);
        reporter.setHtml(html);
        reporter.setClasses(List.of(classes));
        reporter.setSources(List.of());
        reporter.setTabwidth(4);
        reporter.setName("Report");
        reporter.setMerge(true);

        ExecDumpClient dumpClient = mock(ExecDumpClient.class);
        when(dumpClient.dump(anyString(), anyInt())).thenAnswer(invocation -> new ExecFileLoader());
        setPrivate(reporter, "dumpClient", dumpClient);

        reporter.eventReceived(hookEvent("#setup", Result.PASSED));
        PlanNodeSnapshot snapshot = mock(PlanNodeSnapshot.class);
        when(snapshot.getNodeType()).thenReturn(NodeType.TEST_CASE);
        when(snapshot.getId()).thenReturn("ID-Scenario");
        reporter.eventReceived(new Event(Event.NODE_RUN_FINISHED, Instant.now(), snapshot));

        assertThat(out.resolve("ID-Scenario.exec")).doesNotExist();
        assertThat(xml.resolve("ID-Scenario.xml")).doesNotExist();
        assertThat(csv.resolve("ID-Scenario.csv")).doesNotExist();
        assertThat(out.resolve("#setup.exec")).doesNotExist();
        assertThat(xml.resolve("#setup.xml")).doesNotExist();
        assertThat(csv.resolve("#setup.csv")).doesNotExist();
        assertThat(aggregateExec).exists();
        verify(dumpClient, times(2)).dump("first", 6300);
        verify(dumpClient, times(2)).dump("second", 6301);

        reporter.eventReceived(new Event(Event.AFTER_WRITE_OUTPUT_FILES, Instant.now(), null));

        assertThat(aggregateXml).exists();
        assertThat(aggregateCsv).exists();
        assertThat(csv).doesNotExist();
        assertThat(html).isDirectory();
        assertThat(html.resolve("index.html")).exists();
    }

    @Test
    public void lifecycleHooksOnlyWriteToTheAggregateWhenMergeIsEnabled() throws Exception {
        JacocoReporter reporter = new JacocoReporter();
        reporter.setHosts(List.of("localhost:6300"));
        Path output = Files.createTempDirectory("jacoco-hooks-aggregate");
        Path aggregate = Path.of(output.toString() + ".exec");
        temporaries.addAll(List.of(output, aggregate));
        reporter.setOutput(output);
        reporter.setMerge(true);

        ExecDumpClient dumpClient = mock(ExecDumpClient.class);
        ExecFileLoader dumpLoader = mock(ExecFileLoader.class);
        when(dumpClient.dump(anyString(), anyInt())).thenReturn(dumpLoader);
        setPrivate(reporter, "dumpClient", dumpClient);

        reporter.eventReceived(hookEvent("#setup-1", Result.PASSED));
        reporter.eventReceived(hookEvent("#setup-2", Result.PASSED));

        verify(dumpLoader).save(aggregate.toFile(), false);
        verify(dumpLoader).save(aggregate.toFile(), true);
        verify(dumpLoader, never()).save(argThat(file -> file.getName().equals("#setup-1.exec")), anyBoolean());
        verify(dumpLoader, never()).save(argThat(file -> file.getName().equals("#setup-2.exec")), anyBoolean());
    }

    @Test
    public void dumpAttemptsEveryHostBeforeReportingFailures() throws Exception {
        JacocoReporter reporter = new JacocoReporter();
        reporter.setHosts(List.of("unavailable:6300", "available:6301", "also-unavailable:6302"));
        reporter.setRetries(2);
        Path out = Files.createTempDirectory("jacoco-failures");
        temporaries.add(out);
        reporter.setOutput(out);

        ExecDumpClient dumpClient = mock(ExecDumpClient.class);
        ExecFileLoader successfulLoader = new ExecFileLoader();
        when(dumpClient.dump("unavailable", 6300)).thenThrow(new IOException("first failure"));
        when(dumpClient.dump("available", 6301)).thenReturn(successfulLoader);
        when(dumpClient.dump("also-unavailable", 6302)).thenThrow(new IOException("last failure"));
        setPrivate(reporter, "dumpClient", dumpClient);

        PlanNodeSnapshot snapshot = mock(PlanNodeSnapshot.class);
        when(snapshot.getNodeType()).thenReturn(NodeType.TEST_CASE);
        when(snapshot.getId()).thenReturn("TC-errors");

        assertThatThrownBy(() -> reporter.eventReceived(
                new Event(Event.NODE_RUN_FINISHED, Instant.now(), snapshot)))
                .isInstanceOf(WakamitiException.class)
                .hasMessageContaining("TC-errors")
                .hasMessageContaining("2 endpoint(s)")
                .satisfies(exception -> {
                    assertThat(exception.getCause()).hasMessageContaining("unavailable:6300");
                    assertThat(exception.getSuppressed()).hasSize(1);
                    assertThat(exception.getSuppressed()[0]).hasMessageContaining("also-unavailable:6302");
                });

        verify(dumpClient).dump("unavailable", 6300);
        verify(dumpClient).dump("available", 6301);
        verify(dumpClient).dump("also-unavailable", 6302);
        verify(successfulLoader).save(argThat(f -> f.getName().equals("TC-errors.exec")), eq(false));
        assertThat(out.resolve("TC-errors.exec")).exists();
    }

    @Test
    public void scenarioReportsExcludeUncoveredClassesButCompleteReportsRetainThem() throws Exception {
        JacocoReporter reporter = new JacocoReporter();
        Path classes = Files.createTempDirectory("jacoco-filtered-classes");
        temporaries.add(classes);
        copyClass(classes, JacocoReporter.class);
        reporter.setClasses(List.of(classes));

        java.lang.reflect.Method analyze = JacocoReporter.class.getDeclaredMethod(
                "analyze", String.class, ExecutionDataStore.class, boolean.class
        );
        analyze.setAccessible(true);
        IBundleCoverage scenario = (IBundleCoverage) analyze.invoke(
                reporter, "scenario", new ExecutionDataStore(), true
        );
        IBundleCoverage complete = (IBundleCoverage) analyze.invoke(
                reporter, "complete", new ExecutionDataStore(), false
        );

        assertThat(scenario.getClassCounter().getTotalCount()).isZero();
        assertThat(complete.getClassCounter().getTotalCount()).isEqualTo(1);
    }

    private static void setPrivate(
            Object target,
            String field,
            Object value
    ) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Event hookEvent(
            String id,
            Result result
    ) {
        PlanNodeSnapshot snapshot = mock(PlanNodeSnapshot.class);
        when(snapshot.getNodeType()).thenReturn(NodeType.LIFECYCLE_HOOK);
        when(snapshot.getId()).thenReturn(id);
        when(snapshot.getResult()).thenReturn(result);
        return new Event(Event.NODE_RUN_FINISHED, Instant.now(), snapshot);
    }

    private static Object getPrivate(
            Object target,
            String field
    ) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(target);
    }

    private ExecFileLoader executionDataLoader(
            ExecutionData... data
    ) {
        ExecFileLoader loader = new ExecFileLoader();
        for (ExecutionData executionData : data) {
            loader.getExecutionDataStore().put(executionData);
        }
        return loader;
    }

    private List<String> executionDataNames(
            File file
    ) throws IOException {
        ExecFileLoader loader = new ExecFileLoader();
        loader.load(file);
        return loader.getExecutionDataStore().getContents().stream()
                .map(ExecutionData::getName)
                .toList();
    }

    private void copyClass(
            Path root,
            Class<?> type
    ) throws IOException {
        Path file = root.resolve(type.getName().replace('.', '/') + ".class");
        Files.createDirectories(file.getParent());
        try (InputStream input = type.getResourceAsStream(type.getSimpleName() + ".class")) {
            Files.copy(input, file);
        }
    }

}
