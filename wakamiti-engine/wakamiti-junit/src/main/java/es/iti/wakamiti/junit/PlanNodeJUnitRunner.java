/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import imconfig.Configuration;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * JUnit Runner for executing Wakamiti plan nodes representing a test suite.
 *
 * <p>This runner extends the functionality of PlanNodeRunner and implements the
 * WakamitiPlanNodeRunner interface to integrate Wakamiti plan nodes with JUnit for
 * test execution. It handles the execution of plan nodes representing either test
 * suites or test cases within a JUnit framework.</p>
 *
 * <p>The runner provides descriptions for the tests or test suites to be displayed
 * in the test report. It supports the execution of child nodes, whether they are
 * test cases or nested test suites, by creating appropriate runner instances for
 * each child.</p>
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public class PlanNodeJUnitRunner extends PlanNodeRunner implements WakamitiPlanNodeRunner {

    private Description description;
    private RunNotifier notifier;

    PlanNodeJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, backend, logger);
    }

    PlanNodeJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, logger);
    }

    /**
     * Retrieves the description of the test suite.
     *
     * @return The Description object representing the test suite.
     */
    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getNode().displayName(), getUniqueId());
            getChildren().stream().map(WakamitiPlanNodeRunner.class::cast)
                    .forEach(child -> description.addChild(describeChild(child)));

        }
        return description;
    }

    /**
     * Runs the test suite and notifies the RunNotifier.
     *
     * @param notifier The RunNotifier to notify during the test execution.
     * @return         The Result of the test suite execution.
     */
    @Override
    public Result run(RunNotifier notifier) {
        this.notifier = notifier;
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, this.getDescription());
        testNotifier.fireTestSuiteStarted();
        Result result;

        try {
            result = super.runNode();
        } catch (AssumptionViolatedException var9) {
            result = Result.ERROR;
            testNotifier.addFailedAssumption(var9);
        } catch (StoppedByUserException var10) {
            throw var10;
        } catch (Throwable var11) {
            result = Result.ERROR;
            testNotifier.addFailure(var11);
        } finally {
            testNotifier.fireTestSuiteFinished();
        }
        return result;
    }

    /**
     * Specifies the target node types for this runner.
     *
     * @return An array of target node types: [{@code TEST_CASE}]
     */
    public NodeType[] target() {
        return new NodeType[]{NodeType.TEST_CASE};
    }

    /**
     * Creates child runners for the associated PlanNode.
     *
     * @return A list of PlanNodeRunner instances representing the child runners.
     */
    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children()
                .map(child -> child.nodeType().isAnyOf(target()) ?
                        new PlanNodeTargetRunner(child, configuration(), backendFactory(), getBackend(), getLogger())
                        : new PlanNodeJUnitRunner(child, configuration(), backendFactory(), getBackend(), getLogger()))
                .collect(Collectors.toList());
    }

    /**
     * Runs the child nodes of the current test suite and captures the results.
     *
     * @return A stream of pairs containing the timestamp and result for each child node.
     */
    @Override
    protected Stream<Pair<Instant, Result>> runChildren() {
        return getChildren().stream()
                .map(WakamitiPlanNodeRunner.class::cast)
                .map(child -> child.run(notifier))
                .filter(Objects::nonNull)
                .map(result -> new Pair<>(Instant.now(), result));
    }

    /**
     * Retrieves the description for a child node.
     *
     * @param child The Describable representing a child node.
     * @return The Description object representing the child node.
     */
    protected Description describeChild(Describable child) {
        return child.getDescription();
    }

}
