/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiSkippedException;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import org.junit.platform.engine.TestDescriptor;
import org.opentest4j.TestAbortedException;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * JUnit Platform runner for a Wakamiti plan node acting as a leaf test
 * (a scenario or an individual step). It executes the node and reports the
 * proper started/finished/skipped events together with the failure cause.
 */
class PlanNodeTargetRunner extends PlanNodeRunner implements NodeExecution {

    private final UniqueId classUniqueId;
    private final List<String> resourceRoots;
    private WakamitiNodeDescriptor descriptor;

    PlanNodeTargetRunner(
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

    @Override
    public WakamitiNodeDescriptor descriptor() {
        if (descriptor == null) {
            descriptor = new WakamitiNodeDescriptor(
                    classUniqueId.append("test", getNodePath()),
                    displayName(),
                    WakamitiTestSources.from(getNode(), resourceRoots).orElse(null),
                    TestDescriptor.Type.TEST
            );
        }
        return descriptor;
    }

    @Override
    public Result execute(EngineExecutionListener listener) {
        listener.executionStarted(descriptor());
        Result result;
        try {
            result = super.runNode();
        } catch (Throwable error) {
            listener.executionFinished(descriptor(), TestExecutionResult.failed(error));
            return Result.ERROR;
        }

        if (isSkippedExecution(result)) {
            listener.executionFinished(
                    descriptor(),
                    TestExecutionResult.aborted(new TestAbortedException(skipReason()))
            );
            return result;
        }

        listener.executionFinished(descriptor(), toExecutionResult());
        return result;
    }

    private TestExecutionResult toExecutionResult() {
        Optional<Result> result = getNode().result();
        if (result.isEmpty()) {
            return TestExecutionResult.failed(
                    new WakamitiException("Test case not executed due to unknown reasons")
            );
        }
        if (result.get() == Result.PASSED) {
            return TestExecutionResult.successful();
        }
        Throwable error = getNode().errors().findFirst().orElseGet(
                () -> new WakamitiException("Node finished as " + result.get())
        );
        return TestExecutionResult.failed(error);
    }

    private boolean isSkippedExecution(Result result) {
        return result == Result.SKIPPED
                || getNode().errors().anyMatch(WakamitiSkippedException.class::isInstance);
    }

    private String skipReason() {
        return getNode().errors()
                .filter(WakamitiSkippedException.class::isInstance)
                .map(Throwable::getMessage)
                .findFirst()
                .orElse("Skipped");
    }

    private String displayName() {
        return Objects.toString(getNode().displayName(), getNode().name());
    }

}
