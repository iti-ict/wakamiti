/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;


/**
 * Provides the Text Range functionality used by Wakamiti.
 */
public class TextRange {

    /**
     * Creates a zero-based half-open text range.
     *
     * @param startLine         first included line
     * @param startLinePosition first included character
     * @param endLine           line containing the excluded end position
     * @param endLinePosition   first excluded character
     * @return the populated range
     */
    public static TextRange of(
            int startLine,
            int startLinePosition,
            int endLine,
            int endLinePosition
    ) {
        TextRange range = new TextRange();
        range.startLine = startLine;
        range.startLinePosition = startLinePosition;
        range.endLine = endLine;
        range.endLinePosition = endLinePosition;
        return range;
    }

    /**
     * Creates the sentinel range used when no source location exists.
     *
     * @return a range whose coordinates are all {@code -1}
     */
    public static TextRange empty() {
        return of(-1, -1, -1, -1);
    }

    private int startLine;
    private int startLinePosition;
    private int endLine;
    private int endLinePosition;

    /**
     * @return the zero-based start line
     */
    public int startLine() {
        return startLine;
    }

    /**
     * @return the zero-based start character
     */
    public int startLinePosition() {
        return startLinePosition;
    }

    /**
     * @return the zero-based end line
     */
    public int endLine() {
        return endLine;
    }

    /**
     * @return the excluded end character
     */
    public int endLinePosition() {
        return endLinePosition;
    }

    /**
     * Indicates whether start and end coordinates are identical.
     *
     * @return {@code true} for a zero-width range
     */
    public boolean isEmpty() {
        return startLine == endLine && startLinePosition == endLinePosition;
    }

    /**
     * Indicates whether both endpoints lie on the same line.
     *
     * @return {@code true} for a single-line range
     */
    public boolean isSingleLine() {
        return startLine == endLine;
    }

    /**
     * Tests whether this range contains the other range's start position.
     *
     * @param range range whose start is tested
     * @return {@code true} when that position lies strictly inside this range
     */
    public boolean intersect(
            TextRange range
    ) {
        if (startLine < range.startLine && endLine > range.startLine) {
            return true;
        }
        return (
                endLine == range.startLine
                        && startLinePosition < range.startLinePosition && endLinePosition > range.startLinePosition
        );
    }

    /**
     * Associates selected content with this range.
     *
     * @param content text represented by the range
     * @return a new text segment
     */
    public TextSegment withContent(
            String content
    ) {
        return TextSegment.of(this, content);
    }

    @Override
    public String toString() {
        return String.format("[%d,%d - %d,%d]", startLine, startLinePosition, endLine, endLinePosition);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + endLine;
        result = prime * result + endLinePosition;
        result = prime * result + startLine;
        result = prime * result + startLinePosition;
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
        TextRange other = (TextRange) obj;
        if (endLine != other.endLine) {
            return false;
        }
        if (endLinePosition != other.endLinePosition) {
            return false;
        }
        if (startLine != other.startLine) {
            return false;
        }
        return startLinePosition == other.startLinePosition;
    }

    /**
     * Converts this value to the LSP4J range representation.
     *
     * @return a range with equivalent zero-based positions
     */
    public Range toLspRange() {
        return new Range(
                new Position(startLine, startLinePosition),
                new Position(endLine, endLinePosition)
        );
    }

}
