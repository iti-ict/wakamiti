/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import iti.commons.jext.ExtensionPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;



@ExtensionPoint
public interface ResourceType<T> extends Contributor {

    Class<T> contentType();


    String description();


    T parse(InputStream stream, Charset charset) throws IOException;


    T parse(Reader reader) throws IOException;


    boolean acceptsFilename(String filename);

}