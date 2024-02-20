/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;


/**
 * A simple utility class representing a pair of values.
 *
 * @param <T> The type of the key.
 * @param <U> The type of the value.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class Pair<T, U> {

    private final T key;
    private final U value;

    public Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Computes the value of the Pair using the provided function.
     *
     * @param <T>      The type of the key.
     * @param <U>      The type of the value.
     * @param function The function to compute the value.
     * @return A Function that computes the value for a given key.
     */
    public static <T, U> Function<T, Pair<T, U>> computeValue(Function<T, U> function) {
        return key -> new Pair<>(key, function.apply(key));
    }

    /**
     * Returns a Collector that accumulates elements into a LinkedHashMap.
     *
     * @param <T> The type of the key.
     * @param <U> The type of the value.
     * @return A Collector that accumulates elements into a LinkedHashMap.
     */
    public static <T, U> Collector<Pair<T, U>, ?, Map<T, U>> toMap() {
        return Collectors.toMap(Pair<T, U>::key, Pair<T, U>::value, (x, y) -> x, LinkedHashMap::new);
    }

    /**
     * Computes a Pair using the provided function.
     *
     * @param <T>      The type of the key.
     * @param <U>      The type of the value.
     * @param function The function to compute the value.
     * @return A Function that computes a Pair for a given key.
     */
    public static <T, U> Function<T, Pair<T, U>> compute(Function<T, U> function) {
        return first -> new Pair<>(first, function.apply(first));
    }

    /**
     * Gets the key of the pair.
     *
     * @return The key.
     */
    public T key() {
        return key;
    }

    /**
     * Gets the value of the pair.
     *
     * @return The value.
     */
    public U value() {
        return value;
    }

    /**
     * Maps the Pair using the provided BiFunction.
     *
     * @param <R> The type of the new key.
     * @param <P> The type of the new value.
     * @param map The mapping function.
     * @return A new Pair with mapped key and value.
     */
    public <R, P> Pair<R, P> map(BiFunction<T, U, Pair<R, P>> map) {
        return map.apply(key, value);
    }

    /**
     * Maps each element of the Pair using the provided Function.
     *
     * @param <R> The type of the new key and value.
     * @param map The mapping function.
     * @return A new Pair with mapped key and value.
     */
    public <R> Pair<R, R> mapEach(Function<Object, R> map) {
        R key = map.apply(this.key);
        R value = map.apply(this.value);
        return new Pair<>(key, value);
    }

    /**
     * Returns a string representation of the Pair.
     *
     * @return A string representation of the Pair.
     */
    @Override
    public String toString() {
        return "{" + key.toString() + ":" + value.toString() + "}";
    }

}