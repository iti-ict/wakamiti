package iti.kukumo.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class KukumoWorkspaceService implements WorkspaceService {

    private final KukumoLanguageServer server;

    public KukumoWorkspaceService(KukumoLanguageServer server) {
        this.server = server;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        LoggerUtil.log("workspace.didChangeConfiguration",params);

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        LoggerUtil.log("workspace.didChangeWatcherFiles", params);

    }

}
