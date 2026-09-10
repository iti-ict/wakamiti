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
 * Marks a contributor method as cleanup logic to execute after an execution
 * scope.
 * <p>
 * Multiple teardown methods are ordered by {@link #order()}; lower values run
 * first. Cleanup implementations should remain safe when setup or scenario
 * execution terminated early.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TearDown {

    /**
     * Defines this teardown method's position in the cleanup sequence.
     *
     * @return the execution order, where lower values have precedence; defaults
     * to {@code 100}
     */
    int order() default 100;

    /**
     * Selects the execution scope in which this teardown operation runs.
     *
     * @return lifecycle scope; defaults to {@link Level#SCENARIO}
     */
    Level level() default Level.SCENARIO;

}
