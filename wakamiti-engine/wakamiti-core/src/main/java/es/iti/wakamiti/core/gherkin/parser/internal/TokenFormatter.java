/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser.internal;


public class TokenFormatter {

    private static final StringUtils.ToString<GherkinLineSpan> SPAN_TO_STRING =
            o -> o.column + ":" + o.text;

    public String formatToken(
            Token token
    ) {
        if (token.isEOF())
            return "EOF";

        return String.format("(%s:%s)%s:%s/%s/%s",
                toString(token.location.getLine()),
                toString(token.location.getColumn()),
                toString(token.matchedType),
                toString(token.matchedKeyword),
                toString(token.matchedText),
                toString(token.mathcedItems == null ? "" : StringUtils.join(SPAN_TO_STRING, ",", token.mathcedItems))
        );
    }

    private String toString(
            Object o
    ) {
        return o == null ? "" : o.toString();
    }

}
