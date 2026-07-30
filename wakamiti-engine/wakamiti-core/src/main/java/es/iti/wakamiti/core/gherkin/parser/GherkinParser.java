/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.io.Reader;

import es.iti.wakamiti.core.gherkin.parser.internal.GherkinAstBuilder;
import es.iti.wakamiti.core.gherkin.parser.internal.Parser;


/**
 * Parses Gherkin input and exposes its structured representation.
 */
public class GherkinParser {

    private final Parser<GherkinDocument> parser = new Parser<>(new GherkinAstBuilder());

    /**
     * Parses a Gherkin character stream into its syntax-tree representation.
     *
     * @param reader source positioned at the beginning of a Gherkin document
     * @return the parsed document
     * @throws ParserException when tokens do not form a valid Gherkin document
     */
    public GherkinDocument parse(
            Reader reader
    ) {
        return parser.parse(reader);
    }

}
