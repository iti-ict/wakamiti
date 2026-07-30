/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a contributor method as initialization logic to execute before a
 * scenario.
 * <p>
 * Multiple setup methods are ordered by {@link #order()}; lower values run
 * first. Methods that share the same order should not depend on a deterministic
 * relative order.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SetUp {

    /**
     * Defines this setup method's position in the initialization sequence.
     *
     * @return the execution order, where lower values have precedence; defaults
     * to {@code 100}
     */
    int order() default 100;

}
