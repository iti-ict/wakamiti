import es.iti.wakamiti.fileuploader.FilesUploader;
import es.iti.wakamiti.fileuploader.FilesUploaderConfigurator;
import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.api.extensions.EventObserver;

module kukumo.file.uploader {

    requires kukumo.api;
    requires org.apache.commons.net;

    provides ConfigContributor with FilesUploaderConfigurator;
    provides EventObserver with FilesUploader;

}