package iti.kukumo.lsp.internal;

import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.stream.Stream;

import org.eclipse.lsp4j.*;
import org.yaml.snakeyaml.Yaml;

import iti.commons.configurer.Configuration;

public class GherkinWorkspace {


	private final Map<String, GherkinDocumentAssessor> documentAssessors = new HashMap<>();
	private final int baseIndex;
	private final Yaml yaml = new Yaml();

	private String configurationUri;
	private TextDocument configurationDocument;

	public GherkinWorkspace(int baseIndex) {
		this.baseIndex = baseIndex;
	}

	public Stream<DocumentDiagnostics> addGherkin(String uri, String content) {
		documentAssessors.computeIfAbsent(uri, x-> new GherkinDocumentAssessor(uri,content));
		return computeAllDiagnostics();


	}


	public Stream<DocumentDiagnostics> addConfiguration(String uri, String content) {
		this.configurationUri = uri;
		this.configurationDocument = new TextDocument(content);
		return reloadConfiguration();
	}


	public Stream<DocumentDiagnostics> updateConfiguration(TextRange range, String text) {
		this.configurationDocument.replaceRange(range, text);
		return reloadConfiguration();
	}



	private Stream<DocumentDiagnostics> reloadConfiguration() {
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


	public List<CodeAction> computeCodeActions(String uri, List<Diagnostic> diagnostics) {
		var document = document(uri);
		return diagnostics.stream()
			.map(document::retrieveQuickFixes)
			.flatMap(List::stream)
			.collect(toList());
	}





	public List<CompletionItem> computeCompletions(String uri, Position position) {
		return document(uri).collectCompletions(
			position.getLine()- baseIndex,
			position.getCharacter() - baseIndex
		);
	}



	private Stream<DocumentDiagnostics> computeAllDiagnostics() {
		return Stream.concat(
			documentAssessors.values().stream().map(GherkinDocumentAssessor::collectDiagnostics),
			computeInterDocumentDiagnostics()
		);
	}



	private Stream<DocumentDiagnostics>  computeInterDocumentDiagnostics() {

		Map<String,List<Diagnostic>> diagnosticsPerDocument = new HashMap<>();

		var definitionIds = documentAssessors.values().stream()
			.filter(GherkinDocumentAssessor::isDefinition)
			.flatMap(GherkinDocumentAssessor::retriveIdTagSegment)
			.collect(toList())
		;
		var implementationIds = documentAssessors.values().stream()
			.filter(GherkinDocumentAssessor::isImplementation)
			.flatMap(GherkinDocumentAssessor::retriveIdTagSegment)
			.collect(toList())
		;

		computeRepeatedID(diagnosticsPerDocument, definitionIds);
		computeRepeatedID(diagnosticsPerDocument, implementationIds);

		definitionIds.stream()
		.filter(id -> implementationIds.stream().map(DocumentSegment::content).noneMatch(id.content()::equals))
		.forEach(segment -> diagnosticsPerDocument
			.computeIfAbsent(segment.uri(), x->new ArrayList<>())
			.add(DiagnosticHelper.diagnosticWarn(segment.range(), "There is no implementation scenario with this ID"))
		);

		implementationIds.stream()
		.filter(id -> definitionIds.stream().map(DocumentSegment::content).noneMatch(id.content()::equals))
		.forEach(segment -> diagnosticsPerDocument
			.computeIfAbsent(segment.uri(), x->new ArrayList<>())
			.add(DiagnosticHelper.diagnosticWarn(segment.range(), "There is no definition scenario with this ID"))
		);

		return diagnosticsPerDocument.entrySet().stream()
			.map(entry->new DocumentDiagnostics(entry.getKey(), entry.getValue()));
	}



	private void computeRepeatedID(
		Map<String, List<Diagnostic>> diagnosticsPerDocument,
		List<DocumentSegment> definitionIds
	) {
		for (var segment : definitionIds) {
			boolean idIsRepeated = definitionIds.stream()
				.filter(id -> segment != id)
				.map(DocumentSegment::content)
				.anyMatch(segment.content()::equals);

			if (idIsRepeated) {
				diagnosticsPerDocument
				.computeIfAbsent(segment.uri(), x->new ArrayList<>())
				.add(DiagnosticHelper.diagnosticError(segment.range(), "Identifier already used"));
			}
		}
	}



	private GherkinDocumentAssessor document(String uri) {
		return documentAssessors.computeIfAbsent(uri, x-> new GherkinDocumentAssessor(uri,""));
	}




}
