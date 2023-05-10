/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/** @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com */
package iti.wakamiti.core.datatypes.assertion;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;


public class UnaryStringAssertProvider extends AbstractAssertProvider {

    public static final String NULL = "matcher.generic.null";
    public static final String EMPTY = "matcher.generic.empty";
    public static final String NULL_EMPTY = "matcher.generic.null.empty";

    public static final String NOT_NULL = "matcher.generic.not.null";
    public static final String NOT_EMPTY = "matcher.generic.not.empty";
    public static final String NOT_NULL_EMPTY = "matcher.generic.not.null.empty";


    @Override
    protected LinkedHashMap<String, Pattern> translatedExpressions(Locale locale) {
        String[] expressions = {
            NULL,
            EMPTY,
            NULL_EMPTY,
            NOT_NULL,
            NOT_EMPTY,
            NOT_NULL_EMPTY
        };
        LinkedHashMap<String, Pattern> translatedExpressions = new LinkedHashMap<>();
        for (String key : expressions) {
            translatedExpressions
                .put(key, Pattern.compile(translateBundleExpression(locale, key, "")));
        }
        return translatedExpressions;
    }


    @Override
    protected Matcher<?> createMatcher(Locale locale, String expression, String value) {

        Matcher<?> matcher = null;
        if (NULL.equals(expression)) {
            matcher = Matchers.nullValue();

        } else if (EMPTY.equals(expression)) {
            List<Matcher<? super Object>> matchers = new ArrayList<>();
            matchers.add(matcher(Matchers.emptyString(), String.class));
            matchers.add(matcherCollection(Matchers.empty(), Collection.class));
            matcher = Matchers.anyOf(matchers);

        } else if (NULL_EMPTY.equals(expression)) {
            List<Matcher<? super Object>> matchers = new ArrayList<>();
            matchers.add(matcher(Matchers.emptyOrNullString(), String.class));
            matchers.add(matcherCollection(Matchers.empty(), Collection.class));
            matcher = Matchers.anyOf(matchers);

        } else if (NOT_NULL.equals(expression)) {
            matcher = Matchers.notNullValue();

        } else if (NOT_EMPTY.equals(expression)) {
            List<Matcher<? super Object>> matchers = new ArrayList<>();
            matchers.add(matcher(Matchers.emptyString(), String.class));
            matchers.add(matcherCollection(Matchers.empty(), Collection.class));
            matcher = Matchers.anyOf(matchers);
            matcher = Matchers.not(matcher);

        } else if (NOT_NULL_EMPTY.equals(expression)) {
            List<Matcher<? super Object>> matchers = new ArrayList<>();
            matchers.add(matcher(Matchers.emptyOrNullString(), String.class));
            matchers.add(matcherCollection(Matchers.empty(), Collection.class));
            matcher = Matchers.anyOf(matchers);
            matcher = Matchers.not(matcher);
        }
        return matcher;
    }




	@SuppressWarnings("unchecked")
    private <T> Matcher<? super Object> matcher(
        Matcher<? super T> matcher,
        Class<? super T> expectedType
    ) {
        return (Matcher<? super Object>) Matchers.allOf(Matchers.instanceOf(expectedType), matcher);
    }


    @SuppressWarnings("unchecked")
	private Matcher<? super Object> matcherCollection(
    	Matcher<? super Collection<? extends Object>> matcher,
		Class<? super Collection<?>> expectedType
	) {
    	return (Matcher<? super Object>) Matchers.allOf(Matchers.instanceOf(expectedType), matcher);
	}

}