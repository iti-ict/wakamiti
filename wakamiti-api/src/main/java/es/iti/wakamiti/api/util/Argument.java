/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.extensions.PropertyEvaluator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;


/**
 * Represents an argument with the ability to resolve its value
 * using a provided mapper function.
 * <p>
 * An argument can be created using the static factory method
 * {@link #of(String, Function)}.
 * </p>
 * <p>
 * It allows the resolution of its value through the
 * {@link #resolve()} method, which internally uses the provided
 * mapper function. The resolution process also captures any
 * evaluations performed during the resolution.
 * </p>
 * <p>
 * This class is designed to be extended, and the resolution
 * process can be customized by overriding the {@link #doResolve()}
 * and {@link #resolveForEach(String)} methods.
 * </p>
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public class Argument {

    private final Map<String, String> evaluations = new LinkedHashMap<>();
    private String value;
    private Object evaluated;
    private Function<String, Object> mapper;

    /**
     * Static factory method to create an argument with the
     * specified value and mapper function.
     *
     * @param value  The value of the argument.
     * @param mapper The function to map the argument's value.
     * @return An Argument instance with the given value and mapper.
     */
    public static Argument of(String value, Function<String, Object> mapper) {
        Argument arg = new Argument();
        arg.value = value;
        arg.mapper = mapper;
        return arg;
    }

    /**
     * Resolves the argument's value using the provided mapper
     * function.
     *
     * @return The resolved value of the argument.
     */
    public final Object resolve() {
        evaluated = doResolve();
        return evaluated;
    }

    /**
     * Internal method to perform the resolution of the
     * argument's value. Override this method to customize the
     * resolution process.
     *
     * @return The resolved value of the argument.
     */
    protected Object doResolve() {
        return mapper.apply(resolveForEach(value));
    }

    /**
     * Internal method to resolve variables within the provided
     * value.
     *
     * @param value The value containing variables to be
     *              resolved.
     * @return The resolved value with evaluations captured.
     */
    protected final String resolveForEach(String value) {
        PropertyEvaluator.Result result = PropertyEvaluator.makeEval(value);
        result.evaluations().forEach(evaluations::putIfAbsent);
        return result.value();
    }

    /**
     * Gets the original value of the argument.
     *
     * @return The original value of the argument.
     */
    public String value() {
        return value;
    }

    /**
     * Gets the resolved value of the argument.
     *
     * @return The resolved value of the argument.
     */
    public Object evaluated() {
        return evaluated;
    }

    /**
     * Gets the evaluations performed during the resolution
     * of the argument.
     *
     * @return A map of evaluations where keys are variable
     * names and values are their resolved values.
     */
    public Map<String, String> evaluations() {
        return evaluations;
    }

    /**
     * Returns a string representation of the argument.
     *
     * @return A string representation of the argument.
     */
    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
