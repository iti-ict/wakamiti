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
 * Requires two blank lines between package and imports, and between the last
 * import and the first type declaration, including its Javadoc.
 */
public class PackageImportSpacingCheck extends AbstractCheck {

    /** Message-bundle key for incorrect spacing between a package and its imports. */
    public static final String MSG_PACKAGE_IMPORTS = "package.imports.blank.lines";
    /** Message-bundle key for incorrect spacing between imports and the first type. */
    public static final String MSG_IMPORTS_TYPE = "imports.type.blank.lines";

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
    public boolean isCommentNodesRequired() {
        return true;
    }

    @Override
    public void finishTree(
            DetailAST root
    ) {
        SourceSections sections = sourceSections(root);
        validatePackageImportSpacing(sections);
        validateImportTypeSpacing(sections);
    }

    private SourceSections sourceSections(
            DetailAST root
    ) {
        DetailAST packageDefinition = null;
        DetailAST firstImport = null;
        DetailAST lastImport = null;
        DetailAST firstType = null;

        for (
                DetailAST child = root.getFirstChild();
                child != null;
                child = child.getNextSibling()
        ) {
            int type = child.getType();
            switch (type) {
                case TokenTypes.PACKAGE_DEF:
                    packageDefinition = child;
                    break;
                case TokenTypes.IMPORT, TokenTypes.STATIC_IMPORT:
                    if (firstImport == null) {
                        firstImport = child;
                    }
                    lastImport = child;
                    break;
                default:
                    if (firstType == null && AstSupport.isTypeDefinition(child)) {
                        firstType = child;
                    }
                    break;
            }
        }
        return new SourceSections(
                packageDefinition,
                firstImport,
                lastImport,
                firstType
        );
    }

    private void validatePackageImportSpacing(
            SourceSections sections
    ) {
        if (
                sections.packageDefinition() != null
                        && sections.firstImport() != null
                        && !hasExactlyTwoBlankLines(
                        AstSupport.maximumLine(sections.packageDefinition()),
                        AstSupport.minimumCodeLine(sections.firstImport())
                )
        ) {
            log(sections.firstImport(), MSG_PACKAGE_IMPORTS);
        }
    }

    private void validateImportTypeSpacing(
            SourceSections sections
    ) {
        if (
                sections.lastImport() != null
                        && sections.firstType() != null
                        && !hasExactlyTwoBlankLines(
                        AstSupport.maximumLine(sections.lastImport()),
                        typeStartIncludingJavadoc(sections.firstType())
                )
        ) {
            log(sections.firstType(), MSG_IMPORTS_TYPE);
        }
    }

    private int typeStartIncludingJavadoc(
            DetailAST typeDefinition
    ) {
        int result = AstSupport.minimumCodeLine(typeDefinition);
        int previousLineIndex = result - 2;
        if (previousLineIndex < 0
                || !getLine(previousLineIndex).stripTrailing().endsWith("*/")) {
            return result;
        }
        return javadocStartLine(previousLineIndex, result);
    }

    private int javadocStartLine(
            int previousLineIndex,
            int defaultLine
    ) {
        for (int index = previousLineIndex; index >= 0; index--) {
            String line = getLine(index).stripLeading();
            if (line.startsWith("/**")) {
                return index + 1;
            }
            if (line.startsWith("/*")) {
                return defaultLine;
            }
        }
        return defaultLine;
    }

    private boolean hasExactlyTwoBlankLines(
            int previousEndLine,
            int nextStartLine
    ) {
        int blankLines = 0;
        for (
                int lineIndex = previousEndLine;
                lineIndex < nextStartLine - 1;
                lineIndex++
        ) {
            String line = getLine(lineIndex);
            if (line.isBlank()) {
                blankLines++;
            } else if (!line.stripLeading().startsWith("//")) {
                return false;
            }
        }
        return blankLines == 2;
    }

    private record SourceSections(
            DetailAST packageDefinition,
            DetailAST firstImport,
            DetailAST lastImport,
            DetailAST firstType
    ) {

    }

}
