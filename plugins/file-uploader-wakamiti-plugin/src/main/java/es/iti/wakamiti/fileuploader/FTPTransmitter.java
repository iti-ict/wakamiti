/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import es.iti.wakamiti.api.WakamitiException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;


public interface FTPTransmitter {

    static FTPTransmitter of(String protocol) {
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

    boolean isConnected();

    void connect(String username, String host, Integer port, String password, String identity) throws IOException;

    void disconnect() throws IOException;

    void transferFile(Path localFile, Path destinationFolder) throws IOException;

}
