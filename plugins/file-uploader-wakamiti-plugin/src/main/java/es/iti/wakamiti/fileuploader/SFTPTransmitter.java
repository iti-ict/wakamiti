package es.iti.wakamiti.fileuploader;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.nio.file.Path;


public class SFTPTransmitter implements FTPTransmitter {


    private ChannelSftp channel;
    private Session session;


    @Override
    public boolean isConnected() {
        return session != null && session.isConnected();
    }


    @Override
    public void connect(String username, String password, String host, int port) throws IOException {
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch ssh = new JSch();
            this.session = ssh.getSession(username, host, port);
            this.session.setPassword(password);
            this.session.connect();
            this.channel = (ChannelSftp) session.openChannel("sftp");
            this.channel.connect();
        } catch (JSchException e) {
            throw new IOException(e);
        }
    }


    @Override
    public void connect(String username, String password, String host) throws IOException {
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch ssh = new JSch();
            this.session = ssh.getSession(username, host);
            this.session.setPassword(password);
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
            channel.put(
                localFile.toAbsolutePath().toString(),
                destinationFolder.resolve(localFile.getFileName()).toString()
            );
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }


}
