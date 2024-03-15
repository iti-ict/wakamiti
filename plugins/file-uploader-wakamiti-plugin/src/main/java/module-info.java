import es.iti.wakamiti.fileuploader.ReportOutputFilesUploader;
import es.iti.wakamiti.fileuploader.StandardOutputFilesUploader;
import es.iti.wakamiti.fileuploader.FilesUploaderConfigurator;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.fileuploader.TestCaseOutputFilesUploader;


module es.iti.wakamiti.file.uploader {

    requires es.iti.wakamiti.api;
    requires org.apache.commons.net;
    requires com.jcraft.jsch;

    provides ConfigContributor with FilesUploaderConfigurator;
    provides EventObserver with StandardOutputFilesUploader, TestCaseOutputFilesUploader, ReportOutputFilesUploader;

}