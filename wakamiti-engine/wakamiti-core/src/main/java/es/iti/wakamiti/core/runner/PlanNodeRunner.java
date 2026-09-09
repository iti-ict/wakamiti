/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import static es.iti.wakamiti.core.gherkin.GherkinPlanBuilder.GHERKIN_PROPERTY;
import static es.iti.wakamiti.core.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_AFTER_FEATURE;
import static es.iti.wakamiti.core.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_BACKGROUND;
import static es.iti.wakamiti.core.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_BEFORE_FEATURE;
import static es.iti.wakamiti.core.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_FEATURE;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.MDC;

import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.Level;
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
    private Backend lifecycleBackend;
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
        if (backend.isEmpty() && node.nodeType().isAnyOf(NodeType.TEST_CASE, NodeType.LIFECYCLE_HOOK)) {
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

    private Backend lifecycleBackend() {
        if (lifecycleBackend == null) {
            lifecycleBackend = backendFactory.createLifecycleBackend(node, configuration);
        }
        return lifecycleBackend;
    }

    protected PlanNodeLogger getLogger() {
        return logger;
    }

    /**
     * Executes this node silently (without notifying any external listener).
     * Intended for lifecycle hook scenarios that should run but not appear as
     * JUnit or JUnit5 test results.
     *
     * @return node result, or {@code null} when no executable branch applies
     */
    public Result run() {
        return runNode();
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

        if (node.nodeType().isAnyOf(NodeType.TEST_CASE, NodeType.LIFECYCLE_HOOK)) {
            result = runTestCaseNode();
        } else if (!getChildren().isEmpty()) {
            result = isFeatureNode() ? runFeatureNode() : aggregatorFinish(runChildren());
        } else if (node.nodeType().isAnyOf(NodeType.STEP, NodeType.VIRTUAL_STEP)) {
            result = runStep();
        }
        state = State.FINISHED;
        Wakamiti.instance().publishEvent(Event.NODE_RUN_FINISHED, new PlanNodeSnapshot(node));
        return result;
    }

    private Result runTestCaseNode() {
        String previousScenarioId = initializeScenarioLoggingContext();
        try {
            return resolveTestCaseResult();
        } finally {
            restoreScenarioLoggingContext(previousScenarioId);
        }
    }

    private Result resolveTestCaseResult() {
        if (node.filtered()) {
            markFilteredTestCase(node);
            return Result.SKIPPED;
        }
        if (node.descendants().noneMatch(d -> d.nodeType().isAnyOf(NodeType.STEP))) {
            doNotImplemented(node, Result.NOT_IMPLEMENTED);
            return Result.NOT_IMPLEMENTED;
        }
        if (getChildren().isEmpty()) {
            return null;
        }
        return executeTestCase();
    }

    private Result executeTestCase() {
        List<Pair<Instant, Result>> results = new ArrayList<>();
        if (prepareTestCaseExecution(results)) {
            results.addAll(runChildren());
        } else {
            skipPendingChildren();
        }
        finishTestCaseExecution(results);
        return aggregatorFinish(results);
    }

    private boolean prepareTestCaseExecution(
            List<Pair<Instant, Result>> results
    ) {
        if (dryRun) {
            logger.logTestCaseHeader(node);
            return true;
        }
        try {
            testCasePreExecution(node);
            return true;
        } catch (WakamitiException e) {
            results.add(errorResult());
            return !stopExecutionOnError();
        }
    }

    private void finishTestCaseExecution(
            List<Pair<Instant, Result>> results
    ) {
        if (dryRun) {
            return;
        }
        try {
            testCasePostExecution(node);
        } catch (WakamitiException e) {
            results.add(errorResult());
        }
    }

    private Result runFeatureNode() {
        List<PlanNodeRunner> childRunners = getChildren();
        List<PlanNodeRunner> beforeRunners = childRunners.stream()
                .filter(this::isBeforeRunner)
                .toList();
        List<PlanNodeRunner> afterRunners = childRunners.stream()
                .filter(this::isAfterRunner)
                .toList();
        List<PlanNodeRunner> scenarioRunners = childRunners.stream()
                .filter(this::isFeatureRegularRunner)
                .toList();

        if (!hasExecutableFeatureScenario(scenarioRunners)) {
            beforeRunners.forEach(PlanNodeRunner::skipIfPending);
            afterRunners.forEach(PlanNodeRunner::skipIfPending);
            if (scenarioRunners.isEmpty()) {
                Instant instant = Instant.now();
                node.prepareExecution().markStarted(instant);
                node.prepareExecution().markFinished(instant, Result.SKIPPED);
                return Result.SKIPPED;
            }
            return aggregatorFinish(runSelectedChildren(scenarioRunners));
        }

        List<Pair<Instant, Result>> results = new ArrayList<>();
        boolean continueExecution = true;
        boolean hasImplementedSteps = node.descendants().anyMatch(d -> d.nodeType().isAnyOf(NodeType.STEP));
        try {
            if (hasImplementedSteps) {
                featurePreExecution(node);
            }
        } catch (WakamitiException e) {
            results.add(errorResult());
            continueExecution = !stopExecutionOnError();
        }
        if (continueExecution) {
            results.addAll(runChildren());
        } else {
            skipPendingChildren();
        }
        try {
            if (hasImplementedSteps) {
                featurePostExecution(node);
            }
        } catch (WakamitiException e) {
            results.add(errorResult());
        }

        return aggregatorFinish(results);
    }

    private Result aggregatorFinish(
            List<Pair<Instant, Result>> results
    ) {
        Pair<Instant, Result> aux = results.stream()
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
     * @return child execution timestamps and results, excluding
     *         children with a {@code null} result
     */
    protected List<Pair<Instant, Result>> runChildren() {
        return runSelectedChildren(getChildren());
    }

    /**
     * Executes the supplied child runners in encounter order.
     *
     * @param childRunners runners to execute
     * @return timestamped child results, excluding {@code null} results
     */
    protected List<Pair<Instant, Result>> runSelectedChildren(
            List<PlanNodeRunner> childRunners
    ) {
        List<Pair<Instant, Result>> results = new ArrayList<>();
        for (int i = 0; i < childRunners.size(); i++) {
            Result result = runChild(childRunners.get(i));
            if (result != null) {
                results.add(new Pair<>(Instant.now(), result));
            }
            if (stopExecutionOnError() && result == Result.ERROR) {
                skipPendingChildren(childRunners, i + 1);
                break;
            }
        }
        return results;
    }

    /**
     * Executes one child runner.
     *
     * <p>JUnit integrations override this method to publish their native test
     * events while the base class keeps result aggregation and stop-on-error
     * behavior centralized.</p>
     *
     * @param child runner to execute
     * @return child result, or {@code null} when the child has no result
     */
    protected Result runChild(
            PlanNodeRunner child
    ) {
        return child.runNode();
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
            boolean isBackground = GHERKIN_TYPE_BACKGROUND.equals(c.properties().get(GHERKIN_PROPERTY));
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

    void skipIfPending() {
        if (state != State.PREPARED) {
            return;
        }
        state = State.FINISHED;
        markSkippedSelf(node);
        if (children == null) {
            node.children().forEach(PlanNodeRunner::markSkippedRecursively);
        } else {
            children.forEach(PlanNodeRunner::skipIfPending);
        }
    }

    /**
     * Hook executed before a feature node runs its descendants.
     * <p>
     * Default behavior logs the feature header and invokes backend
     * {@link Backend#setUp(Level)}.
     * </p>
     *
     * @param node feature node about to execute
     */
    protected void featurePreExecution(
            PlanNode node
    ) {
        logger.logFeatureHeader(node);
        lifecycleBackend().setUp(Level.FEATURE);
    }

    /**
     * Hook executed after a feature node finishes descendant execution.
     * <p>
     * Default behavior invokes backend {@link Backend#tearDown(Level)}.
     * </p>
     *
     * @param node executed feature node
     */
    protected void featurePostExecution(
            PlanNode node
    ) {
        lifecycleBackend().tearDown(Level.FEATURE);
    }

    /**
     * Hook executed before a test-case node runs its descendants.
     * <p>
     * Default behavior logs the test-case header and invokes backend
     * {@link Backend#setUp(Level)}.
     * </p>
     *
     * @param node test-case node about to execute
     */
    protected void testCasePreExecution(
            PlanNode node
    ) {
        if (node.nodeType() == NodeType.TEST_CASE) {
            logger.logTestCaseHeader(node);
        } else {
            logger.logHookHeader(node);
        }
        getBackend().ifPresent(backend -> backend.setUp(Level.SCENARIO));
    }

    /**
     * Hook executed after a test-case node finishes descendant execution.
     * <p>
     * Default behavior invokes backend {@link Backend#tearDown(Level)}.
     * </p>
     *
     * @param node executed test-case node
     */
    protected void testCasePostExecution(
            PlanNode node
    ) {
        getBackend().ifPresent(backend -> backend.tearDown(Level.SCENARIO));
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

    private boolean stopExecutionOnError() {
        return configuration.get(WakamitiConfiguration.STOP_EXECUTION_ON_ERROR, Boolean.class).get();
    }

    private static Pair<Instant, Result> errorResult() {
        return new Pair<>(Instant.now(), Result.ERROR);
    }

    private boolean isPerScenarioLogEnabled() {
        return configuration.get(WakamitiConfiguration.LOGS_PER_SCENARIO, Boolean.class).get();
    }

    private String initializeScenarioLoggingContext() {
        String previousScenarioId = MDC.get(ScenarioLogContext.KEY);
        if (isPerScenarioLogEnabled()) {
            MDC.put(ScenarioLogContext.KEY, scenarioLogId());
        }
        return previousScenarioId;
    }

    private String scenarioLogId() {
        String scenarioId = node.id();
        return scenarioId == null || scenarioId.isBlank() ? uniqueId : scenarioId;
    }

    private void restoreScenarioLoggingContext(
            String previousScenarioId
    ) {
        if (previousScenarioId == null || previousScenarioId.isBlank()) {
            MDC.remove(ScenarioLogContext.KEY);
        } else {
            MDC.put(ScenarioLogContext.KEY, previousScenarioId);
        }
    }

    private void skipPendingChildren() {
        skipPendingChildren(getChildren(), 0);
    }

    private void skipPendingChildren(
            List<PlanNodeRunner> runners,
            int startIndex
    ) {
        for (int i = startIndex; i < runners.size(); i++) {
            runners.get(i).skipIfPending();
        }
    }

    private static void markSkippedSelf(
            PlanNode node
    ) {
        Instant instant = Instant.now();
        node.prepareExecution().markStarted(instant);
        node.prepareExecution().markFinished(instant, Result.SKIPPED);
    }

    private static void markSkippedRecursively(
            PlanNode node
    ) {
        markSkippedSelf(node);
        node.children().forEach(PlanNodeRunner::markSkippedRecursively);
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

    private boolean isFeatureNode() {
        return node.nodeType() == NodeType.AGGREGATOR
                && GHERKIN_TYPE_FEATURE.equals(node.properties().get(GHERKIN_PROPERTY));
    }

    private boolean isFeatureRegularRunner(
            PlanNodeRunner runner
    ) {
        return runner.getNode().nodeType() != NodeType.LIFECYCLE_HOOK;
    }

    private boolean isBeforeRunner(
            PlanNodeRunner runner
    ) {
        return isLifecycleRunnerType(runner, GHERKIN_TYPE_BEFORE_FEATURE);
    }

    private boolean isAfterRunner(
            PlanNodeRunner runner
    ) {
        return isLifecycleRunnerType(runner, GHERKIN_TYPE_AFTER_FEATURE);
    }

    private boolean isLifecycleRunnerType(
            PlanNodeRunner runner,
            String gherkinType
    ) {
        return gherkinType.equals(runner.getNode().properties().get(GHERKIN_PROPERTY));
    }

    private boolean hasExecutableFeatureScenario(
            List<PlanNodeRunner> scenarioRunners
    ) {
        if (scenarioRunners.isEmpty() || dryRun) {
            return false;
        }
        return scenarioRunners.stream().anyMatch(runner -> runner.getNode().descendants()
                .filter(descendant -> descendant.nodeType() == NodeType.TEST_CASE)
                .anyMatch(descendant -> !descendant.filtered()))
                || scenarioRunners.stream().anyMatch(runner -> runner.getNode().nodeType() == NodeType.TEST_CASE
                        && !runner.getNode().filtered());
    }

    static final class ScenarioLogContext {

        private static final String KEY = "wakamiti.scenarioId";

        private ScenarioLogContext() {
        }

    }

}
