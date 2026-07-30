/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import java.util.function.UnaryOperator;


/**
 * Represents a document for a test plan node.
 */
public class Document implements PlanNodeData {

    private final String content;
    private final String contentType;

    /**
     * Creates a document whose media type is unspecified.
     *
     * @param content the document body
     */
    public Document(
            String content
    ) {
        this.content = content;
        this.contentType = null;
    }

    /**
     * Creates a document with an explicit media type.
     *
     * @param content     the document body
     * @param contentType the media type or format identifier, or {@code null}
     *                    when it is unknown
     */
    public Document(
            String content,
            String contentType
    ) {
        this.content = content;
        this.contentType = contentType;
    }

    /**
     * Returns the document body exactly as supplied.
     *
     * @return the document content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the format associated with the document body.
     *
     * @return the media type or format identifier, or {@code null} when
     * unspecified
     */
    public String getContentType() {
        return contentType;
    }

    @Override
    public PlanNodeData copy() {
        return new Document(content, contentType);
    }

    @Override
    public PlanNodeData copyReplacingVariables(
            UnaryOperator<String> replacer
    ) {
        return new Document(replacer.apply(content), contentType);
    }

}
