package es.iti.wakamiti.fileuploader;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


public class SFTPTransmitter implements FTPTransmitter {


    private ChannelSftp channel;
    private Session session;


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
        } catch (JSchException e) {
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
                destinationFolder.resolve(localFile.getFileName()).toString()
            );
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }



    private void createDestinationDirectory(Path dirPath) throws SftpException {
        if (dirPath.getParent() != null) {
            createDestinationDirectory(dirPath.getParent());
        }
        if (!checkExists(dirPath.toString())) {
            channel.mkdir(dirPath.toString());
        }
    }


    private boolean checkExists(String path) {
        try (InputStream stream = channel.get(path)) {
            return true;
        } catch (IOException | SftpException e) {
            return false;
        }
    }

}
