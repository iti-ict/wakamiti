/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter.datatypes;


import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.jmeter.Metric;
import org.junit.Test;
import us.abstracta.jmeter.javadsl.core.stats.CountMetricSummary;
import us.abstracta.jmeter.javadsl.core.stats.StatsSummary;
import us.abstracta.jmeter.javadsl.core.stats.TimeMetricSummary;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SuppressWarnings("unchecked")
public class WakamitiMetricTypesTest {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es");
    private static final Locale LOCALE_EN = Locale.ENGLISH;

    private final WakamitiMetricTypes types = new WakamitiMetricTypes();

    @Test
    public void testDurationMetricWhenEN() {
        WakamitiDataType<Metric<?>> type = (WakamitiDataType<Metric<?>>) types.contributeTypes().get(0);
        Map<String, Duration> exp = new LinkedHashMap<>();
        exp.put("minimum duration", Duration.of(1, ChronoUnit.SECONDS));
        exp.put("maximum duration", Duration.of(1, ChronoUnit.MINUTES));
        exp.put("average duration", Duration.of(2, ChronoUnit.SECONDS));
        exp.put("median", Duration.of(5, ChronoUnit.SECONDS));
        exp.put("90th percentile", Duration.of(10, ChronoUnit.SECONDS));
        exp.put("95th percentile", Duration.of(30, ChronoUnit.SECONDS));
        exp.put("99th percentile", Duration.of(50, ChronoUnit.SECONDS));

        for (Map.Entry<String, Duration> e : exp.entrySet()) {
            Metric<?> metric = type.parse(LOCALE_EN, e.getKey());
            assertThat(metric).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(metric.apply(summary()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue())
                    .isEqualTo(e.getValue());
        }
    }

    @Test
    public void testDurationMetricWhenES() {
        WakamitiDataType<Metric<?>> type = (WakamitiDataType<Metric<?>>) types.contributeTypes().get(0);
        Map<String, Duration> exp = new LinkedHashMap<>();
        exp.put("duración mínima", Duration.of(1, ChronoUnit.SECONDS));
        exp.put("duración máxima", Duration.of(1, ChronoUnit.MINUTES));
        exp.put("duración media", Duration.of(2, ChronoUnit.SECONDS));
        exp.put("duración promedio", Duration.of(2, ChronoUnit.SECONDS));
        exp.put("mediana", Duration.of(5, ChronoUnit.SECONDS));
        exp.put("percentil 90", Duration.of(10, ChronoUnit.SECONDS));
        exp.put("percentil 95", Duration.of(30, ChronoUnit.SECONDS));
        exp.put("percentil 99", Duration.of(50, ChronoUnit.SECONDS));

        for (Map.Entry<String, Duration> e : exp.entrySet()) {
            Metric<?> metric = type.parse(LOCALE_ES, e.getKey());
            assertThat(metric).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(metric.apply(summary()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue())
                    .isEqualTo(e.getValue());
        }
    }

    @Test
    public void testLongMetricWhenEN() {
        WakamitiDataType<Metric<?>> type = (WakamitiDataType<Metric<?>>) types.contributeTypes().get(1);
        Map<String, Long> exp = new LinkedHashMap<>();
        exp.put("number of samples", 100L);
        exp.put("number of errors", 50L);
        exp.put("number of bytes received", 1000L);
        exp.put("number of bytes sent", 500L);

        for (Map.Entry<String, Long> e : exp.entrySet()) {
            Metric<?> metric = type.parse(LOCALE_EN, e.getKey());
            assertThat(metric).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(metric.apply(summary()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue())
                    .isEqualTo(e.getValue());
        }
    }

    @Test
    public void testLongMetricWhenES() {
        WakamitiDataType<Metric<?>> type = (WakamitiDataType<Metric<?>>) types.contributeTypes().get(1);
        Map<String, Long> exp = new LinkedHashMap<>();
        exp.put("número de muestras", 100L);
        exp.put("número de errores", 50L);
        exp.put("número de bytes recibidos", 1000L);
        exp.put("número de bytes enviados", 500L);

        for (Map.Entry<String, Long> e : exp.entrySet()) {
            Metric<?> metric = type.parse(LOCALE_ES, e.getKey());
            assertThat(metric).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(metric.apply(summary()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue())
                    .isEqualTo(e.getValue());
        }
    }

    @Test
    public void testDoubleMetricWhenEN() {
        WakamitiDataType<Metric<?>> type = (WakamitiDataType<Metric<?>>) types.contributeTypes().get(2);
        Map<String, Double> exp = new LinkedHashMap<>();
        exp.put("number of samples per second", 1.5);
        exp.put("number of errors per second", 0.5);
        exp.put("number of bytes received per second", 10.5);
        exp.put("number of bytes sent per second", 5.2);

        for (Map.Entry<String, Double> e : exp.entrySet()) {
            Metric<?> metric = type.parse(LOCALE_EN, e.getKey());
            assertThat(metric).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(metric.apply(summary()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue())
                    .isEqualTo(e.getValue());
        }
    }

    @Test
    public void testDoubleMetricWhenES() {
        WakamitiDataType<Metric<?>> type = (WakamitiDataType<Metric<?>>) types.contributeTypes().get(2);
        Map<String, Double> exp = new LinkedHashMap<>();
        exp.put("número de muestras por segundo", 1.5);
        exp.put("número de errores por segundo", 0.5);
        exp.put("número de bytes recibidos por segundo", 10.5);
        exp.put("número de bytes enviados por segundo", 5.2);

        for (Map.Entry<String, Double> e : exp.entrySet()) {
            Metric<?> metric = type.parse(LOCALE_ES, e.getKey());
            assertThat(metric).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(metric.apply(summary()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue())
                    .isEqualTo(e.getValue());
        }
    }

    private static StatsSummary summary() {
        StatsSummary summary = mock(StatsSummary.class);
        TimeMetricSummary ts = mock(TimeMetricSummary.class);

        when(summary.sampleTime()).thenReturn(ts);
        when(summary.samples()).then(a -> {
            CountMetricSummary cs = mock(CountMetricSummary.class);
            when(cs.total()).thenReturn(100L);
            when(cs.perSecond()).thenReturn(1.5);
            return cs;
        });
        when(summary.errors()).then(a -> {
            CountMetricSummary cs = mock(CountMetricSummary.class);
            when(cs.total()).thenReturn(50L);
            when(cs.perSecond()).thenReturn(0.5);
            return cs;
        });
        when(summary.receivedBytes()).then(a -> {
            CountMetricSummary cs = mock(CountMetricSummary.class);
            when(cs.total()).thenReturn(1000L);
            when(cs.perSecond()).thenReturn(10.5);
            return cs;
        });
        when(summary.sentBytes()).then(a -> {
            CountMetricSummary cs = mock(CountMetricSummary.class);
            when(cs.total()).thenReturn(500L);
            when(cs.perSecond()).thenReturn(5.2);
            return cs;
        });

        when(ts.min()).thenReturn(Duration.of(1, ChronoUnit.SECONDS));
        when(ts.max()).thenReturn(Duration.of(1, ChronoUnit.MINUTES));
        when(ts.mean()).thenReturn(Duration.of(2, ChronoUnit.SECONDS));
        when(ts.median()).thenReturn(Duration.of(5, ChronoUnit.SECONDS));
        when(ts.perc90()).thenReturn(Duration.of(10, ChronoUnit.SECONDS));
        when(ts.perc95()).thenReturn(Duration.of(30, ChronoUnit.SECONDS));
        when(ts.perc99()).thenReturn(Duration.of(50, ChronoUnit.SECONDS));

        return summary;
    }
}
