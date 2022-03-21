/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin;

import java.util.Collections;
import java.util.List;

import iti.commons.gherkin.internal.StringUtils;
import iti.commons.gherkin.internal.Token;

@SuppressWarnings("serial")
public class ParserException extends RuntimeException {

    private final Location location;

    protected ParserException(String message) {
        super(message);
        location = null;
    }

    protected ParserException(String message, Location location) {
        super(getMessage(message, location));
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    private static String getMessage(String message, Location location) {
        return String.format("(%s:%s): %s", location.getLine(), location.getColumn(), message);
    }

    public static class AstBuilderException extends ParserException {
        public AstBuilderException(String message, Location location) {
            super(message, location);
        }
    }

    public static class NoSuchLanguageException extends ParserException {
        public NoSuchLanguageException(String language, Location location) {
            super("Language not supported: " + language, location);
        }
    }

    public static class UnexpectedTokenException extends ParserException {
        public String stateComment;

        public final Token receivedToken;
        public final List<String> expectedTokenTypes;

        public UnexpectedTokenException(Token receivedToken, List<String> expectedTokenTypes, String stateComment) {
            super(getMessage(receivedToken, expectedTokenTypes), getLocation(receivedToken));
            this.receivedToken = receivedToken;
            this.expectedTokenTypes = expectedTokenTypes;
            this.stateComment = stateComment;
        }

        private static String getMessage(Token receivedToken, List<String> expectedTokenTypes) {
            return String.format("expected: %s, got '%s'",
                    StringUtils.join(", ", expectedTokenTypes),
                    receivedToken.getTokenValue().trim());
        }

        private static Location getLocation(Token receivedToken) {
            return receivedToken.location.getColumn() > 1
                    ? receivedToken.location
                    : new Location(receivedToken.location.getLine(), receivedToken.line.indent() + 1);
        }
    }

    public static class UnexpectedEOFException extends ParserException {
        public final String stateComment;
        public final List<String> expectedTokenTypes;

        public UnexpectedEOFException(Token receivedToken, List<String> expectedTokenTypes, String stateComment) {
            super(getMessage(expectedTokenTypes), receivedToken.location);
            this.expectedTokenTypes = expectedTokenTypes;
            this.stateComment = stateComment;
        }

        private static String getMessage(List<String> expectedTokenTypes) {
            return String.format("unexpected end of file, expected: %s",
                    StringUtils.join(", ", expectedTokenTypes));
        }
    }

    public static class CompositeParserException extends ParserException {

        private final List<ParserException> errors;

        public CompositeParserException(List<ParserException> errors) {
            super(getMessage(errors));
            this.errors = Collections.unmodifiableList(errors);
        }

        private static String getMessage(List<ParserException> errors) {
            if (errors == null) throw new NullPointerException("errors");
            StringUtils.ToString<ParserException> exceptionToString = ParserException::getMessage;
            return "Parser errors:\n" + StringUtils.join(exceptionToString, "\n", errors);
        }

        public List<ParserException> getErrors() {
            return List.copyOf(errors);
        }
    }
}