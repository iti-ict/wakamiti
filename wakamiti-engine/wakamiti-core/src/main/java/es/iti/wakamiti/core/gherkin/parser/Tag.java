/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


/**
 * Provides the Tag functionality used by Wakamiti.
 */
public class Tag extends Node {

    private final String name;

    /**
     * Creates a parsed Gherkin tag.
     *
     * @param location position of the tag marker
     * @param name     complete tag name, including the leading marker when
     *                 retained by the lexer
     */
    public Tag(
            Location location,
            String name
    ) {
        super(location);
        this.name = name;
    }

    /**
     * Returns the tag identifier as represented by the parser.
     *
     * @return the tag name
     */
    public String getName() {
        return name;
    }

}
