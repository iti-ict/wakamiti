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
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * A provider for unary string assertions.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class UnaryStringAssertProvider extends AbstractAssertProvider {

    public static final String NULL = "matcher.generic.null";
    public static final String EMPTY = "matcher.generic.empty";
    public static final String NULL_EMPTY = "matcher.generic.null.empty";

    public static final String NOT_NULL = "matcher.generic.not.null";
    public static final String NOT_EMPTY = "matcher.generic.not.empty";
    public static final String NOT_NULL_EMPTY = "matcher.generic.not.null.empty";

    private final Map<String, Supplier<Matcher<?>>> matchers = new LinkedHashMap<>();

    public UnaryStringAssertProvider() {
        matchers.put(NULL, Matchers::nullValue);
        matchers.put(EMPTY, () -> Matchers.anyOf(
                matcher(Matchers.emptyString(), String.class),
                matcherCollection(Matchers.empty(), Collection.class)));
        matchers.put(NULL_EMPTY, () -> Matchers.anyOf(
                matcher(Matchers.emptyOrNullString(), String.class),
                matcherCollection(Matchers.empty(), Collection.class)));
        matchers.put(NOT_NULL, Matchers::notNullValue);
        matchers.put(NOT_EMPTY, () -> Matchers.not(matchers.get(EMPTY)));
        matchers.put(NOT_NULL_EMPTY, () -> Matchers.not(matchers.get(NULL_EMPTY)));
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
    protected LinkedList<String> regex(Locale locale) {
        return Arrays.stream(expressions())
                .map(exp -> ExpressionMatcher.computeRegularExpression(bundle(locale).getString(exp)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Matcher<?> createMatcher(Locale locale, String expression, String value) {
        return matchers.get(expression).get();
    }

    /**
     * Generates a matcher for the specified type.
     *
     * @param matcher      The matcher to use.
     * @param expectedType The expected type for the matcher.
     * @param <T>          The type of the matcher.
     * @return The generated matcher.
     */
    @SuppressWarnings("unchecked")
    private <T> Matcher<? super Object> matcher(
            Matcher<? super T> matcher,
            Class<? super T> expectedType
    ) {
        return (Matcher<? super Object>) Matchers.allOf(Matchers.instanceOf(expectedType), matcher);
    }

    /**
     * Generates a matcher for collections.
     *
     * @param matcher      The matcher to use for collections.
     * @param expectedType The expected type for the matcher.
     * @return The generated matcher.
     */
    @SuppressWarnings("unchecked")
    private Matcher<? super Object> matcherCollection(
            Matcher<? super Collection<Object>> matcher,
            Class<? super Collection<?>> expectedType
    ) {
        return (Matcher<Object>) Matchers.allOf(Matchers.instanceOf(expectedType), matcher);
    }

}