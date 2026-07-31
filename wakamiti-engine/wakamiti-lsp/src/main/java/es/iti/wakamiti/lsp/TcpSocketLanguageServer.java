/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
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

import org.eclipse.lsp4j.launch.LSPLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.core.Wakamiti;


/**
 * Hosts Wakamiti's language server over TCP sockets.
 * <p>
 * Each accepted socket is served by a dedicated
 * {@link WakamitiLanguageServer} instance created on a cached thread pool.
 * </p>
 */
public class TcpSocketLanguageServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpSocketLanguageServer.class);

    private final InetSocketAddress endpoint;
    private final Thread internalRunner;
    private final int baseIndex;

    private ServerSocket serverSocket;

    /**
     * Creates a TCP language-server host without binding its socket.
     *
     * @param address local endpoint on which {@link #start()} will listen
     * @param baseIndex coordinate offset expected by connected clients
     */
    public TcpSocketLanguageServer(
            InetSocketAddress address,
            int baseIndex
    ) {
        this.endpoint = address;
        this.internalRunner = new Thread(this::run);
        this.baseIndex = baseIndex;
    }

    /**
     * Binds the configured endpoint and starts accepting clients in the
     * internal runner thread.
     *
     * @throws IOException if the endpoint cannot be bound
     * @throws IllegalThreadStateException if this instance has already started
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(endpoint);
        internalRunner.start();
        LOGGER.info("Language Server listening at {}:{}", getAddress(), getPort());
    }

    /**
     * Accept loop that creates one language-server instance per client socket.
     * <p>
     * The loop runs in {@link #internalRunner} after {@link #start()} and
     * stops when the server socket is closed.
     * </p>
     */
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
            threadPool.submit(() -> launchLanguageServer(socket));
        }
    }

    private void launchLanguageServer(
            Socket socket
    ) {
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
                    () -> LOGGER.info("Server instance for connection {} closed.", socket.getPort())
            );
        } catch (IOException e) {
            throw new WakamitiException(e.getMessage(), e);
        }
    }

    private void assertServerRunning() {
        if (serverSocket == null || !serverSocket.isBound() || serverSocket.isClosed()) {
            throw new IllegalStateException("Wakamiti LSP Server is not running");
        }
    }

    /**
     * Returns the bound local port.
     *
     * @return actual listening port, including an automatically assigned port
     *         when the configured endpoint used port zero
     * @throws IllegalStateException if the server is not currently running
     */
    public int getPort() {
        assertServerRunning();
        return serverSocket.getLocalPort();
    }

    /**
     * Returns the local address to which the server socket is bound.
     *
     * @return bound network address
     * @throws IllegalStateException if the server is not currently running
     */
    public InetAddress getAddress() {
        assertServerRunning();
        return serverSocket.getInetAddress();
    }

    /**
     * Stops accepting new connections by closing the listening socket.
     * <p>
     * Calling this method more than once is safe for already-closed sockets but
     * still requires the server to have been started.
     * </p>
     *
     * @throws IOException if the socket cannot be closed
     * @throws NullPointerException if the server has not been started
     */
    public void close() throws IOException {
        serverSocket.close();
    }

}
