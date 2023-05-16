/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package es.iti.wakamiti.api.util;


import java.util.Optional;
import java.util.function.Function;


public class Either<T, U> {


    public static <T,U> Either<T,U> of(T value) {
        return new Either<>(Optional.of(value),null);
    }

    public static <T,U> Either<T,U> of(Optional<T> value, U fallback) {
        return new Either<>(value,fallback);
    }

    public static <T,U> Either<T,U> fallback(U fallback) {
        return new Either<>(Optional.empty(),fallback);
    }


    private final Optional<T> value;
    private final U fallback;


    private Either(Optional<T> value, U fallback) {
        this.value = value;
        this.fallback = fallback;
    }


    public Optional<T> value() {
        return value;
    }


    public U fallback() {
        return fallback;
    }


    public T valueOrMapFallback(Function<U,T> fallbackMapper) {
        return value.orElseGet(()->fallbackMapper.apply(fallback));
    }

    public U mapValueOrFallback(Function<T,U> valueMapper) {
        return value.map(valueMapper).orElse(fallback);
    }


    @Override
    public String toString() {
        return "{" + value.toString() + "," + fallback.toString() + "}";
    }



}