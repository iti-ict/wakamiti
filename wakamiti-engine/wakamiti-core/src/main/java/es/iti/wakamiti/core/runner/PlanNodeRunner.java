/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.model.ExecutionState;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.core.Wakamiti;


/**
 * Executes one {@link PlanNode} and coordinates its descendants.
 * <p>
 * A runner can execute only once. During execution it publishes start/finish
 * node events, lazily resolves a backend for test-case nodes, propagates
 * execution to children and aggregates child results. In dry-run mode, steps
 * are validated through backend dry-run hooks instead of being executed.
 * </p>
 */
public class PlanNodeRunner {

    private final PlanNode node;
    private final String uniqueId;
    private final String nodePath;
    private final Configuration configuration;
    private final PlanNodeLogger logger;
    private final BackendFactory backendFactory;
    private final boolean dryRun;
    private List<PlanNodeRunner> children;
    private Optional<Backend> backend;
    private State state;

    /**
     * Creates a runner with an already selected backend.
     *
     * @param node           node to execute
     * @param configuration  effective node configuration
     * @param backendFactory factory used for descendant test cases
     * @param backend        backend inherited by compatible descendants
     * @param logger         execution logger
     */
    public PlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger
    ) {
        this(node, configuration, backendFactory, backend, logger, false, "0");
    }

    protected PlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger,
            boolean dryRun,
            String nodePath
    ) {
        this.node = node;
        this.configuration = configuration;
        this.nodePath = nodePath;
        this.uniqueId = stableUniqueId(nodePath, node);
        this.state = State.PREPARED;
        this.backendFactory = backendFactory;
        this.backend = backend;
        this.logger = logger;
        this.dryRun = dryRun;
    }

    /**
     * Creates a normal-execution runner that resolves backends as required.
     *
     * @param node           node to execute
     * @param configuration  effective node configuration
     * @param backendFactory factory used to create test-case backends
     * @param logger         execution logger
     */
    public PlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger
    ) {
        this(node, configuration, backendFactory, Optional.empty(), logger, false, "0");
    }

    /**
     * Creates a runner with explicit dry-run behavior.
     * <p>
     * Dry runs resolve and validate steps without invoking contributor
     * implementations.
     * </p>
     *
     * @param node           node to execute or validate
     * @param configuration  effective node configuration
     * @param backendFactory factory used to create test-case backends
     * @param logger         execution logger
     * @param dryRun         {@code true} to validate without executing steps
     */
    public PlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger,
            boolean dryRun
    ) {
        this(node, configuration, backendFactory, Optional.empty(), logger, dryRun, "0");
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

    protected String getNodePath() {
        return nodePath;
    }

    protected String childNodePath(
            int childIndex
    ) {
        return String.format("%s/%d", nodePath, childIndex);
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
     * Executes this node according to its type and lifecycle state.
     *
     * @return node result, or {@code null} when no executable branch applies
     * @throws IllegalStateException when invoked more than once
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
            if (dryRun) {
                logger.logTestCaseHeader(node);
            } else {
                try {
                    testCasePreExecution(node);
                } catch (WakamitiException e) {
                    results = Stream.concat(results, Stream.of(new Pair<>(Instant.now(), Result.ERROR)))
                            .toList().stream(); // prevent lazy stream
                }
            }
            results = Stream.concat(results, runChildren())
                    .toList().stream(); // prevent lazy stream
            if (!dryRun) {
                try {
                    testCasePostExecution(node);
                } catch (WakamitiException e) {
                    results = Stream.concat(results, Stream.of(new Pair<>(Instant.now(), Result.ERROR)))
                            .toList().stream(); // prevent lazy stream
                }
            }
            result = aggregatorFinish(results);
        }

        return result;
    }

    private Result aggregatorFinish(
            Stream<Pair<Instant, Result>> results
    ) {
        Pair<Instant, Result> aux = results
                .max((p1, p2) -> Comparator.<Result>naturalOrder().compare(p1.value(), p2.value()))
                .orElse(new Pair<>(Instant.now(), Result.FAILED));
        Result result = aux.value();
        node.prepareExecution().markFinished(aux.key(), result);
        return result;
    }

    /**
     * Executes child runners in encounter order and timestamps each resulting
     * outcome.
     *
     * @return stream of child execution timestamps and results, excluding
     *         children with a {@code null} result
     */
    protected Stream<Pair<Instant, Result>> runChildren() {
        return getChildren().stream()
                .map(PlanNodeRunner::runNode)
                .filter(Objects::nonNull)
                .map(result -> new Pair<>(Instant.now(), result));
    }

    /**
     * Executes a step node using the resolved backend.
     * <p>
     * Runtime failures are converted into {@link Result#ERROR} on the node
     * execution state. Post-step hooks are always invoked.
     * </p>
     *
     * @return recorded node result, or {@code null} when backend execution did
     *         not produce state
     */
    protected Result runStep() {
        stepPreExecution(node);
        try {
            getBackend().ifPresent(stepBackend -> {
                if (dryRun) {
                    stepBackend.dryRunStep(node);
                } else {
                    stepBackend.runStep(node);
                }
            });
        } catch (Throwable error) {
            Instant now = Instant.now();
            node.prepareExecution().markStarted(now);
            node.prepareExecution().markFinished(now, Result.ERROR, error, null);
        } finally {
            stepPostExecution(node);
        }
        return node.executionState().flatMap(ExecutionState::result).orElse(null);
    }

    private void doNotImplemented(
            PlanNode node,
            Result result
    ) {
        Instant startInstant = Instant.now();

        node.children().forEach(c -> {
            boolean isBackground = c.properties().get("gherkinType").equals("background");
            doNotImplemented(c, isBackground && c.hasChildren() ? Result.SKIPPED : result);
        });

        node.prepareExecution().markStarted(startInstant);
        node.prepareExecution().markFinished(startInstant, result);
    }

    private void markFilteredTestCase(
            PlanNode node
    ) {
        Instant startInstant = Instant.now();
        node.prepareExecution().markStarted(startInstant);
        node.prepareExecution().markFinished(startInstant, Result.SKIPPED);
    }

    protected List<PlanNodeRunner> createChildren() {
        List<PlanNode> childNodes = node.children().toList();
        return IntStream.range(0, childNodes.size())
                .mapToObj(index -> new PlanNodeRunner(
                        childNodes.get(index),
                        configuration,
                        backendFactory,
                        getBackend(),
                        logger,
                        dryRun,
                        childNodePath(index)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Returns the executable node whose state is managed by this runner.
     *
     * @return the underlying plan node
     */
    public PlanNode getNode() {
        return node;
    }

    /**
     * Hook executed before a test-case node runs its descendants.
     * <p>
     * Default behavior logs the test-case header and invokes backend
     * {@link Backend#setUp()}.
     * </p>
     *
     * @param node test-case node about to execute
     */
    protected void testCasePreExecution(
            PlanNode node
    ) {
        logger.logTestCaseHeader(node);
        getBackend().ifPresent(Backend::setUp);
    }

    /**
     * Hook executed after a test-case node finishes descendant execution.
     * <p>
     * Default behavior invokes backend {@link Backend#tearDown()}.
     * </p>
     *
     * @param node executed test-case node
     */
    protected void testCasePostExecution(
            PlanNode node
    ) {
        getBackend().ifPresent(Backend::tearDown);
    }

    /**
     * Hook executed immediately before backend step invocation.
     *
     * @param step step node about to execute
     */
    protected void stepPreExecution(
            PlanNode step
    ) {
        /* nothing by default */
    }

    /**
     * Hook executed after backend step invocation, even when the step failed.
     *
     * @param step executed step node
     */
    protected void stepPostExecution(
            PlanNode step
    ) {
        logger.logStepResult(step);
    }

    /**
     * Internal runner lifecycle.
     */
    protected enum State {

        PREPARED,
        RUNNING,
        FINISHED

    }

    private static String stableUniqueId(
            String nodePath,
            PlanNode node
    ) {
        String stableKey = String.join("|",
                nodePath,
                Objects.toString(node.nodeType(), ""),
                Objects.toString(node.id(), ""),
                Objects.toString(node.source(), ""),
                Objects.toString(node.displayName(), ""),
                Objects.toString(node.name(), "")
        );
        return UUID.nameUUIDFromBytes(stableKey.getBytes(StandardCharsets.UTF_8)).toString();
    }

}
