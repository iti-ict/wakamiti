/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import java.util.List;

import org.eclipse.lsp4j.Diagnostic;


/**
 * Provides the Document Diagnostics functionality used by Wakamiti.
 */
public class DocumentDiagnostics {

    private String uri;
    private List<Diagnostic> diagnostics;

    /**
     * Groups diagnostics for one document publication.
     *
     * @param uri         document URI
     * @param diagnostics diagnostics to publish
     */
    public DocumentDiagnostics(
            String uri,
            List<Diagnostic> diagnostics
    ) {
        this.uri = uri;
        this.diagnostics = diagnostics;
    }

    /**
     * @return the diagnosed document URI
     */
    public String uri() {
        return uri;
    }

    /**
     * @return diagnostics associated with the document
     */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

}
