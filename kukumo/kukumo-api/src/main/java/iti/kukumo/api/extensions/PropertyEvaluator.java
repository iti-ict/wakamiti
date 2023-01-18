/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.api.extensions;

import iti.commons.jext.ExtensionPoint;
import iti.commons.jext.LoadStrategy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This interface allows using dynamic properties to make easier the
 * passing of information to the Scenario execution through the
 * syntax {@code ${[property description]}}.
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
@ExtensionPoint(loadStrategy = LoadStrategy.SINGLETON)
public abstract class PropertyEvaluator implements Contributor {

    public abstract Pattern pattern();

    /**
     * Eval all properties in the given string.
     *
     * @param value The step description
     * @return The result with the evaluations and the given string
     * evaluated
     */
    public final Result eval(String value) {
        Map<String, String> evaluations = new LinkedHashMap<>();
        Matcher matcher = pattern().matcher(value);
        while (matcher.find()) {
            String property = matcher.group();
            String evaluation = evalProperty(property, matcher);
            evaluations.putIfAbsent(property, evaluation);
            value = value.replaceFirst(Pattern.quote(property), evaluation);
        }
        return Result.of(evaluations, value);
    }

    protected abstract String evalProperty(String property, Matcher matcher);


    public static class Result {
        Map<String, String> evaluations;
        String value;

        public static PropertyEvaluator.Result of(Map<String, String> evaluations, String value) {
            PropertyEvaluator.Result result = new PropertyEvaluator.Result();
            result.evaluations = evaluations;
            result.value = value;
            return result;
        }

        public Map<String, String> evaluations() {
            return evaluations;
        }

        public String value() {
            return value;
        }
    }

}

