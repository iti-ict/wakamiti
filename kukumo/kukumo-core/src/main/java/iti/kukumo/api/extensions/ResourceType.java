package iti.kukumo.api.extensions;

import iti.commons.jext.ExtensionPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * @author ITI
 *         Created by ITI on 9/01/19
 */
@ExtensionPoint
public interface ResourceType<T> extends Contributor {

    Class<T> contentType();

    String description();

    T parse(InputStream stream, Charset charset) throws IOException;

    T parse(Reader reader) throws IOException;

    boolean acceptsFilename(String filename);


}
