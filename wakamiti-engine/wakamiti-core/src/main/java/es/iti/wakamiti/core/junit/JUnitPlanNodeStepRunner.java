/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.junit;


import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import imconfig.Configuration;
import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class JUnitPlanNodeStepRunner extends JUnitPlanNodeRunner {

    JUnitPlanNodeStepRunner(
        PlanNode node,
        Configuration configuration,
        BackendFactory backendFactory,
        Optional<Backend> backend,
        PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, backend, logger);
    }


    JUnitPlanNodeStepRunner(
                    PlanNode node, Configuration configuration, BackendFactory backendFactory,
                    PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, logger);
    }


    @Override
    protected boolean isSuite() {
        return getNode().nodeType() != NodeType.STEP && getNode().hasChildren();
    }


    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children()
            .map(
                child -> new JUnitPlanNodeStepRunner(
                    child, configuration(), backendFactory(), getBackend(), getLogger()
                )
            ).collect(Collectors.toList());
    }


    @Override
    protected void stepPreExecution(PlanNode step) {
        testCasePreExecution(step);
        super.stepPreExecution(step);
    }


    @Override
    protected void stepPostExecution(PlanNode step) {
        testCasePostExecution(step);
        super.stepPostExecution(step);
    }

}