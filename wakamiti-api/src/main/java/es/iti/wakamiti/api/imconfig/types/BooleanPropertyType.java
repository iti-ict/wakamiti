/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.types;


import es.iti.wakamiti.api.imconfig.PropertyType;


/**
 * Property type that accepts canonical boolean literals.
 * <p>
 * Accepted values are lowercase {@code "true"} and {@code "false"} only.
 * </p>
 */
public class BooleanPropertyType implements PropertyType {

    @Override
    public String name() {
        return "boolean";
    }

    /**
     * Validates whether a textual value is an accepted boolean literal.
     *
     * @param value value to validate, may be {@code null}
     * @return {@code true} only for lowercase {@code "true"} or
     *         {@code "false"}
     */
    @Override
    public boolean accepts(
            String value
    ) {
        return "true".equals(value) || "false".equals(value);
    }

    @Override
    public String hint() {
        return "true | false";
    }

}
