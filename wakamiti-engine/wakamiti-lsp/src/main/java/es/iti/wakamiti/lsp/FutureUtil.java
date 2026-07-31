/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.lsp;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.wakamiti.api.util.ThrowableFunction;


/**
 * Defines the contract implemented by Throwable Runnable.
 */
public final class FutureUtil {

    private static final long POLLING_INTERVAL_MILLIS = 1000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureUtil.class);

    private static Executor executor = Executors.newCachedThreadPool();

    public interface ThrowableRunnable {

        /**
         * Performs an asynchronous action that may fail.
         *
         * @throws Exception when the action cannot complete
         */
        void run() throws Exception;

    }

    private FutureUtil() {
    }

    /**
     * Schedules a cancellable asynchronous action through LSP4J.
     *
     * @param runnable action to execute
     * @return a future completed when the action finishes
     */
    public static CompletableFuture<Object> run(
            ThrowableRunnable runnable
    ) {
        return CompletableFutures.computeAsync(canceler -> {
            canceler.checkCanceled();
            return runnable;
        });
    }

    /**
     * Applies a potentially failing function asynchronously.
     *
     * @param function function to execute
     * @param input    function input
     * @param <T>      result type
     * @param <U>      input type
     * @return a cancellable future containing the result
     */
    public static <T, U> CompletableFuture<T> run(
            ThrowableFunction<U, T> function,
            U input
    ) {
        return CompletableFutures.computeAsync(canceler -> {
            canceler.checkCanceled();
            return function.apply(input);
        });
    }

    /**
     * Schedules a cancellable action after a fixed delay.
     *
     * @param runnable     action to execute
     * @param delaySeconds delay before execution in seconds
     * @return a future completed when the delayed action finishes
     */
    public static CompletableFuture<Object> runDelayed(
            ThrowableRunnable runnable,
            int delaySeconds
    ) {
        return CompletableFutures.computeAsync(
                CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS),
                canceler -> {
                    canceler.checkCanceled();
                    return runnable;
                }
        );
    }

    /**
     * Executes an action after an arbitrary future reaches a terminal state.
     * Completion is observed by a background polling task.
     *
     * @param future         future to monitor
     * @param actionWhenDone action invoked after completion or cancellation
     */
    public static void whenDone(
            Future<?> future,
            Runnable actionWhenDone
    ) {
        executor.execute(() -> {
            while (!future.isDone()) {
                try {
                    Thread.sleep(POLLING_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error(e.toString(), e);
                }
            }
            actionWhenDone.run();
        });
    }

    /**
     * Returns an already completed future containing {@code null}.
     *
     * @param <T> expected result type
     * @return a completed empty future
     */
    public static <T> CompletableFuture<T> empty() {
        return CompletableFuture.completedFuture(null);
    }

    static <T, U> CompletableFuture<U> processEvent(
            String event,
            T params,
            Function<T, U> method
    ) {
        return CompletableFuture
                .completedFuture(LoggerUtil.logEntry(event, params))
                .thenApply(loggingError(method))
                .thenApply(response -> LoggerUtil.logExit(event, response));
    }

    static <T, U> CompletableFuture<U> processEvent(
            String event,
            T params,
            Supplier<U> method
    ) {
        return CompletableFuture
                .completedFuture(LoggerUtil.logEntry(event, params))
                .thenApply(loggingError(x -> method.get()))
                .thenApply(response -> LoggerUtil.logExit(event, response));
    }

    private static <T, U> Function<T, U> loggingError(
            Function<T, U> method
    ) {
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
