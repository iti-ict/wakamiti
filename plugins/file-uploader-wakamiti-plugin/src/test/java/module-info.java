module es.iti.wakamiti.file.uploader.test {
    requires es.iti.wakamiti.file.uploader;
    requires junit;
    requires org.slf4j;
    requires es.iti.wakamiti.api;
    requires es.iti.wakamiti.core;
    requires ftpserver.core;
    requires ftplet.api;
    exports es.iti.wakamiti.fileuploader.test to junit, es.iti.wakamiti.core;
}