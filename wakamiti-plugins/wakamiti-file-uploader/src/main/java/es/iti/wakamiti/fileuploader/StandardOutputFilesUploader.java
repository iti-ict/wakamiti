package es.iti.wakamiti.fileuploader;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;

@Extension(provider =  "es.iti.wakamiti", name = "standard-output-files-uploader")
public class StandardOutputFilesUploader extends AbstractFilesUploader implements EventObserver {


    public static final String CATEGORY = "standardOutputs";

    public StandardOutputFilesUploader() {
        super(Event.STANDARD_OUTPUT_FILE_WRITTEN, CATEGORY);
    }


}
