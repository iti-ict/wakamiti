/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ResourceType;
import es.iti.wakamiti.core.gherkin.parser.GherkinDocument;
import es.iti.wakamiti.core.gherkin.parser.GherkinParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;


/**
 * Provides methods to parse Gherkin feature files and
 * determine if a given filename is compatible with the
 * Gherkin resource type.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
@Extension(provider = "es.iti.wakamiti", name = GherkinResourceType.NAME,
        extensionPoint = "es.iti.wakamiti.api.extensions.ResourceType", version = "2.6")
public class GherkinResourceType implements ResourceType<GherkinDocument> {

    public static final String NAME = "gherkin";

    public static final GherkinResourceType INSTANCE = new GherkinResourceType();

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<GherkinDocument> contentType() {
        return GherkinDocument.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "GherkinResource File";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GherkinDocument parse(InputStream stream, Charset charset) throws IOException {
        try (Reader reader = new InputStreamReader(stream, charset)) {
            return parse(reader);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GherkinDocument parse(Reader reader) throws IOException {
        return new GherkinParser().parse(reader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsFilename(String filename) {
        return filename.endsWith(".feature") || filename.endsWith(".FEATURE");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return description();
    }
}