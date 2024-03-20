/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import java.util.Optional;
import java.util.function.Function;


/**
 * A class representing a value that can be either a non-null value of type T or a fallback value of type U.
 *
 * <p>The class provides static factory methods for creating instances with a primary value or a fallback value.
 * It also offers methods for extracting the primary value, fallback value, and mapping between the two types.</p>
 *
 * @param <T> The type of the primary value.
 * @param <U> The type of the fallback value.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class Either<T, U> {

    private final Optional<T> value;
    private final U fallback;

    private Either(Optional<T> value, U fallback) {
        this.value = value;
        this.fallback = fallback;
    }

    /**
     * Creates an instance of Either with the specified primary value.
     *
     * @param value The primary value.
     * @param <T>   The type of the primary value.
     * @param <U>   The type of the fallback value.
     * @return An instance of Either with the specified primary value and no fallback.
     */
    public static <T, U> Either<T, U> of(T value) {
        return new Either<>(Optional.of(value), null);
    }

    /**
     * Creates an instance of Either with the specified Optional primary value and a fallback value.
     *
     * @param value    The Optional primary value.
     * @param fallback The fallback value.
     * @param <T>      The type of the primary value.
     * @param <U>      The type of the fallback value.
     * @return An instance of Either with the specified Optional primary value and fallback value.
     */
    public static <T, U> Either<T, U> of(Optional<T> value, U fallback) {
        return new Either<>(value, fallback);
    }

    /**
     * Creates an instance of Either with only a fallback value.
     *
     * @param fallback The fallback value.
     * @param <T>      The type of the primary value.
     * @param <U>      The type of the fallback value.
     * @return An instance of Either with no primary value and the specified fallback value.
     */
    public static <T, U> Either<T, U> fallback(U fallback) {
        return new Either<>(Optional.empty(), fallback);
    }

    /**
     * Gets the primary value wrapped in an Optional.
     *
     * @return The Optional containing the primary value.
     */
    public Optional<T> value() {
        return value;
    }

    /**
     * Gets the fallback value.
     *
     * @return The fallback value.
     */
    public U fallback() {
        return fallback;
    }

    /**
     * Returns the primary value if present; otherwise, applies the provided fallback mapping function to get a value.
     *
     * @param fallbackMapper The function to apply to the fallback value.
     * @return The primary value if present; otherwise, the result of applying the fallback mapping function.
     */
    public T valueOrMapFallback(Function<U, T> fallbackMapper) {
        return value.orElseGet(() -> fallbackMapper.apply(fallback));
    }

    /**
     * Maps the primary value to a new type using the provided mapping function, or returns the fallback value.
     *
     * @param valueMapper The function to apply to the primary value.
     * @return The result of applying the value mapping function, or the fallback value if the primary value is absent.
     */
    public U mapValueOrFallback(Function<T, U> valueMapper) {
        return value.map(valueMapper).orElse(fallback);
    }

    /**
     * Returns a string representation of the Either object.
     *
     * @return A string representation in the form "{primaryValue, fallbackValue}".
     */
    @Override
    public String toString() {
        return "{" + value.toString() + "," + fallback.toString() + "}";
    }

}