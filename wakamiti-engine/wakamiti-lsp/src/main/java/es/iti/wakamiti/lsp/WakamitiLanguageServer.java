/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp;


import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.wakamiti.lsp.internal.DocumentDiagnostics;
import es.iti.wakamiti.lsp.internal.GherkinWorkspace;


/**
 * Language Server Protocol entry point for Wakamiti Gherkin tooling.
 * <p>
 * One server instance owns a shared {@link GherkinWorkspace} and coordinates
 * diagnostics and text-document features through its document/workspace
 * services.
 * </p>
 */
public class WakamitiLanguageServer implements LanguageServer, LanguageClientAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(WakamitiLanguageServer.class);

    private final WakamitiTextDocumentService textDocumentService;
    private final WakamitiWorkspaceService workspaceService;
    private final GherkinWorkspace workspace;

    LanguageClient client;

    /**
     * Creates an LSP server and its shared document workspace.
     *
     * @param baseIndex coordinate offset used by the connected client
     *                  (typically 0 or 1 depending on client conventions)
     */
    public WakamitiLanguageServer(
            int baseIndex
    ) {
        this.workspace = new GherkinWorkspace(baseIndex);
        this.textDocumentService = new WakamitiTextDocumentService(this, workspace, baseIndex);
        this.workspaceService = new WakamitiWorkspaceService(this, workspace);
    }

    /**
     * Describes the protocol features implemented by the Wakamiti server.
     * <p>
     * The server supports incremental synchronization, completion, quick
     * fixes, definition/implementation navigation, whole-document formatting
     * and document symbols.
     *
     * @return a new capabilities descriptor safe for caller modification
     */
    public static ServerCapabilities capabilities() {
        var capabilities = new ServerCapabilities();
        capabilities.setCompletionProvider(new CompletionOptions(true, null));
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
        capabilities.setCodeActionProvider(new CodeActionOptions(List.of("quickfix")));
        capabilities.setImplementationProvider(true);
        capabilities.setDefinitionProvider(true);
        capabilities.setDocumentFormattingProvider(true);
        capabilities.setDocumentSymbolProvider(true);
        return capabilities;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(
            InitializeParams params
    ) {
        return FutureUtil.processEvent("languageServer.initialize", params, x -> {
            InitializeResult result = new InitializeResult();
            result.setCapabilities(capabilities());
            return result;
        });
    }

    @Override
    public void initialized(
            InitializedParams params
    ) {
        LOGGER.info("EVENT initialized:\n{}", params);
        LanguageServer.super.initialized(params);
        client
                .workspaceFolders()
                .thenAccept(this::analyzeWorkspaceFolders)
                .thenAccept(x -> sendWorkspaceDiagnostics());
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        LOGGER.info("EVENT shutdown");
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public void exit() {
        LOGGER.info("EVENT exit");
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        LOGGER.info("EVENT getTextDocumentService");
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        LOGGER.info("EVENT getWorkspaceService");
        return workspaceService;
    }

    @Override
    public void connect(
            LanguageClient client
    ) {
        LOGGER.info("EVENT connect\n{}", client);
        this.client = client;
    }

    private void analyzeWorkspaceFolders(
            List<WorkspaceFolder> folders
    ) {
        folders.forEach(this::analyzeWorkspaceFolder);
    }

    private void analyzeWorkspaceFolder(
            WorkspaceFolder folder
    ) {
        var folderPath = Path.of(URI.create(folder.getUri()));
        if (Files.exists(folderPath)) {
            try (var walker = Files.walk(folderPath)) {
                walker.forEach(this::manageFile);
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Cannot open workspace folder {} : {}", folderPath, e.getMessage());
                LOGGER.debug("{}", e, e);
            }
        }
    }

    private void manageFile(
            Path file
    ) {
        try {
            var filename = file.getFileName().toString();
            if (filename.equals("wakamiti.yaml")) {
                workspace.addConfigurationWithoutDiagnostics(file.toUri().toString(), Files.readString(file));
            } else if (filename.endsWith(".feature")) {
                workspace.addGherkinWithoutDiagnostics(file.toUri().toString(), Files.readString(file));
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Cannot open workspace file {} : {}", file, e.getMessage());
            LOGGER.debug("{}", e, e);
        }
    }

    /**
     * Recomputes and publishes diagnostics for every known workspace document.
     */
    void sendWorkspaceDiagnostics() {
        sendDiagnostics(workspace.computeWorkspaceDiagnostics());
    }

    /**
     * Publishes diagnostics grouped by document URI.
     *
     * @param allDiagnostics diagnostics stream to publish
     */
    void sendDiagnostics(
            Stream<DocumentDiagnostics> allDiagnostics
    ) {
        allDiagnostics.forEach(document -> {
            var uri = document.uri();
            var publishDiagnostics = new PublishDiagnosticsParams(uri, document.diagnostics());
            LoggerUtil.logEntry("textDocument.publishDiagnostics", publishDiagnostics);
            client.publishDiagnostics(publishDiagnostics);
        });
    }

    private MessageParams error(
            String message
    ) {
        return new MessageParams(MessageType.Error, message);
    }

    private MessageParams warn(
            String message
    ) {
        return new MessageParams(MessageType.Warning, message);
    }

    private MessageParams info(
            String message
    ) {
        return new MessageParams(MessageType.Info, message);
    }

}
