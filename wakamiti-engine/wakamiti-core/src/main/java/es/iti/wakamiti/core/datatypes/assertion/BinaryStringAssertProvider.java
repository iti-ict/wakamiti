/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import es.iti.wakamiti.core.backend.ExpressionMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.MapUtils.*;
import static org.hamcrest.Matchers.*;


/**
 * A provider for binary string assertions.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class BinaryStringAssertProvider extends AbstractAssertProvider {

    public static final String EQUALS = "matcher.string.equals";
    public static final String EQUALS_IGNORE_CASE = "matcher.string.equals.ignore.case";
    public static final String EQUALS_IGNORE_WHITESPACE = "matcher.string.equals.ignore.whitespace";
    public static final String STARTS_WITH = "matcher.string.starts.with";
    public static final String STARTS_WITH_IGNORE_CASE = "matcher.string.starts.with.ignore.case";
    public static final String ENDS_WITH = "matcher.string.ends.with";
    public static final String ENDS_WITH_IGNORE_CASE = "matcher.string.ends.with.ignore.case";
    public static final String CONTAINS = "matcher.string.contains";
    public static final String CONTAINS_IGNORE_CASE = "matcher.string.contains.ignore.case";

    public static final String NOT_EQUALS = "matcher.string.not.equals";
    public static final String NOT_EQUALS_IGNORE_CASE = "matcher.string.not.equals.ignore.case";
    public static final String NOT_EQUALS_IGNORE_WHITESPACE = "matcher.string.not.equals.ignore.whitespace";
    public static final String NOT_STARTS_WITH = "matcher.string.not.starts.with";
    public static final String NOT_STARTS_WITH_IGNORE_CASE = "matcher.string.not.starts.with.ignore.case";
    public static final String NOT_ENDS_WITH = "matcher.string.not.ends.with";
    public static final String NOT_ENDS_WITH_IGNORE_CASE = "matcher.string.not.ends.with.ignore.case";
    public static final String NOT_CONTAINS = "matcher.string.not.contains";
    public static final String NOT_CONTAINS_IGNORE_CASE = "matcher.string.not.contains.ignore.case";

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
            entry(NOT_EQUALS, value -> not(equalTo(value))),
            entry(NOT_EQUALS_IGNORE_CASE, value -> not(equalToIgnoringCase(value))),
            entry(NOT_EQUALS_IGNORE_WHITESPACE, value -> not(equalToCompressingWhiteSpace(value))),
            entry(NOT_STARTS_WITH, value -> not(startsWith(value))),
            entry(NOT_STARTS_WITH_IGNORE_CASE, value -> not(startsWithIgnoringCase(value))),
            entry(NOT_ENDS_WITH, value -> not(endsWith(value))),
            entry(NOT_ENDS_WITH_IGNORE_CASE, value -> not(endsWithIgnoringCase(value))),
            entry(NOT_CONTAINS, value -> not(containsString(value))),
            entry(NOT_CONTAINS_IGNORE_CASE, value -> not(containsStringIgnoringCase(value)))
    );

    /**
     * Remove leading and trailing {@code "} or {@code '} and replace escaped
     * characters from the input string.
     *
     * @param input The input string.
     * @return The prepared string.
     */
    private static String prepareString(String input) {
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
    protected LinkedHashMap<String, Pattern> translatedExpressions(Locale locale) {
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
    protected LinkedList<String> regex(Locale locale) {
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