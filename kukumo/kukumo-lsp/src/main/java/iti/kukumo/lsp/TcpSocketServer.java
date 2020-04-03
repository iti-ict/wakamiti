package iti.kukumo.lsp;

import org.eclipse.lsp4j.launch.LSPLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;

public class TcpSocketServer  {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpSocketServer.class);

    private final InetSocketAddress endpoint;
    private final Thread internalRunner;

    private ServerSocket serverSocket;

    public TcpSocketServer(InetSocketAddress address) {
        this.endpoint = address;
        this.internalRunner = new Thread(this::run);
    }


    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(endpoint);
        internalRunner.start();
        LOGGER.info("Listening at {}:{}", getAddress(), getPort());
    }


   private void run() {
        var threadPool = Executors.newCachedThreadPool();
        while (!serverSocket.isClosed()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                LOGGER.info("New client connection: {}", socket.getPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            threadPool.submit(()-> {
                launchLanguageServer(socket);
            });
        }
    }


    private void launchLanguageServer(Socket socket) {
        try {
            LOGGER.info("Creating new server instance for connection {}", socket.getPort());
            var server = new KukumoLanguageServer();
            var launcher = LSPLauncher.createServerLauncher(
                server,
                socket.getInputStream(),
                socket.getOutputStream()
            );
            server.connect(launcher.getRemoteProxy());
            Futures.whenDone(
                launcher.startListening(),
                ()->LOGGER.info("Server instance for connection {} closed.", socket.getPort())
            );
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }

    }


    private void assertServerRunning() {
        if (serverSocket == null || !serverSocket.isBound() || serverSocket.isClosed()) {
            throw new IllegalStateException("Kukumo LSP Server is not running");
        }
    }


    public int getPort() {
        assertServerRunning();
        return serverSocket.getLocalPort();
    }


    public InetAddress getAddress() {
        assertServerRunning();
        return serverSocket.getInetAddress();
    }

}
