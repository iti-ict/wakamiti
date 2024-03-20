/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.model;


import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * This class represents an executable tree node and
 * provides functionality related to execution, such as
 * assigning an execution ID, preparing for execution,
 * and retrieving execution details.
 *
 * @param <S> The type of the executable tree node itself
 * @param <R> The type of the result that can be obtained
 *            after execution
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @see TreeNode
 */
public abstract class ExecutableTreeNode<S extends ExecutableTreeNode<S, R>, R extends Comparable<R>>
        extends TreeNode<S> {

    private String executionID;
    private Optional<ExecutionState<R>> executionState = Optional.empty();


    public ExecutableTreeNode(List<S> children) {
        super(children);
    }


    /**
     * Assigns an execution ID to this node.
     *
     * @param executionID The execution ID to be assigned
     * @throws IllegalStateException If the execution ID
     *                               has already been assigned
     */
    public void assignExecutionID(String executionID) {
        if (this.executionID != null) {
            throw new IllegalStateException("ExecutionID already assigned");
        }
        this.executionID = executionID;
        children().forEach(child -> child.assignExecutionID(executionID));
    }

    /**
     * Gets the assigned execution ID for this node.
     *
     * @return The execution ID
     */
    public String executionID() {
        return executionID;
    }

    /**
     * Prepares the node to be executed.
     *
     * @return The node execution state
     */
    public ExecutionState<R> prepareExecution() {
        if (executionState.isEmpty()) {
            executionState = Optional.of(createExecutionState());
        }
        return executionState.get();
    }

    /**
     * Creates the execution state for this node.
     * Subclasses can override this method to provide a
     * custom execution state.
     *
     * @return The execution state
     */
    protected ExecutionState<R> createExecutionState() {
        return new ExecutionState<>();
    }


    /**
     * Gets the execution state of this node.
     *
     * @return The execution state, which will be empty
     * until {@link #prepareExecution()} is called
     */
    public Optional<ExecutionState<R>> executionState() {
        return executionState;
    }


    /**
     * Gets the start instant of this node, if executed.
     * In the case of child-populated nodes returns the
     * minimum start instant of its children.
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
     * Gets the finish instant of this node, if executed.
     * In the case of child-populated nodes returns the
     * maximum finish instant of its children.
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
     * Gets the duration of the execution of this node, if executed.
     * In the case of child-populated nodes returns the sum of
     * durations of its children.
     *
     * @return The nullable optional duration
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
     * Gets the result of this node, if executed. In the case
     * of child-populated nodes, returns the result of maximum
     * priority among its children.
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
     * Gets a stream with the errors of this node, if executed
     * and failed. In the case of child-populated nodes returns
     * all the errors of its children.
     *
     * @return A stream of errors
     */
    public Stream<Throwable> errors() {
        if (hasChildren()) {
            return children().flatMap(S::errors);
        }
        return executionState().flatMap(ExecutionState::error).stream();
    }


    /**
     * Gets a stream with the error classifiers of this node, if
     * executed and failed. In the case of child-populated nodes
     * returns all the error classifiers of its children.
     *
     * @return A stream of error classifiers
     */
    public Stream<String> errorClassifiers() {
        if (hasChildren()) {
            return children().flatMap(S::errorClassifiers);
        }
        return executionState().flatMap(ExecutionState::errorClassifier).stream();
    }


    /**
     * Checks whether the execution of this node has been marked as
     * started. In the case of child-populated nodes, returns true
     * if every child has been started.
     *
     * @return {@code true} if the execution has started, {@code false}
     * otherwise
     */
    public boolean hasStarted() {
        if (hasChildren()) {
            return children().allMatch(S::hasStarted);
        }
        return executionState().map(ExecutionState::hasStarted).orElse(false);
    }


    /**
     * Checks whether the execution of this node has been marked as
     * finished. In the case of child-populated nodes, returns true
     * if every child has been finished.
     *
     * @return {@code true} if the execution has finished, {@code false}
     * otherwise
     */
    public boolean hasFinished() {
        if (hasChildren()) {
            return children().allMatch(S::hasFinished);
        }
        return executionState().map(ExecutionState::hasFinished).orElse(false);
    }
}