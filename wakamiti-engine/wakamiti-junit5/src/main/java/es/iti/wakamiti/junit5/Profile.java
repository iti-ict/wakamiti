/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Selects one or more Wakamiti configuration profiles for a JUnit 5 test
 * class.
 * <p>
 * The annotation is inherited, allowing a test hierarchy to share the same
 * profile selection unless a subclass declares its own value.
 * </p>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Profile {

    /**
     * Returns profile identifiers in application order.
     *
     * @return the configuration profile names to activate
     */
    String[] value();

}
