/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.files;


/**
 * Stores the configuration used by the Files Step Configuration component.
 */
public final class FilesStepConfiguration {

    /** Configuration key for the maximum time to wait for an accessible file. */
    public static final String FILES_ACCESS_TIMEOUT = "files.timeout";
    /** Configuration key containing symbolic names mapped to file locations. */
    public static final String FILES_LINKS = "files.links";
    /** Configuration key controlling deletion of files created during execution. */
    public static final String FILES_ENABLE_CLEANUP_UPON_COMPLETION = "files.enableCleanupUponCompletion";

    private FilesStepConfiguration() {
        /* avoid instantiation */
    }

    /**
     * Provides the Defaults functionality used by Wakamiti.
     */
    public static final class Defaults {

    /** Default file-access timeout, in seconds, used when no value is configured. */
    public static final Long DEFAULT_FILES_ACCESS_TIMEOUT = 60L;
    /** Default policy that preserves generated files after scenario completion. */
    public static final boolean DEFAULT_FILES_ENABLE_CLEANUP_UPON_COMPLETION = Boolean.FALSE;

        private Defaults() {
            /* avoid instantiation */
        }

    }

}
