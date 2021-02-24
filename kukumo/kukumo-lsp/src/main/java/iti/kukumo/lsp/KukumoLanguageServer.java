package iti.kukumo.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class KukumoLanguageServer implements LanguageServer, LanguageClientAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(KukumoLanguageServer.class);

    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;
    LanguageClient client;


    public KukumoLanguageServer(int baseIndex) {
        this.textDocumentService = new KukumoTextDocumentService(this, baseIndex);
        this.workspaceService = new KukumoWorkspaceService(this);
    }


    public static ServerCapabilities capabilities() {
        var capabilities = new ServerCapabilities();
        capabilities.setCompletionProvider(new CompletionOptions(true, null));
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
        capabilities.setCodeActionProvider(new CodeActionOptions(List.of("quickfix")));
        return capabilities;
    }


    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        return FutureUtil.processEvent("languageServer.initialize", params, x -> {
            InitializeResult result = new InitializeResult();
            result.setCapabilities(capabilities());
            return result;
        });
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
        /*
        client.logMessage(info("[LSP Server] Connected"));
        client.showMessage(info("Connected to Kukumo LSP Server"));
         */

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
