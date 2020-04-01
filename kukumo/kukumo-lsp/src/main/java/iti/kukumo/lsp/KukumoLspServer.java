package iti.kukumo.lsp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KukumoLspServer implements LanguageServer, LanguageClientAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(KukumoLspLauncher.class);

    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;

    LanguageClient client;


    public KukumoLspServer() {
        this.textDocumentService = new KukumoTextDocumentService(this);
        this.workspaceService = new KukumoWorkspaceService(this);
    }


    public static ServerCapabilities capabilities() {
        var capabilities = new ServerCapabilities();
        capabilities.setCompletionProvider(new CompletionOptions(true, null));
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
        return capabilities;
    }


    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        LOGGER.info("EVENT initialize:\n{}",params);
        InitializeResult result = new InitializeResult();
        result.setCapabilities(capabilities());
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public void initialized(InitializedParams params) {
        LOGGER.info("EVENT initialized:\n{}",params);
        LanguageServer.super.initialized(params);
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
    public void connect(LanguageClient client) {
        LOGGER.info("EVENT connect");
        this.client = client;
        client.logMessage(info("[LSP Server] Connected"));
        client.showMessage(info("Connected to Kukumo LSP Server"));

    }




    private MessageParams error(String message) {
        return new MessageParams(MessageType.Error,message);
    }

    private MessageParams warn(String message) {
        return new MessageParams(MessageType.Warning,message);
    }

    private MessageParams info(String message) {
        return new MessageParams(MessageType.Info,message);
    }



}
