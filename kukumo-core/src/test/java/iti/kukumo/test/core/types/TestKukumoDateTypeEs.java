package iti.kukumo.test.core.types;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import iti.kukumo.api.KukumoDataType;
import iti.kukumo.core.datatypes.KukumoDateDataType;

public class TestKukumoDateTypeEs {

    private static final Locale LOCALE = Locale.forLanguageTag("es");
    private static final KukumoDataType<LocalDate> DATE_TYPE = new KukumoDateDataType<>("date", LocalDate.class, true, false, LocalDate::from);
    private static final KukumoDataType<LocalTime> TIME_TYPE = new KukumoDateDataType<>("time", LocalTime.class, false, true, LocalTime::from);
    private static final KukumoDataType<LocalDateTime> DATETIME_TYPE = new KukumoDateDataType<>("datetime", LocalDateTime.class, true, true, LocalDateTime::from);



    @Test
    public void testLocalizedDate1() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "30/05/18").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "30/05/18")).isEqualTo(LocalDate.of(2018, 5, 30));
    }
    @Test
    public void testLocalizedDate2() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "30 de mayo de 2018").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "30 de mayo de 2018")).isEqualTo(LocalDate.of(2018, 5, 30));
    }

    @Test
    public void testLocalizedDate3() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "Miércoles 30 de mayo de 2018").matches()).isTrue();
        Assertions.assertThat(DATE_TYPE.parse(LOCALE, "Miércoles 30 de mayo de 2018")).isEqualTo(LocalDate.of(2018, 5, 30));
    }

    @Test
    public void testLocalizedDate4() {
        Assertions.assertThat(DATE_TYPE.matcher(LOCALE, "5630/18").matches()).isFalse();
    }


    @Test
    public void testLocalizedTime1() {
        Assertions.assertThat(TIME_TYPE.matcher(LOCALE, "17:35").matches()).isTrue();
        Assertions.assertThat(TIME_TYPE.parse(LOCALE, "17:35")).isEqualTo(LocalTime.of(17, 35));
    }
        @Test
        public void testLocalizedTime2() {
            Assertions.assertThat(TIME_TYPE.matcher(LOCALE, "5:35").matches()).isTrue();
            Assertions.assertThat(TIME_TYPE.parse(LOCALE, "5:35")).isEqualTo(LocalTime.of(5, 35));
        }

    @Test
    public void testLocalizedTime3() {
        Assertions.assertThat(TIME_TYPE.matcher(LOCALE, "555:66").matches()).isFalse();
    }


    @Test
    public void testLocalizedDateTime1() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "30/05/18 17:35").matches()).isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "30/05/18 17:35")).isEqualTo(LocalDateTime.of(2018, 5, 30, 17, 35));
    }
    @Test
    public void testLocalizedDateTime2() {
        Assertions.assertThat(DATETIME_TYPE.matcher(LOCALE, "30 de Mayo de 2018 17:35").matches()).isTrue();
        Assertions.assertThat(DATETIME_TYPE.parse(LOCALE, "30 de Mayo de 2018 17:35")).isEqualTo(LocalDateTime.of(2018, 5, 30, 17, 35));
    }





}
