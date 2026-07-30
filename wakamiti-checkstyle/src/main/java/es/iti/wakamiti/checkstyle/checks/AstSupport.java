/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.checkstyle.checks;


import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;


/**
 * Utility helpers for Checkstyle {@link DetailAST} traversal.
 * <p>
 * Methods are null-safe only when explicitly stated by their contract.
 * </p>
 */
final class AstSupport {

    private AstSupport() {
    }

    /**
     * Determines whether a node represents an invokable member declaration.
     *
     * @param ast AST node to inspect, or {@code null}
     * @return {@code true} for method, constructor, or compact constructor
     *         declarations
     */
    static boolean isCallableMember(
            DetailAST ast
    ) {
        return ast != null
                && TokenUtil.isOfType(
                ast,
                TokenTypes.METHOD_DEF,
                TokenTypes.CTOR_DEF,
                TokenTypes.COMPACT_CTOR_DEF
        );
    }

    /**
     * Computes the greatest source line reached by a node subtree.
     *
     * @param ast non-null AST root
     * @return maximum line number among the node and descendants
     */
    static int maximumLine(
            DetailAST ast
    ) {
        int result = ast.getLineNo();
        for (
                DetailAST child = ast.getFirstChild();
                child != null;
                child = child.getNextSibling()
        ) {
            result = Math.max(result, maximumLine(child));
        }
        return result;
    }

    /**
     * Computes the earliest non-comment source line in a subtree.
     *
     * @param ast non-null AST root
     * @return first line number containing code, or {@code 0} when the subtree
     *         contains only comments
     */
    static int minimumCodeLine(
            DetailAST ast
    ) {
        int result = TokenUtil.isCommentType(ast.getType()) ? 0 : ast.getLineNo();
        for (
                DetailAST child = ast.getFirstChild();
                child != null;
                child = child.getNextSibling()
        ) {
            int line = minimumCodeLine(child);
            if (line > 0 && (result == 0 || line < result)) {
                result = line;
            }
        }
        return result;
    }

    /**
     * Determines whether a node declares a Java type.
     *
     * @param ast AST node to inspect
     * @return {@code true} when the node is class, interface, enum, annotation
     *         or record definition
     */
    static boolean isTypeDefinition(
            DetailAST ast
    ) {
        return TokenUtil.isOfType(
                ast,
                TokenTypes.CLASS_DEF,
                TokenTypes.INTERFACE_DEF,
                TokenTypes.ENUM_DEF,
                TokenTypes.ANNOTATION_DEF,
                TokenTypes.RECORD_DEF
        );
    }

}
