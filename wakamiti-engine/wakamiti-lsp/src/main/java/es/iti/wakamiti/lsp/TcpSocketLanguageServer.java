/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.WakamitiException;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.wakamiti.api.*;


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
	    LOGGER.info("Contributors available: {}", Wakamiti.contributors().allContributors());
        var threadPool = Executors.newCachedThreadPool();
        while (!serverSocket.isClosed()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                LOGGER.info("New client connection: {}", socket.getPort());
            } catch (IOException e) {
                throw new WakamitiException(e);
            }
            threadPool.submit(()-> launchLanguageServer(socket));
        }
    }


    private void launchLanguageServer(Socket socket) {
        try {
            LOGGER.info("Creating new server instance for connection {}", socket.getPort());
            var server = new WakamitiLanguageServer(baseIndex);
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
            throw new WakamitiException(e.getMessage(),e);
        }

    }


    private void assertServerRunning() {
        if (serverSocket == null || !serverSocket.isBound() || serverSocket.isClosed()) {
            throw new IllegalStateException("Wakamiti LSP Server is not running");
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