package iti.kukumo.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.kukumo.lsp.internal.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


public class KukumoLanguageServer implements LanguageServer, LanguageClientAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(KukumoLanguageServer.class);

    private final KukumoTextDocumentService textDocumentService;
    private final KukumoWorkspaceService workspaceService;
    private final GherkinWorkspace workspace;

    LanguageClient client;


    public KukumoLanguageServer(int baseIndex) {
    	this.workspace = new GherkinWorkspace(baseIndex);
        this.textDocumentService = new KukumoTextDocumentService(this, workspace, baseIndex);
        this.workspaceService = new KukumoWorkspaceService(this, workspace);
    }


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
        client
        	.workspaceFolders()
        	.thenAccept(this::analyzeWorkspaceFolders)
        	.thenAccept(x->sendWorkspaceDiagnostics());
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
        LOGGER.info("EVENT connect\n{}",client);
        this.client = client;
    }


    private void analyzeWorkspaceFolders(List<WorkspaceFolder> folders) {
    	folders.forEach(this::analyzeWorkspaceFolder);
    }


    private void analyzeWorkspaceFolder(WorkspaceFolder folder) {
    	var folderPath = Path.of(URI.create(folder.getUri()));
		if (Files.exists(folderPath)) {
			try(var walker = Files.walk(folderPath)) {
				walker.forEach(this::manageFile);
			} catch (IOException | RuntimeException e) {
				LOGGER.error("Cannot open workspace folder {} : {}", folderPath, e.getMessage());
	    		LOGGER.debug("{}",e,e);
			};
		}
    }


    private void manageFile(Path file) {
    	try {
	    	var filename = file.getFileName().toString();
	    	if (filename.equals("kukumo.yaml")) {
	    		workspace.addConfigurationWithoutDiagnostics(file.toUri().toString(), Files.readString(file));
	    	} else if (filename.endsWith(".feature")) {
	    		workspace.addGherkinWithoutDiagnostics(file.toUri().toString(), Files.readString(file));
	    	}
    	} catch (IOException | RuntimeException e) {
    		LOGGER.error("Cannot open workspace file {} : {}", file, e.getMessage());
    		LOGGER.debug("{}",e,e);
    	}
    }



    void sendWorkspaceDiagnostics() {
    	sendDiagnostics(workspace.computeWorkspaceDiagnostics());
    }


    void sendDiagnostics(Stream<DocumentDiagnostics> allDiagnostics) {
    	allDiagnostics.forEach(document->{
    		var uri = document.uri();
			var publishDiagnostics = new PublishDiagnosticsParams(uri, document.diagnostics());
			LoggerUtil.logEntry("textDocument.publishDiagnostics", publishDiagnostics);
			client.publishDiagnostics(publishDiagnostics);
    	});
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
