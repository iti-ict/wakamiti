/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.test.core.types;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import iti.kukumo.api.KukumoDataType;
import iti.kukumo.core.datatypes.KukumoDateDataType;


public class TestKukumoDateTypeISO {

    private static final KukumoDataType<LocalDate> DATE_TYPE = new KukumoDateDataType<>(
        "date", LocalDate.class, true, false, LocalDate::from
    );
    private static final KukumoDataType<LocalTime> TIME_TYPE = new KukumoDateDataType<>(
        "time", LocalTime.class, false, true, LocalTime::from
    );
    private static final KukumoDataType<LocalDateTime> DATETIME_TYPE = new KukumoDateDataType<>(
        "datetime", LocalDateTime.class, true, true, LocalDateTime::from
    );


    @Test
    public void testISODate() {
        // ISO date should be accepted by any locale
        for (Locale locale : Locale.getAvailableLocales()) {
            Assertions.assertThat(DATE_TYPE.matcher(locale, "2018-05-30").matches()).isTrue();
            Assertions.assertThat(DATE_TYPE.parse(locale, "2018-05-30"))
                .isEqualTo(LocalDate.of(2018, 5, 30));
        }
    }


    @Test
    public void testISOTime() {
        // ISO time should be accepted by any locale
        for (Locale locale : Locale.getAvailableLocales()) {
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
        for (Locale locale : Locale.getAvailableLocales()) {
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
