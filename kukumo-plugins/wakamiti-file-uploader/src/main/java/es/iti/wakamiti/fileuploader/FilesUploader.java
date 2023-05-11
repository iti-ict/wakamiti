package es.iti.wakamiti.fileuploader;

import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.extensions.EventObserver;
import iti.kukumo.api.util.KukumoLogger;
import iti.kukumo.api.util.PathUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Extension(provider = "iti.kukumo", name = "files-uploader")
public class FilesUploader implements EventObserver {


    private final Logger logger = KukumoLogger.forClass(FilesUploader.class);

    private boolean enabled;
    private String host;
    private String username;
    private String password;
    private String remotePath;
    private String protocol;


    private FTPClient ftpClient;


    public void setEnable(boolean enabled) {
        this.enabled = enabled;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void eventReceived(Event event) {

        if (!enabled) {
            return;
        }

        try {
            if (Event.BEFORE_WRITE_OUTPUT_FILES.equals(event.type())) {
                openFtpConnection();
            } else if (Event.OUTPUT_FILE_PER_TEST_CASE_WRITTEN.equals(event.type())) {
                uploadFile((Path)event.data());
            } else if (Event.AFTER_WRITE_OUTPUT_FILES.equals(event.type())) {
                closeFtpConnection();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }



    private void openFtpConnection() throws IOException {
        if (ftpClient != null && ftpClient.isConnected()) {
            closeFtpConnection();
        }
        if ("ftp".equals(protocol)) {
            ftpClient = new FTPClient();
        } else if ("ftps".equals(protocol)) {
            ftpClient = new FTPSClient();
        } else {
            throw new KukumoException("Protocol not supported: "+protocol);
        }
        if (host.contains(":")) {
            ftpClient.connect(host.split(":")[0],Integer.parseInt(host.split(":")[1]));
        } else {
            ftpClient.connect(host);
        }

        ftpClient.login(username, password);
    }


    private void closeFtpConnection() throws IOException {
        if (ftpClient == null) {
            return;
        }
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
        ftpClient = null;
    }


    private void uploadFile(Path fileToSend) throws IOException {
        Path dirPath = PathUtil.replaceTemporalPlaceholders(Path.of(remotePath));
        createDestinationDirectory(dirPath);
        ftpClient.changeWorkingDirectory(dirPath.toString());
        String fileName = fileToSend.toFile().getName();
        try (InputStream inputStream = Files.newInputStream(fileToSend)) {
            ftpClient.storeFile(fileName, inputStream);
        }
    }


    private void createDestinationDirectory(Path dirPath) throws IOException {
        if (dirPath.getParent()!=null) {
            createDestinationDirectory(dirPath.getParent());
        }
        if (!ftpClient.changeWorkingDirectory(dirPath.toString())) {
            ftpClient.makeDirectory(dirPath.toString());
        }
    }


    @Override
    public boolean acceptType(String eventType) {
        return Event.BEFORE_WRITE_OUTPUT_FILES.equals(eventType) ||
                Event.AFTER_WRITE_OUTPUT_FILES.equals(eventType) ||
                Event.OUTPUT_FILE_PER_TEST_CASE_WRITTEN.equals(eventType);
    }


}
