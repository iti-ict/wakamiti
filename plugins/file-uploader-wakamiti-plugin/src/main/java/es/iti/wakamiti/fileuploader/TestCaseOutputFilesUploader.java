package es.iti.wakamiti.fileuploader;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;

@Extension(provider =  "es.iti.wakamiti", name = "test-case-output-files-uploader", version = "2.5")
public class TestCaseOutputFilesUploader extends AbstractFilesUploader implements EventObserver {


    public static final String CATEGORY = "testCaseOutputs";

    public TestCaseOutputFilesUploader() {
        super(Event.TEST_CASE_OUTPUT_FILE_WRITTEN, CATEGORY);
    }


}
