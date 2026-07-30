/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import static es.iti.wakamiti.api.util.MapUtils.map;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import es.iti.wakamiti.api.ExpressionMatcher;


/**
 * A provider for unary number assertions.
 */
public class UnaryNumberAssertProvider extends AbstractAssertProvider {

    /** Localization key for the matcher requiring an absent or {@code null} value. */
    public static final String NULL = "matcher.generic.null";
    /** Localization key for the matcher requiring a non-{@code null} value. */
    public static final String NOT_NULL = "matcher.generic.not.null";

    private final Map<String, Supplier<Matcher<?>>> matchers = map(
            NULL, Matchers::nullValue,
            NOT_NULL, Matchers::notNullValue
    );

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
    protected LinkedHashMap<String, Pattern> translatedExpressions(
            Locale locale
    ) {
        LinkedHashMap<String, Pattern> translatedExpressions = new LinkedHashMap<>();
        for (String key : expressions()) {
            translatedExpressions
                    .put(key, Pattern.compile(translateBundleExpression(locale, key, "")));
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
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Matcher<?> createMatcher(
            Locale locale,
            String expression,
            String value
    ) {
        return matchers.get(expression).get();
    }

}
