/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.plugins.jmeter.datatypes;


import es.iti.wakamiti.api.ExpressionMatcher;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.datatypes.AbstractProvider;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.plugins.jmeter.Metric;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public abstract class AbstractMetricProvider extends AbstractProvider {

    public static final String RESOURCE = "iti_wakamiti_metric";
    protected static final ResourceLoader resourceLoader = WakamitiAPI.instance().resourceLoader();

    protected AbstractMetricProvider() {
        super(RESOURCE);
    }

    /**
     * Retrieves all expressions with the given prefix for a specific locale.
     *
     * @param locale The locale for which expressions are retrieved.
     * @param prefix The prefix used to filter expressions.
     * @return A list of expressions with the specified prefix.
     */
    public static List<String> getAllExpressions(Locale locale, String prefix) {
        ResourceBundle bundle = resourceLoader.resourceBundle(RESOURCE, locale);
        return bundle.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .map(bundle::getString)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves a map of translated expressions for a specific locale.
     *
     * @param locale The locale for which the expressions are translated.
     * @return A linked hash map of translated expressions.
     */
    protected LinkedHashMap<String, Pattern> translatedExpressions(Locale locale) {
        LinkedHashMap<String, Pattern> translatedExpressions = new LinkedHashMap<>();
        for (String expression : expressions()) {
            translatedExpressions.put(
                    expression,
                    Pattern.compile("^" + bundle(locale).getString(expression) + "$")
            );
        }
        return translatedExpressions;
    }

    public LinkedList<String> regex(Locale locale) {
        return Arrays.stream(expressions())
                .map(exp -> ExpressionMatcher.computeRegularExpression(bundle(locale).getString(exp)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Retrieves a metric from the given expression for a specific locale.
     *
     * @param locale     The locale for which the metric is retrieved.
     * @param expression The expression used to create the metric.
     * @return An optional containing the metric if one is created, or empty otherwise.
     */
    public Optional<Metric<?>> metricFromExpression(Locale locale, String expression) {
        ThrowableFunction<String, Metric<?>> mapper = this::createMetric;
        return fromExpression(locale, expression).map(Pair::key).map(mapper);
    }

    /**
     * Creates a metric for a specific locale, key, and value.
     *
     * @param key    The key identifying the metric.
     * @return The created matcher.
     */
    protected abstract Metric<?> createMetric(String key);

}
