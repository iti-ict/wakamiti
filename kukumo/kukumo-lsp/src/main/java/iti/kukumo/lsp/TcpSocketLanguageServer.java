package iti.kukumo.lsp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.kukumo.api.*;


public class TcpSocketLanguageServer  {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpSocketLanguageServer.class);

    private final InetSocketAddress endpoint;
    private final Thread internalRunner;
    private final int baseIndex;

    private ServerSocket serverSocket;

    public TcpSocketLanguageServer(InetSocketAddress address, int baseIndex) {
        this.endpoint = address;
        this.internalRunner = new Thread(this::run);
        this.baseIndex = baseIndex;
    }


    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(endpoint);
        internalRunner.start();
        LOGGER.info("Language Server listening at {}:{}", getAddress(), getPort());
    }


   private void run() {
	    LOGGER.info("Contributors available: {}", Kukumo.contributors().allContributors());
        var threadPool = Executors.newCachedThreadPool();
        while (!serverSocket.isClosed()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                LOGGER.info("New client connection: {}", socket.getPort());
            } catch (IOException e) {
                throw new KukumoException(e);
            }
            threadPool.submit(()-> launchLanguageServer(socket));
        }
    }


    private void launchLanguageServer(Socket socket) {
        try {
            LOGGER.info("Creating new server instance for connection {}", socket.getPort());
            var server = new KukumoLanguageServer(baseIndex);
            var launcher = LSPLauncher.createServerLauncher(
                server,
                socket.getInputStream(),
                socket.getOutputStream()
            );
            server.connect(launcher.getRemoteProxy());
            FutureUtil.whenDone(
                launcher.startListening(),
                ()->LOGGER.info("Server instance for connection {} closed.", socket.getPort())
            );
        } catch (IOException e) {
            throw new KukumoException(e.getMessage(),e);
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


    public void close() throws IOException {
    	serverSocket.close();
    }
}
