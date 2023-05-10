/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api.util;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class Pair<T, U> {

    private final T key;
    private final U value;


    public Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }


    public T key() {
        return key;
    }


    public U value() {
        return value;
    }


    @Override
    public String toString() {
        return "{" + key.toString() + ":" + value.toString() + "}";
    }


    public static <T, U> Function<T, Pair<T, U>> computeValue(Function<T, U> function) {
        return key -> new Pair<>(key, function.apply(key));
    }


    public static <T,U> Collector<Pair<T, U>, ?, Map<T, U>> toMap() {
        return Collectors.toMap(Pair<T, U>::key, Pair<T, U>::value, (x,y)->x, LinkedHashMap::new);
    }


    public static <T, U> Function<T, Pair<T, U>> compute(Function<T, U> function) {
        return first -> new Pair<>(first, function.apply(first));
    }


}