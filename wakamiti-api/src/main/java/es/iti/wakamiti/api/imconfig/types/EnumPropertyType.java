/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.types;


import java.util.List;
import java.util.stream.Collectors;

import es.iti.wakamiti.api.imconfig.PropertyType;


/**
 * Property type that restricts values to a fixed case-insensitive set.
 */
public class EnumPropertyType implements PropertyType {

    private final List<String> values;

    /**
     * Creates a case-insensitive enumeration validator.
     * <p>
     * Accepted values are normalized to lowercase at construction time.
     * </p>
     *
     * @param values the non-empty collection of accepted textual values
     * @throws IllegalArgumentException if {@code values} is {@code null} or
     *                                  empty
     */
    public EnumPropertyType(
            List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Enumeration values cannot be empty");
        }
        this.values = values.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "enum";
    }

    @Override
    public String hint() {
        return "One of the following: " + String.join(", ", values);
    }

    /**
     * Checks whether the provided value belongs to the configured set.
     *
     * @param value input value to validate
     * @return {@code true} when the lowercase input matches one configured
     *         value
     * @throws NullPointerException when {@code value} is {@code null}
     */
    @Override
    public boolean accepts(
            String value
    ) {
        return values.contains(value.toLowerCase());
    }

}
