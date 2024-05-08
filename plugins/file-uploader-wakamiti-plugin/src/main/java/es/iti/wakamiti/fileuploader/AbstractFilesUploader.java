/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.util.PathUtil;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


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

    protected AbstractFilesUploader(String eventType, String category) {
        this.eventType = eventType;
        this.category = category;
    }

    public void setEnabled(boolean enabled) {
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

    public void setIdentity(String identity) {
        this.identity = identity;
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

    private void uploadFile(Path fileToSend) throws IOException {
        Path dirPath = replaceTemporalPlaceholders(remotePath, executionInstant.atZone(ZoneId.systemDefault()));
        Path localFile = WakamitiAPI.instance().resourceLoader().absolutePath(fileToSend);
        LOGGER.info("Uploading file {uri} to {uri}", localFile, host + "/" + dirPath);
        transmitter.transferFile(localFile, dirPath);
    }

    @Override
    public boolean acceptType(String eventType) {
        return Event.BEFORE_WRITE_OUTPUT_FILES.equals(eventType) ||
                Event.AFTER_WRITE_OUTPUT_FILES.equals(eventType) ||
                this.eventType.equals(eventType);
    }

    // TODO: move to wakamiti-api
    private static final DateTimeFormatter YEAR_4 = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter YEAR_2 = DateTimeFormatter.ofPattern("yy", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd", Locale.ENGLISH);
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH", Locale.ENGLISH);
    private static final DateTimeFormatter MINUTE = DateTimeFormatter.ofPattern("mm", Locale.ENGLISH);
    private static final DateTimeFormatter SEC = DateTimeFormatter.ofPattern("ss", Locale.ENGLISH);
    private static final DateTimeFormatter MILLIS = DateTimeFormatter.ofPattern("SSS", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmssSSS", Locale.ENGLISH);

    private static Path replaceTemporalPlaceholders(String pathString, ZonedDateTime instant) {
        pathString = pathString.replace("%YYYY%", YEAR_4.format(instant));
        pathString = pathString.replace("%YY%", YEAR_2.format(instant));
        pathString = pathString.replace("%MM%", MONTH.format(instant));
        pathString = pathString.replace("%DD%", DAY.format(instant));
        pathString = pathString.replace("%hh%", HOUR.format(instant));
        pathString = pathString.replace("%mm%", MINUTE.format(instant));
        pathString = pathString.replace("%ss%", SEC.format(instant));
        pathString = pathString.replace("%sss%", MILLIS.format(instant));
        pathString = pathString.replace("%DATE%", DATE.format(instant));
        pathString = pathString.replace("%TIME%", TIME.format(instant));
        return Path.of(pathString);
    }
}
