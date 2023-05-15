/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.gherkin.internal;

import static es.iti.commons.gherkin.internal.StringUtils.ltrim;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import es.iti.commons.gherkin.GherkinLanguageConstants;

public class GherkinLine {

    private final String lineText;
    private final String trimmedLineText;

    public GherkinLine(String lineText) {
        this.lineText = lineText;
        this.trimmedLineText = ltrim(lineText);
    }

    public Integer indent() {
        return SymbolCounter.countSymbols(lineText) - SymbolCounter.countSymbols(trimmedLineText);
    }

    public void detach() {

    }

    public String getLineText(int indentToRemove) {
        if (indentToRemove < 0 || indentToRemove > indent())
            return trimmedLineText;
        return lineText.substring(indentToRemove);
    }

    public boolean isEmpty() {
        return trimmedLineText.length() == 0;
    }

    public boolean startsWith(String prefix) {
        return trimmedLineText.startsWith(prefix);
    }

    public String getRestTrimmed(int length) {
        return trimmedLineText.substring(length).trim();
    }

    public List<GherkinLineSpan> getTags() {
        return getSpans("\\s+");
    }

    public boolean startsWithTitleKeyword(String text) {
        int textLength = text.length();
        return trimmedLineText.length() > textLength &&
                trimmedLineText.startsWith(text) &&
                trimmedLineText.substring(textLength, textLength + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length())
                        .equals(GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR);
    }


    public List<GherkinLineSpan> getTableCells() {
        List<GherkinLineSpan> lineSpans = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean beforeFirst = true;
        int startCol = 0;
        for (int col = 0; col < trimmedLineText.length(); col++) {
            char c = trimmedLineText.charAt(col);
            if (c == '|') {
                if (beforeFirst) {
                    // Skip the first empty span
                    beforeFirst = false;
                } else {
                    int contentStart = 0;
                    while (contentStart < cell.length() && Character.isWhitespace(cell.charAt(contentStart))) {
                        contentStart++;
                    }
                    if (contentStart == cell.length()) {
                        contentStart = 0;
                    }
                    lineSpans.add(new GherkinLineSpan(indent() + startCol + contentStart + 2, cell.toString().trim()));
                    startCol = col;
                }
                cell = new StringBuilder();
            } else if (c == '\\') {
                col++;
                c = trimmedLineText.charAt(col);
                if (c == 'n') {
                    cell.append('\n');
                } else {
                    if (c != '|' && c != '\\') {
                        cell.append('\\');
                    }
                    cell.append(c);
                }
            } else {
                cell.append(c);
            }
        }

        return lineSpans;
    }

    private List<GherkinLineSpan> getSpans(String delimiter) {
        List<GherkinLineSpan> lineSpans = new ArrayList<>();
        try(Scanner scanner = new Scanner(trimmedLineText)) {
            scanner.useDelimiter(delimiter);
            while (scanner.hasNext()) {
                String cell = scanner.next();
                int column = scanner.match().start() + indent() + 1;
                lineSpans.add(new GherkinLineSpan(column, cell));
            }
            return lineSpans;
            }
        }
}