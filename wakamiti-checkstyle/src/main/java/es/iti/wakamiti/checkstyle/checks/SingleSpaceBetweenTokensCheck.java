/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.checkstyle.checks;


import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * Forbids using more than one consecutive space between code tokens.
 * Text blocks, regular string and character literals are ignored.
 */
public class SingleSpaceBetweenTokensCheck extends AbstractCheck {

    /** Message-bundle key reported when there is more than one space between tokens. */
    public static final String MSG_SINGLE_SPACE_BETWEEN_TOKENS = "single.space.between.tokens";

    private static final int[] TOKENS = {
            TokenTypes.PACKAGE_DEF,
            TokenTypes.IMPORT,
            TokenTypes.STATIC_IMPORT,
            TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.RECORD_DEF
    };

    private ScanMode scanMode;

    @Override
    public int[] getDefaultTokens() {
        return TOKENS.clone();
    }

    @Override
    public int[] getAcceptableTokens() {
        return TOKENS.clone();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }

    @Override
    public void beginTree(
            DetailAST root
    ) {
        scanMode = ScanMode.CODE;
    }

    @Override
    public void finishTree(
            DetailAST root
    ) {
        String[] lines = getLines();
        for (int index = 0; index < lines.length; index++) {
            scanLine(lines[index], index + 1);
        }
    }

    private void scanLine(
            String line,
            int lineNumber
    ) {
        int index = 0;
        boolean hasTokenBefore = false;
        while (index < line.length()) {
            ScanPosition position = scanNext(
                    line,
                    index,
                    lineNumber,
                    hasTokenBefore
            );
            index = position.index();
            hasTokenBefore = position.hasTokenBefore();
        }
    }

    private ScanPosition scanNext(
            String line,
            int index,
            int lineNumber,
            boolean hasTokenBefore
    ) {
        if (scanMode != ScanMode.CODE) {
            return scanDelimitedContent(line, index);
        }
        if (line.startsWith("//", index)) {
            return new ScanPosition(line.length(), hasTokenBefore);
        }
        if (line.startsWith("/*", index)) {
            scanMode = ScanMode.BLOCK_COMMENT;
            return new ScanPosition(index + 2, hasTokenBefore);
        }
        if (line.startsWith("\"\"\"", index)) {
            scanMode = ScanMode.TEXT_BLOCK;
            return new ScanPosition(index + 3, true);
        }
        return scanCodeCharacter(line, index, lineNumber, hasTokenBefore);
    }

    private ScanPosition scanDelimitedContent(
            String line,
            int index
    ) {
        int closingDelimiter = line.indexOf(scanMode.closingDelimiter(), index);
        if (closingDelimiter < 0) {
            return new ScanPosition(line.length(), true);
        }
        int nextIndex = closingDelimiter + scanMode.closingDelimiter().length();
        scanMode = ScanMode.CODE;
        return new ScanPosition(nextIndex, true);
    }

    private ScanPosition scanCodeCharacter(
            String line,
            int index,
            int lineNumber,
            boolean hasTokenBefore
    ) {
        char character = line.charAt(index);
        return switch (character) {
            case '"', '\'' -> new ScanPosition(
                        endOfQuotedLiteral(line, index, character),
                        true
                );
            case ' ' -> scanSpaces(line, index, lineNumber, hasTokenBefore);
            default -> new ScanPosition(
                        index + 1,
                        hasTokenBefore || !Character.isWhitespace(character)
                );
        };
    }

    private ScanPosition scanSpaces(
            String line,
            int spaceStart,
            int lineNumber,
            boolean hasTokenBefore
    ) {
        int spaceEnd = spaceStart;
        while (spaceEnd < line.length() && line.charAt(spaceEnd) == ' ') {
            spaceEnd++;
        }
        if (
                spaceEnd - spaceStart > 1
                        && hasTokenBefore
                        && hasTokenAfter(line, spaceEnd)
        ) {
            log(lineNumber, spaceStart + 1, MSG_SINGLE_SPACE_BETWEEN_TOKENS);
        }
        return new ScanPosition(spaceEnd, hasTokenBefore);
    }

    private int endOfQuotedLiteral(
            String line,
            int quoteStart,
            char quoteCharacter
    ) {
        int index = quoteStart + 1;
        while (index < line.length()) {
            if (line.charAt(index) == '\\') {
                index += 2;
            } else if (line.charAt(index) == quoteCharacter) {
                return index + 1;
            } else {
                index++;
            }
        }
        return line.length();
    }

    private boolean hasTokenAfter(
            String line,
            int fromIndex
    ) {
        int index = fromIndex;
        while (index < line.length()) {
            if (line.startsWith("//", index) || line.startsWith("/*", index)) {
                return false;
            }
            if (!Character.isWhitespace(line.charAt(index))) {
                return true;
            }
            index++;
        }
        return false;
    }

    private enum ScanMode {

        CODE(""),
        BLOCK_COMMENT("*/"),
        TEXT_BLOCK("\"\"\"");

        private final String closingDelimiter;

        ScanMode(
                String closingDelimiter
        ) {
            this.closingDelimiter = closingDelimiter;
        }

        private String closingDelimiter() {
            return closingDelimiter;
        }

    }

    private record ScanPosition(
            int index,
            boolean hasTokenBefore
    ) {

    }

}
