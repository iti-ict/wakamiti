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
public class TestWakamitiDurationTypeEn {

    private static final Locale locale = Locale.ENGLISH;

    @Test
    public void testDurations() {
        WakamitiDataType<Duration> type = (WakamitiDataType<Duration>) new WakamitiDurationType().contributeTypes().get(0);
        Map<String, Duration> exp = new LinkedHashMap<>();
        exp.put("1 nanosecond", Duration.ofNanos(1));
        exp.put("2 nanoseconds", Duration.ofNanos(2));
        exp.put("-2 nanoseconds", Duration.ofNanos(2));
        exp.put("1 microsecond", Duration.of(1, ChronoUnit.MICROS));
        exp.put("2 microseconds", Duration.of(2, ChronoUnit.MICROS));
        exp.put("-2 microseconds", Duration.of(2, ChronoUnit.MICROS));
        exp.put("1 millisecond", Duration.ofMillis(1));
        exp.put("2 milliseconds", Duration.ofMillis(2));
        exp.put("-2 milliseconds", Duration.ofMillis(2));
        exp.put("1 second", Duration.ofSeconds(1));
        exp.put("2 seconds", Duration.ofSeconds(2));
        exp.put("-2 seconds", Duration.ofSeconds(2));
        exp.put("1 minute", Duration.ofMinutes(1));
        exp.put("2 minutes", Duration.ofMinutes(2));
        exp.put("-2 minutes", Duration.ofMinutes(2));
        exp.put("1 hour", Duration.ofHours(1));
        exp.put("2 hours", Duration.ofHours(2));
        exp.put("-2 hours", Duration.ofHours(2));
        exp.put("1 day", Duration.ofDays(1));
        exp.put("2 days", Duration.ofDays(2));
        exp.put("-2 days", Duration.ofDays(2));

        for (Map.Entry<String, Duration> e : exp.entrySet()) {
            Duration duration = type.parse(locale, e.getKey());
            assertThat(duration).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(duration)
                    .as("failed match for: " + e.getKey() + " with " + e.getValue())
                    .isEqualTo(e.getValue());
        }

    }

}
