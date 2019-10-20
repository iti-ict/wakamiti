/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import iti.commons.jext.ExtensionPoint;



@ExtensionPoint
public interface ResourceType<T> extends Contributor {

    Class<T> contentType();


    String description();


    T parse(InputStream stream, Charset charset) throws IOException;


    T parse(Reader reader) throws IOException;


    boolean acceptsFilename(String filename);

}
