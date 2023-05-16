import es.iti.wakamiti.fileuploader.FilesUploader;
import es.iti.wakamiti.fileuploader.FilesUploaderConfigurator;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.EventObserver;

module es.iti.wakamiti.file.uploader {

    requires es.iti.wakamiti.api;
    requires org.apache.commons.net;

    provides ConfigContributor with FilesUploaderConfigurator;
    provides EventObserver with FilesUploader;

}