/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.api.datatypes.AbstractProvider;
import org.hamcrest.Matcher;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


/**
 * Serves as the base for assertion providers.
 * Provides functionality for retrieving and creating matchers from expressions.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public abstract class AbstractAssertProvider extends AbstractProvider {

    public static final String MATCHERS_RESOURCE = "iti_wakamiti_core-matchers";

    protected AbstractAssertProvider() {
        super(MATCHERS_RESOURCE);
    }

    /**
     * Retrieves all expressions with the given prefix for a specific locale.
     *
     * @param locale The locale for which expressions are retrieved.
     * @param prefix The prefix used to filter expressions.
     * @return A list of expressions with the specified prefix.
     */
    public static List<String> getAllExpressions(Locale locale, String prefix) {
        ResourceBundle bundle = resourceLoader.resourceBundle(MATCHERS_RESOURCE, locale);
        return bundle.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .map(bundle::getString)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a matcher from the given expression for a specific locale.
     *
     * @param locale     The locale for which the matcher is retrieved.
     * @param expression The expression used to create the matcher.
     * @return An optional containing the matcher if one is created, or empty otherwise.
     */
    public Optional<Matcher<?>> matcherFromExpression(Locale locale, String expression) {
        ThrowableFunction<Pair<String, String>, Matcher<?>> mapper = p -> createMatcher(locale, p.key(), p.value());
        return fromExpression(locale, expression).map(mapper);
    }

    /**
     * Creates a matcher for a specific locale, key, and value.
     *
     * @param locale The locale for which the matcher is created.
     * @param key    The key identifying the matcher.
     * @param value  The value used in the matcher.
     * @return The created matcher.
     * @throws ParseException If an error occurs during matcher creation.
     */
    protected abstract Matcher<?> createMatcher(
            Locale locale,
            String key,
            String value
    ) throws ParseException;

}