/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.ResourceLoader;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


public class FTPClientTransmitter implements FTPTransmitter {

    private final FTPClient ftpClient;
    private String home;

    public FTPClientTransmitter(boolean secure) {
        this.ftpClient = (secure ? new FTPSClient() : new FTPClient());
    }

    @Override
    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    @Override
    public void connect(String username, String host, Integer port, String password, String identity) throws IOException {
        if (port != null) {
            ftpClient.connect(host, port);
        } else {
            ftpClient.connect(host);
        }
        if (password == null) {
            throw new WakamitiException("Password is required");
        }
        ftpClient.login(username, password);
        home = ftpClient.printWorkingDirectory();
    }

    @Override
    public void disconnect() throws IOException {
        ftpClient.disconnect();
    }

    @Override
    public void transferFile(Path localFile, Path destinationFolder) throws IOException {
        createDestinationDirectory(destinationFolder);
        ftpClient.changeWorkingDirectory(home);
        ftpClient.changeWorkingDirectory(destinationFolder.toString());
        ftpClient.printWorkingDirectory();
        String fileName = localFile.toFile().getName();
        ResourceLoader resourceLoader = WakamitiAPI.instance().resourceLoader();
        try (InputStream inputStream = Files.newInputStream(resourceLoader.absolutePath(localFile))) {
            ftpClient.storeFile(fileName, inputStream);
        }
        ftpClient.changeWorkingDirectory(home);
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
