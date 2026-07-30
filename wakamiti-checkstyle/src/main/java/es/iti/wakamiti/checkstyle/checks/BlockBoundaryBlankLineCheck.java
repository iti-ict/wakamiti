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
 * Forbids blank lines immediately inside executable block boundaries.
 * Type bodies are handled separately because their member-spacing rules
 * intentionally require blank lines at those boundaries.
 */
public class BlockBoundaryBlankLineCheck extends AbstractCheck {

    /** Message-bundle key reported for a blank line immediately after an opening brace. */
    public static final String MSG_BLOCK_START = "block.start.blank.line";
    /** Message-bundle key reported for a blank line immediately before a closing brace. */
    public static final String MSG_BLOCK_END = "block.end.blank.line";

    private static final int[] TOKENS = {
            TokenTypes.SLIST
    };

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
        return TOKENS.clone();
    }

    @Override
    public void visitToken(
            DetailAST ast
    ) {
        DetailAST closingBrace = ast.findFirstToken(TokenTypes.RCURLY);
        if (closingBrace == null) {
            return;
        }

        int openingBraceLine = ast.getLineNo();
        int closingBraceLine = closingBrace.getLineNo();
        if (
                closingBraceLine > openingBraceLine + 1
                        && getLine(openingBraceLine).isBlank()
        ) {
            log(openingBraceLine + 1, MSG_BLOCK_START);
        }

        if (
                closingBraceLine > openingBraceLine + 1
                        && getLine(closingBraceLine - 2).isBlank()
        ) {
            log(closingBrace, MSG_BLOCK_END);
        }
    }

}
