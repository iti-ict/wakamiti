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

    private boolean insideBlockComment;
    private boolean insideTextBlock;

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
        insideBlockComment = false;
        insideTextBlock = false;
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
            if (insideTextBlock) {
                int closingDelimiter = line.indexOf("\"\"\"", index);
                if (closingDelimiter < 0) {
                    return;
                }
                insideTextBlock = false;
                index = closingDelimiter + 3;
                hasTokenBefore = true;
                continue;
            }

            if (insideBlockComment) {
                int commentEnd = line.indexOf("*/", index);
                if (commentEnd < 0) {
                    return;
                }
                insideBlockComment = false;
                index = commentEnd + 2;
                hasTokenBefore = true;
                continue;
            }

            if (line.startsWith("//", index)) {
                return;
            }
            if (line.startsWith("/*", index)) {
                insideBlockComment = true;
                index += 2;
                continue;
            }
            if (line.startsWith("\"\"\"", index)) {
                insideTextBlock = true;
                index += 3;
                hasTokenBefore = true;
                continue;
            }

            char character = line.charAt(index);
            if (character == '"') {
                index = endOfQuotedLiteral(line, index, '"');
                hasTokenBefore = true;
            } else if (character == '\'') {
                index = endOfQuotedLiteral(line, index, '\'');
                hasTokenBefore = true;
            } else if (character == ' ') {
                int spaceStart = index;
                while (index < line.length() && line.charAt(index) == ' ') {
                    index++;
                }
                if (
                        index - spaceStart > 1
                                && hasTokenBefore
                                && hasTokenAfter(line, index)
                ) {
                    log(lineNumber, spaceStart + 1, MSG_SINGLE_SPACE_BETWEEN_TOKENS);
                }
            } else {
                if (!Character.isWhitespace(character)) {
                    hasTokenBefore = true;
                }
                index++;
            }
        }
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

}
