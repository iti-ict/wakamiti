package iti.kukumo.lsp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.cli.ParseException;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KukumoLspLauncher extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(KukumoLspLauncher.class);




    public static void main(String[] args) throws ParseException {
        CliArguments arguments = new CliArguments().parse(args);
        if (arguments.isHelpActive()) {
            arguments.printUsage();
        } else {
            int port = arguments.port();
            KukumoLspLauncher launcher = new KukumoLspLauncher(port);
            launcher.start();
            System.out.println(launcher.getPort());
        }
    }




    private final SocketAddress endpoint;
    private ServerSocket serverSocket;

    public KukumoLspLauncher(int port) {
        this.endpoint = new InetSocketAddress(port);
    }

    public KukumoLspLauncher() {
        this.endpoint = null;
    }



    @Override
    public synchronized void start() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(endpoint);
            LOGGER.info("Listening at {}:{}",getAddress(),getPort());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
        super.start();
    }

    public void finalize() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }


    /**
     * Don't call this method directly. Use {@link #startServer()} instead
     */
    @Override
    public void run() {
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
                launchServer(socket);
            });
        }
    }


    private KukumoLspServer launchServer(Socket socket) {
        try {
            var server = new KukumoLspServer();
            var launcher = LSPLauncher.createServerLauncher(
                    server,
                socket.getInputStream(),
                socket.getOutputStream()
            );
            server.connect(launcher.getRemoteProxy());
            launcher.startListening();
            return server;
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


    public Socket openSocket() throws IOException {
        return new Socket(getAddress(),getPort());
    }
}
