package iti.kukumo.core.runner;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.api.plan.Result;

public class PlanNodeRunner  {

    private static final Logger LOGGER = Kukumo.LOGGER;

    protected enum State {PREPARED, RUNNING, FINISHED}

    private final PlanNode node;
    private final String uniqueId;
    private final BackendFactory backendFactory;


    private List<PlanNodeRunner> children;
    private Optional<Backend> backend;
    private State state;


    public PlanNodeRunner(String parentUniqueId, PlanNode node, BackendFactory backendFactory, Optional<Backend> backend) {
        this.node = node;
        this.backendFactory = backendFactory;
        this.uniqueId = buildUniqueId(parentUniqueId,node);
        this.state = State.PREPARED;
        this.backend = backend;
    }


    public PlanNodeRunner(String parentUniqueId, PlanNode node, BackendFactory backendFactory) {
        this(parentUniqueId, node, backendFactory, Optional.empty());
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


    protected Result runNode(boolean forceSkip) {
        if (state != State.PREPARED) {
            throw new IllegalStateException("run() method can only be invoked once");
        }
        state = State.RUNNING;
        Kukumo.publishEvent(Event.NODE_RUN_STARTED, node.obtainDescriptor());
        Result result;
        if (!getChildren().isEmpty()) {

            if (node.isTestCase()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("");
                    LOGGER.info("@|bold,white,underline {} {}|@", emptyIfNull(node.keyword()), emptyIfNull(node.name()));
                }
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



    protected void notifyAndLogStepResult(PlanStep step) {
        if (step.isVoid()) {
            return;
        }
        String duration = (
           step.getResult() == Result.SKIPPED ? "" :
           "("+ String.valueOf(Duration.between(step.getStartInstant(),step.getFinishInstant()).toMillis() / 1000f) + ")"
        );
        String errorMsg = step.getError().map(Throwable::getLocalizedMessage).orElse("");
        String messageColor;
        BiConsumer<String,Object[]> logger;
        switch (step.getResult()) {
            case PASSED:
                messageColor = "green";
                logger = LOGGER::info;
                break;
            case ERROR:
            case FAILED:
                messageColor = "red";
                logger = LOGGER::info;
                break;
            case SKIPPED:
                messageColor = "faint";
                logger = LOGGER::info;
                break;
            case UNDEFINED:
                messageColor = "yellow,faint";
                logger = LOGGER::info;
                break;
            default: throw new IllegalStateException();
        }
        String message = "[@|bold,"+messageColor+" {}|@] @|faint {}|@ : @|cyan {}|@ {} @|faint {}|@ @|"+messageColor+" {}|@";
        logger.accept(message, new Object[]{step.getResult(), step.source(), emptyIfNull(step.keyword()), step.name(), duration, errorMsg});
        step.getError().ifPresent(error->LOGGER.debug("stack trace:", error));
    }




    protected List<PlanNodeRunner> createChildren() {
        return  node.children()
                .map(child -> new PlanNodeRunner(uniqueId, child, backendFactory, getBackend()))
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



    private static Object emptyIfNull(Object value) {
        return value == null ? "" : value;
    }



}
