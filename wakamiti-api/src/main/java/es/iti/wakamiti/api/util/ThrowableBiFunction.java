/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.WakamitiException;

import java.util.Objects;
import java.util.function.BiFunction;


/**
 * Represents a function that accepts two arguments and produces a result
 * and may throw checked exceptions.
 * This is the two-arity specialization of {@link ThrowableFunction}.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 *
 * @see ThrowableFunction
 */
@FunctionalInterface
public interface ThrowableBiFunction<T, U, R> extends BiFunction<T, U, R> {

    /**
     * Returns an unchecked version of this ThrowableFunction.
     *
     * @param throwableFunction The ThrowableFunction to be unchecked.
     * @param <T>               the type of the first argument to the function.
     * @param <U>               the type of the second argument to the function.
     * @param <R>               the type of the result of the function.
     * @return An unchecked version of the given ThrowableFunction.
     */
    static <T, U, R> BiFunction<T, U, R> unchecked(ThrowableBiFunction<T, U, R> throwableFunction) {
        return throwableFunction;
    }

    /**
     * Applies this function to the given argument, wrapping any
     * checked exceptions in an unchecked exception.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return The result of applying the function.
     * @throws WakamitiException If an exception occurs during
     *                           the function application.
     */
    @Override
    default R apply(T t, U u) {
        try {
            return applyThrowable(t, u);
        } catch (Exception e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Applies this function to the given argument, potentially
     * throwing a checked exception.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return The result of applying the function.
     * @throws Exception If an exception occurs during the
     *                   function application.
     */
    R applyThrowable(T t, U u) throws Exception;

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> ThrowableBiFunction<T, U, V> andThen(ThrowableFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }

}
