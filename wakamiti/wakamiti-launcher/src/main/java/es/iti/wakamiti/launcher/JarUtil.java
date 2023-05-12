/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.launcher;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarUtil {

    public static Path jarFolder() throws URISyntaxException {
        return Paths.get(
            WakamitiLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
        ).getParent();
    }
}