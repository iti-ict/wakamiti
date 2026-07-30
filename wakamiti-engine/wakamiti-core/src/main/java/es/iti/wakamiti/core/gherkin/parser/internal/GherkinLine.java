/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser.internal;


import static es.iti.wakamiti.core.gherkin.parser.internal.StringUtils.ltrim;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import es.iti.wakamiti.core.gherkin.parser.GherkinLanguageConstants;


/**
 * Provides the Gherkin Line functionality used by Wakamiti.
 */
public class GherkinLine {

    private final String lineText;
    private final String trimmedLineText;

    public GherkinLine(
            String lineText
    ) {
        this.lineText = lineText;
        this.trimmedLineText = ltrim(lineText);
    }

    public Integer indent() {
        return SymbolCounter.countSymbols(lineText) - SymbolCounter.countSymbols(trimmedLineText);
    }

    public void detach() {
        // nothing to do
    }

    public String getLineText(
            int indentToRemove
    ) {
        if (indentToRemove < 0 || indentToRemove > indent())
            return trimmedLineText;
        return lineText.substring(indentToRemove);
    }

    public boolean isEmpty() {
        return trimmedLineText.length() == 0;
    }

    public boolean startsWith(
            String prefix
    ) {
        return trimmedLineText.startsWith(prefix);
    }

    public String getRestTrimmed(
            int length
    ) {
        return trimmedLineText.substring(length).trim();
    }

    public List<GherkinLineSpan> getTags() {
        return getSpans("\\s+");
    }

    public boolean startsWithTitleKeyword(
            String text
    ) {
        int textLength = text.length();
        return trimmedLineText.length() > textLength &&
                trimmedLineText.startsWith(text) &&
                trimmedLineText.substring(textLength, textLength + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length())
                        .equals(GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR);
    }

    public List<GherkinLineSpan> getTableCells() {
        return new TableCellParser(trimmedLineText, indent()).parse();
    }

    private List<GherkinLineSpan> getSpans(
            String delimiter
    ) {
        List<GherkinLineSpan> lineSpans = new ArrayList<>();
        try (Scanner scanner = new Scanner(trimmedLineText)) {
            scanner.useDelimiter(delimiter);
            while (scanner.hasNext()) {
                String cell = scanner.next();
                int column = scanner.match().start() + indent() + 1;
                lineSpans.add(new GherkinLineSpan(column, cell));
            }
            return lineSpans;
        }
    }

    private static final class TableCellParser {

        private final String line;
        private final int indent;
        private final List<GherkinLineSpan> spans = new ArrayList<>();
        private final StringBuilder cell = new StringBuilder();

        private boolean beforeFirst = true;
        private int startColumn;

        private TableCellParser(
                String line,
                int indent
        ) {
            this.line = line;
            this.indent = indent;
        }

        private List<GherkinLineSpan> parse() {
            for (int column = 0; column < line.length(); column++) {
                char character = line.charAt(column);
                switch (character) {
                    case '|':
                        endCell(column);
                        break;
                    case '\\':
                        column = appendEscapedCharacter(column);
                        break;
                    default:
                        cell.append(character);
                        break;
                }
            }
            return spans;
        }

        private void endCell(
                int column
        ) {
            if (beforeFirst) {
                beforeFirst = false;
            } else {
                addCell();
                startColumn = column;
            }
            cell.setLength(0);
        }

        private void addCell() {
            int contentStart = contentStart();
            spans.add(new GherkinLineSpan(
                    indent + startColumn + contentStart + 2,
                    cell.toString().trim()
            ));
        }

        private int contentStart() {
            int result = 0;
            while (result < cell.length() && Character.isWhitespace(cell.charAt(result))) {
                result++;
            }
            return result == cell.length() ? 0 : result;
        }

        private int appendEscapedCharacter(
                int escapeColumn
        ) {
            int characterColumn = escapeColumn + 1;
            char character = line.charAt(characterColumn);
            switch (character) {
                case 'n':
                    cell.append('\n');
                    break;
                case '|':
                case '\\':
                    cell.append(character);
                    break;
                default:
                    cell.append('\\').append(character);
                    break;
            }
            return characterColumn;
        }

    }

}
