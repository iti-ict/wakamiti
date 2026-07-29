/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;


/**
 * JUnit runner for executing Wakamiti test plans with step-level nodes.
 *
 * <p>This runner is specifically designed for handling step-level nodes in a Wakamiti test plan.
 * It provides the ability to run such plans and handle their execution at the step level.</p>
 */
public class PlanNodeStepJUnitRunner extends PlanNodeJUnitRunner {

    PlanNodeStepJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger,
            String nodePath,
            String testClassName
    ) {
        super(node, configuration, backendFactory, backend, logger, nodePath, testClassName);
    }

    PlanNodeStepJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger,
            String nodePath,
            String testClassName
    ) {
        super(node, configuration, backendFactory, logger, nodePath, testClassName);
    }

    /**
     * {@inheritDoc}
     *
     * @return An array of target node types: [{@code STEP}, {@code VIRTUAL_STEP}].
     */
    @Override
    public NodeType[] target() {
        return new NodeType[]{NodeType.STEP, NodeType.VIRTUAL_STEP};
    }

    /**
     * {@inheritDoc}
     *
     * @return A list of PlanNodeRunner instances representing the child runners.
     */
    @Override
    protected List<PlanNodeRunner> createChildren() {
        List<PlanNode> childNodes = getNode().children().toList();
        return IntStream.range(0, childNodes.size())
                .mapToObj(index -> {
                    PlanNode child = childNodes.get(index);
                    String childPath = childNodePath(index);
                    return child.nodeType().isAnyOf(target())
                            ? new PlanNodeTargetRunner(
                            child,
                            configuration(),
                            backendFactory(),
                            getBackend(),
                            getLogger(),
                            childPath,
                            testClassName()
                    )
                            : new PlanNodeStepJUnitRunner(
                            child,
                            configuration(),
                            backendFactory(),
                            getBackend(),
                            getLogger(),
                            childPath,
                            testClassName()
                    );
                })
                .collect(Collectors.toList());
    }

}
