/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Declares one or more execution profiles for a Wakamiti JUnit test class.
 * <p>
 * The active profile is resolved from system properties:
 * <ul>
 *     <li>{@code wakamiti.junit.profile} (preferred)</li>
 *     <li>{@code wakamiti.profile} (fallback)</li>
 * </ul>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Profile {

    /**
     * One or more profile identifiers accepted by the test class.
     *
     * @return the resulting value
     */
    String[] value();

}
