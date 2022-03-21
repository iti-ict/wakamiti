/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.model;


import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


/** A test <tt>PlanNode</tt> object is any of the parts that form a test * iti.kukumo.test.gherkin.plan */
public class ExecutionState<R> {

    private Optional<Instant> startInstant = Optional.empty();
    private Optional<Instant> finishInstant = Optional.empty();
    private Optional<R> result = Optional.empty();
    private Optional<Throwable> error = Optional.empty();


    /**
     * Get the start instant of this node, if executed.
     *
     * @return The nullable optional start instant
     */
    public Optional<Instant> startInstant() {
        return startInstant;
    }


    /**
     * Get the start instant of this node, if executed.
     *
     * @return The nullable optional finish instant
     */
    public Optional<Instant> finishInstant() {
        return finishInstant;
    }


    /**
     * @return The duration between {@link #startInstant()} and
     *         {@link #finishInstant()}, if both are present.
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
     * @return Whether the result of the execution is the same as the one given
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
     * Mark the current execution as started at the given instant
     *
     * @param instant The start instant
     * @throws IllegalStateException If the execution was already marked as
     *                               started
     */
    public void markStarted(Instant instant) {
        if (startInstant.isPresent()) {
            throw new IllegalStateException("Node execution already started");
        }
        startInstant = Optional.of(instant);
    }


    /**
     * Mark the current execution as finished with the given result
     *
     * @param instant The finish instant
     * @param result  The finish result
     * @throws IllegalStateException If the execution was already marked as
     *                               finished
     */
    public void markFinished(Instant instant, R result) {
        markFinished(instant, result, null);
    }


    /**
     * Mark the current execution as finished with the given result and error
     *
     * @param instant The finish instant
     * @param result  The finish result
     * @param error   The exception that caused the failure. Can be null.
     * @throws IllegalStateException If the execution was already marked as
     *                               finished
     */
    public void markFinished(Instant instant, R result, Throwable error) {
        if (finishInstant.isPresent()) {
            throw new IllegalStateException("Node execution already finished");
        }
        finishInstant = Optional.of(instant);
        this.result = Optional.of(result);
        this.error = Optional.ofNullable(error);

    }


    /**
     * @return Check whether the execution has been marked as started.
     */
    public boolean hasStarted() {
        return startInstant.isPresent();
    }


    /**
     * @return Check whether the execution has been marked as finished.
     */
    public boolean hasFinished() {
        return finishInstant.isPresent();
    }

}