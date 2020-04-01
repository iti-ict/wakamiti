package iti.kukumo.lsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import iti.kukumo.lsp.internal.GherkinCompleter;
import iti.kukumo.lsp.internal.TextRange;

public class KukumoTextDocumentService implements TextDocumentService {


    private final Map<String,GherkinCompleter> completers = new HashMap<>();
    private final KukumoLspServer server;


    public KukumoTextDocumentService(KukumoLspServer server) {
        this.server = server;
    }


    

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem input) {
        LoggerUtil.log("textDocument.resolveCompletionItem",input);
        return Futures.empty();
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        LoggerUtil.log("textDocument.completion",params);
        return Futures.run(input -> {
            String uri = input.getTextDocument().getUri();
            GherkinCompleter completer = completers.get(uri);
            List<CompletionItem> suggestions;
            if (completer == null) {
                suggestions = List.of();
            } else {
                suggestions = completer.suggest(
                    input.getPosition().getLine(),
                    input.getPosition().getCharacter(
                ));
            }
            return Either.forLeft(suggestions);
        }, params);

    }




    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LoggerUtil.log("textDocument.didOpen",params);
        String uri = params.getTextDocument().getUri();
        String type = params.getTextDocument().getLanguageId();
        String content = params.getTextDocument().getText();
        if (type.equals("gherkin")) {
            completers.computeIfAbsent(uri, key -> new GherkinCompleter()).resetDocument(content);
            List<Diagnostic> diagnostics = completers.get(uri).diagnostics();            
			PublishDiagnosticsParams publishDiagnostics = new PublishDiagnosticsParams(uri, diagnostics );
			server.client.publishDiagnostics(publishDiagnostics);
            System.out.println("-------------------");
            System.out.println(completers.get(uri).currentContent());
            System.out.println("-------------------");
        }
    }


    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LoggerUtil.log("textDocument.didChange",params);
        try {
        	var uri = params.getTextDocument().getUri();
            var completer = completers.get(uri);
            if (completer != null) {
                for (var event : params.getContentChanges()) {
                    int startLine = event.getRange().getStart().getLine();
                    int endLine = event.getRange().getEnd().getLine();
                    int startPosition = event.getRange().getStart().getCharacter();
                    int endPosition = event.getRange().getEnd().getCharacter();
                    String delta = event.getText();
                    completer.updateDocument(TextRange.of(startLine,startPosition,endLine,endPosition),delta);
                    System.out.println("-------------------");
                    System.out.println(completer.currentContent());
                    System.out.println("-------------------");
                    List<Diagnostic> diagnostics = completer.diagnostics();            
        			PublishDiagnosticsParams publishDiagnostics = new PublishDiagnosticsParams(uri, diagnostics);
        			server.client.publishDiagnostics(publishDiagnostics);
                }
            }
        } catch (Exception e) {
            // something wrong has happened, ask for the whole file
            throw e;
        }
    }


    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        LoggerUtil.log("textDocument.didClose",params);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        LoggerUtil.log("textDocument.didSave",params);
    }

}
