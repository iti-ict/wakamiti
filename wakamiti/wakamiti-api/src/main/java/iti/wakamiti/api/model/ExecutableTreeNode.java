/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api.model;


import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public abstract class ExecutableTreeNode<S extends ExecutableTreeNode<S, R>, R extends Comparable<R>>
extends TreeNode<S> {

    private String executionID;
    private Optional<ExecutionState<R>> executionState = Optional.empty();


    public ExecutableTreeNode(List<S> children) {
        super(children);
    }


    /** Assign an execution ID */
    public void assignExecutionID(String executionID) {
        if (this.executionID != null) {
            throw new IllegalStateException("ExecutionID already assigned");
        }
        this.executionID = executionID;
        children().forEach(child -> child.assignExecutionID(executionID));
    }


    public String executionID() {
        return executionID;
    }


    /**
     * Prepare the node to be executed.
     *
     * @return The node execution state
     */
    public ExecutionState<R> prepareExecution() {
        if (!executionState.isPresent()) {
            executionState = Optional.of(createExecutionState());
        }
        return executionState.get();
    }


    protected ExecutionState<R> createExecutionState() {
        return new ExecutionState<>();
    }


    /**
     * @return The execution details of the node. It will be empty until
     *         {@link #prepareExecution()} is called.
     */
    public Optional<ExecutionState<R>> executionState() {
        return executionState;
    }


    /**
     * Get the start instant of this node, if executed. In the case of
     * child-populated nodes, return the minimum start instant of its children.
     *
     * @return The nullable optional start instant
     */
    public Optional<Instant> startInstant() {
        if (hasChildren()) {
            return children()
                .map(S::startInstant)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder());
        }
        return executionState().flatMap(ExecutionState::startInstant);
    }


    /**
     * Get the start instant of this node, if executed. In the case of
     * child-populated nodes, return the minimum start instant of its children.
     *
     * @return The nullable optional finish instant
     */
    public Optional<Instant> finishInstant() {
        if (hasChildren()) {
            return children()
                .map(S::finishInstant)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.naturalOrder());
        }
        return executionState().flatMap(ExecutionState::finishInstant);
    }


    /**
     * Get the duration of the execution of this node, if executed. In the case
     * of child-populated nodes, return the minimum start instant of its
     * children.
     *
     * @return The nullable optional finish instant
     */
    public Optional<Duration> duration() {
        if (hasChildren()) {
            return children()
                    .map(S::duration)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(Duration::plus);
        }
        return executionState().flatMap(ExecutionState::duration);
    }



    /**
     * Get the result of this node, if executed. In the case of child-populated
     * nodes, return the result of max priority of its children.
     *
     * @return The nullable optional result
     */
    public Optional<R> result() {
        Optional<R> result = executionState().flatMap(ExecutionState::result);
        if (result.isEmpty() && hasChildren()) {
            return children()
                .map(S::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.naturalOrder());
        }
        return result;
    }


    /**
     * Get a stream with the errors of this node, if executed and failed. In the
     * case of child-populated nodes, return all the errors of its children.
     *
     * @return A stream of errors
     */
    public Stream<Throwable> errors() {
        if (hasChildren()) {
            return children().flatMap(S::errors);
        }
        return executionState().flatMap(ExecutionState::error)
            .map(Stream::of)
            .orElse(Stream.empty());
    }


    /**
     * @return Check whether the execution of this node has been marked as
     *         started. In the case of child-populated nodes, return if every
     *         child has been started.
     */
    public boolean hasStarted() {
        if (hasChildren()) {
            return children().allMatch(S::hasStarted);
        }
        return executionState().map(ExecutionState::hasStarted).orElse(false);
    }


    /**
     * @return Check whether the execution of this node has been marked as
     *         finished. In the case of child-populated nodes, return if every
     *         child has been finished.
     */
    public boolean hasFinished() {
        if (hasChildren()) {
            return children().allMatch(S::hasFinished);
        }
        return executionState().map(ExecutionState::hasFinished).orElse(false);
    }
}