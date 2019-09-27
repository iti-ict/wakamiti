package iti.kukumo.junit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.core.runner.PlanNodeLogger;
import iti.kukumo.core.runner.PlanNodeRunner;

public class JUnitPlanNodeStepRunner extends JUnitPlanNodeRunner {


    public JUnitPlanNodeStepRunner(PlanNode node, BackendFactory backendFactory, Optional<Backend> backend, PlanNodeLogger logger) {
        super(node, backendFactory, backend, logger);
    }


    public JUnitPlanNodeStepRunner(PlanNode node, BackendFactory backendFactory, PlanNodeLogger logger) {
        super(node, backendFactory, logger);
    }


    @Override
    protected boolean isSuite() {
        return  getNode().nodeType() != NodeType.STEP && getNode().hasChildren();
    }


    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children().map(child ->
                new JUnitPlanNodeStepRunner(child, getBackendFactory(), getBackend(), getLogger())
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
