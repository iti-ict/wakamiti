/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;


/**
 * Provides the Test Case Output Files Uploader functionality used by Wakamiti.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "test-case-output-files-uploader",
        version = "2.13"
)
public class TestCaseOutputFilesUploader extends AbstractFilesUploader implements EventObserver {

    /** Configuration category for files produced by individual test cases. */
    public static final String CATEGORY = "testCaseOutputs";

    /**
     * Creates an uploader subscribed to test-case-output-file events.
     */
    public TestCaseOutputFilesUploader() {
        super(Event.TEST_CASE_OUTPUT_FILE_WRITTEN, CATEGORY);
    }

}
