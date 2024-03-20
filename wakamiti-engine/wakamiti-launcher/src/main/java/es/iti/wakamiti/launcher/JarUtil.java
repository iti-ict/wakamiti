/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.launcher;


import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Utility class for handling JAR files in the context of WakamitiLauncher.
 *
 */
public class JarUtil {

    /**
     * Retrieves the folder containing the JAR file from which WakamitiLauncher is executed.
     *
     * <p>This method uses the protection domain of the WakamitiLauncher class to obtain the code source,
     * converts the URI of the code source to a Path, and adjusts the path for compatibility across
     * different operating systems.</p>
     *
     * @return The Path representing the folder containing the JAR file.
     * @throws URISyntaxException If a URI syntax exception occurs.
     */
    public static Path jarFolder() throws URISyntaxException {
        return Paths.get(
            WakamitiLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
                    .replaceAll("^/\\w:", "")
        ).getParent();
    }
}