/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.util.Collections;
import java.util.List;

import es.iti.wakamiti.core.gherkin.parser.internal.StringUtils;
import es.iti.wakamiti.core.gherkin.parser.internal.Token;


/**
 * Exception thrown when a Gherkin document cannot be parsed.
 */
public class ParserException extends RuntimeException {

    private final transient es.iti.wakamiti.core.gherkin.parser.Location location;

    protected ParserException(
            String message
    ) {
        super(message);
        location = null;
    }

    protected ParserException(
            String message,
            es.iti.wakamiti.core.gherkin.parser.Location location
    ) {
        super(getMessage(message, location));
        this.location = location;
    }

    /**
     * Returns the source coordinate associated with the parse failure.
     *
     * @return the location, or {@code null} for errors that span several
     * locations
     */
    public es.iti.wakamiti.core.gherkin.parser.Location getLocation() {
        return location;
    }

    private static String getMessage(
            String message,
            es.iti.wakamiti.core.gherkin.parser.Location location
    ) {
        return String.format("(%s:%s): %s", location.getLine(), location.getColumn(), message);
    }

    public static class AstBuilderException extends ParserException {

        /**
         * Creates an error raised while translating parser events into the
         * public syntax tree.
         *
         * @param message  description of the invalid AST state
         * @param location source coordinate where construction failed
         */
        public AstBuilderException(
                String message,
                es.iti.wakamiti.core.gherkin.parser.Location location
        ) {
            super(message, location);
        }

    }

    public static class NoSuchLanguageException extends ParserException {

        /**
         * Creates an error for an unsupported Gherkin language directive.
         *
         * @param language unsupported language code
         * @param location location of the directive, or {@code null}
         */
        public NoSuchLanguageException(
                String language,
                es.iti.wakamiti.core.gherkin.parser.Location location
        ) {
            super("Language not supported: " + language, location);
        }

    }

    public static class UnexpectedTokenException extends ParserException {

        private String stateComment;

        private final transient Token receivedToken;
        private final transient List<String> expectedTokenTypes;

        /**
         * Creates an error describing a token that is invalid in the current
         * parser state.
         *
         * @param receivedToken     actual token read from the source
         * @param expectedTokenTypes human-readable expected token categories
         * @param stateComment      parser-state context retained for diagnostics
         */
        public UnexpectedTokenException(
                Token receivedToken,
                List<String> expectedTokenTypes,
                String stateComment
        ) {
            super(getMessage(receivedToken, expectedTokenTypes), getLocation(receivedToken));
            this.receivedToken = receivedToken;
            this.expectedTokenTypes = expectedTokenTypes;
            this.stateComment = stateComment;
        }

        private static String getMessage(
                Token receivedToken,
                List<String> expectedTokenTypes
        ) {
            return String.format("expected: %s, got '%s'",
                    StringUtils.join(", ", expectedTokenTypes),
                    receivedToken.getTokenValue().trim());
        }

        private static es.iti.wakamiti.core.gherkin.parser.Location getLocation(
                Token receivedToken
        ) {
            return receivedToken.location.getColumn() > 1
                    ? receivedToken.location
                    : new Location(receivedToken.location.getLine(), receivedToken.line.indent() + 1);
        }

    }

    public static class UnexpectedEOFException extends ParserException {

        private final String stateComment;
        private final transient List<String> expectedTokenTypes;

        /**
         * Creates an error for input ending before the current grammar
         * production was complete.
         *
         * @param receivedToken     end-of-file token carrying the source
         *                          location
         * @param expectedTokenTypes token categories that could have continued
         *                           the production
         * @param stateComment      parser-state context retained for diagnostics
         */
        public UnexpectedEOFException(
                Token receivedToken,
                List<String> expectedTokenTypes,
                String stateComment
        ) {
            super(getMessage(expectedTokenTypes), receivedToken.location);
            this.expectedTokenTypes = expectedTokenTypes;
            this.stateComment = stateComment;
        }

        private static String getMessage(
                List<String> expectedTokenTypes
        ) {
            return String.format("unexpected end of file, expected: %s",
                    StringUtils.join(", ", expectedTokenTypes));
        }

    }

    public static class CompositeParserException extends ParserException {

        private final List<ParserException> errors;

        /**
         * Combines multiple recoverable parse failures into one exception.
         *
         * @param errors non-null list of errors in discovery order
         * @throws NullPointerException if {@code errors} is {@code null}
         */
        public CompositeParserException(
                List<ParserException> errors
        ) {
            super(getMessage(errors));
            this.errors = Collections.unmodifiableList(errors);
        }

        private static String getMessage(
                List<ParserException> errors
        ) {
            if (errors == null) {
                throw new NullPointerException("errors");
            }
            StringUtils.ToString<ParserException> exceptionToString = ParserException::getMessage;
            return "Parser errors:\n" + StringUtils.join(exceptionToString, "\n", errors);
        }

        /**
         * Returns the individual failures represented by this aggregate.
         *
         * @return an immutable copy of the parser errors
         */
        public List<ParserException> getErrors() {
            return List.copyOf(errors);
        }

    }

}
