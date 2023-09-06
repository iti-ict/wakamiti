/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.lsp;

import java.util.concurrent.*;
import java.util.function.*;

import es.iti.wakamiti.api.util.ThrowableFunction;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.slf4j.*;





public final class FutureUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FutureUtil.class);

    private static Executor executor = Executors.newCachedThreadPool();

    public interface ThrowableRunnable {
        void run() throws Exception;
    }

    private FutureUtil() { }


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



    static <T,U>  CompletableFuture<U> processEvent(String event, T params, Function<T, U> method) {
        return CompletableFuture
                .completedFuture(LoggerUtil.logEntry(event, params))
                .thenApply(loggingError(method))
                .thenApply(response -> LoggerUtil.logExit(event, response));

    }



	static <T,U>  CompletableFuture<U> processEvent(String event, T params, Supplier<U> method) {
        return CompletableFuture
                .completedFuture(LoggerUtil.logEntry(event, params))
                .thenApply(loggingError(x -> method.get()))
                .thenApply(response -> LoggerUtil.logExit(event, response));

    }



    private static <T,U> Function<T,U> loggingError(Function<T, U> method) {
		return input -> {
			try {
				return method.apply(input);
			} catch (Exception e) {
				LOGGER.error("UNEXPECTED ERROR", e);
				throw e;
			}
		};
	}

}