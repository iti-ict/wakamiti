/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.imconfig.Configuration;


public class JacocoConfigTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static Object getField(
            Object target,
            String name
    ) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void defaultConfigurationHasExpectedDefaults() {
        JacocoConfig config = new JacocoConfig();
        Configuration defaults = config.defaultConfiguration();

        assertThat(defaults.getList(JacocoConfig.JACOCO_HOSTS, String.class))
                .containsExactly("localhost:6300");
        assertThat(defaults.get(JacocoConfig.JACOCO_RETRIES, String.class)).hasValue("10");
        assertThat(defaults.get(JacocoConfig.JACOCO_OUTPUT, String.class)).hasValue(".");
        assertThat(defaults.get(JacocoConfig.JACOCO_TABWITH, String.class)).hasValue("4");
        assertThat(defaults.get(JacocoConfig.JACOCO_NAME, String.class)).hasValue("JaCoCo Coverage Report");
        assertThat(defaults.get(JacocoConfig.JACOCO_MERGE, Boolean.class)).hasValue(true);
    }

    @Test
    public void configureSetsAllKnownPropertiesAndRequiresClasses() throws IOException {
        JacocoConfig config = new JacocoConfig();
        JacocoReporter reporter = new JacocoReporter();

        Path output = temporaryFolder.newFolder("out").toPath();
        Path xml = temporaryFolder.newFolder("xml").toPath();
        Path csv = temporaryFolder.newFolder("csv").toPath();
        Path html = temporaryFolder.newFolder("html").toPath();
        List<Path> classes = List.of(
                temporaryFolder.newFolder("classes").toPath(),
                temporaryFolder.newFolder("generated-classes").toPath()
        );
        List<Path> sources = List.of(Path.of("src/main/java"));

        Configuration cfg = Configuration.factory().fromMap(Map.ofEntries(
                Map.entry(JacocoConfig.JACOCO_HOSTS, List.of("127.0.0.1:1234", "jacoco-agent:6300")),
                Map.entry(JacocoConfig.JACOCO_RETRIES, "3"),
                Map.entry(JacocoConfig.JACOCO_OUTPUT, output.toString()),
                Map.entry(JacocoConfig.JACOCO_XML, xml.toString()),
                Map.entry(JacocoConfig.JACOCO_CSV, csv.toString()),
                Map.entry(JacocoConfig.JACOCO_HTML, html.toString()),
                Map.entry(JacocoConfig.JACOCO_CLASSES, classes.stream().map(Path::toString).toList()),
                Map.entry(JacocoConfig.JACOCO_SOURCES, "src/main/java"),
                Map.entry(JacocoConfig.JACOCO_TABWITH, "2"),
                Map.entry(JacocoConfig.JACOCO_NAME, "My Report"),
                Map.entry(JacocoConfig.JACOCO_MERGE, "false")
        ));

        config.configurer().configure(reporter, cfg);

        assertThat(getField(reporter, "hosts"))
                .isEqualTo(List.of("127.0.0.1:1234", "jacoco-agent:6300"));
        assertThat(getField(reporter, "retries")).isEqualTo(3);
        assertThat(getField(reporter, "output")).isEqualTo(output);
        assertThat(getField(reporter, "xml")).isEqualTo(xml);
        assertThat(getField(reporter, "csv")).isEqualTo(csv);
        assertThat(getField(reporter, "html")).isEqualTo(html);
        assertThat(getField(reporter, "classes")).isEqualTo(classes);
        assertThat(getField(reporter, "sources")).isEqualTo(sources);
        assertThat(getField(reporter, "tabwidth")).isEqualTo(2);
        assertThat(getField(reporter, "name")).isEqualTo("My Report");
        assertThat(getField(reporter, "merge")).isEqualTo(false);
    }

    @Test
    public void configureThrowsWhenRequiredClassesMissing() {
        JacocoConfig config = new JacocoConfig();
        JacocoReporter reporter = new JacocoReporter();

        // Intentionally omit JACOCO_CLASSES
        Map<String, Object> map = Map.of(JacocoConfig.JACOCO_HOSTS, "localhost:6300");
        Configuration cfg = Configuration.factory().fromMap(map);

        assertThatThrownBy(() -> config.configurer().configure(reporter, cfg))
                .isInstanceOf(WakamitiException.class)
                .hasMessageContaining("Property '" + JacocoConfig.JACOCO_CLASSES + "' is required");
    }

    @Test
    public void configureThrowsWhenClassesAreEmpty() {
        Configuration cfg = Configuration.factory().fromMap(Map.of(
                JacocoConfig.JACOCO_HOSTS, "localhost:6300",
                JacocoConfig.JACOCO_CLASSES, List.of()
        ));

        assertThatThrownBy(() -> new JacocoConfig().configurer().configure(new JacocoReporter(), cfg))
                .isInstanceOf(WakamitiException.class)
                .hasMessageContaining("Property '" + JacocoConfig.JACOCO_CLASSES + "' is required");
    }

    @Test
    public void configureThrowsWhenHostsAreEmpty() throws IOException {
        Configuration cfg = Configuration.factory().fromMap(Map.of(
                JacocoConfig.JACOCO_HOSTS, List.of(),
                JacocoConfig.JACOCO_CLASSES, classesDirectory().toString()
        ));

        assertThatThrownBy(() -> new JacocoConfig().configurer().configure(new JacocoReporter(), cfg))
                .isInstanceOf(WakamitiException.class)
                .hasMessageContaining("requires at least one host");
    }

    @Test
    public void configureRejectsInvalidHosts() throws IOException {
        List<String> invalidHosts = List.of(
                "localhost", ":6300", "localhost:", "localhost:port",
                "localhost:0", "localhost:65536", "localhost:6300:extra",
                "local host:6300", "[::1]:6300", "-host:6300", "256.10.10.10:6300"
        );
        Path classes = classesDirectory();

        for (String invalidHost : invalidHosts) {
            Configuration cfg = Configuration.factory().fromMap(Map.of(
                    JacocoConfig.JACOCO_HOSTS, invalidHost,
                    JacocoConfig.JACOCO_CLASSES, classes.toString()
            ));

            assertThatThrownBy(() -> new JacocoConfig().configurer().configure(new JacocoReporter(), cfg))
                    .as("Host %s should be rejected", invalidHost)
                    .isInstanceOf(WakamitiException.class)
                    .hasMessageContaining(JacocoConfig.JACOCO_HOSTS)
                    .hasMessageContaining("host:port");
        }
    }

    @Test
    public void configureAcceptsBoundaryPortsAndDnsOrIpv4Hosts() throws IOException {
        Path classes = classesDirectory();
        Configuration cfg = Configuration.factory().fromMap(Map.of(
                JacocoConfig.JACOCO_HOSTS, List.of("localhost:1", "agent.example.org:65535", "10.0.0.1:6300"),
                JacocoConfig.JACOCO_CLASSES, classes.toString()
        ));
        JacocoReporter reporter = new JacocoReporter();

        new JacocoConfig().configurer().configure(reporter, cfg);

        assertThat(getField(reporter, "hosts")).isEqualTo(
                List.of("localhost:1", "agent.example.org:65535", "10.0.0.1:6300")
        );
        assertThat(getField(reporter, "classes")).isEqualTo(List.of(classes));
    }

    @Test
    public void configureAcceptsOutputPathsBeforeTheyExist() throws IOException {
        Path root = temporaryFolder.getRoot().toPath();
        Path output = root.resolve("out");
        Path xml = root.resolve("xml");
        Path csv = root.resolve("csv");
        Path html = root.resolve("html");
        JacocoReporter reporter = new JacocoReporter();
        Configuration cfg = Configuration.factory().fromMap(Map.ofEntries(
                Map.entry(JacocoConfig.JACOCO_HOSTS, "localhost:6300"),
                Map.entry(JacocoConfig.JACOCO_CLASSES, classesDirectory().toString()),
                Map.entry(JacocoConfig.JACOCO_OUTPUT, output.toString()),
                Map.entry(JacocoConfig.JACOCO_XML, xml.toString()),
                Map.entry(JacocoConfig.JACOCO_CSV, csv.toString()),
                Map.entry(JacocoConfig.JACOCO_HTML, html.toString())
        ));

        new JacocoConfig().configurer().configure(reporter, cfg);

        assertThat(output).doesNotExist();
        assertThat(xml).doesNotExist();
        assertThat(csv).doesNotExist();
        assertThat(html).doesNotExist();
        assertThat(getField(reporter, "output")).isEqualTo(output);
        assertThat(getField(reporter, "xml")).isEqualTo(xml);
        assertThat(getField(reporter, "csv")).isEqualTo(csv);
        assertThat(getField(reporter, "html")).isEqualTo(html);
    }

    @Test
    public void configureRejectsMissingOrNonDirectoryClassRoots() throws IOException {
        Path validClasses = classesDirectory();
        List<Path> invalidClasses = List.of(
                temporaryFolder.getRoot().toPath().resolve("missing-classes"),
                temporaryFolder.newFile("classes-file").toPath()
        );

        for (Path invalidClassesRoot : invalidClasses) {
            Configuration cfg = Configuration.factory().fromMap(Map.of(
                    JacocoConfig.JACOCO_HOSTS, "localhost:6300",
                    JacocoConfig.JACOCO_CLASSES, List.of(validClasses.toString(), invalidClassesRoot.toString())
            ));

            assertThatThrownBy(() -> new JacocoConfig().configurer().configure(new JacocoReporter(), cfg))
                    .isInstanceOf(WakamitiException.class)
                    .hasMessageContaining(JacocoConfig.JACOCO_CLASSES)
                    .hasMessageContaining(invalidClassesRoot.toString())
                    .hasMessageContaining("existing directory");
        }
    }

    private Path classesDirectory() throws IOException {
        return temporaryFolder.newFolder("classes").toPath();
    }

}
