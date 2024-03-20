/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.model.ExecutionState;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.core.Wakamiti;
import imconfig.Configuration;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The PlanNodeRunner class is responsible for executing a given
 * PlanNode and managing its lifecycle.
 * It provides methods to run a node, handle pre- and post-execution
 * actions, and create child runners for nested nodes.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class PlanNodeRunner {

    private final PlanNode node;
    private final String uniqueId;
    private final Configuration configuration;
    private final PlanNodeLogger logger;
    private final BackendFactory backendFactory;
    private List<PlanNodeRunner> children;
    private Optional<Backend> backend;
    private State state;

    public PlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger
    ) {
        this.node = node;
        this.configuration = configuration;
        this.uniqueId = UUID.randomUUID().toString();
        this.state = State.PREPARED;
        this.backendFactory = backendFactory;
        this.backend = backend;
        this.logger = logger;
    }

    public PlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger
    ) {
        this(node, configuration, backendFactory, Optional.empty(), logger);
    }

    /**
     * Gets the list of child runners for this PlanNodeRunner.
     *
     * @return The list of child runners.
     */
    public List<PlanNodeRunner> getChildren() {
        if (children == null) {
            children = createChildren();
        }
        return children;
    }

    /**
     * Gets the unique identifier of this PlanNodeRunner.
     *
     * @return The unique identifier.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    protected Optional<Backend> getBackend() {
        if (backend.isEmpty() && node.nodeType() == NodeType.TEST_CASE) {
            backend = Optional.of(backendFactory.createBackend(node, configuration));
        }
        return backend;
    }

    protected Configuration configuration() {
        return configuration;
    }

    protected BackendFactory backendFactory() {
        return backendFactory;
    }

    protected PlanNodeLogger getLogger() {
        return logger;
    }

    /**
     * Runs the associated PlanNode and returns the result.
     *
     * @return The result of the node execution.
     * @throws IllegalStateException If the run() method is invoked more than once.
     */
    protected Result runNode() {
        if (state != State.PREPARED) {
            throw new IllegalStateException("run() method can only be invoked once");
        }
        Result result = null;
        state = State.RUNNING;
        Wakamiti.instance().publishEvent(Event.NODE_RUN_STARTED, new PlanNodeSnapshot(node));

        if (node.nodeType() == NodeType.TEST_CASE) {
            result = runTestCaseNode();
        } else if (!getChildren().isEmpty()) {
            result = aggregatorFinish(runChildren());
        } else if (node.nodeType().isAnyOf(NodeType.STEP, NodeType.VIRTUAL_STEP)) {
            result = runStep();
        }
        state = State.FINISHED;
        Wakamiti.instance().publishEvent(Event.NODE_RUN_FINISHED, new PlanNodeSnapshot(node));
        return result;
    }

    private Result runTestCaseNode() {
        Result result = null;
        if (node.filtered()) {
            result = Result.SKIPPED;
            markFilteredTestCase(node);
        } else if (node.descendants().noneMatch(d -> d.nodeType().isAnyOf(NodeType.STEP))) {
            result = Result.NOT_IMPLEMENTED;
            doNotImplemented(node, result);
        } else if (!getChildren().isEmpty()) {
            Stream<Pair<Instant, Result>> results = Stream.empty();
            try {
                testCasePreExecution(node);
            } catch (WakamitiException e) {
                results = Stream.concat(results, Stream.of(new Pair<>(Instant.now(), Result.ERROR)))
                        .collect(Collectors.toList()).stream(); // prevent lazy stream
            }
            results = Stream.concat(results, runChildren())
                    .collect(Collectors.toList()).stream(); // prevent lazy stream
            try {
                testCasePostExecution(node);
            } catch (WakamitiException e) {
                results = Stream.concat(results, Stream.of(new Pair<>(Instant.now(), Result.ERROR)))
                        .collect(Collectors.toList()).stream(); // prevent lazy stream
            }
            result = aggregatorFinish(results);
        }

        return result;
    }

    private Result aggregatorFinish(Stream<Pair<Instant, Result>> results) {
        Pair<Instant, Result> aux = results
                .max((p1, p2) -> Comparator.<Result>naturalOrder().compare(p1.value(), p2.value()))
                .orElse(new Pair<>(Instant.now(), Result.FAILED));
        Result result = aux.value();
        node.prepareExecution().markFinished(aux.key(), result);
        return result;
    }

    protected Stream<Pair<Instant, Result>> runChildren() {
        return children.stream()
                .map(PlanNodeRunner::runNode)
                .filter(Objects::nonNull)
                .map(result -> new Pair<>(Instant.now(), result));
    }

    protected Result runStep() {
        stepPreExecution(node);
        getBackend().ifPresent(stepBackend -> stepBackend.runStep(node));
        stepPostExecution(node);
        return node.executionState().flatMap(ExecutionState::result).orElse(null);
    }

    private void doNotImplemented(PlanNode node, Result result) {
        Instant startInstant = Instant.now();

        node.children().forEach(c -> {
            boolean isBackground = c.properties().get("gherkinType").equals("background");
            doNotImplemented(c, isBackground && c.hasChildren() ? Result.SKIPPED : result);
        });

        node.prepareExecution().markStarted(startInstant);
        node.prepareExecution().markFinished(startInstant, result);
    }

    private void markFilteredTestCase(PlanNode node) {
        Instant startInstant = Instant.now();
        node.prepareExecution().markStarted(startInstant);
        node.prepareExecution().markFinished(startInstant, Result.SKIPPED);
    }

    protected List<PlanNodeRunner> createChildren() {
        return node.children()
                .map(
                        child -> new PlanNodeRunner(
                                child, configuration, backendFactory, getBackend(), logger
                        )
                )
                .collect(Collectors.toList());
    }

    public PlanNode getNode() {
        return node;
    }

    protected void testCasePreExecution(PlanNode node) {
        logger.logTestCaseHeader(node);
        getBackend().ifPresent(Backend::setUp);
    }

    protected void testCasePostExecution(PlanNode node) {
        getBackend().ifPresent(Backend::tearDown);
    }

    protected void stepPreExecution(PlanNode step) {
        /* nothing by default */
    }

    protected void stepPostExecution(PlanNode step) {
        logger.logStepResult(step);
    }

    protected enum State {
        PREPARED, RUNNING, FINISHED
    }

}