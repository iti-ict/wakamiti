/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser.internal;


import java.util.List;

import es.iti.wakamiti.core.gherkin.parser.GherkinDialect;
import es.iti.wakamiti.core.gherkin.parser.Location;


/**
 * Provides the Token functionality used by Wakamiti.
 */
public class Token {

    /** Source line from which this token was scanned; {@code null} denotes EOF. */
    public final es.iti.wakamiti.core.gherkin.parser.internal.GherkinLine line;
    /** Grammar token type assigned by the token matcher. */
    public Parser.TokenType matchedType;
    /** Localized Gherkin keyword matched at the start of the token. */
    public String matchedKeyword;
    /** Semantic text remaining after the matched keyword and delimiters. */
    public String matchedText;
    /** Data-table cells or other subspans recognized inside the source line. */
    public List<GherkinLineSpan> mathcedItems;
    /** Number of leading indentation characters removed during matching. */
    public int matchedIndent;
    /** Gherkin dialect selected by a language token or inherited parser state. */
    public GherkinDialect matchedGherkinDialect;
    /** One-based source location reported for this token. */
    public Location location;

    public Token(
            GherkinLine line,
            Location location
    ) {
        this.line = line;
        this.location = location;
    }

    public boolean isEOF() {
        return line == null;
    }

    public void detach() {
        if (line != null)
            line.detach();
    }

    public String getTokenValue() {
        return isEOF() ? "EOF" : line.getLineText(-1);
    }

    @Override
    public String toString() {
        return String.format("%s: %s/%s", matchedType, matchedKeyword, matchedText);
    }

}
