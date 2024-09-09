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
public class TestWakamitiAssertTypesEn {

    private final Locale locale = Locale.ENGLISH;


    @Test
    public void testInteger() throws ParseException {

        WakamitiDataType<Assertion> intMatcher = WakamitiAssertTypes
                .binaryNumberAssert("int-assert", false, Number::intValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is -7", -7);
        exp.put("is equal to 8,000", 8000);
        exp.put("is greater than 11", 12);
        exp.put("is greater than or equal to 12", 12);
        exp.put("is less than 13", 12);
        exp.put("is less than or equal to 13", 13);
        exp.put("is not -7", -8);
        exp.put("is not equal to 8,000", 8001);
        exp.put("is not greater than 11", 11);
        exp.put("is not greater than or equal to 12", 10);
        exp.put("is not less than 13", 13);
        exp.put("is not less than or equal to 13", 15);

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = intMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        intMatcher.parse(locale, "is null").test(null);
        intMatcher.parse(locale, "is not null").test(7);

    }


    @Test
    public void testLong() throws ParseException {

        WakamitiDataType<Assertion> longMatcher = WakamitiAssertTypes
                .binaryNumberAssert("long-assert", false, Number::longValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is -7", -7L);
        exp.put("is equal to 8,000", 8000L);
        exp.put("is greater than 11", 12L);
        exp.put("is greater than or equal to 12", 12L);
        exp.put("is less than 13", 12L);
        exp.put("is less than or equal to 13", 13L);
        exp.put("is not -7", -8L);
        exp.put("is not equal to 8,000", 8001L);
        exp.put("is not greater than 11", 11L);
        exp.put("is not greater than or equal to 12", 10L);
        exp.put("is not less than 13", 13L);
        exp.put("is not less than or equal to 13", 15L);

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = longMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        longMatcher.parse(locale, "is null").test(null);
        longMatcher.parse(locale, "is not null").test(7L);

    }


    @Test
    public void testDouble() throws ParseException {
        WakamitiDataType<Assertion> doubleMatcher = WakamitiAssertTypes
                .binaryNumberAssert("double-assert", true, Number::doubleValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is -7.0", -7.0);
        exp.put("is equal to 8.0", 8.0);
        exp.put("is not 9.0", 8.0);
        exp.put("is not equal to 10.0", 9.0);
        exp.put("is greater than 11.0", 12.0);
        exp.put("is greater than or equal to 12.0", 12.0);
        exp.put("is less than 13.0", 12.0);
        exp.put("is less than or equal to 13.0", 13.0);

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = doubleMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }
    }


    @Test
    public void testBigDecimal() throws ParseException {
        WakamitiDataType<Assertion> bigDecimalMatcher = WakamitiAssertTypes
                .binaryBigDecimalAssert("bigdecimal-assert", true, x -> x);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is -7.0", BigDecimal.valueOf(7.0));
        exp.put("is equal to 8.0", BigDecimal.valueOf(7.0));
        exp.put("is not 9.0", BigDecimal.valueOf(9.0));
        exp.put("is not equal to 10.0", BigDecimal.valueOf(10.0));
        exp.put("is greater than 11.0", BigDecimal.valueOf(10.0));
        exp.put("is greater than or equal to 12.0", BigDecimal.valueOf(10.0));
        exp.put("is less than 13.0", BigDecimal.valueOf(15.0));
        exp.put("is less than or equal to 13.0", BigDecimal.valueOf(15.0));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = bigDecimalMatcher.parse(locale, e.getKey());
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
        exp.put("is -7 seconds", Duration.ofSeconds(7L));
        exp.put("is equal to 8,000 milliseconds", Duration.ofMillis(8000L));
        exp.put("is greater than 11 hours", Duration.ofHours(12L));
        exp.put("is greater than or equal to 12 minutes", Duration.ofMinutes(12L));
        exp.put("is less than 13 seconds", Duration.ofSeconds(12L));
        exp.put("is less than or equal to 13 seconds", Duration.ofSeconds(13L));
        exp.put("is not -7 days", Duration.ofDays(-8L));
        exp.put("is not equal to 8,000 milliseconds", Duration.ofMillis(8001L));
        exp.put("is not greater than 11 minutes", Duration.ofMinutes(11L));
        exp.put("is not greater than or equal to 12 hours", Duration.ofHours(10L));
        exp.put("is not less than 13 minutes", Duration.ofMinutes(13L));
        exp.put("is not less than or equal to 13 seconds", Duration.ofSeconds(15L));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = durationMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        durationMatcher.parse(locale, "is null").test(null);
        durationMatcher.parse(locale, "is not null").test(Duration.ofSeconds(7L));
    }

    @Test
    public void testDateTime() {
        WakamitiDataType<Assertion> dateMatcher = WakamitiAssertTypes
                .binaryDateAssert("datetime-assert", LocalDateTime.class);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is Tuesday, January 30, 2018, 5:35 PM", LocalDateTime.of(2018, 1, 30, 17, 35));
        exp.put("is equal to 5/30/18, 5:35 PM", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("is greater than 2018-05-30T17:35", LocalDateTime.of(2018, 5, 30, 17, 35, 1));
        exp.put("is greater than or equal to 2018-05-30T17:35", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("is less than 2018-05-30T17:35:29", LocalDateTime.of(2018, 5, 30, 17, 35, 28));
        exp.put("is less than or equal to 2018-05-30T17:35:29", LocalDateTime.of(2018, 5, 30, 17, 35, 29));
        exp.put("is not 2018-05-30T17:35:29.743", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("is not equal to 5/30/18, 5:35 PM", LocalDateTime.of(2018, 5, 30, 17, 35, 1));
        exp.put("is not greater than 2018-05-30T17:35:29", LocalDateTime.of(2018, 5, 30, 17, 35, 29));
        exp.put("is not greater than or equal to 2018-05-30T17:35:29", LocalDateTime.of(2018, 5, 30, 17, 35, 28));
        exp.put("is not less than 2018-05-30T17:35", LocalDateTime.of(2018, 5, 30, 17, 35));
        exp.put("is not less than or equal to 2018-05-30T17:35", LocalDateTime.of(2018, 5, 30, 17, 35, 1));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = dateMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        dateMatcher.parse(locale, "is null").test(null);
        dateMatcher.parse(locale, "is not null").test(LocalDateTime.of(2018, 5, 30, 17, 35));
    }

    @Test
    public void testDate() {
        WakamitiDataType<Assertion> dateMatcher = WakamitiAssertTypes
                .binaryDateAssert("date-assert", LocalDate.class);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is Tuesday, January 30, 2018", LocalDate.of(2018, 1, 30));
        exp.put("is equal to 5/30/18", LocalDate.of(2018, 5, 30));
        exp.put("is greater than 2018-05-30", LocalDate.of(2018, 5, 31));
        exp.put("is greater than or equal to 2018-05-30", LocalDate.of(2018, 5, 30));
        exp.put("is less than 2018-05-30", LocalDate.of(2018, 5, 29));
        exp.put("is less than or equal to 2018-05-30", LocalDate.of(2018, 5, 30));
        exp.put("is not 2018-05-30", LocalDate.of(2018, 5, 29));
        exp.put("is not equal to 5/30/18", LocalDate.of(2018, 5, 29));
        exp.put("is not greater than 2018-05-30", LocalDate.of(2018, 5, 30));
        exp.put("is not greater than or equal to 2018-05-30", LocalDate.of(2018, 5, 29));
        exp.put("is not less than 2018-05-30", LocalDate.of(2018, 5, 30));
        exp.put("is not less than or equal to 2018-05-30", LocalDate.of(2018, 5, 31));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = dateMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        dateMatcher.parse(locale, "is null").test(null);
        dateMatcher.parse(locale, "is not null").test(LocalDate.of(2018, 5, 30));
    }

    @Test
    public void testTime() {
        WakamitiDataType<Assertion> dateMatcher = WakamitiAssertTypes
                .binaryDateAssert("time-assert", LocalTime.class);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is 5:35 PM", LocalTime.of(17, 35));
        exp.put("is equal to 5:35 PM", LocalTime.of(17, 35));
        exp.put("is greater than 17:35", LocalTime.of(17, 35, 1));
        exp.put("is greater than or equal to 17:35", LocalTime.of(17, 35));
        exp.put("is less than 17:35:29", LocalTime.of(17, 35, 28));
        exp.put("is less than or equal to 17:35:29", LocalTime.of(17, 35, 29));
        exp.put("is not 17:35:29.743", LocalTime.of(17, 35));
        exp.put("is not equal to 5:35 PM", LocalTime.of(17, 35, 1));
        exp.put("is not greater than 17:35:29", LocalTime.of(17, 35, 29));
        exp.put("is not greater than or equal to 17:35:29", LocalTime.of(17, 35, 28));
        exp.put("is not less than 17:35", LocalTime.of(17, 35));
        exp.put("is not less than or equal to 17:35", LocalTime.of(17, 35, 1));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = dateMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }

        dateMatcher.parse(locale, "is null").test(null);
        dateMatcher.parse(locale, "is not null").test(LocalTime.of(17, 35));
    }

    @Test
    public void testText() {
        WakamitiDataType<Assertion> textMatcher = WakamitiAssertTypes
                .binaryStringAssert("text-assert");
        Map<String, String> exp = new LinkedHashMap<>();
        exp.put("is 'something'", "something");
        exp.put("is equal to 'something'", "something");
        exp.put("is 'something' (ignoring case)", "somEthing");
        exp.put("is equal to 'something' (ignoring case)", "somEthing");
        exp.put("is 'some thing' (ignoring whitespace)", " some  thing ");
        exp.put("is equal to 'some thing' (ignoring whitespace)", " some  thing ");
        exp.put("starts with 'some'", "something");
        exp.put("starts with 'some' (ignoring case)", "somEthing");
        exp.put("ends with 'thing'", "something");
        exp.put("ends with 'thing' (ignoring case)", "sometHing");
        exp.put("contains 'omet'", "something");
        exp.put("contains 'omet' (ignoring case)", "somEthing");

        exp.put("is not 'something'", "somEthing");
        exp.put("is not equal to 'something'", "someThing");
        exp.put("is not 'something' (ignoring case)", "somEthings");
        exp.put("is not equal to 'something' (ignoring case)", "someThings");
        exp.put("is not 'something' (ignoring whitespace)", "some things");
        exp.put("is not equal to 'something' (ignoring whitespace)", "some things");
        exp.put("does not start with 'omet'", "something");
        exp.put("does not start with 'omet' (ignoring case)", "somEthing");
        exp.put("does not end with 'thin'", "something");
        exp.put("does not end with 'thin' (ignoring case)", "sometHing");
        exp.put("does not contain 'things'", "something");
        exp.put("does not contain 'things' (ignoring case)", "somEthing");

        for (Entry<String, String> e : exp.entrySet()) {
            Assertion<?> matcher = textMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                    .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }
    }

}