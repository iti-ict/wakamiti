/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.imconfig.Configuration;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class JacocoConfigTest {

    private static Object getField(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void defaultConfiguration_has_expected_defaults() {
        JacocoConfig config = new JacocoConfig();
        Configuration defaults = config.defaultConfiguration();

        assertThat(defaults.get(JacocoConfig.JACOCO_HOST, String.class)).hasValue("localhost");
        assertThat(defaults.get(JacocoConfig.JACOCO_PORT, String.class)).hasValue("6300");
        assertThat(defaults.get(JacocoConfig.JACOCO_RETRIES, String.class)).hasValue("10");
        assertThat(defaults.get(JacocoConfig.JACOCO_OUTPUT, String.class)).hasValue(".");
        assertThat(defaults.get(JacocoConfig.JACOCO_TABWITH, String.class)).hasValue("4");
        assertThat(defaults.get(JacocoConfig.JACOCO_NAME, String.class)).hasValue("JaCoCo Coverage Report");
    }

    @Test
    public void configure_sets_all_known_properties_and_requires_classes() {
        JacocoConfig config = new JacocoConfig();
        JacocoReporter reporter = new JacocoReporter();

        Path output = Path.of("build/out");
        Path xml = Path.of("build/xml");
        Path csv = Path.of("build/csv");
        Path html = Path.of("build/html");
        Path classes = Path.of("build/classes");
        Path sources = Path.of("src/main/java");

        Configuration cfg = Configuration.factory().fromPairs(
                JacocoConfig.JACOCO_HOST, "127.0.0.1",
                JacocoConfig.JACOCO_PORT, "1234",
                JacocoConfig.JACOCO_RETRIES, "3",
                JacocoConfig.JACOCO_OUTPUT, "build/out",
                JacocoConfig.JACOCO_XML, "build/xml",
                JacocoConfig.JACOCO_CSV, "build/csv",
                JacocoConfig.JACOCO_HTML, "build/html",
                JacocoConfig.JACOCO_CLASSES, "build/classes",
                JacocoConfig.JACOCO_SOURCES, "src/main/java",
                JacocoConfig.JACOCO_TABWITH, "2",
                JacocoConfig.JACOCO_NAME, "My Report"
        );

        config.configurer().configure(reporter, cfg);

        assertThat(getField(reporter, "host")).isEqualTo("127.0.0.1");
        assertThat(getField(reporter, "port")).isEqualTo("1234");
        assertThat(getField(reporter, "retries")).isEqualTo(3);
        assertThat(getField(reporter, "output")).isEqualTo(output);
        assertThat(getField(reporter, "xml")).isEqualTo(xml);
        assertThat(getField(reporter, "csv")).isEqualTo(csv);
        assertThat(getField(reporter, "html")).isEqualTo(html);
        assertThat(getField(reporter, "classes")).isEqualTo(classes);
        assertThat(getField(reporter, "sources")).isEqualTo(sources);
        assertThat(getField(reporter, "tabwidth")).isEqualTo(2);
        assertThat(getField(reporter, "name")).isEqualTo("My Report");
    }

    @Test
    public void configure_throws_when_required_classes_missing() {
        JacocoConfig config = new JacocoConfig();
        JacocoReporter reporter = new JacocoReporter();

        // Intentionally omit JACOCO_CLASSES
        Map<String, Object> map = Map.of(
                JacocoConfig.JACOCO_HOST, "localhost"
        );
        Configuration cfg = Configuration.factory().fromMap(map);

        assertThatThrownBy(() -> config.configurer().configure(reporter, cfg))
                .isInstanceOf(WakamitiException.class)
                .hasMessageContaining("Property '" + JacocoConfig.JACOCO_CLASSES + "' is required");
    }
}
