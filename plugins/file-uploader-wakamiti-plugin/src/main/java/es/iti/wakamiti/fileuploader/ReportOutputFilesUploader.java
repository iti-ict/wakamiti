package es.iti.wakamiti.fileuploader;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;

@Extension(provider =  "es.iti.wakamiti", name = "report-output-files-uploader", version = "2.5")
public class ReportOutputFilesUploader extends AbstractFilesUploader implements EventObserver {


    public static final String CATEGORY = "reportOutputs";

    public ReportOutputFilesUploader() {
        super(Event.REPORT_OUTPUT_FILE_WRITTEN, CATEGORY);
    }


}
