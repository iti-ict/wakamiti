package iti.commons.gherkin;

import java.io.Reader;

import iti.commons.gherkin.internal.GherkinAstBuilder;
import iti.commons.gherkin.internal.Parser;

public class GherkinParser {

    private final Parser<GherkinDocument> parser = new Parser<>(new GherkinAstBuilder());

    public GherkinDocument parse(Reader reader) {
        return parser.parse(reader);
    }


}
