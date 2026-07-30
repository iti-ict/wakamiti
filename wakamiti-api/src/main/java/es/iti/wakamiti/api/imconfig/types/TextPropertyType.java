/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.types;


import java.util.regex.Pattern;

import es.iti.wakamiti.api.imconfig.PropertyType;


/**
 * Provides the Text Property Type functionality used by Wakamiti.
 */
public class TextPropertyType implements PropertyType {

    private final Pattern pattern;

    /**
     * Creates a text validator with an optional regular-expression constraint.
     *
     * @param pattern the expression that the complete value must match, or
     *                {@code null} to accept any text
     * @throws java.util.regex.PatternSyntaxException if {@code pattern} is
     *                                               invalid
     */
    public TextPropertyType(
            String pattern
    ) {
        this.pattern = (pattern == null ? null : Pattern.compile(pattern));
    }

    @Override
    public String name() {
        return "text";
    }

    @Override
    public boolean accepts(
            String value
    ) {
        return pattern == null || pattern.matcher(value).matches();
    }

    @Override
    public String hint() {
        return pattern == null ? "Any text" : "Text satisfying regex //" + pattern + "//";
    }

    /**
     * Returns the regular expression used to validate values.
     *
     * @return the configured expression
     * @throws NullPointerException if this type was created without a pattern
     */
    public String pattern() {
        return this.pattern.pattern();
    }

}
