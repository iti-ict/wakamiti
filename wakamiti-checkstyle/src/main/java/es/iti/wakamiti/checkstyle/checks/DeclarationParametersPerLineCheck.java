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
 * Requires every method or constructor parameter to start on its own line.
 */
public class DeclarationParametersPerLineCheck extends AbstractCheck {

    public static final String MSG_PARAMETER_LINE = "declaration.parameter.line";

    private static final int[] TOKENS = {
            TokenTypes.METHOD_DEF,
            TokenTypes.CTOR_DEF
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
        DetailAST leftParenthesis = ast.findFirstToken(TokenTypes.LPAREN);
        DetailAST parameters = ast.findFirstToken(TokenTypes.PARAMETERS);
        int previousParameterLine = leftParenthesis.getLineNo();

        for (
                DetailAST child = parameters.getFirstChild();
                child != null;
                child = child.getNextSibling()
        ) {
            if (child.getType() == TokenTypes.PARAMETER_DEF) {
                int currentLine = child.getLineNo();
                if (currentLine == previousParameterLine) {
                    log(child, MSG_PARAMETER_LINE);
                }
                previousParameterLine = currentLine;
            }
        }
    }

}
