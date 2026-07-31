/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.checkstyle.checks;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * Requires exactly one blank line at both inner boundaries of every type and
 * immediately after its closing brace.
 */
public class TypeClosingBlankLineCheck extends AbstractCheck {

    /** Message-bundle key reported when a type lacks its required boundary blank line. */
    public static final String MSG_TYPE_BOUNDARY_LINES = "type.boundary.blank.lines";

    private static final int[] TOKENS = {
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
        return TOKENS.clone();
    }

    @Override
    public void visitToken(
            DetailAST ast
    ) {
        DetailAST objectBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
        DetailAST openingBrace = objectBlock.findFirstToken(TokenTypes.LCURLY);
        DetailAST closingBrace = objectBlock.findFirstToken(TokenTypes.RCURLY);
        int openingBraceLine = openingBrace.getLineNo();
        int closingBraceLine = closingBrace.getLineNo();

        boolean validStart = hasExactlyOneBlankLineAfter(
                openingBraceLine,
                closingBraceLine
        );
        boolean validEnd = hasExactlyOneBlankLineBefore(
                openingBraceLine,
                closingBraceLine
        );
        boolean validAfterType = hasExactlyOneBlankLineAfterType(closingBraceLine);
        if (!validStart || !validEnd || !validAfterType) {
            log(closingBrace, MSG_TYPE_BOUNDARY_LINES);
        }
    }

    private boolean hasExactlyOneBlankLineAfter(
            int openingBraceLine,
            int closingBraceLine
    ) {
        return closingBraceLine >= openingBraceLine + 2
                && getLine(openingBraceLine).isBlank()
                && (
                closingBraceLine == openingBraceLine + 2
                        || !getLine(openingBraceLine + 1).isBlank()
        );
    }

    private boolean hasExactlyOneBlankLineBefore(
            int openingBraceLine,
            int closingBraceLine
    ) {
        return closingBraceLine >= openingBraceLine + 2
                && getLine(closingBraceLine - 2).isBlank()
                && (
                closingBraceLine == openingBraceLine + 2
                        || !getLine(closingBraceLine - 3).isBlank()
        );
    }

    private boolean hasExactlyOneBlankLineAfterType(
            int closingBraceLine
    ) {
        String[] lines = getLines();
        boolean finalLineBreakRepresentsBlankLine = lines.length == closingBraceLine
                && endsWithLineBreak();
        boolean explicitBlankLineBeforeMoreContent = lines.length >= closingBraceLine + 2
                && getLine(closingBraceLine).isBlank()
                && !getLine(closingBraceLine + 1).isBlank();
        return finalLineBreakRepresentsBlankLine
                || explicitBlankLineBeforeMoreContent;
    }

    private boolean endsWithLineBreak() {
        Path file = Path.of(getFilePath());
        try (SeekableByteChannel channel = Files.newByteChannel(file)) {
            long fileSize = channel.size();
            if (fileSize == 0) {
                return false;
            }
            ByteBuffer finalByte = ByteBuffer.allocate(1);
            channel.position(fileSize - 1);
            channel.read(finalByte);
            byte finalCharacter = finalByte.array()[0];
            return finalCharacter == '\n' || finalCharacter == '\r';
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to inspect the final line break of " + file,
                    exception
            );
        }
    }

}
