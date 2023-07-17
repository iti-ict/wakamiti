/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser.internal;

import java.util.List;

import es.iti.wakamiti.core.gherkin.parser.GherkinDialect;
import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.internal.GherkinLine;
import es.iti.wakamiti.core.gherkin.parser.internal.GherkinLineSpan;
import es.iti.wakamiti.core.gherkin.parser.internal.Parser;

public class Token {
    public final es.iti.wakamiti.core.gherkin.parser.internal.GherkinLine line;
    public Parser.TokenType matchedType;
    public String matchedKeyword;
    public String matchedText;
    public List<GherkinLineSpan> mathcedItems;
    public int matchedIndent;
    public GherkinDialect matchedGherkinDialect;
    public Location location;

    public Token(GherkinLine line, Location location) {
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