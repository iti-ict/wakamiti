package iti.kukumo.lsp.internal;

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
