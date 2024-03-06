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
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import imconfig.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class PlanNodeStepJUnitRunner extends PlanNodeJUnitRunner {

    PlanNodeStepJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, backend, logger);
    }

    PlanNodeStepJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, logger);
    }

    @Override
    public NodeType[] target() {
        return new NodeType[] {NodeType.STEP, NodeType.VIRTUAL_STEP};
    }

    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children()
                .map(child -> child.nodeType().isAnyOf(target()) ?
                        new PlanNodeTargetRunner(child, configuration(), backendFactory(), getBackend(), getLogger())
                        : new PlanNodeStepJUnitRunner(child, configuration(), backendFactory(), getBackend(), getLogger()))
                .collect(Collectors.toList());
    }

}
