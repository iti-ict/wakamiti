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


final class AstSupport {

    private AstSupport() {
    }

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
