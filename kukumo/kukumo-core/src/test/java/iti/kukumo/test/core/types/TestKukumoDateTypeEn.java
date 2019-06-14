package iti.kukumo.test.core.types;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import iti.kukumo.api.KukumoDataType;
import iti.kukumo.core.datatypes.KukumoDateDataType;

public class TestKukumoDateTypeEn {

    private static final Locale LOCALE = Locale.ENGLISH;
    private static final KukumoDataType<LocalDate> DATE_TYPE = new KukumoDateDataType<>("date", LocalDate.class, true, false, LocalDate::from);
    private static final KukumoDataType<LocalTime> TIME_TYPE = new KukumoDateDataType<>("time", LocalTime.class, false, true, LocalTime::from);
    private static final KukumoDataType<LocalDateTime> DATETIME_TYPE = new KukumoDateDataType<>("datetime", LocalDateTime.class, true, true, LocalDateTime::from);


    @Test
    public void testLocalizedDate1() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "5/30/18").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "5/30/18")).isEqualTo(LocalDate.of(2018, 5, 30));
    }

    @Test
    public void testLocalizedDate2() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "Jan 30, 2018").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "Jan 30, 2018")).isEqualTo(LocalDate.of(2018, 1, 30));
    }

    @Test
    public void testLocalizedDate3() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "January 30, 2018").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "January 30, 2018")).isEqualTo(LocalDate.of(2018, 1, 30));
    }

    @Test
    public void testLocalizedDate4() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "Tuesday, January 30, 2018").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "Tuesday, January 30, 2018")).isEqualTo(LocalDate.of(2018, 1, 30));
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
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "5/30/18 5:35 PM").matches()).isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "5/30/18 5:35 PM")).isEqualTo(LocalDateTime.of(2018, 5, 30, 17, 35));
    }

    @Test
    public void testLocalizedDateTime2() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "Jan 30, 2018 5:35 PM").matches()).isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "Jan 30, 2018 5:35 PM")).isEqualTo(LocalDateTime.of(2018, 1, 30, 17, 35));
    }

    @Test
    public void testLocalizedDateTime3() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "January 30, 2018 5:35 PM").matches()).isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "January 30, 2018 5:35 PM")).isEqualTo(LocalDateTime.of(2018, 1, 30, 17, 35));
    }

    @Test
    public void testLocalizedDateTime4() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "January 30, 2018 5:35 PM").matches()).isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "Tuesday, January 30, 2018 5:35 PM")).isEqualTo(LocalDateTime.of(2018, 1, 30, 17, 35));
    }

    @Test
    public void testLocalizedDateTime5() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "5999/30/18 555:66").matches()).isFalse();
    }


}
