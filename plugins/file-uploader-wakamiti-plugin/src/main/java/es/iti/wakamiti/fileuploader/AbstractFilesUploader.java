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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.util.WakamitiLogger;


/**
 * Base {@link EventObserver} for uploading generated Wakamiti output files to
 * a remote FTP, FTPS or SFTP server.
 * <p>
 * Implementations bind the uploader to a specific report-output event type and
 * category.
 * </p>
 */
public abstract class AbstractFilesUploader implements EventObserver {

    private static final Logger LOGGER = WakamitiLogger.forClass(AbstractFilesUploader.class);

    private final String eventType;
    private final String category;

    private boolean enabled;
    private String host;
    private String username;
    private String password;
    private String remotePath;
    private String protocol;
    private String identity;
    private FTPTransmitter transmitter;
    private Instant executionInstant;

    protected AbstractFilesUploader(
            String eventType,
            String category
    ) {
        this.eventType = eventType;
        this.category = category;
    }

    /**
     * Enables or disables remote uploads for this output category.
     *
     * @param enabled Whether this category uploads files during report events
     */
    public void setEnabled(
            boolean enabled
    ) {
        this.enabled = enabled;
    }

    /**
     * Sets the remote endpoint, optionally including a port as
     * {@code host:port}.
     *
     * @param host FTP, FTPS or SFTP endpoint
     */
    public void setHost(
            String host
    ) {
        this.host = host;
    }

    /**
     * Sets the username used to authenticate against the remote endpoint.
     *
     * @param username Remote account name
     */
    public void setUsername(
            String username
    ) {
        this.username = username;
    }

    /**
     * Sets the password used to authenticate against the remote endpoint.
     *
     * @param password Remote password; mandatory for FTP/FTPS
     */
    public void setPassword(
            String password
    ) {
        this.password = password;
    }

    /**
     * Sets the remote destination directory template.
     * Temporal placeholders such as {@code %DATE%}, {@code %TIME%} and
     * {@code %YYYY%} are resolved once per report-output cycle.
     *
     * @param remotePath Remote directory template
     */
    public void setRemotePath(
            String remotePath
    ) {
        this.remotePath = remotePath;
    }

    /**
     * Selects the transport protocol implementation used for uploads.
     *
     * @param protocol One of {@code ftp}, {@code ftps} or {@code sftp}
     */
    public void setProtocol(
            String protocol
    ) {
        this.protocol = protocol;
    }

    /**
     * Sets the SSH identity used by SFTP authentication when applicable.
     *
     * @param identity Optional SSH private-key path used by SFTP
     */
    public void setIdentity(
            String identity
    ) {
        this.identity = identity;
    }

    /**
     * Returns the configuration subsection associated with this uploader type.
     *
     * @return Configuration subsection associated with this output category
     */
    public String category() {
        return this.category;
    }

    @Override
    public void eventReceived(
            Event event
    ) {
        if (!enabled) {
            return;
        }

        try {
            if (Event.BEFORE_WRITE_OUTPUT_FILES.equals(event.type())) {
                openFtpConnection();
                this.executionInstant = Instant.now();
            } else if (this.eventType.equals(event.type()) && isConnected()) {
                uploadFile((Path) event.data());
            } else if (Event.AFTER_WRITE_OUTPUT_FILES.equals(event.type())) {
                closeFtpConnection();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Reports whether a transmitter exists and its underlying session is open.
     *
     * @return {@code true} while connected to the remote server
     */
    public boolean isConnected() {
        return transmitter != null && transmitter.isConnected();
    }

    private void openFtpConnection() throws IOException {
        if (isConnected()) {
            closeFtpConnection();
        }
        LOGGER.info("Opening FTP connection to {}", host);
        transmitter = FTPTransmitter.of(protocol);
        if (host.contains(":")) {
            transmitter.connect(username,
                    host.split(":")[0],
                    Integer.parseInt(host.split(":")[1]), password, identity);
        } else {
            transmitter.connect(username, host, null, password, identity);
        }
    }

    private void closeFtpConnection() throws IOException {
        if (transmitter == null) {
            return;
        }
        if (transmitter.isConnected()) {
            LOGGER.info("Closing FTP connection to {}", host);
            transmitter.disconnect();
        }
        transmitter = null;
    }

    private void uploadFile(
            Path fileToSend
    ) throws IOException {
        Path dirPath = replaceTemporalPlaceholders(remotePath, executionInstant.atZone(ZoneId.systemDefault()));
        Path localFile = WakamitiAPI.instance().resourceLoader().absolutePath(fileToSend);
        LOGGER.info("Uploading file {uri} to {uri}", localFile, host + "/" + dirPath);
        transmitter.transferFile(localFile, dirPath);
    }

    @Override
    public boolean acceptType(
            String eventType
    ) {
        return Event.BEFORE_WRITE_OUTPUT_FILES.equals(eventType)
                || Event.AFTER_WRITE_OUTPUT_FILES.equals(eventType)
                || this.eventType.equals(eventType);
    }

    // TODO: This helper belongs in wakamiti-api.
    private static Path replaceTemporalPlaceholders(
            String pathString,
            ZonedDateTime instant
    ) {
        pathString = pathString.replace("%YYYY%", DateTimeFormatter.ofPattern("yyyy").format(instant));
        pathString = pathString.replace("%YY%", DateTimeFormatter.ofPattern("yy").format(instant));
        pathString = pathString.replace("%MM%", DateTimeFormatter.ofPattern("MM").format(instant));
        pathString = pathString.replace("%DD%", DateTimeFormatter.ofPattern("dd").format(instant));
        pathString = pathString.replace("%hh%", DateTimeFormatter.ofPattern("HH").format(instant));
        pathString = pathString.replace("%mm%", DateTimeFormatter.ofPattern("mm").format(instant));
        pathString = pathString.replace("%ss%", DateTimeFormatter.ofPattern("ss").format(instant));
        pathString = pathString.replace("%sss%", DateTimeFormatter.ofPattern("SSS").format(instant));
        pathString = pathString.replace("%DATE%", DateTimeFormatter.ofPattern("yyyyMMdd").format(instant));
        pathString = pathString.replace("%TIME%", DateTimeFormatter.ofPattern("HHmmssSSS").format(instant));
        return Path.of(pathString);
    }

}
