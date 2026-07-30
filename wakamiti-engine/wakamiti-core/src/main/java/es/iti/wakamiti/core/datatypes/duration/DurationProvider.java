/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.duration;


import static es.iti.wakamiti.api.util.MapUtils.map;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import es.iti.wakamiti.api.ExpressionMatcher;
import es.iti.wakamiti.api.datatypes.AbstractProvider;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.core.datatypes.WakamitiNumberDataType;


/**
 * A provider for durations.
 */
public class DurationProvider extends AbstractProvider {

    /** Resource-bundle base name containing localized duration-unit expressions. */
    public static final String DURATIONS_RESOURCE = "iti_wakamiti_core-durations";

    /** Localization key used to recognize nanosecond values. */
    public static final String NANOSECONDS = "duration.nanoseconds";
    /** Localization key used to recognize microsecond values. */
    public static final String MICROSECONDS = "duration.microseconds";
    /** Localization key used to recognize millisecond values. */
    public static final String MILLISECONDS = "duration.milliseconds";
    /** Localization key used to recognize second values. */
    public static final String SECONDS = "duration.seconds";
    /** Localization key used to recognize minute values. */
    public static final String MINUTES = "duration.minutes";
    /** Localization key used to recognize hour values. */
    public static final String HOURS = "duration.hours";
    /** Localization key used to recognize day values. */
    public static final String DAYS = "duration.days";

    private final Map<String, Function<Long, Duration>> durations = map(
            NANOSECONDS, value -> Duration.of(value, ChronoUnit.NANOS),
            MICROSECONDS, value -> Duration.of(value, ChronoUnit.MICROS),
            MILLISECONDS, value -> Duration.of(value, ChronoUnit.MILLIS),
            SECONDS, value -> Duration.of(value, ChronoUnit.SECONDS),
            MINUTES, value -> Duration.of(value, ChronoUnit.MINUTES),
            HOURS, value -> Duration.of(value, ChronoUnit.HOURS),
            DAYS, value -> Duration.of(value, ChronoUnit.DAYS)
    );

    private final ThrowableFunction<Locale, String> numberRegexProvider;
    private final ThrowableFunction<Locale, NumberFormat> formatter;

    protected DurationProvider() {
        super(DURATIONS_RESOURCE);
        this.numberRegexProvider = locale -> WakamitiNumberDataType.numericRegexPattern(locale, false);
        this.formatter = locale -> WakamitiNumberDataType.decimalFormat(locale, false);
    }

    /**
     * Retrieves all expressions with the given prefix for a specific locale.
     *
     * @param locale The locale for which expressions are retrieved.
     * @return A list of expressions with the specified prefix.
     */
    public static List<String> getAllExpressions(
            Locale locale
    ) {
        ResourceBundle bundle = RESOURCE_LOADER.resourceBundle(DURATIONS_RESOURCE, locale);
        return bundle.keySet().stream()
                .map(bundle::getString)
                .collect(Collectors.toList());
    }

    @Override
    protected String[] expressions() {
        return durations.keySet().toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LinkedHashMap<String, Pattern> translatedExpressions(
            Locale locale
    ) {
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
    public LinkedList<String> regex(
            Locale locale
    ) {
        return Arrays.stream(expressions())
                .map(exp -> ExpressionMatcher.computeRegularExpression(bundle(locale).getString(exp)))
                .map(exp -> exp.replace(VALUE_WILDCARD, numberRegexProvider.apply(locale)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Retrieves a duration from the given expression for a specific locale.
     *
     * @param locale     The locale for which the matcher is retrieved.
     * @param expression The expression used to create the matcher.
     * @return An optional containing the duration if one is created, or empty otherwise.
     */
    public Optional<Duration> durationFromExpression(
            Locale locale,
            String expression
    ) {
        return fromExpression(locale, expression).map((ThrowableFunction<Pair<String, String>, Duration>) p -> {
            Long numericValue = Math.abs(formatter.apply(locale).parse(p.value()).longValue());
            return durations.get(p.key()).apply(numericValue);
        });
    }

}
