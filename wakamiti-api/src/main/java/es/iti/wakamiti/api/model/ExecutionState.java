/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.model;


import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


/**
 * Mutable execution lifecycle state for one plan node.
 * <p>
 * The state transitions from "not started" to "started" to "finished". A node
 * can be marked as started and finished only once. This class is not
 * thread-safe.
 * </p>
 *
 * @param <R> result type used by the owning execution model
 */
public class ExecutionState<R> {

    private Optional<Instant> startInstant = Optional.empty();
    private Optional<Instant> finishInstant = Optional.empty();
    private Optional<R> result = Optional.empty();
    private Optional<Throwable> error = Optional.empty();
    private Optional<String> errorClassifier = Optional.empty();
    private Optional<String> response = Optional.empty();

    /**
     * Get the start instant of this node, if executed.
     *
     * @return The nullable optional start instant
     */
    public Optional<Instant> startInstant() {
        return startInstant;
    }

    /**
     * Get the finish instant of this node, if executed.
     *
     * @return The nullable optional finish instant
     */
    public Optional<Instant> finishInstant() {
        return finishInstant;
    }

    /**
     * Get the duration between {@link #startInstant()} and
     * {@link #finishInstant()}, if both are present.
     *
     * @return The nullable optional duration
     */
    public Optional<Duration> duration() {
        return startInstant.isPresent() && finishInstant.isPresent()
                ? Optional.of(Duration.between(startInstant.get(), finishInstant.get()))
                : Optional.empty();
    }

    /**
     * Get the result of this node, if executed.
     *
     * @return The nullable optional result
     */
    public Optional<R> result() {
        return result;
    }

    /**
     * Checks whether the result of the execution is the same
     * as the one given.
     *
     * @param result The result to check
     * @return True if the result of the execution is equal to
     * the given result, false otherwise
     */
    public boolean hasResult(
            R result
    ) {
        return this.result.isPresent() && this.result.get().equals(result);
    }

    /**
     * Get the error of this node, if executed and failed.
     *
     * @return The nullable optional error
     */
    public Optional<Throwable> error() {
        return error;
    }

    /**
     * Get the error classifier of this node, if executed and
     * failed, and the executing step has info defined.
     *
     * @return The error classifier
     */
    public Optional<String> errorClassifier() {
        return errorClassifier;
    }

    /**
     * Get the returned value of this node, if any.
     *
     * @return The nullable optional returned value
     */
    public Optional<String> response() {
        return response;
    }

    /**
     * Marks execution as started.
     *
     * @param instant start timestamp
     * @throws IllegalStateException when start has already been recorded
     */
    public void markStarted(
            Instant instant
    ) {
        if (startInstant.isPresent()) {
            throw new IllegalStateException("Node execution already started");
        }
        startInstant = Optional.of(instant);
    }

    /**
     * Marks execution as finished with a result and no error details.
     *
     * @param instant finish timestamp
     * @param result  final execution result
     * @throws IllegalStateException when finish has already been recorded
     */
    public void markFinished(
            Instant instant,
            R result
    ) {
        markFinished(instant, result, null, null, null);
    }

    /**
     * Marks execution as finished with optional error metadata.
     *
     * @param instant         finish timestamp
     * @param result          final execution result
     * @param error           error that caused the outcome, or {@code null}
     * @param errorClassifier optional error category, or {@code null}
     * @throws IllegalStateException when finish has already been recorded
     */
    public void markFinished(
            Instant instant,
            R result,
            Throwable error,
            String errorClassifier
    ) {
        markFinished(instant, result, error, errorClassifier, null);
    }

    /**
     * Marks execution as finished with full optional diagnostics.
     * <p>
     * This method sets finish instant, result and optional error/classifier/
     * response atomically from the caller perspective.
     * </p>
     *
     * @param instant         finish timestamp
     * @param result          final execution result
     * @param error           error that caused the outcome, or {@code null}
     * @param errorClassifier optional error category, or {@code null}
     * @param response        optional response payload, or {@code null}
     * @throws IllegalStateException when finish has already been recorded
     */
    public void markFinished(
            Instant instant,
            R result,
            Throwable error,
            String errorClassifier,
            String response
    ) {
        if (finishInstant.isPresent()) {
            throw new IllegalStateException("Node execution already finished");
        }
        finishInstant = Optional.of(instant);
        this.result = Optional.of(result);
        this.error = Optional.ofNullable(error);
        this.errorClassifier = Optional.ofNullable(errorClassifier);
        this.response = Optional.ofNullable(response);
    }

    /**
     * Checks whether the execution has been marked as started.
     *
     * @return {@code true} if the execution has started, {@code false}
     * otherwise
     */
    public boolean hasStarted() {
        return startInstant.isPresent();
    }

    /**
     * Checks whether the execution has been marked as finished.
     *
     * @return {@code true} if the execution has finished, {@code false}
     * otherwise
     */
    public boolean hasFinished() {
        return finishInstant.isPresent();
    }

}
