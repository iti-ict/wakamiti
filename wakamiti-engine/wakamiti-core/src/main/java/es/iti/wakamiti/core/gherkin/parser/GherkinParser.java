/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import java.io.Reader;

import es.iti.wakamiti.core.gherkin.parser.GherkinDocument;
import es.iti.wakamiti.core.gherkin.parser.internal.GherkinAstBuilder;
import es.iti.wakamiti.core.gherkin.parser.internal.Parser;

public class GherkinParser {

    private final Parser<es.iti.wakamiti.core.gherkin.parser.GherkinDocument> parser = new Parser<>(new GherkinAstBuilder());

    public GherkinDocument parse(Reader reader) {
        return parser.parse(reader);
    }


}