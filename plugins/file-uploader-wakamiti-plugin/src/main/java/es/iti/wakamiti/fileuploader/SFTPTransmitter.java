/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;

import java.io.IOException;
import java.nio.file.Path;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


/**
 * {@link FTPTransmitter} implementation backed by an SFTP connection.
 * <p>
 * The transmitter keeps one SSH session and one SFTP channel open between
 * {@link #connect(String, String, Integer, String, String)} and
 * {@link #disconnect()}.
 * </p>
 */
public class SFTPTransmitter implements FTPTransmitter {

    private ChannelSftp channel;
    private Session session;
    private String home;

    /**
     * Indicates whether the underlying SSH session is currently connected.
     *
     * @return {@code true} when the session exists and is connected
     */
    @Override
    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    /**
     * Opens an SSH session and SFTP channel using either password or identity
     * authentication.
     *
     * @param username login user
     * @param host     remote host
     * @param port     remote port, or {@code null} to use the SSH default
     * @param password password credential, or {@code null}
     * @param identity local private-key path, or {@code null}
     * @throws IOException when the session or channel cannot be opened
     */
    @Override
    public void connect(
            String username,
            String host,
            Integer port,
            String password,
            String identity
    ) throws IOException {
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

    /**
     * Closes the SFTP channel and SSH session if they were opened.
     *
     * @throws IOException kept for interface compatibility
     */
    @Override
    public void disconnect() throws IOException {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * Uploads one local file to the destination folder.
     * <p>
     * Missing destination directories are created recursively before the file
     * transfer starts.
     * </p>
     *
     * @param localFile         local file to upload
     * @param destinationFolder remote folder where the file will be stored
     * @throws IOException when directory creation or upload fails
     */
    @Override
    public void transferFile(
            Path localFile,
            Path destinationFolder
    ) throws IOException {
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

    private void createDestinationDirectory(
            Path dirPath
    ) throws SftpException {
        if (dirPath.getParent() != null) {
            createDestinationDirectory(dirPath.getParent());
        }
        if (!checkExists(dirPath.toString().replace("\\", "/"))) {
            channel.mkdir(dirPath.toString().replace("\\", "/"));
        }
    }

    private boolean checkExists(
            String path
    ) throws SftpException {
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
