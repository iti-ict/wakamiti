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
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;


/**
 * Requires exactly one blank line between consecutive methods or constructors.
 * Comments and Javadoc belonging to the following declaration are not counted
 * as the declaration boundary.
 */
public class CallableSeparationCheck extends AbstractCheck {

    /** Message-bundle key reported when methods or constructors lack required separation. */
    public static final String MSG_CALLABLE_SEPARATOR = "callable.separator";

    private static final int[] TOKENS = {
            TokenTypes.OBJBLOCK
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
    public boolean isCommentNodesRequired() {
        return true;
    }

    @Override
    public void visitToken(
            DetailAST objectBlock
    ) {
        DetailAST previousMember = null;
        for (
                DetailAST child = objectBlock.getFirstChild();
                child != null;
                child = child.getNextSibling()
        ) {
            if (isSignificantMember(child)) {
                if (
                        AstSupport.isCallableMember(previousMember)
                                && AstSupport.isCallableMember(child)
                                && !hasExactlyOneBlankLineBetween(previousMember, child)
                ) {
                    log(child, MSG_CALLABLE_SEPARATOR);
                }
                previousMember = child;
            }
        }
    }

    private boolean hasExactlyOneBlankLineBetween(
            DetailAST previousCallable,
            DetailAST nextCallable
    ) {
        int previousEndLine = AstSupport.maximumLine(previousCallable);
        int nextStartLine = AstSupport.minimumCodeLine(nextCallable);
        int blankLines = 0;
        boolean insideBlockComment = false;

        for (
                int lineIndex = previousEndLine;
                lineIndex < nextStartLine - 1;
                lineIndex++
        ) {
            String line = getLine(lineIndex);
            if (!insideBlockComment && line.isBlank()) {
                blankLines++;
            } else {
                CommentLineResult result = parseCommentLine(
                        line,
                        insideBlockComment
                );
                if (!result.commentOnly()) {
                    return false;
                }
                insideBlockComment = result.insideBlockComment();
            }
        }
        return blankLines == 1;
    }

    private CommentLineResult parseCommentLine(
            String line,
            boolean initiallyInsideBlockComment
    ) {
        String remaining = line.strip();
        boolean insideBlockComment = initiallyInsideBlockComment;
        boolean commentFound = initiallyInsideBlockComment;

        while (!remaining.isEmpty()) {
            if (insideBlockComment) {
                int commentEnd = remaining.indexOf("*/");
                if (commentEnd < 0) {
                    remaining = "";
                } else {
                    insideBlockComment = false;
                    remaining = remaining.substring(commentEnd + 2).strip();
                }
            } else if (remaining.startsWith("//")) {
                commentFound = true;
                remaining = "";
            } else if (remaining.startsWith("/*")) {
                commentFound = true;
                insideBlockComment = true;
                remaining = remaining.substring(2);
            } else {
                return new CommentLineResult(false, insideBlockComment);
            }
        }
        return new CommentLineResult(commentFound, insideBlockComment);
    }

    private boolean isSignificantMember(
            DetailAST ast
    ) {
        int type = ast.getType();
        return type != TokenTypes.LCURLY
                && type != TokenTypes.RCURLY
                && type != TokenTypes.SEMI
                && type != TokenTypes.COMMA
                && !TokenUtil.isCommentType(type);
    }

    private record CommentLineResult(
            boolean commentOnly,
            boolean insideBlockComment
    ) {

    }

}
