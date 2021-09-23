/** @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com */
package iti.kukumo.core.datatypes.assertion;


import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;


public class UnaryNumberAssertProvider extends AbstractAssertProvider {

    public static final String NULL = "matcher.generic.null";
    public static final String NOT_NULL = "matcher.generic.not.null";


    @Override
    protected LinkedHashMap<String, Pattern> translatedExpressions(Locale locale) {
        String[] expressions = {
            NULL,
            NOT_NULL,
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
        } else if (NOT_NULL.equals(expression)) {
            matcher = Matchers.notNullValue();
        }
        return matcher;
    }


}