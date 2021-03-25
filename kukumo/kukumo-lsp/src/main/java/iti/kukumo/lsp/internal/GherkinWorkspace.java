package iti.kukumo.lsp.internal;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.stream.*;
import iti.kukumo.util.Pair;


import org.eclipse.lsp4j.*;
import org.yaml.snakeyaml.Yaml;

import iti.commons.configurer.Configuration;

public class GherkinWorkspace {


	private final Map<String, GherkinDocumentAssessor> documentAssessors = new HashMap<>();
	private final int baseIndex;
	private final Yaml yaml = new Yaml();
	private final Map<String, Pair<DocumentSegment,DocumentSegment>> linkMap = new HashMap<>();

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
		var documentDiagnostics = documentAssessors.values().stream()
			.map(GherkinDocumentAssessor::collectDiagnostics)
			.collect(toList());
		return computeInterDocumentDiagnostics(documentDiagnostics);
	}



	private Stream<DocumentDiagnostics>  computeInterDocumentDiagnostics(
		List<DocumentDiagnostics> documentDiagnostics
	) {

		Map<String,List<Diagnostic>> diagnosticsPerDocument = new HashMap<>();
		documentDiagnostics.forEach(document->
			diagnosticsPerDocument.put(document.uri(), document.diagnostics())
		);


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

		var repeatedDefs = computeRepeatedID(diagnosticsPerDocument, definitionIds);
		var repeatedImpls = computeRepeatedID(diagnosticsPerDocument, implementationIds);
		var nonLinkedDefs = computeNonLinkedID(
			diagnosticsPerDocument,
			definitionIds,
			implementationIds,
			"There is no implementation scenario with this ID"
		);
		var nonLinkedImpls = computeNonLinkedID(
			diagnosticsPerDocument,
			definitionIds,
			implementationIds,
			"There is no definition scenario with this ID"
		);

		var linkableIds = Stream.concat(definitionIds.stream(), implementationIds.stream())
			.map(DocumentSegment::content)
			.filter(id -> !repeatedDefs.contains(id))
			.filter(id -> !repeatedImpls.contains(id))
			.filter(id -> !nonLinkedDefs.contains(id))
			.filter(id -> !nonLinkedImpls.contains(id))
			.collect(toSet());

		computeLinkMap(definitionIds, implementationIds, linkableIds);

		return diagnosticsPerDocument.entrySet().stream()
			.map(entry->new DocumentDiagnostics(entry.getKey(), entry.getValue()));
	}




	private Set<String> computeRepeatedID(
		Map<String, List<Diagnostic>> diagnosticsPerDocument,
		List<DocumentSegment> definitionIds
	) {
		Set<String> repeatedIds = new HashSet<>();
		for (var segment : definitionIds) {
			boolean idIsRepeated = definitionIds.stream()
				.filter(id -> segment != id)
				.map(DocumentSegment::content)
				.anyMatch(segment.content()::equals);

			if (idIsRepeated) {
				diagnosticsPerDocument
					.computeIfAbsent(segment.uri(), x->new ArrayList<>())
					.add(DiagnosticHelper.diagnosticError(segment.range(), "Identifier already used"));
				repeatedIds.add(segment.content());
			}
		}
		return repeatedIds;
	}



	private Set<String> computeNonLinkedID(
		Map<String, List<Diagnostic>> diagnosticsPerDocument,
		List<DocumentSegment> sources,
		List<DocumentSegment> destinations,
		String message
	) {
		Set<String> nonLinkedIds = new HashSet<>();
		sources.stream()
		.filter(id -> destinations.stream().map(DocumentSegment::content).noneMatch(id.content()::equals))
		.forEach(segment -> {
			diagnosticsPerDocument
				.computeIfAbsent(segment.uri(), x->new ArrayList<>())
				.add(DiagnosticHelper.diagnosticWarn(segment.range(), message));
			nonLinkedIds.add(segment.content());
		});
		return nonLinkedIds;
	}





	private void computeLinkMap(
		List<DocumentSegment> definitionIds,
		List<DocumentSegment> implementationIds,
		Set<String> linkableIds
	) {
		linkMap.clear();
		var definitionsMap = definitionIds.stream().collect(toMap(DocumentSegment::content,x->x));
		var implementationsMap = implementationIds.stream().collect(toMap(DocumentSegment::content,x->x));
		for (String id : linkableIds) {
			var definitionId = definitionsMap.get(id);
			var implementationId = implementationsMap.get(id);
			linkMap.put(id, new Pair<>(definitionId, implementationId));
		}
	}





	public Optional<DocumentSegment> resolveImplementationLink(String uri, Position position) {
		return Optional.of(document(uri))
			.filter(GherkinDocumentAssessor::isDefinition)
			.flatMap(document -> document.obtainIdTagAt(position))
			.map(TextSegment::content)
			.map(linkMap::get)
			.map(Pair::value);
	}


	public Optional<DocumentSegment> resolveDefinitionLink(String uri, Position position) {
		return Optional.of(document(uri))
			.filter(GherkinDocumentAssessor::isImplementation)
			.flatMap(document -> document.obtainIdTagAt(position))
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



	private GherkinDocumentAssessor document(String uri) {
		return documentAssessors.computeIfAbsent(uri, x-> new GherkinDocumentAssessor(uri,""));
	}







}
