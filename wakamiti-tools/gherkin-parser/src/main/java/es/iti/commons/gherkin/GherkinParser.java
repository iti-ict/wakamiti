/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.gherkin;

import java.io.Reader;

import es.iti.commons.gherkin.internal.GherkinAstBuilder;
import es.iti.commons.gherkin.internal.Parser;

public class GherkinParser {

    private final Parser<GherkinDocument> parser = new Parser<>(new GherkinAstBuilder());

    public GherkinDocument parse(Reader reader) {
        return parser.parse(reader);
    }


}