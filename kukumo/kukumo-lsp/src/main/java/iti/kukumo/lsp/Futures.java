package iti.kukumo.lsp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.kukumo.util.ThrowableFunction;

public final class Futures {

    private static final Logger LOGGER = LoggerFactory.getLogger(Futures.class);

    private static Executor executor = Executors.newCachedThreadPool();

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


    public static <T,U> CompletableFuture<T> run(ThrowableFunction<U, T> function, U input) {
        return CompletableFutures.computeAsync(canceler -> {
            canceler.checkCanceled();
            return function.apply(input);
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


    public static void whenDone(Future<?> future, Runnable actionWhenDone) {
        executor.execute(()->{
            while (!future.isDone()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error(e.toString(),e);
                }
            }
            actionWhenDone.run();
        });
    }


    public static <T> CompletableFuture<T> empty() {
        return CompletableFuture.completedFuture(null);
    }



}
