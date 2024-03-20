/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.datatypes.assertion;


import es.iti.wakamiti.core.backend.ExpressionMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * A provider for unary number assertions.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class UnaryNumberAssertProvider extends AbstractAssertProvider {

    public static final String NULL = "matcher.generic.null";
    public static final String NOT_NULL = "matcher.generic.not.null";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] expressions() {
        return new String[]{NULL, NOT_NULL};
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

        Matcher<?> matcher = null;
        if (NULL.equals(expression)) {
            matcher = Matchers.nullValue();
        } else if (NOT_NULL.equals(expression)) {
            matcher = Matchers.notNullValue();
        }
        return matcher;
    }

}