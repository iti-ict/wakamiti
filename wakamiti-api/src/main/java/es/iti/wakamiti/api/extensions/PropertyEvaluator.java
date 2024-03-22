/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.commons.jext.LoadStrategy;
import es.iti.wakamiti.api.WakamitiAPI;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This interface allows using dynamic properties to make easier the passing of
 * information to the Scenario execution through the syntax
 * {@code ${[property description]}}.
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
@ExtensionPoint(loadStrategy = LoadStrategy.SINGLETON)
public abstract class PropertyEvaluator implements Contributor {

    /**
     * Makes the evaluation based on the given value and action.
     *
     * @param value  The string to evaluate.
     * @param action The evaluation action.
     * @return The result with the evaluations and the given string evaluated.
     */
    private static Result makeEval(String value, BiFunction<PropertyEvaluator, String, Result> action) {
        Map<String, String> evaluations = new LinkedHashMap<>();
        AtomicReference<String> result = new AtomicReference<>(value);
        WakamitiAPI.instance().extensionManager().getExtensions(PropertyEvaluator.class)
                .forEach(evaluator -> {
                    Result evalResult = action.apply(evaluator, result.get());
                    evalResult.evaluations().forEach(evaluations::putIfAbsent);
                    result.set(evalResult.value());
                });
        return Result.of(evaluations, result.get());
    }

    /**
     * Evaluates all properties in the given string using all available
     * {@link PropertyEvaluator}.
     *
     * @param value The string to evaluate.
     * @return The result with the evaluations and the given string evaluated.
     */
    public static Result makeEval(String value) {
        return makeEval(value, PropertyEvaluator::eval);
    }

    /**
     * Evaluates all properties in the given string using all available
     * {@link PropertyEvaluator} and ignoring any issue that occurs in the
     * process.
     *
     * @param value The string to evaluate.
     * @return The result with the evaluations and the given string evaluated.
     */
    public static Result makeEvalIfCan(String value) {
        return makeEval(value, (evaluator, currentValue) -> evaluator.evalOr(currentValue, p -> p));
    }

    /**
     * The property pattern that this {@link PropertyEvaluator} will evaluate.
     *
     * @return The property pattern.
     */
    public abstract Pattern pattern();

    /**
     * Evaluates all properties in the given string.
     *
     * @param value The string to evaluate.
     * @return The result with the evaluations and the given string evaluated.
     */
    public final Result eval(String value) {
        Map<String, String> evaluations = new LinkedHashMap<>();
        Matcher matcher = pattern().matcher(value);
        while (matcher.find()) {
            String property = matcher.group();
            String evaluation = evalProperty(property, matcher);
            evaluations.putIfAbsent(property, evaluation);
            value = value.replaceFirst(Pattern.quote(property), evaluation.replace("$", "\\$"));
        }
        return Result.of(evaluations, value);
    }

    /**
     * Evaluates all properties in the given string. If any issue occurs resolving a
     * property, the given alternative will be set.
     *
     * @param value               The string to evaluate.
     * @param propertyAlternative The alternative for handling unresolved properties.
     * @return The result with the evaluations and the given string evaluated.
     */
    public final Result evalOr(String value, UnaryOperator<String> propertyAlternative) {
        Map<String, String> evaluations = new LinkedHashMap<>();
        Matcher matcher = pattern().matcher(value);
        while (matcher.find()) {
            String property = matcher.group();
            String evaluation;
            try {
                evaluation = evalProperty(property, matcher);
            } catch (Exception e) {
                evaluation = propertyAlternative.apply(property);
            }
            evaluations.putIfAbsent(property, evaluation);
            value = value.replaceFirst(Pattern.quote(property), evaluation);
        }
        return Result.of(evaluations, value);
    }

    /**
     * Evaluates a single property of the global string.
     *
     * @param property The unevaluated property.
     * @param matcher  The {@link Matcher} of the global string.
     * @return The evaluated property result.
     */
    protected abstract String evalProperty(String property, Matcher matcher);

    /**
     * This container provides the evaluation value and a record of all
     * evaluations carried out in the process.
     */
    public static class Result {
        Map<String, String> evaluations;
        String value;

        public static Result of(Map<String, String> evaluations, String value) {
            Result result = new Result();
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

