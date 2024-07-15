/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import es.iti.wakamiti.api.ExpressionMatcher;
import es.iti.wakamiti.api.util.ThrowableBiFunction;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.core.datatypes.WakamitiDateDataType;
import es.iti.wakamiti.core.datatypes.WakamitiNumberDataType;
import es.iti.wakamiti.core.datatypes.duration.WakamitiDurationDataType;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.MapUtils.map;
import static org.hamcrest.Matchers.*;


/**
 * A provider for binary number assertions.
 *
 * @param <T> The type of numbers to compare.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class BinaryNumberAssertProvider<T extends Comparable<T>, R> extends AbstractAssertProvider {

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

    private final Map<String, Function<T, Matcher<T>>> matchers = map(
            EQUALS, Matchers::comparesEqualTo,
            GREATER, Matchers::greaterThan,
            LESS, Matchers::lessThan,
            GREATER_EQUALS, Matchers::greaterThanOrEqualTo,
            LESS_EQUALS, Matchers::lessThanOrEqualTo,
            NOT_EQUALS, value -> not(comparesEqualTo(value)),
            NOT_GREATER, value -> not(greaterThan(value)),
            NOT_LESS, value -> not(lessThan(value)),
            NOT_GREATER_EQUALS, value -> not(greaterThanOrEqualTo(value)),
            NOT_LESS_EQUALS, value -> not(lessThanOrEqualTo(value))
    );

    private final ThrowableFunction<Locale, String> numberRegexProvider;
    private final ThrowableFunction<R, T> mapper;
    private final ThrowableBiFunction<Locale, String, R> formatter;

    /**
     * Constructs a BinaryNumberAssertProvider.
     *
     * @param numberRegexProvider A function providing the number regex.
     * @param mapper              A function to map the number.
     * @param formatter           A function providing the number formatter.
     */
    protected BinaryNumberAssertProvider(
            ThrowableFunction<Locale, String> numberRegexProvider,
            ThrowableFunction<R, T> mapper,
            ThrowableBiFunction<Locale, String, R> formatter
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
    public static <T extends Comparable<T>> BinaryNumberAssertProvider<T, Number> createFromNumber(
            ThrowableFunction<Locale, String> numberRegexProvider,
            ThrowableFunction<Number, T> converter
    ) {
        return new BinaryNumberAssertProvider<>(
                numberRegexProvider,
                converter,
                (locale, value) -> WakamitiNumberDataType.decimalFormat(locale, false).parse(value)
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
    public static <T extends Comparable<T>> BinaryNumberAssertProvider<T, Number> createFromBigDecimal(
            ThrowableFunction<Locale, String> numberRegexProvider,
            ThrowableFunction<BigDecimal, T> converter
    ) {
        return new BinaryNumberAssertProvider<>(
                numberRegexProvider,
                WakamitiNumberDataType.castConverter(BigDecimal.class::cast, converter),
                (locale, value) -> WakamitiNumberDataType.decimalFormat(locale, true).parse(value)
        );
    }

    /**
     * Creates a BinaryNumberAssertProvider from a number.
     *
     * @param regexProvider A function providing the duration regex.
     * @param converter     A function to convert the duration to the desired type.
     * @param <T>           The type of numbers to compare.
     * @return A BinaryNumberAssertProvider instance.
     */
    public static <T extends Comparable<T>> BinaryNumberAssertProvider<T, Duration> createFromDuration(
            ThrowableFunction<Locale, String> regexProvider,
            ThrowableFunction<Duration, T> converter
    ) {
        return new BinaryNumberAssertProvider<>(
                regexProvider,
                converter,
                (locale, value) -> WakamitiDurationDataType.parser(locale).parse(value)
        );
    }

    public static <T extends Comparable<T> & TemporalAccessor>
    BinaryNumberAssertProvider<T, ? extends TemporalAccessor> createFromDate(
            ThrowableFunction<Locale, String> regexProvider,
            Class<? extends T> dateType
    ) {
        return new BinaryNumberAssertProvider<>(
                regexProvider,
                x -> x,
                (locale, value) -> WakamitiDateDataType.dateTimeParser(locale,
                        WakamitiDateDataType.temporalProperties(dateType),
                        WakamitiDateDataType.temporalQuery(dateType)).parse(value)
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
    public LinkedList<String> regex(Locale locale) {
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
    ) {
        T numericValue = mapper.apply(formatter.apply(locale, value));
        return matchers.get(key).apply(numericValue);
    }

}