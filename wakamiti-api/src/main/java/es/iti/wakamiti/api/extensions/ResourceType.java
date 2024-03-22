/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;


/**
 * This interface defines a contract for handling different resource types.
 *
 * @param <T> The type of content that the resource represents.
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @implNote Implementations should provide logic for parsing different types of inputs
 * (e.g., InputStream, Reader) and specify the content type they handle.
 */
@ExtensionPoint
public interface ResourceType<T> extends Contributor {

    /**
     * Get the class of the content type that the resource represents.
     *
     * @return The class of the content type.
     */
    Class<T> contentType();

    /**
     * Get a description of the resource type.
     *
     * @return The description of the resource type.
     */
    String description();

    /**
     * Parse the input stream with the specified charset to obtain the content.
     *
     * @param stream  The input stream to parse.
     * @param charset The charset to use for parsing.
     * @return The parsed content of the resource.
     * @throws IOException If an I/O error occurs during parsing.
     */
    T parse(InputStream stream, Charset charset) throws IOException;

    /**
     * Parse the reader to obtain the content.
     *
     * @param reader The reader to parse.
     * @return The parsed content of the resource.
     * @throws IOException If an I/O error occurs during parsing.
     */
    T parse(Reader reader) throws IOException;

    /**
     * Check if the resource type accepts the given filename.
     *
     * @param filename The filename to check.
     * @return True if the resource type accepts the filename, false otherwise.
     */
    boolean acceptsFilename(String filename);

}