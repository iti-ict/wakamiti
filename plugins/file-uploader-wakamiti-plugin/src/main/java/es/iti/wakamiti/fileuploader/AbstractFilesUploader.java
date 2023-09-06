package es.iti.wakamiti.fileuploader;

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.util.PathUtil;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;
import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractFilesUploader implements EventObserver {


    private final Logger logger = WakamitiLogger.forClass(AbstractFilesUploader.class);

    private final String eventType;
    private final String category;

    private boolean enabled;
    private String host;
    private String username;
    private String password;
    private String remotePath;
    private String protocol;

    private FTPTransmitter transmitter;


    protected AbstractFilesUploader(String eventType, String category) {
        this.eventType = eventType;
        this.category = category;
    }


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


    public String category() {
        return this.category;
    }


    @Override
    public void eventReceived(Event event) {
        if (!enabled) {
            return;
        }

        try {
            if (Event.BEFORE_WRITE_OUTPUT_FILES.equals(event.type())) {
                openFtpConnection();
            } else if (this.eventType.equals(event.type())) {
                uploadFile((Path) event.data());
            } else if (Event.AFTER_WRITE_OUTPUT_FILES.equals(event.type())) {
                closeFtpConnection();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }


    private void openFtpConnection() throws IOException {
        if (transmitter != null && transmitter.isConnected()) {
            closeFtpConnection();
        }
        logger.info("Opening FTP connection to {}", host);
        transmitter = FTPTransmitter.of(protocol);
        if (host.contains(":")) {
            transmitter.connect(username, password, host.split(":")[0], Integer.parseInt(host.split(":")[1]));
        } else {
            transmitter.connect(username, password, host);
        }

    }


    private void closeFtpConnection() throws IOException {
        if (transmitter == null) {
            return;
        }
        if (transmitter.isConnected()) {
            logger.info("Closing FTP connection to {}", host);
            transmitter.disconnect();
        }
        transmitter = null;
    }


    private void uploadFile(Path fileToSend) throws IOException {
        Path dirPath = PathUtil.replaceTemporalPlaceholders(Path.of(remotePath));
        Path localFile = WakamitiAPI.instance().resourceLoader().absolutePath(fileToSend);
        logger.info("Uploading file {uri} to {uri}", localFile, host + "/" + dirPath);
        transmitter.transferFile(localFile, dirPath);
    }



    @Override
    public boolean acceptType(String eventType) {
        return Event.BEFORE_WRITE_OUTPUT_FILES.equals(eventType) ||
                Event.AFTER_WRITE_OUTPUT_FILES.equals(eventType) ||
                this.eventType.equals(eventType);
    }



}
