package es.iti.wakamiti.fileuploader;


import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import static java.util.Collections.singletonList;


public class MockSftpServer {

    private final int port;
    private SshServer server;
    private FileSystem fileSystem;
    private Path tmpDir;

    public MockSftpServer(int port) {
        this.port = port;
    }

    public MockSftpServer start() throws IOException {
        SshServer server = SshServer.setUpDefaultServer();
        server.setPort(port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setHostBasedAuthenticator(this::authenticator);
        server.setPasswordAuthenticator(this::authenticate);
        server.setSubsystemFactories(singletonList(new SftpSubsystemFactory()));
        this.tmpDir = Files.createTempDirectory("testftp");
        server.setFileSystemFactory(new VirtualFileSystemFactory(tmpDir));
        server.start();
        this.server = server;
        return this;
    }

    private boolean authenticate(
            String username,
            String password,
            ServerSession session
    ) {
        return true;
    }

    private boolean authenticator(ServerSession var1, String var2, PublicKey var3, String var4, String var5, List<X509Certificate> var6) {
        return true;
    }

    public int getPort() {
        return port;
    }

    public void stop() throws IOException {
        server.stop();
    }

    public Path getTmpDir() {
        return tmpDir;
    }


}
