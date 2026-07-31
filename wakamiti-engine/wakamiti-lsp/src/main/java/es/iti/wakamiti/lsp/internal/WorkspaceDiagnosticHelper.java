/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp.internal;


import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.CreateFileOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import es.iti.wakamiti.api.util.Pair;


/**
 * Provides the Workspace Diagnostic Helper functionality used by Wakamiti.
 */
public class WorkspaceDiagnosticHelper {

    private final GherkinWorkspace workspace;
    private final Map<String, Map<Range, List<CodeAction>>> quickFixes = new HashMap<>();

    /**
     * Creates a helper for diagnostics that depend on relationships between
     * multiple workspace documents.
     *
     * @param workspace workspace whose definition and implementation IDs are
     *                  correlated
     */
    public WorkspaceDiagnosticHelper(
            GherkinWorkspace workspace
    ) {
        this.workspace = workspace;
    }

    /**
     * Retrieves workspace-level fixes for selected diagnostics.
     *
     * @param uri document containing the diagnostics
     * @param diagnostics diagnostics selected by the client
     * @return actions whose registered ranges match the selected diagnostics
     */
    public Stream<CodeAction> retrieveCodeActions(
            String uri,
            List<Diagnostic> diagnostics
    ) {
        List<Range> ranges = diagnostics.stream().map(Diagnostic::getRange).toList();
        return quickFixes
                .getOrDefault(uri, Map.of())
                .entrySet().stream()
                .filter(e -> ranges.contains(e.getKey()))
                .flatMap(e -> e.getValue().stream());
    }

    /**
     * Rebuilds quick fixes from the latest inter-document diagnostics.
     * <p>
     * A missing implementation ID receives an action that either appends the
     * corresponding scenario to an existing implementation file or creates a
     * new implementation feature.
     *
     * @param diagnosticsPerDocument diagnostics grouped by document URI
     */
    public void registerWorkspaceQuickFixes(
            Map<String, List<Diagnostic>> diagnosticsPerDocument
    ) {
        quickFixes.clear();
        for (var documentDiagnostic : diagnosticsPerDocument.entrySet()) {
            String uri = documentDiagnostic.getKey();
            for (var diagnostic : documentDiagnostic.getValue()) {
                var message = diagnostic.getMessage();
                if (!message.isLeft() || !"There is no implementation scenario with this ID".equals(message.getLeft())) {
                    continue;
                }
                String id = diagnostic.getRelatedInformation().get(0).getMessage();
                var codeAction = createImplementationCodeAction(uri, id, diagnostic);
                quickFixes
                        .computeIfAbsent(uri, x -> new HashMap<>())
                        .computeIfAbsent(diagnostic.getRange(), x -> new ArrayList<>())
                        .add(codeAction);
            }
        }
    }

    /**
     * Validates scenario-ID relationships across definition and implementation
     * documents.
     * <p>
     * Duplicate IDs and IDs missing on either side are reported. Only unique,
     * bidirectionally matched IDs are entered in the workspace navigation map.
     * The returned groups include both the input diagnostics and newly created
     * relationship diagnostics.
     *
     * @param documentDiagnostics document-local diagnostics to augment
     * @return augmented diagnostics grouped by document
     */
    public Stream<DocumentDiagnostics> computeInterDocumentDiagnostics(
            List<DocumentDiagnostics> documentDiagnostics
    ) {
        Map<String, List<Diagnostic>> diagnosticsPerDocument = new HashMap<>();
        documentDiagnostics.forEach(document ->
                diagnosticsPerDocument.put(document.uri(), document.diagnostics())
        );

        var definitionIds = workspace.documentAssessors.values().stream()
                .filter(GherkinDocumentAssessor::isDefinition)
                .flatMap(GherkinDocumentAssessor::retriveIdTagSegment)
                .collect(toList());
        var implementationIds = workspace.documentAssessors.values().stream()
                .filter(GherkinDocumentAssessor::isImplementation)
                .flatMap(GherkinDocumentAssessor::retriveIdTagSegment)
                .collect(toList());

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
                implementationIds,
                definitionIds,
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

        registerWorkspaceQuickFixes(diagnosticsPerDocument);

        return diagnosticsPerDocument.entrySet().stream()
                .map(entry -> new DocumentDiagnostics(entry.getKey(), entry.getValue()));
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
                        .computeIfAbsent(segment.uri(), x -> new ArrayList<>())
                        .add(DocumentDiagnosticHelper.diagnostic(
                                DiagnosticSeverity.Error,
                                segment.uri(),
                                segment.range(),
                                "Identifier already used",
                                segment.content()
                        ));
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
                            .computeIfAbsent(segment.uri(), x -> new ArrayList<>())
                            .add(DocumentDiagnosticHelper.diagnostic(
                                    DiagnosticSeverity.Warning,
                                    segment.uri(),
                                    segment.range(),
                                    message,
                                    segment.content()
                            ));
                    nonLinkedIds.add(segment.content());
                });
        return nonLinkedIds;
    }

    private void computeLinkMap(
            List<DocumentSegment> definitionIds,
            List<DocumentSegment> implementationIds,
            Set<String> linkableIds
    ) {
        workspace.linkMap.clear();
        var definitionsMap = definitionIds.stream().collect(toMap(DocumentSegment::content, x -> x));
        var implementationsMap = implementationIds.stream().collect(toMap(DocumentSegment::content, x -> x));
        for (String id : linkableIds) {
            var definitionId = definitionsMap.get(id);
            var implementationId = implementationsMap.get(id);
            workspace.linkMap.put(id, new Pair<>(definitionId, implementationId));
        }
    }

    private CodeAction createImplementationCodeAction(
            String uri,
            String id,
            Diagnostic diagnostic
    ) {
        List<Either<TextDocumentEdit, ResourceOperation>> changes = new ArrayList<>();

        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        CodeAction codeAction = new CodeAction();
        codeAction.setEdit(workspaceEdit);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setIsPreferred(Boolean.TRUE);
        codeAction.setTitle("Create implementation");
        codeAction.setEdit(workspaceEdit);
        codeAction.setDiagnostics(List.of(diagnostic));
        workspaceEdit.setDocumentChanges(changes);

        GherkinDocumentAssessor definition = workspace.document(uri);
        GherkinDocumentAssessor implementation = definition.obtainIdTags()
                .stream()
                .map(TextSegment::content)
                .filter(workspace.linkMap::containsKey)
                .map(workspace.linkMap::get)
                .map(Pair::value)
                .map(DocumentSegment::uri)
                .map(workspace::document)
                .findAny()
                .orElse(null);

        if (implementation == null) {
            var definitionFilename = definition.path().getFileName().toString();
            var newFilename = definitionFilename.replace(".", "-impl.");
            var implementationUri = definition.path().getParent().resolve(newFilename).toString();
            var createFile = new CreateFile(implementationUri);
            createFile.setOptions(new CreateFileOptions(false, false));
            changes.add(Either.forRight(createFile));

            String newFileContent =
                    Snippets.implementationFeatureSnippet(definition)
                            + Snippets.implementationScenarioSnippet(id, definition, implementation);

            TextEdit textEdit = new TextEdit(startOfLine(0), newFileContent);
            var textDocument = new VersionedTextDocumentIdentifier(implementationUri, null);
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
            textDocumentEdit.setTextDocument(textDocument);
            textDocumentEdit.setEdits(List.of(Either.forLeft(textEdit)));
            changes.add(Either.forLeft(textDocumentEdit));
        } else {
            String newText = Snippets.implementationScenarioSnippet(id, definition, implementation);
            Range range = startOfLine(implementation.numberOfLines());
            TextEdit textEdit = new TextEdit(range, newText);
            var textDocument = new VersionedTextDocumentIdentifier(implementation.uri(), null);
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
            textDocumentEdit.setTextDocument(textDocument);
            textDocumentEdit.setEdits(List.of(Either.forLeft(textEdit)));
            changes.add(Either.forLeft(textDocumentEdit));
        }
        return codeAction;
    }

    private Range startOfLine(
            int line
    ) {
        return new Range(new Position(line, 0), new Position(line, 0));
    }

}
