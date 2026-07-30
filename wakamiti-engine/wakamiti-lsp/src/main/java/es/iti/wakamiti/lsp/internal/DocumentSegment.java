/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import org.eclipse.lsp4j.Range;


/**
 * Provides the Document Segment functionality used by Wakamiti.
 */
public class DocumentSegment {

    private final String uri;
    private final Range range;
    private final String content;

    /**
     * Creates a source fragment addressable through the language-server
     * protocol.
     *
     * @param uri     document URI
     * @param range   LSP range occupied by the fragment
     * @param content fragment text
     */
    public DocumentSegment(
            String uri,
            Range range,
            String content
    ) {
        this.uri = uri;
        this.range = range;
        this.content = content;
    }

    /**
     * @return the fragment text
     */
    public String content() {
        return content;
    }

    /**
     * @return the fragment's LSP range
     */
    public Range range() {
        return range;
    }

    /**
     * @return the URI of the containing document
     */
    public String uri() {
        return uri;
    }

}
