/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.internal;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static java.util.Objects.requireNonNull;


/**
 * This class implements the virtual protocol 'classpath:' to be used in URLs
 */
public class ClasspathURLStreamHandler extends URLStreamHandler {

    private final ClassLoader classLoader;

    public ClasspathURLStreamHandler(ClassLoader classLoader) {
        this.classLoader = requireNonNull(classLoader,"A class loader is required for 'classpath:' schema");
    }


    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        var path = url.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        final URL resourceUrl = classLoader.getResource(path);
        if (resourceUrl == null) {
            throw new FileNotFoundException("Cannot access to classpath resource "+url);
        }
        return resourceUrl.openConnection();
    }

}