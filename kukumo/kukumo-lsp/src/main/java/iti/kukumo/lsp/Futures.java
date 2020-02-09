package iti.kukumo.lsp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.jsonrpc.CompletableFutures;

public final class Futures {

    public interface ThrowableRunnable {
        void run() throws Exception;
    }

    private Futures() { }


    public static CompletableFuture<Object> run(ThrowableRunnable runnable) {
        return CompletableFutures.computeAsync(canceler -> {
            canceler.checkCanceled();
            return runnable;
        });
    }



    public static CompletableFuture<Object> runDelayed(ThrowableRunnable runnable, int delaySeconds) {
        return CompletableFutures.computeAsync(
            CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS),
            canceler -> {
                canceler.checkCanceled();
                return runnable;
            }
        );
    }



}
