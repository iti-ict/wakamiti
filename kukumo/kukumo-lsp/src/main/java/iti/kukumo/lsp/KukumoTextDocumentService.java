package iti.kukumo.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.kukumo.lsp.internal.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public class KukumoTextDocumentService implements TextDocumentService {

    private static final Logger logger = LoggerFactory.getLogger("kukumix.lsp");
    private final Map<String, GherkinDocumentAssessor> documentAssessors = new HashMap<>();
    private final KukumoLanguageServer server;
    private final int baseIndex;


    public KukumoTextDocumentService(KukumoLanguageServer server, int baseIndex) {
        this.server = server;
        this.baseIndex = baseIndex;
    }


    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem input) {
        LoggerUtil.logEntry("textDocument.resolveCompletionItem", input);
        return FutureUtil.empty();
    }


    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return FutureUtil.processEvent("textDocument.completion", params, this::doCompletion);
    }


    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
        CodeActionParams params
    ) {
        return FutureUtil.processEvent("textDocument.codeAction", params, this::doCodeAction);
    }


    private Either<List<CompletionItem>, CompletionList> doCompletion (CompletionParams params) {
        String uri = params.getTextDocument().getUri();
        GherkinDocumentAssessor documentAssessor = documentAssessors.get(uri);
        List<CompletionItem> suggestions;
        if (documentAssessor == null) {
            suggestions = List.of();
        } else {
            suggestions = documentAssessor.collectCompletions(
                params.getPosition().getLine() - baseIndex,
                params.getPosition().getCharacter() - baseIndex
            );
        }
        return Either.forLeft(suggestions);
    }


    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didOpen", params);
        String uri = params.getTextDocument().getUri();
        String type = params.getTextDocument().getLanguageId();
        String content = params.getTextDocument().getText();
        if (type.equals("kukumix.gherkin")) {
            documentAssessors.computeIfAbsent(uri, key -> new GherkinDocumentAssessor(content));
            sendDiagnostics(uri);
        }
    }


    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didChange", params);
        try {
            var uri = params.getTextDocument().getUri();
            var documentAssessor = documentAssessors.get(uri);
            if (documentAssessor != null) {
                for (var event : params.getContentChanges()) {
                    documentAssessor.updateDocument(textRange(event.getRange()), event.getText());
                    sendDiagnostics(uri);
                }
            }
        } catch (Exception e) {
            // something wrong has happened, ask for the whole file
            throw e;
        }
    }


    private List<Either<Command, CodeAction>> doCodeAction(CodeActionParams params) {

        var uri = params.getTextDocument().getUri();
        var documentAssessor = documentAssessors.get(uri);
        if (documentAssessor == null) {
            return List.of();
        }

        List<Either<Command, CodeAction>> codeActions =  params.getContext().getDiagnostics()
            .stream()
            .map(documentAssessor::retrieveQuickFixes)
            .flatMap(list->list.stream())
            .map(codeAction->Either.<Command, CodeAction>forRight(codeAction))
            .collect(toList());

           VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier(
            params.getTextDocument().getUri(),
            null
        );

        codeActions.stream()
            .map(either->either.getRight())
            .map(CodeAction::getEdit)
            .map(edit->edit.getDocumentChanges())
            .flatMap(list->list.stream())
            .map(either->either.getLeft())
            .forEach(textDocumentEdit->textDocumentEdit.setTextDocument(textDocument));

        return codeActions;
    }


    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didClose", params);
    }


    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didSave", params);
    }


    private void sendDocumentAdditionalInfo(String uri) {
        sendDiagnostics(uri);
    }


    private void sendDiagnostics(String uri) {
        GherkinDocumentAssessor documentAssessor = documentAssessors.get(uri);
        if (documentAssessor != null) {
            List<Diagnostic> diagnostics = documentAssessor.collectDiagnostics();
            PublishDiagnosticsParams publishDiagnostics = new PublishDiagnosticsParams(uri, diagnostics);
            LoggerUtil.logEntry("textDocument.publishDiagnostics", publishDiagnostics);
            server.client.publishDiagnostics(publishDiagnostics);
        }
    }


    private TextRange textRange(Range range) {
        int startLine = range.getStart().getLine() - baseIndex;
        int endLine = range.getEnd().getLine() - baseIndex;
        int startPosition = range.getStart().getCharacter() - baseIndex;
        int endPosition = range.getEnd().getCharacter() - baseIndex;
        return TextRange.of(startLine,startPosition,endLine,endPosition);
    }

}
