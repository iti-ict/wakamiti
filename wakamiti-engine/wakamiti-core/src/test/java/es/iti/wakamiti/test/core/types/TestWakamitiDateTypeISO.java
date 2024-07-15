/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.core.types;


import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.core.datatypes.WakamitiDateDataType;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class TestWakamitiDateTypeISO {

    private static final WakamitiDataType<LocalDate> DATE_TYPE = new WakamitiDateDataType<>(
            "date", LocalDate.class);
    private static final WakamitiDataType<LocalTime> TIME_TYPE = new WakamitiDateDataType<>(
            "time", LocalTime.class);
    private static final WakamitiDataType<LocalDateTime> DATETIME_TYPE = new WakamitiDateDataType<>(
            "datetime", LocalDateTime.class);

    private static final List<Locale> testLocales = Arrays.asList(
            Locale.CANADA, Locale.CHINESE, Locale.ENGLISH, Locale.JAPANESE, Locale.FRENCH, Locale.GERMAN, Locale.forLanguageTag("es")
    );

    @Test
    public void testISODate() {
        // ISO date should be accepted by any locale
        for (Locale locale : testLocales) {
            Assertions.assertThat(DATE_TYPE.matcher(locale, "2018-05-30").matches()).isTrue();
            Assertions.assertThat(DATE_TYPE.parse(locale, "2018-05-30"))
                    .isEqualTo(LocalDate.of(2018, 5, 30));
        }
    }


    @Test
    public void testISOTime() {
        // ISO time should be accepted by any locale
        for (Locale locale : testLocales) {
            Assertions.assertThat(TIME_TYPE.matcher(locale, "17:35").matches()).isTrue();
            Assertions.assertThat(TIME_TYPE.parse(locale, "17:35")).isEqualTo(LocalTime.of(17, 35));
            Assertions.assertThat(TIME_TYPE.matcher(locale, "17:35:29").matches()).isTrue();
            Assertions.assertThat(TIME_TYPE.parse(locale, "17:35:29"))
                    .isEqualTo(LocalTime.of(17, 35, 29));
            Assertions.assertThat(TIME_TYPE.matcher(locale, "17:35:29.743").matches()).isTrue();
            Assertions.assertThat(TIME_TYPE.parse(locale, "17:35:29.743"))
                    .isEqualTo(LocalTime.of(17, 35, 29, 743000000));
        }
    }


    @Test
    public void testISODateTime() {
        // ISO time should be accepted by any locale
        for (Locale locale : testLocales) {
            Assertions.assertThat(DATETIME_TYPE.matcher(locale, "2018-05-30T17:35").matches())
                    .isTrue();
            Assertions.assertThat(DATETIME_TYPE.parse(locale, "2018-05-30T17:35"))
                    .isEqualTo(LocalDateTime.of(2018, 5, 30, 17, 35));
            Assertions.assertThat(DATETIME_TYPE.matcher(locale, "2018-05-30T17:35:29").matches())
                    .isTrue();
            Assertions.assertThat(DATETIME_TYPE.parse(locale, "2018-05-30T17:35:29"))
                    .isEqualTo(LocalDateTime.of(2018, 5, 30, 17, 35, 29));
            Assertions.assertThat(
                    DATETIME_TYPE.matcher(locale, "2018-05-30T17:35:29.743").matches()
            ).isTrue();
            Assertions.assertThat(DATETIME_TYPE.parse(locale, "2018-05-30T17:35:29.743"))
                    .isEqualTo(LocalDateTime.of(2018, 5, 30, 17, 35, 29, 743000000));
        }
    }

}