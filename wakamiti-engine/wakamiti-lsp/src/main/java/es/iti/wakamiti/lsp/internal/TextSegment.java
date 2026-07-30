/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


/**
 * Provides the Text Segment functionality used by Wakamiti.
 */
public final class TextSegment {

    /**
     * Creates content associated with a source range.
     *
     * @param range   range occupied by the content
     * @param content selected text
     * @return a new segment
     */
    public static TextSegment of(
            TextRange range,
            String content
    ) {
        return new TextSegment(range, content);
    }

    private final TextRange range;
    private final String content;

    private TextSegment(
            TextRange range,
            String content
    ) {
        this.range = range;
        this.content = content;
    }

    /**
     * @return the source range occupied by this segment
     */
    public TextRange range() {
        return range;
    }

    /**
     * @return the text selected by the range
     */
    public String content() {
        return content;
    }

    @Override
    public String toString() {
        return range + "<" + content + ">";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((range == null) ? 0 : range.hashCode());
        return result;
    }

    @Override
    public boolean equals(
            Object obj
    ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextSegment other = (TextSegment) obj;
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        if (range == null) {
            return other.range == null;
        } else {
            return range.equals(other.range);
        }
    }

}
