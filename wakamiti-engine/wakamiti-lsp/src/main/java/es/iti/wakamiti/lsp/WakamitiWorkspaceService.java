/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp;


import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import es.iti.wakamiti.lsp.internal.GherkinWorkspace;


/**
 * LSP workspace service for project-level notifications.
 * <p>
 * Current implementation logs workspace events and delegates no state changes
 * to {@link GherkinWorkspace}. Diagnostics are recomputed by document-level
 * events.
 * </p>
 */
public class WakamitiWorkspaceService implements WorkspaceService {

    private final WakamitiLanguageServer server;
    private final GherkinWorkspace workspace;

    WakamitiWorkspaceService(
            WakamitiLanguageServer server,
            GherkinWorkspace workspace
    ) {
        this.server = server;
        this.workspace = workspace;
    }

    /**
     * Receives client workspace-configuration changes.
     * <p>
     * This implementation currently logs the event and performs no additional
     * action.
     * </p>
     */
    @Override
    public void didChangeConfiguration(
            DidChangeConfigurationParams params
    ) {
        LoggerUtil.logEntry("workspace.didChangeConfiguration", params);
    }

    /**
     * Receives file-watcher notifications from the client workspace.
     * <p>
     * This implementation currently logs the event and performs no additional
     * action.
     * </p>
     */
    @Override
    public void didChangeWatchedFiles(
            DidChangeWatchedFilesParams params
    ) {
        LoggerUtil.logEntry("workspace.didChangeWatcherFiles", params);
    }

}
