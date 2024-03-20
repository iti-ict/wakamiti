/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.model;


import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


/**
 * A test {@code PlanNode} object is any of the parts that form a test plan.
 *
 * @param <R> The type of the result of the execution
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class ExecutionState<R> {

    private Optional<Instant> startInstant = Optional.empty();
    private Optional<Instant> finishInstant = Optional.empty();
    private Optional<R> result = Optional.empty();
    private Optional<Throwable> error = Optional.empty();
    private Optional<String> errorClassifier = Optional.empty();

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
    public boolean hasResult(R result) {
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
     * Mark the current execution as started at the given instant.
     *
     * @param instant The start instant
     * @throws IllegalStateException If the execution was already
     *                               marked as started
     */
    public void markStarted(Instant instant) {
        if (startInstant.isPresent()) {
            throw new IllegalStateException("Node execution already started");
        }
        startInstant = Optional.of(instant);
    }

    /**
     * Mark the current execution as finished with the given result.
     *
     * @param instant The finish instant
     * @param result  The finish result
     * @throws IllegalStateException If the execution was already
     *                               marked as finished
     */
    public void markFinished(Instant instant, R result) {
        markFinished(instant, result, null, null);
    }

    /**
     * Mark the current execution as finished with the given result
     * and error.
     *
     * @param instant         The finish instant
     * @param result          The finish result
     * @param error           The exception that caused the failure.
     *                        Can be null.
     * @param errorClassifier The error classifier associated with the
     *                        error
     * @throws IllegalStateException If the execution was already marked
     *                               as finished
     */
    public void markFinished(Instant instant, R result, Throwable error, String errorClassifier) {
        if (finishInstant.isPresent()) {
            throw new IllegalStateException("Node execution already finished");
        }
        finishInstant = Optional.of(instant);
        this.result = Optional.of(result);
        this.error = Optional.ofNullable(error);
        this.errorClassifier = Optional.ofNullable(errorClassifier);

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