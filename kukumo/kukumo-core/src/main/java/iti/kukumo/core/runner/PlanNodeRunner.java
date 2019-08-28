package iti.kukumo.core.runner;

import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.api.plan.Result;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlanNodeRunner  {

    private static final Logger LOGGER = Kukumo.LOGGER;


    protected enum State {PREPARED, RUNNING, FINISHED}

    private final PlanNode node;
    private final String uniqueId;
    private final BackendFactory backendFactory;
    private final PlanNodeLogger logger;


    private List<PlanNodeRunner> children;
    private Optional<Backend> backend;
    private State state;


    public PlanNodeRunner(String parentUniqueId, PlanNode node, BackendFactory backendFactory, Optional<Backend> backend, PlanNodeLogger logger) {
        this.node = node;
        this.backendFactory = backendFactory;
        this.uniqueId = buildUniqueId(parentUniqueId,node);
        this.state = State.PREPARED;
        this.backend = backend;
        this.logger = logger;
    }


    public PlanNodeRunner(String parentUniqueId, PlanNode node, BackendFactory backendFactory, PlanNodeLogger logger) {
        this(parentUniqueId, node, backendFactory, Optional.empty(), logger);
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
        if (!backend.isPresent() && node.isTestCase()) {
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
        Kukumo.publishEvent(Event.NODE_RUN_STARTED, node.obtainDescriptor());
        Result result;
        if (!getChildren().isEmpty()) {

            if (node.isTestCase()) {
                logger.logTestCaseHeader(node);
                getBackend().ifPresent(Backend::setUp);
            }

            result = runChildren();

            if (node.isTestCase()) {
                getBackend().ifPresent(Backend::tearDown);
                LOGGER.info("");
            }

        } else if (node instanceof PlanStep){
            result = runStep(forceSkip);
        } else {
            // not implemented
            result = Result.SKIPPED;
        }
        state = State.FINISHED;
        Kukumo.publishEvent(Event.NODE_RUN_FINISHED, node.obtainDescriptor());
        return result;
    }




    protected Result runChildren() {
        Result childResult = null;
        boolean forceSkipChild = false;
        for (PlanNodeRunner child : children) {
            childResult = child.runNode(forceSkipChild);
            if (child.getNode() instanceof PlanStep && childResult != Result.PASSED) {
                forceSkipChild = true;
            }
        }
        return childResult;
    }


    protected Result runStep(boolean forceSkip) {
        PlanStep step = (PlanStep) node;
        if (forceSkip) {
            getBackend().ifPresent(stepBackend -> stepBackend.skipStep(step));
        } else {
            getBackend().ifPresent(stepBackend -> stepBackend.runStep(step));
        }
        notifyAndLogStepResult(step);
        return step.getResult();
    }







    protected List<PlanNodeRunner> createChildren() {
        return  node.children()
                .map(child -> new PlanNodeRunner(uniqueId, child, backendFactory, getBackend(), logger))
                .collect(Collectors.toList());
    }



    protected String buildUniqueId(String parentUniqueId, PlanNode node) {
        final StringBuilder nodeUniqueId = new StringBuilder(parentUniqueId).append(':');
        if (node.id() != null) {
            nodeUniqueId.append(node.id());
        } else if (node.name() != null) {
            nodeUniqueId.append(node.name());
        } else {
            nodeUniqueId.append(node.nodeType());
        }
        return nodeUniqueId.toString();
    }


    public PlanNode getNode() {
        return node;
    }


    protected void notifyAndLogStepResult(PlanStep step) {
        logger.logStepResult(step);
    }




}
