/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import static es.iti.wakamiti.lsp.internal.GherkinFormatter.Type.DOCUMENT_CONTENT;
import static es.iti.wakamiti.lsp.internal.GherkinFormatter.Type.FEATURE;
import static es.iti.wakamiti.lsp.internal.GherkinFormatter.Type.FEATURE_CONTENT;
import static es.iti.wakamiti.lsp.internal.GherkinFormatter.Type.STEP;
import static es.iti.wakamiti.lsp.internal.GherkinFormatter.Type.TABLE_HEADER;
import static es.iti.wakamiti.lsp.internal.GherkinFormatter.Type.TABLE_ROW;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.iti.wakamiti.core.gherkin.parser.GherkinDialect;


/**
 * Provides the Gherkin Formatter functionality used by Wakamiti.
 */
public final class GherkinFormatter {

    private GherkinFormatter() {
    }

    private static final String TRIPLE_QUOTE = "\"\"\"";
    private static final String TRIPLE_BACKQUOTE = "```";

    /**
     * Defines the values supported by Type.
     */
    enum Type {

        EMPTY(false, 0, 0),
        COMMENT(false, 0, 1),
        TAG(false, 0, 1),
        FEATURE(true, 0, 0),
        FEATURE_CONTENT(true, 1, 0),
        DESCRIPTION(false, 0, -1),
        STEP(true, 2, 0),
        TABLE_HEADER(false, 2, 0),
        TABLE_ROW(false, 2, 0),
        DOCUMENT_START(false, 2, 0),
        DOCUMENT_END(false, 2, 0),
        DOCUMENT_CONTENT(false, 2, 0);

        Type(
                boolean isKeyword,
                int level,
                int lookupDirection
        ) {
            this.isKeyword = isKeyword;
            this.level = level;
            this.lookupDirection = lookupDirection;
        }

        /** Structural indentation level assigned to this line type. */
        public final int level;
        /** Whether this line type represents a dialect keyword. */
        public final boolean isKeyword;
        /** Direction used to inherit indentation: previous, none or next keyword. */
        public final int lookupDirection;

    }

    /**
     * Formats Gherkin source without modifying the supplied document map.
     * <p>
     * Structural lines are indented according to feature, scenario and step
     * nesting; redundant spaces outside doc strings are collapsed; and data
     * table columns are padded to a common width. Doc-string content is
     * preserved except when indentation must be added.
     *
     * @param documentMap parsed source and active dialect
     * @param tabSize spaces per structural indentation level
     * @return the complete formatted source
     * @throws IllegalArgumentException if {@code tabSize} is negative
     */
    public static String format(
            GherkinDocumentMap documentMap,
            int tabSize
    ) {
        var document = documentMap.document().copy();
        Type[] lineTypes = classifyLines(documentMap);
        int[] marginLevels = analyzeMarginLevels(lineTypes);
        String margin = " ".repeat(tabSize);
        formatMargins(document, lineTypes, marginLevels, margin);
        formatTables(document, lineTypes, margin);
        return document.rawText();
    }

    private static void formatMargins(
            TextDocument document,
            Type[] lineTypes,
            int[] marginLevels,
            String margin
    ) {
        String marginLevel;
        String line;
        String formattedLine;
        for (int lineNumber = 0; lineNumber < document.numberOfLines(); lineNumber++) {
            marginLevel = margin.repeat(marginLevels[lineNumber]);
            line = document.extractLine(lineNumber);
            if (lineTypes[lineNumber] == DOCUMENT_CONTENT && line.startsWith(marginLevel)) {
                formattedLine = line;
            } else {
                formattedLine = marginLevel + line.strip().replaceAll(" +", " ");
            }
            document.replaceLine(lineNumber, formattedLine);
        }
    }

    private static void formatTables(
            TextDocument document,
            Type[] lineTypes,
            String margin
    ) {
        int lineNumber = 0;
        while (lineNumber < document.numberOfLines()) {
            if (lineTypes[lineNumber] == TABLE_HEADER) {
                lineNumber += formatTable(document, lineTypes, lineNumber, margin);
            } else {
                lineNumber++;
            }
        }
    }

    private static int formatTable(
            TextDocument document,
            Type[] lineTypes,
            int headerLineNumber,
            String margin
    ) {
        String header = document.extractLine(headerLineNumber);
        int columns = (int) header.chars().filter(c -> c == '|').count() - 1;
        int rows = 1;
        for (int lineNumber = headerLineNumber + 1; lineNumber < document.numberOfLines(); lineNumber++) {
            if (lineTypes[lineNumber] == TABLE_ROW) {
                rows++;
            } else {
                break;
            }
        }
        String[][] table = new String[rows][columns];
        for (int r = 0; r < rows; r++) {
            String[] row = document.extractLine(headerLineNumber + r).split("\\|");
            for (int c = 0; c < columns; c++) {
                table[r][c] = row[c + 1].strip();
            }
        }
        int[] maxColumnLength = new int[columns];
        for (int c = 0; c < columns; c++) {
            int max = 0;
            for (int r = 0; r < rows; r++) {
                max = Math.max(max, table[r][c].length());
            }
            maxColumnLength[c] = max;
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                table[r][c] = padding(table[r][c], maxColumnLength[c]);
            }
            String formattedLine = Stream.of(table[r]).collect(Collectors.joining(
                    " | ",
                    margin.repeat(TABLE_ROW.level) + "| ",
                    " |"
            ));
            document.replaceLine(headerLineNumber + r, formattedLine);
        }
        return rows;
    }

    private static int[] analyzeMarginLevels(
            Type[] lineTypes
    ) {
        int[] level = new int[lineTypes.length];
        for (int lineNumber = lineTypes.length - 1; lineNumber >= 0; lineNumber--) {
            level[lineNumber] = marginLevel(lineTypes, lineNumber);
        }
        return level;
    }

    private static int marginLevel(
            Type[] lineTypes,
            int lineNumber
    ) {
        Type type = lineTypes[lineNumber];
        if (type.isKeyword) {
            return type.level;
        }
        return switch (type.lookupDirection) {
            case 1 -> levelOf(nextKeywordType(lineTypes, lineNumber));
            case -1 -> levelOf(previousKeywordType(lineTypes, lineNumber));
            default -> type.level;
        };
    }

    private static int levelOf(
            Type type
    ) {
        return type == null ? 0 : type.level;
    }

    private static Type nextKeywordType(
            Type[] lineTypes,
            int lineNumber
    ) {
        Type type;
        for (int i = lineNumber; i < lineTypes.length; i++) {
            type = lineTypes[i];
            if (type == FEATURE || type == FEATURE_CONTENT || type == STEP) {
                return type;
            }
        }
        return null;
    }

    private static Type previousKeywordType(
            Type[] lineTypes,
            int lineNumber
    ) {
        Type type;
        for (int i = lineNumber; i >= 0; i--) {
            type = lineTypes[i];
            if (type == FEATURE || type == FEATURE_CONTENT || type == STEP) {
                return type;
            }
        }
        return null;
    }

    private static Type[] classifyLines(
            GherkinDocumentMap documentMap
    ) {
        var document = documentMap.document();
        Type[] lineType = new Type[document.numberOfLines()];
        Type previousType = null;

        for (int lineNumber = 0; lineNumber < document.numberOfLines(); lineNumber++) {
            String line = document.extractLine(lineNumber).stripLeading();
            Type type = classifyLine(documentMap, lineNumber, line, previousType);
            lineType[lineNumber] = type;
            previousType = type;
        }
        return lineType;
    }

    private static Type classifyLine(
            GherkinDocumentMap documentMap,
            int lineNumber,
            String line,
            Type previousType
    ) {
        Type type = basicLineType(line);
        if (type == null) {
            type = contextualLineType(line, previousType);
        }
        if (type == null) {
            type = keywordLineType(documentMap, lineNumber, line);
        }
        return type;
    }

    private static Type basicLineType(
            String line
    ) {
        if (line.isBlank()) {
            return Type.EMPTY;
        }
        if (line.startsWith("#")) {
            return Type.COMMENT;
        }
        if (line.startsWith("@")) {
            return Type.TAG;
        }
        return null;
    }

    private static Type contextualLineType(
            String line,
            Type previousType
    ) {
        Type tableType = tableLineType(line, previousType);
        return tableType == null
                ? documentLineType(line, previousType)
                : tableType;
    }

    private static Type tableLineType(
            String line,
            Type previousType
    ) {
        if (!line.startsWith("|")) {
            return null;
        }
        if (previousType == Type.STEP) {
            return Type.TABLE_HEADER;
        }
        if (previousType == Type.TABLE_HEADER || previousType == Type.TABLE_ROW) {
            return Type.TABLE_ROW;
        }
        return null;
    }

    private static Type documentLineType(
            String line,
            Type previousType
    ) {
        if (previousType == Type.STEP && isDocumentDelimiter(line)) {
            return Type.DOCUMENT_START;
        }
        if (previousType == Type.DOCUMENT_CONTENT) {
            return isDocumentDelimiter(line)
                    ? Type.DOCUMENT_END
                    : Type.DOCUMENT_CONTENT;
        }
        if (previousType == Type.DOCUMENT_START) {
            return Type.DOCUMENT_CONTENT;
        }
        return null;
    }

    private static boolean isDocumentDelimiter(
            String line
    ) {
        return line.startsWith(TRIPLE_QUOTE)
                || line.startsWith(TRIPLE_BACKQUOTE);
    }

    private static Type keywordLineType(
            GherkinDocumentMap documentMap,
            int lineNumber,
            String line
    ) {
        if (documentMap.hasKeyword(
                lineNumber,
                line,
                GherkinDialect::getFeatureKeywords
        )) {
            return Type.FEATURE;
        }
        if (documentMap.hasKeyword(
                lineNumber,
                line,
                GherkinDialect::getFeatureContentKeywords
        )) {
            return Type.FEATURE_CONTENT;
        }
        if (documentMap.hasKeyword(
                lineNumber,
                line,
                GherkinDialect::getStepKeywords
        )) {
            return Type.STEP;
        }
        return Type.DESCRIPTION;
    }

    private static String padding(
            String string,
            int length
    ) {
        return string + " ".repeat(length - string.length());
    }

}
