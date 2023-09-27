package es.iti.wakamiti.fileuploader.test;


import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MockFtpServer {


    private FtpServer server;
    private ListenerFactory factory;
    private Listener listener;
    private int port;
    private Path tmpDir;


    public MockFtpServer start() throws FtpException, IOException {
        var serverFactory = new FtpServerFactory();
        this.factory = new ListenerFactory();
        this.port = findFreePort();
        factory.setPort(port);
        this.listener = factory.createListener();
        serverFactory.addListener("default", listener);
        var userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(passwordEncryptor());
        BaseUser user = createUser();
        UserManager userManager = userManagerFactory.createUserManager();
        userManager.save(user);
        serverFactory.setUserManager(userManager);
        this.server = serverFactory.createServer();
        server.start();
        return this;
    }


    public int getPort() {
        return port;
    }


    public void stop() {
        server.stop();
    }


    public Path getTmpDir() {
        return tmpDir;
    }

    private BaseUser createUser() throws IOException {
        BaseUser user = new BaseUser();
        user.setName("test");
        user.setPassword("test");
        this.tmpDir = Files.createTempDirectory("testftp");
        user.setHomeDirectory(tmpDir.toString());
        System.out.println(tmpDir);
        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        return user;
    }


    private PasswordEncryptor passwordEncryptor() {
        return new PasswordEncryptor() {
            @Override
            public String encrypt(String password) {
                return password;
            }
            @Override
            public boolean matches(String passwordToCheck, String storedPassword) {
                return passwordToCheck.equals(storedPassword);
            }
        };
    }


    private int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }


}
