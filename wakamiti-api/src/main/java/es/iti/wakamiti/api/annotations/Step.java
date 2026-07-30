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
 * Exposes a contributor method as an executable Wakamiti step.
 * <p>
 * The annotation connects a localized step definition with the Java method
 * that implements it. Captured arguments are mapped to method parameters using
 * {@link #args()}, while {@link #classifier()} can separate otherwise
 * overlapping definitions.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Step {

    /**
     * Returns the key or expression used to locate the step definition.
     *
     * @return the step definition identifier
     */
    String value();

    /**
     * Returns the logical names assigned to captured step arguments.
     * <p>
     * Names are declared in method-parameter order and allow contributors to
     * describe the meaning of values extracted from the step text.
     * </p>
     *
     * @return the argument names, or an empty array when the step has no named
     * captures
     */
    String[] args() default {};

    /**
     * Returns an optional discriminator for selecting this definition among
     * steps with the same expression.
     *
     * @return the classifier, or an empty string when no discriminator is
     * required
     */
    String classifier() default "";

}
