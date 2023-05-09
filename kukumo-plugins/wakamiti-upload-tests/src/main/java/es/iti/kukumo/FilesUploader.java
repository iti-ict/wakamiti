package es.iti.kukumo;

import imconfig.Configurable;
import imconfig.Configuration;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.extensions.EventObserver;
import iti.kukumo.api.util.KukumoLogger;
import iti.kukumo.api.util.PathUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FilesUploader implements EventObserver, Configurable {
    private static final String FTP_ENABLE = "fileUploader.enable";
    private static final String FTP_HOST = "fileUploader.host";
    private static final String FTP_USER = "fileUploader.credentials.username";
    private static final String FTP_PRIVATE_KEY_PATH = "fileUploader.credentials.password";
    private static final String FTP_REMOTE_DIR = "fileUploader.destinationDir";
    private final Logger logger = KukumoLogger.forClass(FilesUploader.class);

    @Override
    public void eventReceived(Event event) {
        if (FTP_ENABLE == "true") {
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(FTP_HOST);
                ftpClient.login(FTP_USER, FTP_PRIVATE_KEY_PATH);
                String dirPath = FTP_REMOTE_DIR;
                dirPath = PathUtil.replaceTemporalPlaceholders(dirPath);

                if (!ftpClient.changeWorkingDirectory(dirPath)) {
                    if (ftpClient.makeDirectory(dirPath)) {
                        ftpClient.changeWorkingDirectory(dirPath);
                    }
                }

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                Path results = (Path) event.data();
                File file = results.toFile();
                String fileName = file.getName();
                InputStream inputStream = new FileInputStream(file);
                ftpClient.storeFile(fileName, inputStream);
                inputStream.close();

                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean acceptType(String eventType) {
        return false;
    }

    @Override
    public void configure(Configuration configuration) {

    }
}
