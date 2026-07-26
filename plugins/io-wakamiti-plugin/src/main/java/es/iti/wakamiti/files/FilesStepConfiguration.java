/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.files;


public final class FilesStepConfiguration {

    public static final String FILES_ACCESS_TIMEOUT = "files.timeout";
    public static final String FILES_LINKS = "files.links";
    public static final String FILES_ENABLE_CLEANUP_UPON_COMPLETION = "files.enableCleanupUponCompletion";

    private FilesStepConfiguration() {
        /* avoid instantiation */
    }

    public static final class Defaults {

    public static final Long DEFAULT_FILES_ACCESS_TIMEOUT = 60L;
    public static final boolean DEFAULT_FILES_ENABLE_CLEANUP_UPON_COMPLETION = Boolean.FALSE;

        private Defaults() {
            /* avoid instantiation */
        }

    }

}
