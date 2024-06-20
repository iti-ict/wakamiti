/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.datatypes;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.ExpressionMatcher;

import java.util.*;
import java.util.regex.Pattern;


/**
 * Serves as the base for providers.
 *
 */
public abstract class AbstractProvider {

    protected static final String VALUE_GROUP = "x";
    protected static final String VALUE_WILDCARD = "~x~";
    protected static final ResourceLoader resourceLoader = WakamitiAPI.instance().resourceLoader();

    private final String resource;
    private final Map<Locale, ResourceBundle> bundles = new HashMap<>();
    private final Map<Locale, Map<String, Pattern>> translatedExpressions = new HashMap<>();

    protected AbstractProvider(final String resource) {
        this.resource = resource;
    }

    /**
     * Retrieves the resource bundle for a specific locale.
     *
     * @param locale The locale for which the resource bundle is retrieved.
     * @return The resource bundle for the specified locale.
     */
    protected ResourceBundle bundle(Locale locale) {
        return bundles.computeIfAbsent(
                locale,
                bundleLocale -> resourceLoader.resourceBundle(resource, bundleLocale)
        );
    }

    /**
     * Retrieves an array of expressions.
     *
     * @return An array of expressions.
     */
    protected abstract String[] expressions();

    /**
     * Retrieves a map of translated expressions for a specific locale.
     *
     * @param locale The locale for which the expressions are translated.
     * @return A linked hash map of translated expressions.
     */
    protected abstract LinkedHashMap<String, Pattern> translatedExpressions(Locale locale);

    /**
     * Retrieves a linked list of regular expressions for a specific locale.
     *
     * @param locale The locale for which the regular expressions are retrieved.
     * @return A linked list of regular expressions.
     */
    public abstract LinkedList<String> regex(Locale locale);

    /**
     * Retrieves a key-value pair from the given expression for a specific locale.
     *
     * @param locale     The locale for which the matcher is retrieved.
     * @param expression The expression used to create the matcher.
     * @return An optional containing the key-value pair if one is created, or empty otherwise.
     */
    protected Optional<Pair<String, String>> fromExpression(Locale locale, String expression) {

        Map<String, Pattern> expressions = translatedExpressions
                .computeIfAbsent(locale, this::translatedExpressions);

        String key = null;
        String value = null;
        boolean found = false;

        // locate the proper _expression
        for (Map.Entry<String, Pattern> e : expressions.entrySet()) {
            key = e.getKey();
            Pattern pattern = e.getValue();
            java.util.regex.Matcher patternMatcher = pattern.matcher(expression);
            if (patternMatcher.find()) {
                found = true;
                value = (pattern.pattern().contains("<" + VALUE_GROUP + ">")
                        ? patternMatcher.group(VALUE_GROUP)
                        : null);
                break;
            }
        }

        if (found) {
            return Optional.of(new Pair<>(key, value));
        }
        return Optional.empty();

    }

    /**
     * Translates a bundle expression for a specific locale, replacing the value group.
     *
     * @param locale              The locale for which the expression is translated.
     * @param expression          The original expression.
     * @param valueGroupReplacing The value group replacement.
     * @return The translated bundle expression.
     */
    protected String translateBundleExpression(
            Locale locale,
            String expression,
            String valueGroupReplacing
    ) {
        String translatedExpression = bundle(locale).getString(expression);
        translatedExpression = ExpressionMatcher.computeRegularExpression(translatedExpression);
        String regexGroupExpression = "(?<" + VALUE_GROUP + ">" + valueGroupReplacing + ")";
        translatedExpression = translatedExpression.replace(VALUE_WILDCARD, regexGroupExpression);
        return "^" + translatedExpression + "$";
    }

}
