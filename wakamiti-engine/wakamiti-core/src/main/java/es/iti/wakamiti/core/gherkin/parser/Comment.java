/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


/**
 * Provides the Comment functionality used by Wakamiti.
 */
public class Comment extends Node {

    private final String text;

    /**
     * Creates a source comment.
     *
     * @param location location of the comment marker
     * @param text     complete comment text as parsed from the source
     */
    public Comment(
            Location location,
            String text
    ) {
        super(location);
        this.text = text;
    }

    /**
     * Returns the comment text.
     *
     * @return the source text associated with the comment
     */
    public String getText() {
        return text;
    }

}
