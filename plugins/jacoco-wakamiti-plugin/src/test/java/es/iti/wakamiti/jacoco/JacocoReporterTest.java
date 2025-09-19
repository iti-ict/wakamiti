/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.apache.commons.io.FileUtils;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecDumpClient;
import org.jacoco.core.tools.ExecFileLoader;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class JacocoReporterTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(JacocoReporter.class);

    private final List<Path> temporaries = new ArrayList<>();

    @After
    public void clean() {
        temporaries.forEach(p -> {
            try {
                FileUtils.cleanDirectory(p.toFile());
                Files.deleteIfExists(p);
            } catch (IOException e) {
                LOGGER.warn("Cannot delete file '{}'", p, e);
            }
        });
        temporaries.clear();
    }

    @Test
    public void acceptType_recognizes_expected_events_only() {
        JacocoReporter reporter = new JacocoReporter();
        assertThat(reporter.acceptType(Event.NODE_RUN_FINISHED)).isTrue();
        assertThat(reporter.acceptType(Event.AFTER_WRITE_OUTPUT_FILES)).isTrue();

        // Some negative checks
        assertThat(reporter.acceptType(Event.NODE_RUN_STARTED)).isFalse();
        assertThat(reporter.acceptType(Event.PLAN_RUN_STARTED)).isFalse();
        assertThat(reporter.acceptType("SOME_UNKNOWN_EVENT")).isFalse();
    }

    @Test
    public void eventReceived_does_nothing_when_data_is_null() {
        assertThatNoException().isThrownBy(() -> LOGGER.info("OK"));
        JacocoReporter reporter = new JacocoReporter();
        // Should not throw on null data

        reporter.eventReceived(new Event(Event.NODE_RUN_FINISHED, Instant.now(), null));
    }

    @Test
    public void event_with_test_case_triggers_dump_only_when_no_xml_or_csv() throws Exception {
        // Arrange reporter
        JacocoReporter reporter = new JacocoReporter();
        reporter.setHost("localhost");
        reporter.setPort("6300");
        reporter.setRetries(5);
        Path out = Files.createTempDirectory("jacoco-out");
        reporter.setOutput(out);
        reporter.setClasses(Files.createTempDirectory("classes"));
        reporter.setSources(Files.createTempDirectory("sources"));
        reporter.setName("Report");

        // Create mocks
        ExecDumpClient dumpClient = mock(ExecDumpClient.class);
        ExecFileLoader dumpLoader = mock(ExecFileLoader.class);
        when(dumpClient.dump(anyString(), anyInt())).thenReturn(dumpLoader);

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
        verify(dumpLoader).save(argThat(f -> f.getName().equals("TC-1.exec")), eq(true));
        // And ensure per-test loader (distinct field) was not used (kept null)
        Object fileLoaderField = getPrivate(reporter, "fileLoader");
        assertThat(fileLoaderField).isNull();
    }

    @Test
    public void event_with_test_case_and_xml_triggers_execute_single_and_produces_xml() throws Exception {
        // Arrange temporary filesystem
        Path out = Files.createTempDirectory("jacoco-out");
        Path xml = Files.createTempDirectory("jacoco-xml");
        Path classes = Files.createTempDirectory("jacoco-classes");
        Path sources = Files.createTempDirectory("jacoco-sources");
        temporaries.addAll(List.of(out, xml, classes, sources));

        // Reporter with configuration
        JacocoReporter reporter = new JacocoReporter();
        reporter.setHost("127.0.0.1");
        reporter.setPort("6300");
        reporter.setRetries(1);
        reporter.setOutput(out);
        reporter.setXml(xml);
        reporter.setClasses(classes);
        reporter.setSources(sources);
        reporter.setTabwidth(4);
        reporter.setName("Report");

        // Mock dump client that writes the dumped exec file
        ExecDumpClient dumpClient = mock(ExecDumpClient.class);
        ExecFileLoader dumpLoader = mock(ExecFileLoader.class);
        when(dumpClient.dump(anyString(), anyInt())).thenReturn(dumpLoader);
        // When save is called, ensure the file exists so executeSingle can load it
        doAnswer(inv -> {
            File f = inv.getArgument(0);
            f.getParentFile().mkdirs();
            f.createNewFile();
            return null;
        }).when(dumpLoader).save(any(File.class), anyBoolean());
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
        assertThat(Files.exists(producedXml)).isTrue();
    }

    private static void setPrivate(Object target, String field, Object value) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getPrivate(Object target, String field) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(target);
    }
}
