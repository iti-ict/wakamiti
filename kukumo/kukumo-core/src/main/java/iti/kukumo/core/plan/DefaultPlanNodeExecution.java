package iti.kukumo.core.plan;

import java.time.Instant;
import java.util.Optional;

import iti.kukumo.api.plan.PlanNodeExecution;
import iti.kukumo.api.plan.Result;

public class DefaultPlanNodeExecution implements PlanNodeExecution {

    private Optional<Instant> startInstant = Optional.empty();
    private Optional<Instant> finishInstant = Optional.empty();
    private Optional<Result> result = Optional.empty();
    private Optional<Throwable> error = Optional.empty();



    @Override
    public Optional<Instant> startInstant() {
        return startInstant;
    }


    @Override
    public Optional<Instant> finishInstant() {
        return finishInstant;
    }


    @Override
    public Optional<Result> result() {
        return result;
    }


    @Override
    public Optional<Throwable> error() {
        return error;
    }


    @Override
    public void markStarted(Instant instant) {
        if (startInstant.isPresent()) {
            throw new IllegalStateException("Node execution already started");
        }
        startInstant = Optional.of(instant);
    }


    @Override
    public void markPassed(Instant instant) {
        if (finishInstant.isPresent()) {
            throw new IllegalStateException("Node execution already finished");
        }
        finishInstant = Optional.of(instant);
        result = Optional.of(Result.PASSED);
    }


    @Override
    public void markFailure(Instant instant, Result result, Throwable error) {
        if (finishInstant.isPresent()) {
            throw new IllegalStateException("Node execution already finished");
        }
        finishInstant = Optional.of(instant);
        this.result = Optional.of(result);
        this.error = Optional.ofNullable(error);

    }


    @Override
    public boolean hasStarted() {
        return startInstant.isPresent();
    }

    @Override
    public boolean hasFinished() {
        return finishInstant.isPresent();
    }

}
