/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import es.iti.wakamiti.api.WakamitiException;


/**
 * Defines the contract implemented by FTPTransmitter.
 */
public interface FTPTransmitter {

    /**
     * Creates a transmitter for a configured protocol.
     *
     * @param protocol lowercase {@code ftp}, {@code ftps} or {@code sftp}
     * @return a disconnected transmitter
     * @throws WakamitiException when the protocol is unsupported
     */
    static FTPTransmitter of(
            String protocol
    ) {
        Map<String, Supplier<FTPTransmitter>> factory = Map.of(
                "ftp", () -> new FTPClientTransmitter(false),
                "ftps", () -> new FTPClientTransmitter(true),
                "sftp", SFTPTransmitter::new
        );
        if (!factory.containsKey(protocol)) {
            throw new WakamitiException("Protocol not supported: " + protocol);
        }
        return factory.get(protocol).get();
    }

    /**
     * @return whether the underlying control connection or SSH session is open
     */
    boolean isConnected();

    /**
     * Opens and authenticates a remote transfer session.
     *
     * @param username remote account
     * @param host remote host name or address
     * @param port explicit port, or {@code null} for the protocol default
     * @param password optional for key-based SFTP, required by FTP/FTPS
     * @param identity optional SSH private-key path
     * @throws IOException if connection or authentication fails
     */
    void connect(
            String username,
            String host,
            Integer port,
            String password,
            String identity
    ) throws IOException;

    /**
     * Closes the remote transfer session.
     *
     * @throws IOException if the transport cannot disconnect cleanly
     */
    void disconnect() throws IOException;

    /**
     * Uploads one local file, creating missing remote directories recursively.
     *
     * @param localFile local source path
     * @param destinationFolder remote destination directory
     * @throws IOException if directory creation or transfer fails
     */
    void transferFile(
            Path localFile,
            Path destinationFolder
    ) throws IOException;

}
