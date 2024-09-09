/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Vector;

import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;


public class SFTPTransmitter implements FTPTransmitter {

    private ChannelSftp channel;
    private Session session;
    private String home;

    @Override
    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    @Override
    public void connect(String username, String host, Integer port, String password, String identity) throws IOException {
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch ssh = new JSch();
            if (identity != null) {
                ssh.addIdentity(identity);
            }
            if (port != null) {
                this.session = ssh.getSession(username, host, port);
            } else {
                this.session = ssh.getSession(username, host);
            }
            if (password != null) {
                this.session.setPassword(password);
            }
            this.session.connect();
            this.channel = (ChannelSftp) session.openChannel("sftp");
            this.channel.connect();
            this.home = channel.pwd();
        } catch (JSchException | SftpException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    @Override
    public void transferFile(Path localFile, Path destinationFolder) throws IOException {
        try {
            createDestinationDirectory(destinationFolder);
            channel.put(
                    localFile.toAbsolutePath().toString(),
                    destinationFolder.resolve(localFile.getFileName()).toString().replace("\\", "/")
            );
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    private void createDestinationDirectory(Path dirPath) throws SftpException {
        if (dirPath.getParent() != null) {
            createDestinationDirectory(dirPath.getParent());
        }
        if (!checkExists(dirPath.toString().replace("\\", "/"))) {
            channel.mkdir(dirPath.toString().replace("\\", "/"));
        }
    }

    private boolean checkExists(String path) throws SftpException {
        try {
            return !channel.ls(path).isEmpty();
        } catch (SftpException e) {
            if (e.id == SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            throw e;
        }
    }
}
