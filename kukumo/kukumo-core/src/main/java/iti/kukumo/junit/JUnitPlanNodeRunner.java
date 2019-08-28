package iti.kukumo.junit;

import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.KukumoSkippedException;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.api.plan.Result;
import iti.kukumo.core.runner.PlanNodeLogger;
import iti.kukumo.core.runner.PlanNodeRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JUnitPlanNodeRunner extends PlanNodeRunner {

    private Description description;
    private RunNotifier notifier;

    public JUnitPlanNodeRunner(String parentUniqueId, PlanNode node, BackendFactory backendFactory, Optional<Backend> backend, PlanNodeLogger logger) {
        super(parentUniqueId, node, backendFactory, backend, logger);
    }


    public JUnitPlanNodeRunner(String parentUniqueId, PlanNode node, BackendFactory backendFactory, PlanNodeLogger logger) {
        super(parentUniqueId, node, backendFactory, logger);
    }


    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getNode().displayName(), getUniqueId());
            for (PlanNodeRunner child : getChildren()) {
                description.addChild(((JUnitPlanNodeRunner)child).getDescription());
            }
        }
        return description;
    }




    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children().map(child -> 
            new JUnitPlanNodeRunner(getUniqueId(), child, getBackendFactory(), getBackend(), getLogger())
        ).collect(Collectors.toList());
    }



    public Result runNode(RunNotifier notifier, boolean forceSkip) {
        this.notifier = notifier;
        if (getChildren().isEmpty() && !(getNode() instanceof PlanStep)) {
            notifier.fireTestIgnored(getDescription());
        }
        return super.runNode(forceSkip);
    }


    @Override
    protected Result runChildren() {
        Result childResult = null;
        boolean forceSkipChild = false;
        for (PlanNodeRunner child : getChildren()) {
            childResult = ((JUnitPlanNodeRunner)child).runNode(notifier, forceSkipChild);
            if (child.getNode() instanceof PlanStep && childResult != Result.PASSED) {
                forceSkipChild = true;
            }
        }
        return childResult;
    }

    @Override
    protected Result runStep(boolean forceSkip) {
        notifier.fireTestStarted(getDescription());
        return super.runStep(forceSkip);
    }



    @Override
    protected void notifyAndLogStepResult(PlanStep step) {
        Exception notExecuted = new KukumoException("Step not executed due to unknown reasons");
        Exception skipped = new KukumoSkippedException("Step skipped due to previous step failed");
        if (step.getResult() == Result.PASSED) {
            notifier.fireTestFinished(getDescription());
        } else if (step.getResult() == Result.SKIPPED) {
            notifier.fireTestFailure(new Failure(getDescription(), skipped));
        } else {
            notifier.fireTestFailure(new Failure(getDescription(), step.getError().orElse(notExecuted)));
        }
        super.notifyAndLogStepResult(step);
    }


}
