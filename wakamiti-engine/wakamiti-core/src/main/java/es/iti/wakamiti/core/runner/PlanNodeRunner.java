/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.Pair;
import imconfig.Configuration;
import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.model.ExecutionState;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
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

    public List<PlanNodeRunner> getChildren() {
        if (children == null) {
            children = createChildren();
        }
        return children;
    }

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

    protected Result runNode() {
        if (state != State.PREPARED) {
            throw new IllegalStateException("run() method can only be invoked once");
        }
        Result result = null;
        state = State.RUNNING;
        Wakamiti.instance().publishEvent(Event.NODE_RUN_STARTED, new PlanNodeSnapshot(node));

        if (node.nodeType() == NodeType.TEST_CASE && node.filtered()) {
            markFilteredTestCase(node);
        } else if (node.nodeType() == NodeType.TEST_CASE
                && node.descendants().noneMatch(d -> d.nodeType().isAnyOf(NodeType.STEP))) {
            doNotImplemented(node, Result.NOT_IMPLEMENTED);
        } else if (!getChildren().isEmpty()) {
            Stream<Pair<Instant, Result>> results = Stream.empty();
            if (node.nodeType() == NodeType.TEST_CASE) {
                try {
                    testCasePreExecution(node);
                } catch (WakamitiException e) {
                    results = Stream.concat(results, Stream.of(new Pair<>(Instant.now(), Result.ERROR)));
                }
            }
            results = Stream.concat(results, runChildren());
            if (node.nodeType() == NodeType.TEST_CASE) {
                try {
                    testCasePostExecution(node);
                } catch (WakamitiException e) {
                    results = Stream.concat(results, Stream.of(new Pair<>(Instant.now(), Result.ERROR)));
                }
            }

            results.max((p1, p2) -> Comparator.<Result>naturalOrder().compare(p1.value(), p2.value()))
                    .ifPresent(p -> node.prepareExecution().markFinished(p.key(), p.value()));
        } else if (node.nodeType().isAnyOf(NodeType.STEP, NodeType.VIRTUAL_STEP)) {
            result = runStep();
        }
        state = State.FINISHED;
        Wakamiti.instance().publishEvent(Event.NODE_RUN_FINISHED, new PlanNodeSnapshot(node));
        return result;
    }


    protected Stream<Pair<Instant, Result>> runChildren() {
        return children.stream()
                .map(PlanNodeRunner::runNode)
                .filter(Objects::nonNull)
                .map(result -> new Pair<>(Instant.now(), result))
                /*.max(Comparator.naturalOrder())
                .ifPresent(r -> node.prepareExecution().markFinished(Instant.now(), r))*/;
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