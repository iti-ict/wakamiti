/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.core.types;


import es.iti.wakamiti.core.datatypes.WakamitiDateDataType;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;


/**
 * Since JRE 9, the default date/time format has changed in order to honour CLDR,
 * being the main difference that a comma is required between date and hour, whereas no
 * comma is used in previous JRE versions.
 *
 * @see https://www.oracle.com/technetwork/java/javase/9-relnote-issues-3704069.html#JDK-8008577
 * @see http://openjdk.java.net/jeps/252
 * <p>
 * These tests are targeted to JRE >=9.
 */
public class TestWakamitiDateTypeEn {

    private static final Locale LOCALE = Locale.ENGLISH;
    private static final WakamitiDateDataType<LocalDate> DATE_TYPE = new WakamitiDateDataType<>(
            "date", LocalDate.class, true, false, LocalDate::from
    );
    private static final WakamitiDateDataType<LocalTime> TIME_TYPE = new WakamitiDateDataType<>(
            "time", LocalTime.class, false, true, LocalTime::from
    );
    private static final WakamitiDateDataType<LocalDateTime> DATETIME_TYPE = new WakamitiDateDataType<>(
            "datetime", LocalDateTime.class, true, true, LocalDateTime::from
    );
    @ClassRule
    public static JavaVersionRule javaVersionRule = new JavaVersionRule(version -> version >= 9);

    @Test
    public void testLocalizedDate1() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "5/30/18").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "5/30/18"))
                .isEqualTo(LocalDate.of(2018, 5, 30));
    }


    @Test
    public void testLocalizedDate2() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "Jan 30, 2018").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "Jan 30, 2018"))
                .isEqualTo(LocalDate.of(2018, 1, 30));
    }


    @Test
    public void testLocalizedDate3() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "January 30, 2018").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "January 30, 2018"))
                .isEqualTo(LocalDate.of(2018, 1, 30));
    }


    @Test
    public void testLocalizedDate4() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "Tuesday, January 30, 2018").matches())
                .isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "Tuesday, January 30, 2018"))
                .isEqualTo(LocalDate.of(2018, 1, 30));
    }


    @Test
    public void testLocalizedDate5() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "5999/30/18").matches()).isFalse();
    }


    @Test
    public void testLocalizedTime1() {
        Assertions.assertThat(TIME_TYPE.matcher(LOCALE, "5:35 PM").matches()).isTrue();
        Assertions.assertThat(TIME_TYPE.parse(LOCALE, "5:35 PM")).isEqualTo(LocalTime.of(17, 35));
    }


    @Test
    public void testLocalizedTime2() {
        Assertions.assertThat(TIME_TYPE.matcher(LOCALE, "11:35 PM").matches()).isTrue();
        Assertions.assertThat(TIME_TYPE.parse(LOCALE, "11:35 PM")).isEqualTo(LocalTime.of(23, 35));
    }


    @Test
    public void testLocalizedTime3() {
        Assertions.assertThat(TIME_TYPE.matcher(LOCALE, "555:66").matches()).isFalse();
    }


    @Test
    public void testLocalizedDateTime1() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "5/30/18, 5:35 PM").matches()).isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "5/30/18, 5:35 PM"))
                .isEqualTo(LocalDateTime.of(2018, 5, 30, 17, 35));
    }


    @Test
    public void testLocalizedDateTime2() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "Jan 30, 2018, 5:35 PM").matches())
                .isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "Jan 30, 2018, 5:35 PM"))
                .isEqualTo(LocalDateTime.of(2018, 1, 30, 17, 35));
    }


    @Test
    public void testLocalizedDateTime3() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "January 30, 2018, 5:35 PM").matches())
                .isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "January 30, 2018, 5:35 PM"))
                .isEqualTo(LocalDateTime.of(2018, 1, 30, 17, 35));
    }


    @Test
    public void testLocalizedDateTime4() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "January 30, 2018, 5:35 PM").matches())
                .isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "Tuesday, January 30, 2018, 5:35 PM"))
                .isEqualTo(LocalDateTime.of(2018, 1, 30, 17, 35));
    }


    @Test
    public void testLocalizedDateTime5() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "5999/30/18 555:66").matches())
                .isFalse();
    }

}