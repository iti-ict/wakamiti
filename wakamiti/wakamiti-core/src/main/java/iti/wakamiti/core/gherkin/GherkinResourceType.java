/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.core.gherkin;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import iti.commons.gherkin.GherkinDocument;
import iti.commons.gherkin.GherkinParser;
import iti.commons.jext.Extension;
import iti.wakamiti.api.extensions.ResourceType;



@Extension(provider = "iti.wakamiti", name = GherkinResourceType.NAME, extensionPoint = "iti.wakamiti.api.extensions.ResourceType", version = "1.1")
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