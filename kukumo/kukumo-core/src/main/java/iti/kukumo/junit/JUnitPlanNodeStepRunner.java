package iti.kukumo.junit;

import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.core.runner.PlanNodeLogger;
import iti.kukumo.core.runner.PlanNodeRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JUnitPlanNodeStepRunner extends JUnitPlanNodeRunner {

    private Description description;
    private RunNotifier notifier;


    public JUnitPlanNodeStepRunner(String parentUniqueId, PlanNode node, BackendFactory backendFactory, Optional<Backend> backend, PlanNodeLogger logger) {
        super(parentUniqueId, node, backendFactory, backend, logger);
    }


    public JUnitPlanNodeStepRunner(String parentUniqueId, PlanNode node, BackendFactory backendFactory, PlanNodeLogger logger) {
        super(parentUniqueId, node, backendFactory, logger);
    }


    @Override
    protected boolean isSuite() {
        return  !getNode().isStep() && getNode().hasChildren();
    }


    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children().map(child ->
                new JUnitPlanNodeStepRunner(getUniqueId(), child, getBackendFactory(), getBackend(), getLogger())
        ).collect(Collectors.toList());
    }


    @Override
    protected void stepPreExecution(PlanStep step, boolean forceSkip) {
        testCasePreExecution(step);
        super.stepPreExecution(step, forceSkip);
    }


    @Override
    protected void stepPostExecution(PlanStep step, boolean forceSkip) {
        testCasePostExecution(step);
        super.stepPostExecution(step, forceSkip);
    }

}
