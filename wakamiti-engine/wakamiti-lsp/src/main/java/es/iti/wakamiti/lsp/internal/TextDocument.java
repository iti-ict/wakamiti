/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Store a text document allowing the manipulation of the text
 * using text ranges.
 */
public class TextDocument {

    private static final char EOL = '\n';

    // the raw text document, including eol characters
    private String rawDocument;
    // the positions of each eol character in the overall raw document
    private int[] endOfLines;

    /**
     * Creates an editable document and indexes its newline offsets.
     *
     * @param rawDocument complete text using newline characters as line
     *                    separators
     */
    public TextDocument(
            String rawDocument
    ) {
        this.rawDocument = rawDocument;
        this.endOfLines = locateEndOfLines(rawDocument);
    }

    /**
     * Extracts a zero-based line without its trailing newline.
     *
     * @param lineNumber zero-based line index
     * @return line content
     */
    public String extractLine(
            int lineNumber
    ) {
        int start = start(lineNumber);
        int end = start(lineNumber + 1);
        if (lineNumber >= 0 && lineNumber < endOfLines.length) {
            end--;
        }
        return rawDocument.substring(start, end);
    }

    /**
     * Extracts text in a half-open range: the start is included and the end is
     * excluded.
     *
     * @param range zero-based document range
     * @return selected text, possibly spanning newlines
     */
    public String extractRange(
            TextRange range
    ) {
        int start = start(range.startLine()) + range.startLinePosition();
        int end = start(range.endLine()) + range.endLinePosition();
        return rawDocument.substring(start, end);
    }

    /**
     * Replaces a half-open range and rebuilds the line-offset index.
     *
     * @param range range to remove
     * @param text  replacement text
     * @return this mutated document
     */
    public TextDocument replaceRange(
            TextRange range,
            String text
    ) {
        int start = start(range.startLine()) + range.startLinePosition();
        int end = start(range.endLine()) + range.endLinePosition();
        this.rawDocument = rawDocument.substring(0, start) + text + rawDocument.substring(end);
        this.endOfLines = locateEndOfLines(rawDocument);
        return this;
    }

    /**
     * Replaces one line while retaining its existing line terminator.
     *
     * @param lineNumber zero-based line index
     * @param line       replacement content without a newline
     * @return this mutated document
     */
    public TextDocument replaceLine(
            int lineNumber,
            String line
    ) {
        return replaceRange(
                TextRange.of(lineNumber, 0, lineNumber, extractLine(lineNumber).length()),
                line
        );
    }

    /**
     * Returns the complete current text including line terminators.
     *
     * @return raw document text
     */
    public String rawText() {
        return rawDocument;
    }

    /**
     * Indicates whether the document contains no indexed lines.
     *
     * @return {@code true} for empty text
     */
    public boolean isEmpty() {
        return numberOfLines() == 0;
    }

    /**
     * Counts logical lines, accounting for whether the text ends in a newline.
     *
     * @return number of addressable lines
     */
    public int numberOfLines() {
        if (endOfLines.length == 0) {
            return 0;
        }
        // the text may or not end with a eol char
        return endOfLines[endOfLines.length - 1] < rawDocument.length() - 1
                ? endOfLines.length + 1
                : endOfLines.length;
    }

    /**
     * Splits the current document into lines without terminators.
     *
     * @return lines in document order
     */
    public String[] extractLines() {
        String[] lines = new String[numberOfLines()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = extractLine(i);
        }
        return lines;
    }

    /**
     * Finds complete regular-expression matches across every line.
     *
     * @param pattern pattern applied independently to each line
     * @return matching segments in document order
     */
    public List<TextSegment> extractSegments(
            Pattern pattern
    ) {
        return extractSegments(pattern, 0);
    }

    /**
     * Finds a selected capture group across every line.
     *
     * @param pattern    line-oriented pattern
     * @param regexGroup capture group used as segment content
     * @return matching segments in document order
     */
    public List<TextSegment> extractSegments(
            Pattern pattern,
            int regexGroup
    ) {
        List<TextSegment> segments = new ArrayList<>();
        for (int lineNumber = 0; lineNumber < numberOfLines(); lineNumber++) {
            segments.addAll(extractSegments(lineNumber, pattern, regexGroup));
        }
        return segments;
    }

    /**
     * Finds complete matches on one line.
     *
     * @param lineNumber zero-based line index
     * @param pattern    pattern to apply
     * @return matching segments from left to right
     */
    public List<TextSegment> extractSegments(
            int lineNumber,
            Pattern pattern
    ) {
        return extractSegments(lineNumber, pattern, 0);
    }

    /**
     * Finds a selected capture group on one line.
     *
     * @param lineNumber zero-based line index
     * @param pattern    pattern to apply
     * @param regexGroup group returned as segment content
     * @return matching segments from left to right
     */
    public List<TextSegment> extractSegments(
            int lineNumber,
            Pattern pattern,
            int regexGroup
    ) {
        List<TextSegment> segments = new ArrayList<>();
        String line = extractLine(lineNumber);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            var content = matcher.group(regexGroup);
            var range = TextRange.of(lineNumber, matcher.start(), lineNumber, matcher.end());
            segments.add(range.withContent(content));
        }
        return segments;
    }

    private int start(
            int line
    ) {
        if (line <= 0) {
            return 0;
        }
        if (line > endOfLines.length) {
            return rawDocument.length();
        }
        return endOfLines[line - 1] + 1;
    }

    private static int[] locateEndOfLines(
            String rawDocument
    ) {
        int index = 0;
        int start = 0;
        int occurrences = -1;
        while (index != -1) {
            occurrences++;
            index = rawDocument.indexOf(EOL, start);
            start = index + 1;
        }
        int[] indexes = new int[occurrences];
        index = -1;
        for (int i = 0; i < occurrences; i++) {
            index = rawDocument.indexOf(EOL, index + 1);
            indexes[i] = index;
        }
        return indexes;
    }

    /**
     * Creates an independent document with the same current text.
     *
     * @return a mutable copy with its own line index
     */
    public TextDocument copy() {
        return new TextDocument(rawDocument);
    }

    /**
     * Returns a half-open range covering the complete document.
     *
     * @return range from the first character to the start of the line after
     * the last logical line
     */
    public TextRange wholeRange() {
        return TextRange.of(0, 0, numberOfLines(), 0);
    }

}
