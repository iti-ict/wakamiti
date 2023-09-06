package es.iti.wakamiti.fileuploader;

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.util.ResourceLoader;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FTPClientTransmitter implements FTPTransmitter {

    private FTPClient ftpClient;

    public FTPClientTransmitter(boolean secure) {
        this.ftpClient = (secure ? new FTPSClient() : new FTPClient());
    }


    @Override
    public boolean isConnected() {
        return ftpClient.isConnected();
    }


    @Override
    public void connect(String username, String password, String host, int port) throws IOException {
        ftpClient.connect(host,port);
        ftpClient.login(username,password);
    }


    @Override
    public void connect(String username, String password, String host) throws IOException {
        ftpClient.connect(host);
        ftpClient.login(username,password);
    }


    @Override
    public void disconnect() throws IOException {
        ftpClient.disconnect();
    }


    @Override
    public void transferFile(Path localFile, Path destinationFolder) throws IOException {
        createDestinationDirectory(destinationFolder);
        ftpClient.changeWorkingDirectory(destinationFolder.toString());
        String fileName = localFile.toFile().getName();
        ResourceLoader resourceLoader = WakamitiAPI.instance().resourceLoader();
        try (InputStream inputStream = Files.newInputStream(resourceLoader.absolutePath(localFile))) {
            ftpClient.storeFile(fileName, inputStream);
        }
    }



    private void createDestinationDirectory(Path dirPath) throws IOException {
        if (dirPath.getParent() != null) {
            createDestinationDirectory(dirPath.getParent());
        }
        if (!ftpClient.changeWorkingDirectory(dirPath.toString())) {
            ftpClient.makeDirectory(dirPath.toString());
        }
    }


}
