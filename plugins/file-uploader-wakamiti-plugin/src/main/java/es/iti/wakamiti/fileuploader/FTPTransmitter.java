package es.iti.wakamiti.fileuploader;

import es.iti.wakamiti.api.WakamitiException;
import java.io.IOException;
import java.nio.file.Path;


public interface FTPTransmitter {


    static FTPTransmitter of(String protocol) {
        if ("ftp".equals(protocol)) {
            return new FTPClientTransmitter(false);
        } else if ("ftps".equals(protocol)) {
            return new FTPClientTransmitter(true);
        } else if ("sftp".equals(protocol)) {
            return new SFTPTransmitter();
        } else {
            throw new WakamitiException("Protocol not supported: " + protocol);
        }
    }

    boolean isConnected();

    void connect(String username, String host, Integer port, String password, String identity) throws IOException;

    void disconnect() throws IOException;

    void transferFile(Path localFile, Path destinationFolder) throws IOException;
}
