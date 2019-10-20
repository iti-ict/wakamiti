/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.test.core.types;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import iti.kukumo.api.KukumoException;
import iti.kukumo.core.datatypes.KukumoNumberDataType;


public class TestKukumoNumberTypeEs {

    private static final Locale LOCALE = Locale.forLanguageTag("es-ES");


    @Test
    public void testAttempParseWithWrongValue() {
        final KukumoNumberDataType<Integer> type = KukumoNumberDataType
            .createFromNumber("int", Integer.class, false, Number::intValue);
        try {
            type.parse(LOCALE, "xxxxx");
            Assert.fail("exception was expected to be thrown by this line");
        } catch (final Exception e) {
            assertThat(e).isExactlyInstanceOf(KukumoException.class);
            assertThat(e.getMessage())
                .startsWith("Error parsing type int using language es_ES: 'xxxxx'");
        }
    }


    @Test
    public void testInteger() {
        final KukumoNumberDataType<Integer> type = KukumoNumberDataType
            .createFromNumber("int", Integer.class, false, Number::intValue);
        assertThat(type.matcher(LOCALE, "12345").matches()).isTrue();
        assertThat(type.parse(LOCALE, "12345")).isEqualTo(12345);
        assertThat(type.matcher(LOCALE, "12.345").matches()).isTrue();
        assertThat(type.parse(LOCALE, "12.345")).isEqualTo(12345);
        assertThat(type.matcher(LOCALE, "12.345,54").matches()).isFalse();
        assertThat(type.matcher(LOCALE, "xxxxx").matches()).isFalse();
    }


    @Test
    public void testBigDecimal() {
        final KukumoNumberDataType<BigDecimal> type = KukumoNumberDataType
            .createFromBigDecimal("bigdecimal", BigDecimal.class, true, x -> x);
        assertThat(type.matcher(LOCALE, "12345").matches()).isFalse();
        assertThat(type.matcher(LOCALE, "12345,0").matches()).isTrue();
        assertThat(type.parse(LOCALE, "12345,0")).isEqualByComparingTo(BigDecimal.valueOf(12345.0));
        assertThat(type.matcher(LOCALE, "12.345").matches()).isFalse();
        assertThat(type.matcher(LOCALE, "12.345,0").matches()).isTrue();
        assertThat(type.parse(LOCALE, "12.345,0"))
            .isEqualByComparingTo(BigDecimal.valueOf(12345.0));
        assertThat(type.matcher(LOCALE, "12.345,54").matches()).isTrue();
        assertThat(type.parse(LOCALE, "12.345,54"))
            .isEqualByComparingTo(BigDecimal.valueOf(12345.54));
        assertThat(type.matcher(LOCALE, "xxxxx").matches()).isFalse();
    }

}
