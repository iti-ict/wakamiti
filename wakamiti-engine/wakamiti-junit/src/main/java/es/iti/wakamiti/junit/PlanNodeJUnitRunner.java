/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import imconfig.Configuration;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PlanNodeJUnitRunner extends PlanNodeRunner implements WakamitiPlanNodeRunner<PlanNodeRunner> {

    private Description description;
    private RunNotifier notifier;

    PlanNodeJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, backend, logger);
    }

    PlanNodeJUnitRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, logger);
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getNode().displayName(), getUniqueId());
            getChildren().stream().map(child -> (WakamitiPlanNodeRunner<?>) child)
                    .forEach(child -> description.addChild(describeChild(child)));

        }
        return description;
    }

    @Override
    public Result run(RunNotifier notifier) {
        this.notifier = notifier;
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, this.getDescription());
        testNotifier.fireTestSuiteStarted();
        Result result;

        try {
            result = super.runNode();
        } catch (AssumptionViolatedException var9) {
            result = Result.ERROR;
            testNotifier.addFailedAssumption(var9);
        } catch (StoppedByUserException var10) {
            throw var10;
        } catch (Throwable var11) {
            result = Result.ERROR;
            testNotifier.addFailure(var11);
        } finally {
            testNotifier.fireTestSuiteFinished();
        }
        return result;
    }

    public NodeType[] target() {
        return new NodeType[]{NodeType.TEST_CASE};
    }

    @Override
    protected List<PlanNodeRunner> createChildren() {
        return getNode().children()
                .map(child -> child.nodeType().isAnyOf(target()) ?
                        new PlanNodeTargetRunner(child, configuration(), backendFactory(), getBackend(), getLogger())
                        : new PlanNodeJUnitRunner(child, configuration(), backendFactory(), getBackend(), getLogger()))
                .collect(Collectors.toList());
    }

    @Override
    protected Stream<Pair<Instant, Result>> runChildren() {
        return getChildren().stream()
                .map(child -> (WakamitiPlanNodeRunner<?>) child)
                .map(child -> child.run(notifier))
                .filter(Objects::nonNull)
                .map(result -> new Pair<>(Instant.now(), result));
    }

    protected Description describeChild(Describable child) {
        return child.getDescription();
    }

}
