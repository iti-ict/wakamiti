/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.WakamitiException;

import java.util.function.Function;


/**
 * Represents a function that accepts one argument and produces a result
 * and may throw checked exceptions.
 *
 * @param <T> The type of the input to the function.
 * @param <R> The type of the result of the function.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
@FunctionalInterface
public interface ThrowableFunction<T, R> extends Function<T, R> {

    /**
     * Returns an unchecked version of this ThrowableFunction.
     *
     * @param throwableFunction The ThrowableFunction to be unchecked.
     * @param <T>               The type of the input to the function.
     * @param <R>               The type of the result of the function.
     * @return An unchecked version of the given ThrowableFunction.
     */
    static <T, R> Function<T, R> unchecked(ThrowableFunction<T, R> throwableFunction) {
        return throwableFunction;
    }

    /**
     * Applies this function to the given argument, wrapping any
     * checked exceptions in an unchecked exception.
     *
     * @param t The input argument.
     * @return The result of applying the function.
     * @throws WakamitiException If an exception occurs during
     *                           the function application.
     */
    @Override
    default R apply(T t) {
        try {
            return applyThrowable(t);
        } catch (Exception e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Applies this function to the given argument, potentially
     * throwing a checked exception.
     *
     * @param t The input argument.
     * @return The result of applying the function.
     * @throws Exception If an exception occurs during the
     *                   function application.
     */
    R applyThrowable(T t) throws Exception;

    /**
     * Returns a composed function that first applies this
     * function to its input, and then applies the provided
     * function to the result.
     *
     * @param chainFunction The function to apply after this
     *                      function.
     * @param <U>           The type of the result of the composed
     *                      function.
     * @return A composed function that first applies this function
     * and then applies the chainFunction.
     */
    default <U> ThrowableFunction<T, U> andThen(ThrowableFunction<R, U> chainFunction) {
        return t -> chainFunction.apply(this.apply(t));
    }

}