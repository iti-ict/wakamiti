/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.core.backend.ExpressionMatcher;
import org.hamcrest.Matcher;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
public abstract class AbstractAssertProvider {

    public static final String MATCHERS_RESOURCE = "iti_wakamiti_core-matchers";
    protected static final String VALUE_GROUP = "x";
    protected static final String VALUE_WILDCARD = "~x~";
    protected static final ResourceLoader resourceLoader = WakamitiAPI.instance().resourceLoader();

    private final Map<Locale, ResourceBundle> bundles = new HashMap<>();
    private final Map<Locale, Map<String, Pattern>> translatedExpressions = new HashMap<>();


    public AbstractAssertProvider() {

    }

    public static List<String> getAllExpressions(Locale locale, String prefix) {
        ResourceBundle bundle = resourceLoader.resourceBundle(MATCHERS_RESOURCE, locale);
        return bundle.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .map(bundle::getString)
                .collect(Collectors.toList());
    }

    protected ResourceBundle bundle(Locale locale) {
        return bundles.computeIfAbsent(
                locale,
                bundleLocale -> resourceLoader.resourceBundle(MATCHERS_RESOURCE, bundleLocale)
        );
    }

    public Optional<Matcher<?>> matcherFromExpression(Locale locale, String expression) {

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

        Matcher<?> matcher = null;
        if (found) {
            try {
                matcher = createMatcher(locale, key, value);
            } catch (Exception e) {
                throw new WakamitiException(e);
            }
        }
        return Optional.ofNullable(matcher);

    }

    protected abstract String[] expressions();

    protected abstract LinkedHashMap<String, Pattern> translatedExpressions(Locale locale);

    protected abstract LinkedList<String> regex(Locale locale);

    protected abstract Matcher<?> createMatcher(
            Locale locale,
            String key,
            String value
    ) throws ParseException;

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