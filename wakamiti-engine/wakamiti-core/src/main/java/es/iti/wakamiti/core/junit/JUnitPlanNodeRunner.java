/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.junit;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiSkippedException;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import es.iti.wakamiti.api.imconfig.Configuration;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Provides a JUnit-compatible runner for Gherkin feature files.
 * It handles the execution of Gherkin feature nodes, including
 * the creation of JUnit descriptions, running child nodes, and
 * notifying JUnit about test events.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @deprecated
 */
@Deprecated(since = "2.4.0", forRemoval = true)
public class JUnitPlanNodeRunner extends PlanNodeRunner {

    private Description description;
    private RunNotifier notifier;

    /**
     * Constructs a {@code JUnitPlanNodeRunner} with the specified parameters.
     *
     * @param node           The Gherkin {@code feature} node to be executed.
     * @param configuration  The configuration for the execution.
     * @param backendFactory The factory for creating the backend.
     * @param backend        The optional backend to be used.
     * @param logger         The logger for logging execution information.
     */
    JUnitPlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, backend, logger);
    }

    /**
     * Constructs a {@code JUnitPlanNodeRunner} with the specified parameters.
     *
     * @param node           The Gherkin {@code feature} node to be executed.
     * @param configuration  The configuration for the execution.
     * @param backendFactory The factory for creating the backend.
     * @param logger         The logger for logging execution information.
     */
    JUnitPlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, logger);
    }

    /**
     * Checks if the current node is a suite.
     *
     * @return {@code true} if the node is a suite, {@code false} otherwise.
     */
    protected boolean isSuite() {
        return getNode().nodeType().isNoneOf(NodeType.TEST_CASE, NodeType.STEP);
    }

    protected NodeType target() {
        return NodeType.TEST_CASE;
    }

    /**
     * Gets the description of the JUnit test.
     *
     * @return The JUnit test description.
     */
    public Description getDescription() {
        if (description == null) {
            if (isSuite()) {
                description = Description
                        .createSuiteDescription(getNode().displayName(), getUniqueId());
                for (PlanNodeRunner child : getChildren()) {
                    description.addChild(((JUnitPlanNodeRunner) child).getDescription());
                }
            } else {
                description = Description
                        .createTestDescription("", getNode().displayName(), getUniqueId());
            }
        }
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children()
                .map(
                        child -> new JUnitPlanNodeRunner(
                                child, configuration(), backendFactory(), getBackend(), getLogger()
                        )
                ).collect(Collectors.toList());
    }

    /**
     * Runs the JUnit test node with the specified notifier.
     *
     * @param notifier The JUnit notifier.
     */
    public Result runNode(RunNotifier notifier) {
        this.notifier = notifier;
        Result result;
        try {
            result = super.runNode();
        } catch (Exception e) {
            result = Result.ERROR;
        }
        if (getNode().nodeType() == target()) {
            notifyResult();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Stream<Pair<Instant, Result>> runChildren() {
        return getChildren().stream()
                .map(child -> (JUnitPlanNodeRunner) child)
                .map(child -> child.runNode(notifier))
                .filter(Objects::nonNull)
                .map(result -> new Pair<>(Instant.now(), result));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void testCasePreExecution(PlanNode node) {
        if (node.nodeType() == NodeType.TEST_CASE && getChildren().isEmpty()) {
            notifier.fireTestIgnored(getDescription());
        } else {
            notifier.fireTestStarted(getDescription());
        }
        super.testCasePreExecution(node);
    }

    protected void notifyResult() {
        Exception notExecuted = new WakamitiException("Test case not executed due to unknown reasons");
        Optional<Result> result = getNode().result();
        if (result.isPresent()) {
            if (result.get() == Result.PASSED) {
                notifier.fireTestFinished(getDescription());
            } else if (result.get() == Result.SKIPPED) {
                notifier.fireTestFailure(new Failure(getDescription(),
                        new WakamitiSkippedException("Test case skipped")));
            } else {
                Throwable error = getNode().errors().findFirst().orElse(notExecuted);
                if (error instanceof WakamitiSkippedException) {
                    notifier.fireTestIgnored(getDescription());
                } else {
                    notifier.fireTestFailure(new Failure(getDescription(), error));
                }
            }
        } else {
            notifier.fireTestFailure(new Failure(getDescription(), notExecuted));
        }

    }
}