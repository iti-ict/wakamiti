/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;


@Extension(provider =  "es.iti.wakamiti", name = "standard-output-files-uploader", version = "2.5")
public class StandardOutputFilesUploader extends AbstractFilesUploader implements EventObserver {

    public static final String CATEGORY = "standardOutputs";

    public StandardOutputFilesUploader() {
        super(Event.STANDARD_OUTPUT_FILE_WRITTEN, CATEGORY);
    }

}
