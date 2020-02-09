package iti.kukumo.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KukumoTextDocumentService implements TextDocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KukumoTextDocumentService.class);


    public KukumoTextDocumentService(InitializeParams params) {
        // TODO Auto-generated constructor stub
    }


    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
        LOGGER.info("EVENT completion:\n{}",position);
        return CompletableFuture.completedFuture(Either.forLeft(List.of()));
    }


    @Override
    public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
        LOGGER.info("EVENT documentColor:\n{}",params);
        return CompletableFuture.completedFuture(List.of());
    }


    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LOGGER.info("EVENT didOpen:\n{}",params);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LOGGER.info("EVENT didChange:\n{}",params);
    }


    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        LOGGER.info("EVENT didClose:\n{}",params);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        LOGGER.info("EVENT didSave:\n{}",params);
    }

}
