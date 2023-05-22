/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp.internal;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;

public class DocumentDiagnostics {

	private String uri;
	private List<Diagnostic> diagnostics;


	public DocumentDiagnostics(String uri, List<Diagnostic> diagnostics) {
		this.uri = uri;
		this.diagnostics = diagnostics;
	}


	public String uri() {
		return uri;
	}


	public List<Diagnostic> diagnostics() {
		return diagnostics;
	}



}