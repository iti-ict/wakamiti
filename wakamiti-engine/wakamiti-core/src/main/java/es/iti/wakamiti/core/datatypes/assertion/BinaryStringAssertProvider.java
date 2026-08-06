/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import static es.iti.wakamiti.api.util.MapUtils.entry;
import static es.iti.wakamiti.api.util.MapUtils.mapEntries;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.endsWithIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.startsWithIgnoringCase;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import es.iti.wakamiti.api.ExpressionMatcher;


/**
 * A provider for binary string assertions.
 */
public class BinaryStringAssertProvider extends AbstractAssertProvider {

    /** Localization key for case-sensitive string equality. */
    public static final String EQUALS = "matcher.string.equals";
    /** Localization key for case-insensitive string equality. */
    public static final String EQUALS_IGNORE_CASE = "matcher.string.equals.ignore.case";
    /** Localization key for equality after insignificant whitespace is ignored. */
    public static final String EQUALS_IGNORE_WHITESPACE = "matcher.string.equals.ignore.whitespace";
    /** Localization key for a case-sensitive prefix comparison. */
    public static final String STARTS_WITH = "matcher.string.starts.with";
    /** Localization key for a case-insensitive prefix comparison. */
    public static final String STARTS_WITH_IGNORE_CASE = "matcher.string.starts.with.ignore.case";
    /** Localization key for a case-sensitive suffix comparison. */
    public static final String ENDS_WITH = "matcher.string.ends.with";
    /** Localization key for a case-insensitive suffix comparison. */
    public static final String ENDS_WITH_IGNORE_CASE = "matcher.string.ends.with.ignore.case";
    /** Localization key for a case-sensitive substring comparison. */
    public static final String CONTAINS = "matcher.string.contains";
    /** Localization key for a case-insensitive substring comparison. */
    public static final String CONTAINS_IGNORE_CASE = "matcher.string.contains.ignore.case";
    /** Localization key for a regular expression match. */
    public static final String MATCHES = "matcher.string.matches";

    /** Localization key for case-sensitive string inequality. */
    public static final String NOT_EQUALS = "matcher.string.not.equals";
    /** Localization key for case-insensitive string inequality. */
    public static final String NOT_EQUALS_IGNORE_CASE = "matcher.string.not.equals.ignore.case";
    /** Localization key for inequality after insignificant whitespace is ignored. */
    public static final String NOT_EQUALS_IGNORE_WHITESPACE = "matcher.string.not.equals.ignore.whitespace";
    /** Localization key requiring a value not to start with a case-sensitive prefix. */
    public static final String NOT_STARTS_WITH = "matcher.string.not.starts.with";
    /** Localization key requiring a value not to start with a prefix, ignoring case. */
    public static final String NOT_STARTS_WITH_IGNORE_CASE = "matcher.string.not.starts.with.ignore.case";
    /** Localization key requiring a value not to end with a case-sensitive suffix. */
    public static final String NOT_ENDS_WITH = "matcher.string.not.ends.with";
    /** Localization key requiring a value not to end with a suffix, ignoring case. */
    public static final String NOT_ENDS_WITH_IGNORE_CASE = "matcher.string.not.ends.with.ignore.case";
    /** Localization key requiring absence of a case-sensitive substring. */
    public static final String NOT_CONTAINS = "matcher.string.not.contains";
    /** Localization key requiring absence of a substring when case is ignored. */
    public static final String NOT_CONTAINS_IGNORE_CASE = "matcher.string.not.contains.ignore.case";
    /** Localization key for a regular expression mismatch. */
    public static final String NOT_MATCHES = "matcher.string.not.matches";

    private final Map<String, Function<String, Matcher<String>>> matchers = mapEntries(
            entry(EQUALS, Matchers::equalTo),
            entry(EQUALS_IGNORE_CASE, Matchers::equalToIgnoringCase),
            entry(EQUALS_IGNORE_WHITESPACE, Matchers::equalToCompressingWhiteSpace),
            entry(STARTS_WITH, Matchers::startsWith),
            entry(STARTS_WITH_IGNORE_CASE, Matchers::startsWithIgnoringCase),
            entry(ENDS_WITH, Matchers::endsWith),
            entry(ENDS_WITH_IGNORE_CASE, Matchers::endsWithIgnoringCase),
            entry(CONTAINS, Matchers::containsString),
            entry(CONTAINS_IGNORE_CASE, Matchers::containsStringIgnoringCase),
            entry(MATCHES, Matchers::matchesPattern),
            entry(NOT_EQUALS, value -> not(equalTo(value))),
            entry(NOT_EQUALS_IGNORE_CASE, value -> not(equalToIgnoringCase(value))),
            entry(NOT_EQUALS_IGNORE_WHITESPACE, value -> not(equalToCompressingWhiteSpace(value))),
            entry(NOT_STARTS_WITH, value -> not(startsWith(value))),
            entry(NOT_STARTS_WITH_IGNORE_CASE, value -> not(startsWithIgnoringCase(value))),
            entry(NOT_ENDS_WITH, value -> not(endsWith(value))),
            entry(NOT_ENDS_WITH_IGNORE_CASE, value -> not(endsWithIgnoringCase(value))),
            entry(NOT_CONTAINS, value -> not(containsString(value))),
            entry(NOT_CONTAINS_IGNORE_CASE, value -> not(containsStringIgnoringCase(value))),
            entry(NOT_MATCHES, value -> not(Matchers.matchesPattern(value)))
    );

    /**
     * Remove leading and trailing {@code "} or {@code '} and replace escaped
     * characters from the input string.
     *
     * @param input The input string.
     * @return The prepared string.
     */
    private static String prepareString(
            String input
    ) {
        return input
                .substring(1, input.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\'", "'");
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
    protected LinkedHashMap<String, Pattern> translatedExpressions(
            Locale locale
    ) {
        LinkedHashMap<String, Pattern> translatedExpressions = new LinkedHashMap<>();
        for (String expression : expressions()) {
            translatedExpressions.put(
                    expression,
                    Pattern.compile(
                            translateBundleExpression(
                                    locale,
                                    expression,
                                    "\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(\\\\.[^'\\\\]*)*)'"
                            )
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
                .map(exp -> exp.replace(VALUE_WILDCARD, "(\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(\\\\.[^'\\\\]*)*)')"))
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
        value = prepareString(value);
        return matchers.get(key).apply(value);
    }

}
