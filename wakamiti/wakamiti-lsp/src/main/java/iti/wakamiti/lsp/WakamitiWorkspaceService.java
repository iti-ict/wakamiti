/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.WorkspaceService;

import iti.wakamiti.lsp.internal.GherkinWorkspace;

public class WakamitiWorkspaceService implements WorkspaceService {

    private final WakamitiLanguageServer server;
	private final GherkinWorkspace workspace;

    WakamitiWorkspaceService(WakamitiLanguageServer server, GherkinWorkspace workspace) {
        this.server = server;
        this.workspace = workspace;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        LoggerUtil.logEntry("workspace.didChangeConfiguration",params);

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        LoggerUtil.logEntry("workspace.didChangeWatcherFiles", params);

    }

}