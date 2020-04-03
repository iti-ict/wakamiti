package iti.kukumo.lsp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import iti.kukumo.lsp.internal.GherkinDocument;
import iti.kukumo.lsp.internal.TextRange;

public class KukumoTextDocumentService implements TextDocumentService {


    private final Map<String, GherkinDocument> gherkinDocuments = new HashMap<>();
    private final KukumoLanguageServer server;


    public KukumoTextDocumentService(KukumoLanguageServer server) {
        this.server = server;
    }


    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem input) {
        LoggerUtil.log("textDocument.resolveCompletionItem", input);
        return Futures.empty();
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        LoggerUtil.log("textDocument.completion", params);
        return Futures.run(input -> {
            String uri = input.getTextDocument().getUri();
            GherkinDocument document = gherkinDocuments.get(uri);
            List<CompletionItem> suggestions;
            if (document == null) {
                suggestions = List.of();
            } else {
                suggestions = document.collectCompletions(
                        input.getPosition().getLine(),
                        input.getPosition().getCharacter(
                        ));
            }
            return Either.forLeft(suggestions);
        }, params);

    }


    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LoggerUtil.log("textDocument.didOpen", params);
        String uri = params.getTextDocument().getUri();
        String type = params.getTextDocument().getLanguageId();
        String content = params.getTextDocument().getText();
        if (type.equals("gherkin")) {
            gherkinDocuments.computeIfAbsent(uri, key -> new GherkinDocument()).resetDocument(content);
            sendDiagnostics(uri);
        }
    }


    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LoggerUtil.log("textDocument.didChange", params);
        try {
            var uri = params.getTextDocument().getUri();
            var document = gherkinDocuments.get(uri);
            if (document != null) {
                for (var event : params.getContentChanges()) {
                    document.updateDocument(textRange(event.getRange()), event.getText());
                    sendDiagnostics(uri);
                }
            }
        } catch (Exception e) {
            // something wrong has happened, ask for the whole file
            throw e;
        }
    }



    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        LoggerUtil.log("textDocument.didClose", params);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        LoggerUtil.log("textDocument.didSave", params);
    }


    private void sendDiagnostics(String uri) {
        GherkinDocument document = gherkinDocuments.get(uri);
        if (document != null) {
            sendDiagnostics(uri, document.collectDiagnostics());
        }
    }


    private void sendDiagnostics(String uri, List<Diagnostic> diagnostics) {
        PublishDiagnosticsParams publishDiagnostics = new PublishDiagnosticsParams(uri, diagnostics);
			server.client.publishDiagnostics(publishDiagnostics);
    }

    private static TextRange textRange(Range range) {
        int startLine = range.getStart().getLine();
        int endLine = range.getEnd().getLine();
        int startPosition = range.getStart().getCharacter();
        int endPosition = range.getEnd().getCharacter();
        return TextRange.of(startLine,startPosition,endLine,endPosition);
    }

}
