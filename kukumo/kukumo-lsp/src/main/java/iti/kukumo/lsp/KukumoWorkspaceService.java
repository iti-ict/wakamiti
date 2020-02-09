package iti.kukumo.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class KukumoWorkspaceService implements WorkspaceService {

    public KukumoWorkspaceService(InitializeParams params) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // TODO Auto-generated method stub
        System.out.println("didChangeConfiguration");

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // TODO Auto-generated method stub
        System.out.println("didChangeWatchedFiles");

    }

}
