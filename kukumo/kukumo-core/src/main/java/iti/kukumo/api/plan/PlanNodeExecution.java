package iti.kukumo.api.plan;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * A test <tt>PlanNode</tt> object is any of the parts that form a test iti.kukumo.test.gherkin.plan
 */
public interface PlanNodeExecution {

    /**
     * Get the start instant of this node, if executed.
     * @return The nullable optional start instant
     */
    Optional<Instant> startInstant();

    /**
     * Get the start instant of this node, if executed.
     * @return The nullable optional finish instant
     */
    Optional<Instant> finishInstant();


    /**
     * @return The duration between {@link #startInstant()} and {@link #finishInstant()}, if both
     * are present.
     */
    default Optional<Duration> duration() {
        return startInstant().isPresent() && finishInstant().isPresent() ?
                Optional.of(Duration.between(startInstant().get(), finishInstant().get())) :
                Optional.empty();
    }


    /**
     * Get the result of this node, if executed.
     * @return The nullable optional result
     */
    Optional<Result> result();


    /**
     * Get the error of this node, if executed and failed.
     * @return The nullable optional error
     */
    Optional<Throwable> error();


    /**
     * Mark the current execution as started at the given instant
     * @param instant The start instant
     * @throws IllegalStateException If the execution was already marked as started
     */
    void markStarted(Instant instant);

    /**
     * Mark the current execution as finished with result {@link Result.PASSED}
     * @param instant The finish instant
     * @throws IllegalStateException If the execution was already marked as finished
     */
    void markPassed(Instant instant);

    /**
     * Mark the current execution as finished with the given result and error
     * @param instant The finish instant
     * @param result The finish result
     * @param error The exception that caused the failure. Can be null.
     * @throws IllegalStateException If the execution was already marked as finished
     */
    void markFailure(Instant instant, Result result, Throwable error);


    /**
     * @return Check whether the execution has been marked as started.
     */
    boolean hasStarted();

    /**
     * @return Check whether the execution has been marked as finished.
     */
    boolean hasFinished();

}
