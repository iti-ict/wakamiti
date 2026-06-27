/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import org.junit.platform.engine.UniqueId;

import java.util.List;
import java.util.Optional;


/**
 * JUnit Platform container runner used when steps are treated as individual
 * tests. It behaves like {@link PlanNodeJUnitRunner} but exposes steps
 * (and virtual steps) as leaf tests instead of scenarios.
 */
class PlanNodeStepJUnitRunner extends PlanNodeJUnitRunner {

    PlanNodeStepJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger,
            String nodePath,
            UniqueId classUniqueId,
            List<String> resourceRoots
    ) {
        super(node, configuration, backendFactory, backend, logger, nodePath, classUniqueId, resourceRoots);
    }

    @Override
    protected NodeType[] target() {
        return new NodeType[] {NodeType.STEP, NodeType.VIRTUAL_STEP};
    }

    @Override
    protected PlanNodeRunner newContainerRunner(PlanNode node, String nodePath) {
        return new PlanNodeStepJUnitRunner(
                node, configuration(), backendFactory(), getBackend(), getLogger(), nodePath,
                classUniqueId, resourceRoots
        );
    }

}
