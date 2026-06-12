/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.it;

import org.junit.AssumptionViolatedException;
import org.testcontainers.lifecycle.Startable;

import java.util.Locale;

final class TestcontainersWindowsNpipe {

    private static final String[] WINDOWS_NPIPE_ERROR_MARKERS = {
            "npipe://",
            "\\\\.\\pipe\\",
            "//./pipe/",
            "named pipe",
            "docker_engine",
            "dockerdesktoplinuxengine",
            "dockerdesktopwindowsengine"
    };

    private TestcontainersWindowsNpipe() {
    }

    static void startOrSkipOnWindowsNpipeFailure(Startable container) {
        try {
            container.start();
        } catch (RuntimeException e) {
            if (isWindows() && isWindowsNpipeConnectionError(e)) {
                throw new AssumptionViolatedException(
                        "Skipping Testcontainers test: Docker npipe is not reachable on Windows", e);
            }
            throw e;
        }
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name", "");
        return osName.toLowerCase(Locale.ROOT).contains("win");
    }

    static boolean isWindowsNpipeConnectionError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalizedMessage = message.toLowerCase(Locale.ROOT);
                for (String marker : WINDOWS_NPIPE_ERROR_MARKERS) {
                    if (normalizedMessage.contains(marker)) {
                        return true;
                    }
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
