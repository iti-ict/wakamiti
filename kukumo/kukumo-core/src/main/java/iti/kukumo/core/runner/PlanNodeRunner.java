package iti.kukumo.core.runner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeExecution;
import iti.kukumo.api.plan.Result;

public class PlanNodeRunner  {

    protected enum State {PREPARED, RUNNING, FINISHED}

    private final PlanNode node;
    private final String uniqueId;
    private final BackendFactory backendFactory;
    private final PlanNodeLogger logger;


    private List<PlanNodeRunner> children;
    private Optional<Backend> backend;
    private State state;


    public PlanNodeRunner(PlanNode node, BackendFactory backendFactory, Optional<Backend> backend, PlanNodeLogger logger) {
        this.node = node;
        this.backendFactory = backendFactory;
        this.uniqueId = UUID.randomUUID().toString();
        this.state = State.PREPARED;
        this.backend = backend;
        this.logger = logger;
    }


    public PlanNodeRunner(PlanNode node, BackendFactory backendFactory, PlanNodeLogger logger) {
        this(node, backendFactory, Optional.empty(), logger);
    }


    public List<PlanNodeRunner> getChildren() {
        if (children == null) {
            children = createChildren();
        }
        return children;
    }

    public String getUniqueId() {
        return uniqueId;
    }


    protected Optional<Backend> getBackend() {
        if (!backend.isPresent() && node.nodeType() == NodeType.TEST_CASE) {
            backend = Optional.of(backendFactory.createBackend(node));
        }
        return backend;
    }


    protected BackendFactory getBackendFactory() {
        return backendFactory;
    }

    protected PlanNodeLogger getLogger() {
        return logger;
    }


    protected Result runNode(boolean forceSkip) {
        if (state != State.PREPARED) {
            throw new IllegalStateException("run() method can only be invoked once");
        }
        state = State.RUNNING;
        Kukumo.instance().publishEvent(Event.NODE_RUN_STARTED, node.obtainDescriptor());
        Result result;
        if (!getChildren().isEmpty()) {
            if (node.nodeType() == NodeType.TEST_CASE) {
                testCasePreExecution(node);
            }
            result = runChildren();
            if (node.nodeType() == NodeType.TEST_CASE) {
                testCasePostExecution(node);
            }
        } else if (node.nodeType().isAnyOf(NodeType.STEP,NodeType.VIRTUAL_STEP)){
            result = runStep(forceSkip);
        } else {
            // not implemented
            result = Result.SKIPPED;
        }
        state = State.FINISHED;
        Kukumo.instance().publishEvent(Event.NODE_RUN_FINISHED, node.obtainDescriptor());
        return result;
    }




    protected Result runChildren() {
        Result childResult = null;
        boolean forceSkipChild = false;
        for (PlanNodeRunner child : children) {
            childResult = child.runNode(forceSkipChild);
            if (child.getNode().nodeType() == NodeType.STEP && childResult != Result.PASSED) {
                forceSkipChild = true;
            }
        }
        return childResult;
    }


    protected Result runStep(boolean forceSkip) {
        stepPreExecution(node,forceSkip);
        if (forceSkip) {
            getBackend().ifPresent(stepBackend -> stepBackend.skipStep(node));
        } else {
            getBackend().ifPresent(stepBackend -> stepBackend.runStep(node));
        }
        stepPostExecution(node,forceSkip);
        return node.execution().flatMap(PlanNodeExecution::result).orElse(null);
    }







    protected List<PlanNodeRunner> createChildren() {
        return  node.children()
                .map(child -> new PlanNodeRunner(child, backendFactory, getBackend(), logger))
                .collect(Collectors.toList());
    }




    public PlanNode getNode() {
        return node;
    }





    protected void testCasePreExecution(PlanNode node) {
        logger.logTestCaseHeader(node);
        getBackend().ifPresent(Backend::setUp);
    }


    protected void testCasePostExecution(PlanNode node) {
        getBackend().ifPresent(Backend::tearDown);
    }


    protected void stepPreExecution(PlanNode step, boolean forceSkip) {
        /* nothing by default */
    }


    protected void stepPostExecution(PlanNode step, boolean forceSkip) {
        logger.logStepResult(step);
    }


}
