package iti.kukumo.gherkin;


import gherkin.Parser;
import gherkin.ast.GherkinDocument;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ResourceType;
import iti.kukumo.gherkin.parser.GherkinAstBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * @author ITI
 *         Created by ITI on 9/01/19
 */
@Extension(
    provider="iti.kukumo", 
    name = GherkinResourceType.NAME, 
    version = "1.0",
    extensionPoint = "iti.kukumo.api.extensions.ResourceType",
    extensionPointVersion = "1.0"
)
public class GherkinResourceType implements ResourceType<GherkinDocument> {

    public static final String NAME = "gherkin";

    public static final GherkinResourceType INSTANCE = new GherkinResourceType();


    @Override
    public Class<GherkinDocument> contentType() {
        return GherkinDocument.class;
    }

    @Override
    public String description() {
        return "GherkinResource File";
    }

    @Override
    public GherkinDocument parse(InputStream stream, Charset charset) throws IOException {
        try (Reader reader = new InputStreamReader(stream, charset)) {
            return parse(reader);
        }
    }

    @Override
    public GherkinDocument parse(Reader reader) throws IOException {
        Parser<GherkinDocument> gherkinParser = new Parser<>(new GherkinAstBuilder());
        return gherkinParser.parse(reader);
    }


    @Override
    public boolean acceptsFilename(String filename) {
        return filename.endsWith(".feature") || filename.endsWith(".FEATURE");
    }

    @Override
    public String toString() {
        return description();
    }
}
