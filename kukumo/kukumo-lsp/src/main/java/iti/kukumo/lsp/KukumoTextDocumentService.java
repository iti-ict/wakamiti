package iti.kukumo.lsp;

import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import iti.kukumo.lsp.internal.*;

public class KukumoTextDocumentService implements TextDocumentService {

	private static final String FILE_TYPE_GHERKIN = "kukumo-gherkin";
	private static final String FILE_TYPE_CONFIGURATION = "yaml";

    private final KukumoLanguageServer server;
    private final int baseIndex;

    private GherkinWorkspace workspace;


    public KukumoTextDocumentService(KukumoLanguageServer server, int baseIndex) {
        this.server = server;
        this.baseIndex = baseIndex;
        this.workspace = new GherkinWorkspace(baseIndex);
    }


    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem input) {
        LoggerUtil.logEntry("textDocument.resolveCompletionItem", input);
        return FutureUtil.empty();
    }


    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
		CompletionParams params
	) {
        return FutureUtil.processEvent(
    		"textDocument.completion",
    		params,
    		()-> Either.forLeft(
    			workspace.computeCompletions(params.getTextDocument().getUri(), params.getPosition())
    		)
		);
    }


    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
        CodeActionParams params
    ) {
        return FutureUtil.processEvent("textDocument.codeAction", params, this::doCodeAction);
    }

    private List<Either<Command, CodeAction>> doCodeAction(CodeActionParams params) {

    	var uri = params.getTextDocument().getUri();

        VersionedTextDocumentIdentifier textDocument = new VersionedTextDocumentIdentifier(
            params.getTextDocument().getUri(),
            null
        );

        var codeActions = workspace.computeCodeActions(uri, params.getContext().getDiagnostics())
        	.stream()
            .map(Either::<Command,CodeAction>forRight)
            .collect(toList());

        codeActions.stream()
            .map(Either<Command, CodeAction>::getRight)
            .map(CodeAction::getEdit)
            .map(WorkspaceEdit::getDocumentChanges)
            .flatMap(List<Either<TextDocumentEdit, ResourceOperation>>::stream)
            .map(Either<TextDocumentEdit, ResourceOperation>::getLeft)
            .forEach(textDocumentEdit->textDocumentEdit.setTextDocument(textDocument));

        return codeActions;
    }


    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didOpen", params);
        String uri = params.getTextDocument().getUri();
        String type = params.getTextDocument().getLanguageId();
        String content = params.getTextDocument().getText();
        if (FILE_TYPE_GHERKIN.equals(type)) {
            sendDiagnostics(workspace.addGherkin(uri, content) );
        } else if (FILE_TYPE_CONFIGURATION.equals(type)) {
        	sendDiagnostics(workspace.addConfiguration(uri, content));
        }
    }


    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didChange", params);
        var uri = params.getTextDocument().getUri();
        for (var event : params.getContentChanges()) {
            sendDiagnostics(
        		workspace.update(uri, textRange(event.getRange()), event.getText())
    		);
        }
    }





    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didClose", params);
    }


    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didSave", params);
    }



    private void sendDiagnostics(Stream<DocumentDiagnostics> allDiagnostics) {
    	allDiagnostics.forEach(document->{
    		var uri = document.uri();
			var publishDiagnostics = new PublishDiagnosticsParams(uri, document.diagnostics());
			LoggerUtil.logEntry("textDocument.publishDiagnostics", publishDiagnostics);
			server.client.publishDiagnostics(publishDiagnostics);
    	});
}


    private TextRange textRange(Range range) {
        int startLine = range.getStart().getLine() - baseIndex;
        int endLine = range.getEnd().getLine() - baseIndex;
        int startPosition = range.getStart().getCharacter() - baseIndex;
        int endPosition = range.getEnd().getCharacter() - baseIndex;
        return TextRange.of(startLine,startPosition,endLine,endPosition);
    }

}
