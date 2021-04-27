package iti.kukumo.lsp.internal;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.stream.*;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.yaml.snakeyaml.Yaml;

import iti.commons.configurer.Configuration;
import iti.kukumo.util.Pair;

public class GherkinWorkspace {


	final Map<String, GherkinDocumentAssessor> documentAssessors = new HashMap<>();
	final int baseIndex;
	final Yaml yaml = new Yaml();
	final Map<String, Pair<DocumentSegment,DocumentSegment>> linkMap = new HashMap<>();
	final WorkspaceDiagnosticHelper diagnosticHelper;

	private String configurationUri;
	private TextDocument configurationDocument;

	public GherkinWorkspace(int baseIndex) {
		this.baseIndex = baseIndex;
		this.diagnosticHelper = new WorkspaceDiagnosticHelper(this);
	}

	public Stream<DocumentDiagnostics> addGherkin(String uri, String content) {
		documentAssessors.computeIfAbsent(uri, x-> new GherkinDocumentAssessor(uri,content));
		return computeAllDiagnostics();


	}


	public Stream<DocumentDiagnostics> addConfiguration(String uri, String content) {
		this.configurationUri = uri;
		this.configurationDocument = new TextDocument(content);
		return computeWorkspaceDiagnostics();
	}


	public void addGherkinWithoutDiagnostics(String uri, String content) {
		documentAssessors.computeIfAbsent(uri, x-> new GherkinDocumentAssessor(uri,content));
	}


	public void addConfigurationWithoutDiagnostics(String uri, String content) {
		this.configurationUri = uri;
		this.configurationDocument = new TextDocument(content);
	}




	public Stream<DocumentDiagnostics> updateConfiguration(TextRange range, String text) {
		this.configurationDocument.replaceRange(range, text);
		return computeWorkspaceDiagnostics();
	}

	public DocumentDiagnostics computeDiagnostics(String uri) {
		return document(uri).collectDiagnostics();
	}


	public Stream<DocumentDiagnostics> computeWorkspaceDiagnostics() {
		try {
			var workspaceConfiguration = Configuration.fromMap(
				yaml.load(configurationDocument.rawText())
			);
			documentAssessors.values().forEach(
				document->document.setWorkspaceConfiguration(workspaceConfiguration)
			);
			return computeAllDiagnostics();

		} catch (RuntimeException e) {
			return Stream.empty();
		}
	}



	public Stream<DocumentDiagnostics> update(String uri, TextRange range, String text) {
		if (uri.equals(configurationUri)) {
			return updateConfiguration(range, text);
		} else {
			document(uri).updateDocument(range, text);
			return computeAllDiagnostics();
		}
	}


	public List<CodeAction> obtainCodeActions(String uri, List<Diagnostic> diagnostics) {
		var document = document(uri);

		Stream<CodeAction> codeActionsFromDocument = diagnostics.stream()
			.map(document::retrieveQuickFixes)
			.flatMap(List::stream);

		Stream<CodeAction> codeActionsFromWorkspace = diagnosticHelper
			.retrieveCodeActions(uri, diagnostics);

		return Stream
			.concat(codeActionsFromDocument, codeActionsFromWorkspace)
			.collect(Collectors.toList());
	}



	public List<CompletionItem> computeCompletions(String uri, Position position) {
		return document(uri).collectCompletions(
			position.getLine()- baseIndex,
			position.getCharacter() - baseIndex
		);
	}



	public List<DocumentSymbol> documentSymbols(String uri) {
		return document(uri).collectSymbols();
	}



	private Stream<DocumentDiagnostics> computeAllDiagnostics() {
		var documentDiagnostics = documentAssessors.values().stream()
			.map(GherkinDocumentAssessor::collectDiagnostics)
			.collect(toList());
		return diagnosticHelper.computeInterDocumentDiagnostics(documentDiagnostics);
	}










	public Optional<DocumentSegment> resolveImplementationLink(String uri, Position position) {
		return Optional.of(document(uri))
			.filter(GherkinDocumentAssessor::isDefinition)
			.flatMap(document -> document.obtainIdAt(position))
			.map(TextSegment::content)
			.map(linkMap::get)
			.map(Pair::value);
	}


	public Optional<DocumentSegment> resolveDefinitionLink(String uri, Position position) {
		return Optional.of(document(uri))
			.filter(GherkinDocumentAssessor::isImplementation)
			.flatMap(document -> document.obtainIdAt(position))
			.map(TextSegment::content)
			.map(linkMap::get)
			.map(Pair::key);
	}


	public Pair<Range, String> format(String uri, int tabSize) {
		var document = document(uri);
		int numberOfLines = document.documentMap.document().numberOfLines();
		int lastPosition = document.documentMap.document().extractLine(numberOfLines-1).length();
		Range range = new Range(new Position(0,0), new Position(numberOfLines, lastPosition));
		String formatted = GherkinFormatter.format(document.documentMap, tabSize);
		return new Pair<>(range,formatted);
	}



	GherkinDocumentAssessor document(String uri) {
		return documentAssessors.computeIfAbsent(uri, x-> new GherkinDocumentAssessor(uri,""));
	}










}
