/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.files;

public class FilesStepConfiguration {

    public static String FILES_ACCESS_TIMEOUT = "files.timeout";
    public static String FILES_LINKS = "files.links";
    public static String FILES_ENABLE_CLEANUP_UPON_COMPLETION = "files.enableCleanupUponCompletion";

    private FilesStepConfiguration() {
        /* avoid instantiation */
    }

    public static class Defaults {

        public static Long DEFAULT_FILES_ACCESS_TIMEOUT = 60L;
        public static boolean DEFAULT_FILES_ENABLE_CLEANUP_UPON_COMPLETION = false;

        private Defaults() {
            /* avoid instantiation */
        }
    }
}