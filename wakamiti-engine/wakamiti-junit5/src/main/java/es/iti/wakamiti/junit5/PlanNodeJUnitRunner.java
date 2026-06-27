/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * JUnit Platform runner for a Wakamiti plan node acting as a container
 * (a feature or a scenario). It builds the descriptor sub-tree and delegates
 * the execution of its children, reporting container execution events.
 */
class PlanNodeJUnitRunner extends PlanNodeRunner implements NodeExecution {

    protected final UniqueId classUniqueId;
    protected final List<String> resourceRoots;
    private WakamitiNodeDescriptor descriptor;
    private EngineExecutionListener listener;

    PlanNodeJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger,
            String nodePath,
            UniqueId classUniqueId,
            List<String> resourceRoots
    ) {
        super(node, configuration, backendFactory, backend, logger, false, nodePath);
        this.classUniqueId = classUniqueId;
        this.resourceRoots = resourceRoots;
    }

    /**
     * @return The node types that must be represented as leaf tests.
     */
    protected NodeType[] target() {
        return new NodeType[] {NodeType.TEST_CASE};
    }

    @Override
    public WakamitiNodeDescriptor descriptor() {
        if (descriptor == null) {
            descriptor = new WakamitiNodeDescriptor(
                    classUniqueId.append("node", getNodePath()),
                    displayName(),
                    WakamitiTestSources.from(getNode(), resourceRoots).orElse(null),
                    TestDescriptor.Type.CONTAINER
            );
            getChildren().stream()
                    .map(NodeExecution.class::cast)
                    .forEach(child -> descriptor.addChild(child.descriptor()));
        }
        return descriptor;
    }

    @Override
    public Result execute(EngineExecutionListener listener) {
        this.listener = listener;
        listener.executionStarted(descriptor());
        Result result;
        TestExecutionResult executionResult;
        try {
            result = super.runNode();
            executionResult = TestExecutionResult.successful();
        } catch (Throwable error) {
            result = Result.ERROR;
            executionResult = TestExecutionResult.failed(error);
        }
        listener.executionFinished(descriptor(), executionResult);
        return result;
    }

    @Override
    protected Stream<Pair<Instant, Result>> runChildren() {
        return getChildren().stream()
                .map(NodeExecution.class::cast)
                .map(child -> child.execute(listener))
                .filter(Objects::nonNull)
                .map(result -> new Pair<>(Instant.now(), result));
    }

    @Override
    protected List<PlanNodeRunner> createChildren() {
        List<PlanNode> childNodes = getNode().children().collect(Collectors.toList());
        return IntStream.range(0, childNodes.size())
                .mapToObj(index -> {
                    PlanNode child = childNodes.get(index);
                    String childPath = childNodePath(index);
                    if (child.nodeType().isAnyOf(target())) {
                        return new PlanNodeTargetRunner(
                                child, configuration(), backendFactory(), getBackend(),
                                getLogger(), childPath, classUniqueId, resourceRoots
                        );
                    }
                    return newContainerRunner(child, childPath);
                })
                .collect(Collectors.toList());
    }

    protected PlanNodeRunner newContainerRunner(PlanNode node, String nodePath) {
        return new PlanNodeJUnitRunner(
                node, configuration(), backendFactory(), getBackend(), getLogger(), nodePath, classUniqueId, resourceRoots
        );
    }

    protected String displayName() {
        return Objects.toString(getNode().displayName(), getNode().name());
    }

}
