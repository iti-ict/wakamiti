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
 * Provides the Report Output Files Uploader functionality used by Wakamiti.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "report-output-files-uploader",
        version = "2.6"
)
public class ReportOutputFilesUploader extends AbstractFilesUploader implements EventObserver {

    /** Configuration category for files emitted by report plugins. */
    public static final String CATEGORY = "reportOutputs";

    /**
     * Creates an uploader subscribed to report-output-file events.
     */
    public ReportOutputFilesUploader() {
        super(Event.REPORT_OUTPUT_FILE_WRITTEN, CATEGORY);
    }

}
