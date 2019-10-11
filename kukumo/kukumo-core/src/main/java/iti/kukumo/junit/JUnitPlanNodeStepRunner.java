package iti.kukumo.junit;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.core.runner.PlanNodeLogger;
import iti.kukumo.core.runner.PlanNodeRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JUnitPlanNodeStepRunner extends JUnitPlanNodeRunner {


    public JUnitPlanNodeStepRunner(PlanNode node, Configuration configuration, BackendFactory backendFactory, Optional<Backend> backend, PlanNodeLogger logger) {
        super(node, configuration, backendFactory, backend, logger);
    }


    public JUnitPlanNodeStepRunner(PlanNode node, Configuration configuration, BackendFactory backendFactory, PlanNodeLogger logger) {
        super(node, configuration, backendFactory, logger);
    }


    @Override
    protected boolean isSuite() {
        return  getNode().nodeType() != NodeType.STEP && getNode().hasChildren();
    }


    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children().map(child ->
                new JUnitPlanNodeStepRunner(child, configuration(), backendFactory(), getBackend(), getLogger())
        ).collect(Collectors.toList());
    }


    @Override
    protected void stepPreExecution(PlanNode step, boolean forceSkip) {
        testCasePreExecution(step);
        super.stepPreExecution(step, forceSkip);
    }


    @Override
    protected void stepPostExecution(PlanNode step, boolean forceSkip) {
        testCasePostExecution(step);
        super.stepPostExecution(step, forceSkip);
    }

}
