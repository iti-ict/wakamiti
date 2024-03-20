/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.core.types;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.core.datatypes.WakamitiNumberDataType;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;


public class TestWakamitiNumberTypeEs {

    private static final Locale LOCALE = Locale.forLanguageTag("es-ES");


    @Test
    public void testAttempParseWithWrongValue() {
        final WakamitiNumberDataType<Integer> type = WakamitiNumberDataType
                .createFromNumber("int", Integer.class, false, Number::intValue);
        try {
            type.parse(LOCALE, "xxxxx");
            Assert.fail("exception was expected to be thrown by this line");
        } catch (final Exception e) {
            assertThat(e).isExactlyInstanceOf(WakamitiException.class);
            assertThat(e.getMessage())
                    .startsWith("Error parsing type int using language es_ES: 'xxxxx'");
        }
    }


    @Test
    public void testInteger() {
        final WakamitiNumberDataType<Integer> type = WakamitiNumberDataType
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
        final WakamitiNumberDataType<BigDecimal> type = WakamitiNumberDataType
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