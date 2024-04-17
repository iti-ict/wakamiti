/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.core.backend.ExpressionMatcher;
import es.iti.wakamiti.core.datatypes.WakamitiNumberDataType;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * A provider for binary number assertions.
 *
 * @param <T> The type of numbers to compare.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
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

    private final Map<String, Function<T, Matcher<T>>> matchers = new LinkedHashMap<>();

     {
        matchers.put(EQUALS, Matchers::comparesEqualTo);
        matchers.put(GREATER, Matchers::greaterThan);
        matchers.put(LESS, Matchers::lessThan);
        matchers.put(GREATER_EQUALS, Matchers::greaterThanOrEqualTo);
        matchers.put(LESS_EQUALS, Matchers::lessThanOrEqualTo);
        matchers.put(NOT_EQUALS, (value) -> Matchers.not(Matchers.comparesEqualTo(value)));
        matchers.put(NOT_GREATER, (value) -> Matchers.not(Matchers.greaterThan(value)));
        matchers.put(NOT_LESS, (value) -> Matchers.not(Matchers.lessThan(value)));
        matchers.put(NOT_GREATER_EQUALS, (value) -> Matchers.not(Matchers.greaterThanOrEqualTo(value)));
        matchers.put(NOT_LESS_EQUALS, (value) -> Matchers.not(Matchers.lessThanOrEqualTo(value)));
    }

    private final ThrowableFunction<Locale, String> numberRegexProvider;
    private final ThrowableFunction<Number, T> mapper;
    private final ThrowableFunction<Locale, NumberFormat> formatter;

    /**
     * Constructs a BinaryNumberAssertProvider.
     *
     * @param numberRegexProvider A function providing the number regex.
     * @param mapper              A function to map the number.
     * @param formatter           A function providing the number formatter.
     */
    protected BinaryNumberAssertProvider(
            ThrowableFunction<Locale, String> numberRegexProvider,
            ThrowableFunction<Number, T> mapper,
            ThrowableFunction<Locale, NumberFormat> formatter
    ) {
        this.numberRegexProvider = numberRegexProvider;
        this.mapper = mapper;
        this.formatter = formatter;
    }

    /**
     * Creates a BinaryNumberAssertProvider from a number.
     *
     * @param numberRegexProvider A function providing the number regex.
     * @param converter           A function to convert the number to the desired type.
     * @param <T>                 The type of numbers to compare.
     * @return A BinaryNumberAssertProvider instance.
     */
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

    /**
     * Creates a BinaryNumberAssertProvider from a BigDecimal.
     *
     * @param numberRegexProvider A function providing the number regex.
     * @param converter           A function to convert the BigDecimal to the desired type.
     * @param <T>                 The type of numbers to compare.
     * @return A BinaryNumberAssertProvider instance.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected LinkedHashMap<String, Pattern> translatedExpressions(Locale locale) {
        LinkedHashMap<String, Pattern> translatedExpressions = new LinkedHashMap<>();
        for (String expression : expressions()) {
            translatedExpressions.put(
                    expression,
                    Pattern.compile(
                            translateBundleExpression(locale, expression, numberRegexProvider.apply(locale))
                    )
            );
        }
        return translatedExpressions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] expressions() {
        return matchers.keySet().toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LinkedList<String> regex(Locale locale) {
        return Arrays.stream(expressions())
                .map(exp -> ExpressionMatcher.computeRegularExpression(bundle(locale).getString(exp)))
                .map(exp -> exp.replace(VALUE_WILDCARD, numberRegexProvider.apply(locale)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Matcher<?> createMatcher(
            Locale locale,
            String key,
            String value
    ) throws ParseException {
        T numericValue = mapper.apply(formatter.apply(locale).parse(value));
        return matchers.get(key).apply(numericValue);
    }

}