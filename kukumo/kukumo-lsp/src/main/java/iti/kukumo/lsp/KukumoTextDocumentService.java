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
    private final GherkinWorkspace workspace;


    KukumoTextDocumentService(KukumoLanguageServer server, GherkinWorkspace workspace, int baseIndex) {
        this.server = server;
        this.baseIndex = baseIndex;
        this.workspace = workspace;
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
            server.sendDiagnostics(workspace.addGherkin(uri, content) );
        } else if (FILE_TYPE_CONFIGURATION.equals(type)) {
        	server.sendDiagnostics(workspace.addConfiguration(uri, content));
        }
    }


    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LoggerUtil.logEntry("textDocument.didChange", params);
        var uri = params.getTextDocument().getUri();
        for (var event : params.getContentChanges()) {
            server.sendDiagnostics(
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


    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(
		ImplementationParams params
	) {
    	return FutureUtil.processEvent("textDocument.implementation", params, this::resolveImplementationLink);
    }


    private Either<List<? extends Location>, List<? extends LocationLink>> resolveImplementationLink(
    	ImplementationParams params
	) {
    	var uri = params.getTextDocument().getUri();
    	var position = params.getPosition();
    	List<Location> links = workspace.resolveImplementationLink(uri, position).stream()
			.map(link -> new Location(link.uri(), link.range()))
			.collect(toList());
    	return Either.<List<? extends Location>, List<? extends LocationLink>>forLeft(links);
    }



    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
		DefinitionParams params
	) {
      	return FutureUtil.processEvent("textDocument.definition", params, this::resolveDefinitionLink);
    }


    private Either<List<? extends Location>, List<? extends LocationLink>> resolveDefinitionLink(
    	DefinitionParams params
	) {
    	var uri = params.getTextDocument().getUri();
    	var position = params.getPosition();
    	List<Location> links = workspace.resolveDefinitionLink(uri, position).stream()
			.map(link -> new Location(link.uri(), link.range()))
			.collect(toList());
    	return Either.<List<? extends Location>, List<? extends LocationLink>>forLeft(links);
    }



    private TextRange textRange(Range range) {
        int startLine = range.getStart().getLine() - baseIndex;
        int endLine = range.getEnd().getLine() - baseIndex;
        int startPosition = range.getStart().getCharacter() - baseIndex;
        int endPosition = range.getEnd().getCharacter() - baseIndex;
        return TextRange.of(startLine,startPosition,endLine,endPosition);
    }

}
