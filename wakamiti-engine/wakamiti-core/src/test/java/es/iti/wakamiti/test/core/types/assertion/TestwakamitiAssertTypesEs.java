/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.core.types.assertion;


import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.core.datatypes.assertion.WakamitiAssertTypes;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("rawtypes")
public class TestwakamitiAssertTypesEs {

    private final Locale locale = Locale.forLanguageTag("es");


    @Test
    public void testInteger() throws ParseException {

        WakamitiDataType<Assertion> intAssertion = WakamitiAssertTypes
                .binaryNumberAssert("int-assert", false, Number::intValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("es -7", -7);
        exp.put("es igual a 8.000", 8000);
        exp.put("es igual que 8.000", 8000);
        exp.put("es mayor que 11", 12);
        exp.put("es mayor o igual que 12", 12);
        exp.put("es menor que 13", 12);
        exp.put("es menor o igual que 13", 13);
        exp.put("no es -7", -8);
        exp.put("no es igual a 8.000", 8001);
        exp.put("no es igual que 8.000", 8001);
        exp.put("no es mayor que 11", 11);
        exp.put("no es mayor o igual que 12", 10);
        exp.put("no es menor que 13", 13);
        exp.put("no es menor o igual que 13", 15);

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = intAssertion.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        intAssertion.parse(locale, "es nulo").test(null);
        intAssertion.parse(locale, "no es nulo").test(7);

    }


    @Test
    public void testLong() throws ParseException {

        WakamitiDataType<Assertion> longAssertion = WakamitiAssertTypes
                .binaryNumberAssert("long-assert", false, Number::longValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("es -7", -7L);
        exp.put("es igual a 8.000", 8000L);
        exp.put("es igual que 8.000", 8000L);
        exp.put("es mayor que 11", 12L);
        exp.put("es mayor o igual que 12", 12L);
        exp.put("es menor que 13", 12L);
        exp.put("es menor o igual que 13", 13L);
        exp.put("no es -7", -8L);
        exp.put("no es igual a 8.000", 8001L);
        exp.put("no es igual que 8.000", 8001L);
        exp.put("no es mayor que 11", 11L);
        exp.put("no es mayor o igual que 12", 10L);
        exp.put("no es menor que 13", 13L);
        exp.put("no es menor o igual que 13", 15L);

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = longAssertion.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        longAssertion.parse(locale, "es nulo").test(null);
        longAssertion.parse(locale, "no es nulo").test(7L);

    }


    @Test
    public void testDouble() throws ParseException {

        WakamitiDataType<Assertion> doubleAssertion = WakamitiAssertTypes
                .binaryNumberAssert("double-assert", true, Number::doubleValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("es -7,0", -7.0);
        exp.put("es igual a 8,0", 8.0);
        exp.put("es igual que 8,0", 8.0);
        exp.put("no es 9,0", 8.0);
        exp.put("no es igual a 10,0", 9.0);
        exp.put("no es igual que 10,0", 9.0);
        exp.put("es mayor que 11,0", 12.0);
        exp.put("es mayor o igual que 12,0", 12.0);
        exp.put("es menor que 13,0", 12.0);
        exp.put("es menor o igual que 13,0", 13.0);

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = doubleAssertion.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }
    }


    @Test
    public void testBigDecimal() throws ParseException {

        WakamitiDataType<Assertion> bigDecimalAssertion = WakamitiAssertTypes
                .binaryBigDecimalAssert("bigdecimal-assert", true, x -> x);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("es -7,0", BigDecimal.valueOf(7.0));
        exp.put("es igual a 8,0", BigDecimal.valueOf(7.0));
        exp.put("es igual que 8,0", BigDecimal.valueOf(7.0));
        exp.put("no es 9,0", BigDecimal.valueOf(9.0));
        exp.put("no es igual a 10,0", BigDecimal.valueOf(10.0));
        exp.put("no es igual que 10,0", BigDecimal.valueOf(10.0));
        exp.put("es mayor que 11,0", BigDecimal.valueOf(10.0));
        exp.put("es mayor o igual que 12,0", BigDecimal.valueOf(10.0));
        exp.put("es menor que 13,0", BigDecimal.valueOf(15.0));
        exp.put("es menor o igual que 13,0", BigDecimal.valueOf(15.0));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = bigDecimalAssertion.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed negative match for: " + e.getKey() + " with " + e.getValue()).isFalse();
        }
    }

    @Test
    public void testDuration() {
        WakamitiDataType<Assertion> durationMatcher = WakamitiAssertTypes
                .binaryDurationAssert("duration-assert", x -> x);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("es -7 segundos", Duration.ofSeconds(7L));
        exp.put("es igual a 8.000 milisegundos", Duration.ofMillis(8000L));
        exp.put("es mayor que 11 horas", Duration.ofHours(12L));
        exp.put("es mayor o igual que 12 minutos", Duration.ofMinutes(12L));
        exp.put("es menor que 13 segundos", Duration.ofSeconds(12L));
        exp.put("es menor o igual que 13 segundos", Duration.ofSeconds(13L));
        exp.put("no es -7 días", Duration.ofDays(8L));
        exp.put("no es igual a 8000 milisegundos", Duration.ofMillis(8001L));
        exp.put("no es mayor que 11 minutos", Duration.ofMinutes(11L));
        exp.put("no es mayor o igual que 12 horas", Duration.ofHours(10L));
        exp.put("no es menor que 13 minutos", Duration.ofMinutes(13L));
        exp.put("no es menor o igual que 13 segundos", Duration.ofSeconds(15L));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = durationMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        durationMatcher.parse(locale, "es nulo").test(null);
        durationMatcher.parse(locale, "no es nulo").test(Duration.ofSeconds(7L));
    }

    @Test
    public void testDateTime() {
        WakamitiDataType<Assertion> dateMatcher = WakamitiAssertTypes
                .binaryDateAssert("datetime-assert", LocalDateTime.class);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("es Miércoles, 30 de mayo de 2018 17:35", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("es igual a 30/05/18 17:35", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("es mayor que 2018-05-30T17:35", LocalDateTime.of(2018, 5, 30, 17, 35, 1));
        exp.put("es mayor o igual que 2018-05-30T17:35", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("es menor que 2018-05-30T17:35:29", LocalDateTime.of(2018, 5, 30, 17, 35, 28));
        exp.put("es menor o igual que 2018-05-30T17:35:29", LocalDateTime.of(2018, 5, 30, 17, 35, 29));
        exp.put("no es 2018-05-30T17:35:29.743", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("no es igual que 30/05/18 17:35", LocalDateTime.of(2018, 5, 30, 17, 35, 1));
        exp.put("no es mayor que 2018-05-30T17:35:29", LocalDateTime.of(2018, 5, 30, 17, 35, 29));
        exp.put("no es mayor o igual que 2018-05-30T17:35:29", LocalDateTime.of(2018, 5, 30, 17, 35, 28));
        exp.put("no es menor que 2018-05-30T17:35", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("no es menor o igual que 2018-05-30T17:35", LocalDateTime.of(2018, 5, 30, 17, 35, 1));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = dateMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        dateMatcher.parse(locale, "es nulo").test(null);
        dateMatcher.parse(locale, "no es nulo").test(LocalDateTime.of(2018, 5, 30, 17, 35));
    }

    @Test
    public void testDate() {
        WakamitiDataType<Assertion> dateMatcher = WakamitiAssertTypes
                .binaryDateAssert("date-assert", LocalDate.class);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("es Miércoles, 30 de mayo de 2018", LocalDate.of(2018, 5, 30));
        exp.put("es igual a 30 de mayo de 2018", LocalDate.of(2018, 5, 30));
        exp.put("es mayor que 2018-05-30", LocalDate.of(2018, 5, 31));
        exp.put("es mayor o igual que 2018-05-30", LocalDate.of(2018, 5, 30));
        exp.put("es menor que 2018-05-30", LocalDate.of(2018, 5, 29));
        exp.put("es menor o igual que 2018-05-30", LocalDate.of(2018, 5, 30));
        exp.put("no es 2018-05-30", LocalDate.of(2018, 5, 29));
        exp.put("no es igual a 30/05/18", LocalDate.of(2018, 5, 29));
        exp.put("no es mayor que 2018-05-30", LocalDate.of(2018, 5, 30));
        exp.put("no es mayor o igual que 2018-05-30", LocalDate.of(2018, 5, 29));
        exp.put("no es menor que 2018-05-30", LocalDate.of(2018, 5, 30));
        exp.put("no es menor o igual que 2018-05-30", LocalDate.of(2018, 5, 31));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = dateMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        dateMatcher.parse(locale, "es nulo").test(null);
        dateMatcher.parse(locale, "no es nulo").test(LocalDate.of(2018, 5, 30));
    }

    @Test
    public void testTime() {
        WakamitiDataType<Assertion> dateMatcher = WakamitiAssertTypes
                .binaryDateAssert("time-assert", LocalTime.class);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("es 17:35", LocalTime.of(17, 35));
        exp.put("es igual que 17:35", LocalTime.of(17, 35));
        exp.put("es mayor que 17:35", LocalTime.of(17, 35, 1));
        exp.put("es mayor o igual que 17:35", LocalTime.of(17, 35));
        exp.put("es menor que 17:35:29", LocalTime.of(17, 35, 28));
        exp.put("es menor o igual que 17:35:29", LocalTime.of(17, 35, 29));
        exp.put("no es 17:35:29.743", LocalTime.of(17, 35));
        exp.put("no es igual que 17:35", LocalTime.of(17, 35, 1));
        exp.put("no es mayor que 17:35:29", LocalTime.of(17, 35, 29));
        exp.put("no es mayor o igual que 17:35:29", LocalTime.of(17, 35, 28));
        exp.put("no es menor que 17:35", LocalTime.of(17, 35));
        exp.put("no es menor o igual que 17:35", LocalTime.of(17, 35, 1));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = dateMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        dateMatcher.parse(locale, "es nulo").test(null);
        dateMatcher.parse(locale, "no es nulo").test(LocalTime.of(17, 35));
    }

    @Test
    public void testText() {
        WakamitiDataType<Assertion> textMatcher = WakamitiAssertTypes
                .binaryStringAssert("text-assert");
        Map<String, String> exp = new LinkedHashMap<>();
        exp.put("es 'something'", "something");
        exp.put("es igual a 'something'", "something");
        exp.put("es igual que 'something'", "something");
        exp.put("es 'something' (sin distinguir mayúsculas)", "somEthing");
        exp.put("es igual a 'something' (sin distinguir mayúsculas)", "somEthing");
        exp.put("es igual que 'something' (sin distinguir mayúsculas)", "somEthing");
        exp.put("es 'some thing' (ignorando espacios)", " some  thing ");
        exp.put("es igual a 'some thing' (ignorando espacios)", " some  thing ");
        exp.put("es igual que 'some thing' (ignorando espacios)", " some  thing ");
        exp.put("empieza por 'some'", "something");
        exp.put("empieza por 'some' (sin distinguir mayúsculas)", "somEthing");
        exp.put("acaba en 'thing'", "something");
        exp.put("acaba en 'thing' (sin distinguir mayúsculas)", "sometHing");
        exp.put("contiene 'omet'", "something");
        exp.put("contiene 'omet' (sin distinguir mayúsculas)", "somEthing");

        exp.put("no es 'something'", "somEthing");
        exp.put("no es igual a 'something'", "someThing");
        exp.put("no es igual que 'something'", "someThing");
        exp.put("no es 'something' (sin distinguir mayúsculas)", "somEthings");
        exp.put("no es igual a 'something' (sin distinguir mayúsculas)", "someThings");
        exp.put("no es igual que 'something' (sin distinguir mayúsculas)", "someThings");
        exp.put("no es 'something' (ignorando espacios)", "some things");
        exp.put("no es igual a 'something' (ignorando espacios)", "some things");
        exp.put("no es igual que 'something' (ignorando espacios)", "some things");
        exp.put("no empieza por 'omet'", "something");
        exp.put("no empieza por 'omet' (sin distinguir mayúsculas)", "somEthing");
        exp.put("no acaba en 'thin'", "something");
        exp.put("no acaba en 'thin' (sin distinguir mayúsculas)", "sometHing");
        exp.put("no contiene 'things'", "something");
        exp.put("no contiene 'things' (sin distinguir mayúsculas)", "somEthing");

        for (Entry<String, String> e : exp.entrySet()) {
            Assertion<?> matcher = textMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }
    }
}