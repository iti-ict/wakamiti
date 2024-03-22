/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.junit;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import imconfig.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Provides a JUnit-compatible runner for Gherkin step nodes.
 * It handles the execution of Gherkin step nodes, including
 * creating descriptions, running child nodes, and notifying
 * JUnit about test events.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @deprecated
 */
@Deprecated(since = "2.4.0", forRemoval = true)
public class JUnitPlanNodeStepRunner extends JUnitPlanNodeRunner {

    /**
     * Constructs a {@code JUnitPlanNodeStepRunner} with the specified parameters.
     *
     * @param node           The Gherkin step node to be executed.
     * @param configuration  The configuration for the execution.
     * @param backendFactory The factory for creating the backend.
     * @param backend        The optional backend to be used.
     * @param logger         The logger for logging execution information.
     */
    JUnitPlanNodeStepRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, backend, logger);
    }

    /**
     * Constructs a {@code JUnitPlanNodeStepRunner} with the specified parameters.
     *
     * @param node           The Gherkin step node to be executed.
     * @param configuration  The configuration for the execution.
     * @param backendFactory The factory for creating the backend.
     * @param logger         The logger for logging execution information.
     */
    JUnitPlanNodeStepRunner(
            PlanNode node, Configuration configuration, BackendFactory backendFactory,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, logger);
    }

    /**
     * Checks if the current node is a suite.
     *
     * @return {@code true} if the node is a suite, {@code false} otherwise.
     */
    @Override
    protected boolean isSuite() {
        return getNode().nodeType() != NodeType.STEP && getNode().hasChildren();
    }

    @Override
    protected NodeType target() {
        return NodeType.STEP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children()
                .map(
                        child -> new JUnitPlanNodeStepRunner(
                                child, configuration(), backendFactory(), getBackend(), getLogger()
                        )
                ).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stepPreExecution(PlanNode step) {
        testCasePreExecution(step);
        super.stepPreExecution(step);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stepPostExecution(PlanNode step) {
        testCasePostExecution(step);
        super.stepPostExecution(step);
    }

}