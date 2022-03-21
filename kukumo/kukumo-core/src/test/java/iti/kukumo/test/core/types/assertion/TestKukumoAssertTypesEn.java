/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.test.core.types.assertion;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.datatypes.Assertion;
import iti.kukumo.core.datatypes.assertion.KukumoAssertTypes;


@SuppressWarnings("rawtypes")
public class TestKukumoAssertTypesEn {

    private final Locale locale = Locale.ENGLISH;


    @Test
    public void testInteger() throws ParseException {

        KukumoDataType<Assertion> intMatcher = KukumoAssertTypes
            .binaryNumberAssert("int-assert", false, Number::intValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is -7", -7);
        exp.put("is equals to 8,000", 8000);
        exp.put("is greater than 11", 12);
        exp.put("is greater than or equals to 12", 12);
        exp.put("is less than 13", 12);
        exp.put("is less than or equals to 13", 13);
        exp.put("is not -7", -8);
        exp.put("is not equals to 8,000", 8001);
        exp.put("is not greater than 11", 11);
        exp.put("is not greater than or equals to 12", 10);
        exp.put("is not less than 13", 13);
        exp.put("is not less than or equals to 13", 15);

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

        KukumoDataType<Assertion> longMatcher = KukumoAssertTypes
            .binaryNumberAssert("long-assert", false, Number::longValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is -7", -7L);
        exp.put("is equals to 8,000", 8000L);
        exp.put("is greater than 11", 12L);
        exp.put("is greater than or equals to 12", 12L);
        exp.put("is less than 13", 12L);
        exp.put("is less than or equals to 13", 13L);
        exp.put("is not -7", -8L);
        exp.put("is not equals to 8,000", 8001L);
        exp.put("is not greater than 11", 11L);
        exp.put("is not greater than or equals to 12", 10L);
        exp.put("is not less than 13", 13L);
        exp.put("is not less than or equals to 13", 15L);

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
        KukumoDataType<Assertion> doubleMatcher = KukumoAssertTypes
            .binaryNumberAssert("double-assert", true, Number::doubleValue);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is -7.0", -7.0);
        exp.put("is equals to 8.0", 8.0);
        exp.put("is not 9.0", 8.0);
        exp.put("is not equals to 10.0", 9.0);
        exp.put("is greater than 11.0", 12.0);
        exp.put("is greater than or equals to 12.0", 12.0);
        exp.put("is less than 13.0", 12.0);
        exp.put("is less than or equals to 13.0", 13.0);

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = doubleMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                .as("failed match for: " + e.getKey() + " with " + e.getValue()).isTrue();
        }
    }


    @Test
    public void testBigDecimal() throws ParseException {
        KukumoDataType<Assertion> bigDecimalMatcher = KukumoAssertTypes
            .binaryBigDecimalAssert("bigdecimal-assert", true, x -> x);
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("is -7.0", BigDecimal.valueOf(7.0));
        exp.put("is equals to 8.0", BigDecimal.valueOf(7.0));
        exp.put("is not 9.0", BigDecimal.valueOf(9.0));
        exp.put("is not equals to 10.0", BigDecimal.valueOf(10.0));
        exp.put("is greater than 11.0", BigDecimal.valueOf(10.0));
        exp.put("is greater than or equals to 12.0", BigDecimal.valueOf(10.0));
        exp.put("is less than 13.0", BigDecimal.valueOf(15.0));
        exp.put("is less than or equals to 13.0", BigDecimal.valueOf(15.0));

        for (Entry<String, Object> e : exp.entrySet()) {
            Assertion<?> matcher = bigDecimalMatcher.parse(locale, e.getKey());
            assertThat(matcher).as("null assertion for: " + e.getKey()).isNotNull();
            assertThat(matcher.test(e.getValue()))
                .as("failed negative match for: " + e.getKey() + " with " + e.getValue()).isFalse();
        }
    }

}