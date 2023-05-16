package es.iti.wakamiti.fileuploader;

import imconfig.Configuration;
import imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;

@Extension(provider =  "es.iti.wakamiti", name = "file-uploader-configurator", extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor")
public class FilesUploaderConfigurator implements ConfigContributor<FilesUploader> {

    private static final String ENABLE = "fileUploader.enable";
    private static final String HOST = "fileUploader.host";
    private static final String USERNAME = "fileUploader.credentials.username";
    private static final String PASSWORD = "fileUploader.credentials.password";
    private static final String DESTINATION_DIR = "fileUploader.destinationDir";
    private static final String PROTOCOL = "fileUploader.protocol";


    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
            ENABLE,"false",
            PROTOCOL,"ftps"
        );
    }


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof FilesUploader;
    }


    @Override
    public Configurer<FilesUploader> configurer() {
        return this::configure;
    }


    private void configure(FilesUploader filesUploader, Configuration configuration) {
        configuration.get(ENABLE,Boolean.class).ifPresent(filesUploader::setEnable);
        configuration.get(HOST,String.class).ifPresent(filesUploader::setHost);
        configuration.get(USERNAME,String.class).ifPresent(filesUploader::setUsername);
        configuration.get(PASSWORD,String.class).ifPresent(filesUploader::setPassword);
        configuration.get(DESTINATION_DIR,String.class).ifPresent(filesUploader::setRemotePath);
        configuration.get(PROTOCOL,String.class).ifPresent(filesUploader::setProtocol);
    }


}
