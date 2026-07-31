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
 * Associates a contributor type with the resource bundle that contains its
 * localized step definitions and user-facing messages.
 * <p>
 * The value identifies a classpath resource using the naming convention
 * expected by Wakamiti's internationalization loader. Locale-specific variants
 * can then be selected without changing the annotated contributor.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface I18nResource {

    /**
     * Returns the base name of the internationalization resource.
     *
     * @return the resource-bundle base name, without a locale suffix
     */
    String value();

}
