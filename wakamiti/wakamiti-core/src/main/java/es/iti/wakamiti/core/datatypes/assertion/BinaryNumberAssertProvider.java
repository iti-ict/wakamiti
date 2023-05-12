/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.datatypes.assertion;


import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import es.iti.wakamiti.api.util.ThrowableFunction;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import es.iti.wakamiti.core.datatypes.WakamitiNumberDataType;



public class BinaryNumberAssertProvider<T extends Comparable<T>> extends AbstractAssertProvider {

    public static final String EQUALS = "matcher.number.equals";
    public static final String GREATER = "matcher.number.greater";
    public static final String LESS = "matcher.number.less";
    public static final String GREATER_EQUALS = "matcher.number.greater.equals";
    public static final String LESS_EQUALS = "matcher.number.less.equals";

    public static final String NOT_EQUALS = "matcher.number.not.equals";
    public static final String NOT_GREATER = "matcher.number.not.greater";
    public static final String NOT_LESS = "matcher.number.not.less";
    public static final String NOT_GREATER_EQUALS = "matcher.number.not.greater.equals";
    public static final String NOT_LESS_EQUALS = "matcher.number.not.less.equals";

    private final ThrowableFunction<Locale, String> numberRegexProvider;
    private final ThrowableFunction<Number, T> mapper;
    private final ThrowableFunction<Locale, NumberFormat> formatter;


    public static <T extends Comparable<T>> BinaryNumberAssertProvider<T> createFromNumber(
        ThrowableFunction<Locale, String> numberRegexProvider,
        ThrowableFunction<Number, T> converter
    ) {
        return new BinaryNumberAssertProvider<>(
            numberRegexProvider,
            converter,
            locale -> WakamitiNumberDataType.decimalFormat(locale, false)
        );
    }


    public static <T extends Comparable<T>> BinaryNumberAssertProvider<T> createFromBigDecimal(
        ThrowableFunction<Locale, String> numberRegexProvider,
        ThrowableFunction<BigDecimal, T> converter
    ) {
        return new BinaryNumberAssertProvider<>(
            numberRegexProvider,
            WakamitiNumberDataType.castConverter(BigDecimal.class::cast, converter),
            locale -> WakamitiNumberDataType.decimalFormat(locale, true)
        );
    }


    protected BinaryNumberAssertProvider(
        ThrowableFunction<Locale, String> numberRegexProvider,
        ThrowableFunction<Number, T> mapper,
        ThrowableFunction<Locale, NumberFormat> formatter
    ) {
        this.numberRegexProvider = numberRegexProvider;
        this.mapper = mapper;
        this.formatter = formatter;
    }


    @Override
    protected LinkedHashMap<String, Pattern> translatedExpressions(Locale locale) {
        // binary numeric matchers
        String[] expressions = {
            EQUALS,
            GREATER,
            GREATER_EQUALS,
            LESS,
            LESS_EQUALS,
            NOT_EQUALS,
            NOT_GREATER,
            NOT_GREATER_EQUALS,
            NOT_LESS,
            NOT_LESS_EQUALS,
        };

        LinkedHashMap<String, Pattern> translatedExpressions = new LinkedHashMap<>();
        for (String expression : expressions) {
            translatedExpressions.put(
                expression,
                Pattern.compile(
                    translateBundleExpression(locale, expression, numberRegexProvider.apply(locale))
                )
            );
        }
        return translatedExpressions;
    }


    @Override
    protected Matcher<?> createMatcher(
        Locale locale,
        String key,
        String value
    ) throws ParseException {
        Matcher<T> matcher = null;
        T numericValue = mapper.apply(formatter.apply(locale).parse(value));
        if (EQUALS.equals(key)) {
            matcher = Matchers.comparesEqualTo(numericValue);
        } else if (GREATER.equals(key)) {
            matcher = Matchers.greaterThan(numericValue);
        } else if (LESS.equals(key)) {
            matcher = Matchers.lessThan(numericValue);
        } else if (GREATER_EQUALS.equals(key)) {
            matcher = Matchers.greaterThanOrEqualTo(numericValue);
        } else if (LESS_EQUALS.equals(key)) {
            matcher = Matchers.lessThanOrEqualTo(numericValue);
        } else if (NOT_EQUALS.equals(key)) {
            matcher = Matchers.not(Matchers.comparesEqualTo(numericValue));
        } else if (NOT_GREATER.equals(key)) {
            matcher = Matchers.not(Matchers.greaterThan(numericValue));
        } else if (NOT_LESS.equals(key)) {
            matcher = Matchers.not(Matchers.lessThan(numericValue));
        } else if (NOT_GREATER_EQUALS.equals(key)) {
            matcher = Matchers.not(Matchers.greaterThanOrEqualTo(numericValue));
        } else if (NOT_LESS_EQUALS.equals(key)) {
            matcher = Matchers.not(Matchers.lessThanOrEqualTo(numericValue));
        }
        return matcher;
    }

}