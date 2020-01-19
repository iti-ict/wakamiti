/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.gherkin;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import iti.commons.gherkin.GherkinDocument;
import iti.commons.gherkin.GherkinParser;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ResourceType;



@Extension(provider = "iti.kukumo", name = GherkinResourceType.NAME, extensionPoint = "iti.kukumo.api.extensions.ResourceType")
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
        return new GherkinParser().parse(reader);
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
