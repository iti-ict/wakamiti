/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


/**
 * Provides the Doc String functionality used by Wakamiti.
 */
public class DocString extends Node {

    private final String contentType;
    private final String content;

    /**
     * Creates a delimited block argument.
     *
     * @param location    position of the opening delimiter
     * @param contentType optional media type declared after the delimiter
     * @param content     de-indented block content
     */
    public DocString(
            Location location,
            String contentType,
            String content
    ) {
        super(location);
        this.contentType = contentType;
        this.content = content;
    }

    /**
     * Returns the block content without its delimiters.
     *
     * @return the parsed document body
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the optional media type declared by the document string.
     *
     * @return the content type, or {@code null} when omitted
     */
    public String getContentType() {
        return contentType;
    }

}
