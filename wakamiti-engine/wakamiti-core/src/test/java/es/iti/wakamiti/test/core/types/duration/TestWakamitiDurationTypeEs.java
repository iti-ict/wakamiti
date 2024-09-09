/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.core.types.duration;


import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.core.datatypes.duration.WakamitiDurationType;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("unchecked")
public class TestWakamitiDurationTypeEs {

    private final Locale locale = Locale.forLanguageTag("es");

    @Test
    public void testDurations() {
        WakamitiDataType<Duration> type = (WakamitiDataType<Duration>) new WakamitiDurationType().contributeTypes().get(0);
        Map<String, Duration> exp = new LinkedHashMap<>();
        exp.put("1 nanosegundo", Duration.ofNanos(1));
        exp.put("2 nanosegundos", Duration.ofNanos(2));
        exp.put("-2 nanosegundos", Duration.ofNanos(2));
        exp.put("1 microsegundo", Duration.of(1, ChronoUnit.MICROS));
        exp.put("2 microsegundos", Duration.of(2, ChronoUnit.MICROS));
        exp.put("-2 microsegundos", Duration.of(2, ChronoUnit.MICROS));
        exp.put("1 milisegundo", Duration.ofMillis(1));
        exp.put("2 milisegundos", Duration.ofMillis(2));
        exp.put("-2 milisegundos", Duration.ofMillis(2));
        exp.put("1 segundo", Duration.ofSeconds(1));
        exp.put("2 segundos", Duration.ofSeconds(2));
        exp.put("-2 segundos", Duration.ofSeconds(2));
        exp.put("1 minuto", Duration.ofMinutes(1));
        exp.put("2 minutos", Duration.ofMinutes(2));
        exp.put("-2 minutos", Duration.ofMinutes(2));
        exp.put("1 hora", Duration.ofHours(1));
        exp.put("2 horas", Duration.ofHours(2));
        exp.put("-2 horas", Duration.ofHours(2));
        exp.put("1 día", Duration.ofDays(1));
        exp.put("2 días", Duration.ofDays(2));
        exp.put("-2 días", Duration.ofDays(2));

        for (Map.Entry<String, Duration> e : exp.entrySet()) {
            Duration duration = type.parse(locale, e.getKey());
            assertThat(duration).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(duration)
                    .as("failed match for: " + e.getKey() + " with " + e.getValue())
                    .isEqualTo(e.getValue());
        }

    }
}
