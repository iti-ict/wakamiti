/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiSkippedException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import imconfig.Configuration;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import java.util.Optional;


/**
 * JUnit Runner for executing Wakamiti plan nodes targeted as test cases.
 *
 * <p>This runner is responsible for executing Wakamiti plan nodes designed to act
 * as individual test cases within a JUnit framework. It extends the functionality
 * of PlanNodeRunner and implements WakamitiPlanNodeRunner interface to integrate
 * with JUnit for test execution.</p>
 *
 * <p>It handles the execution of targeted plan nodes, managing the notifications
 * and results using JUnit's RunNotifier and EachTestNotifier. Additionally, it
 * provides descriptions for the tests to be displayed in the test report.</p>
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public class PlanNodeTargetRunner extends PlanNodeRunner implements WakamitiPlanNodeRunner {

    private Description description;

    PlanNodeTargetRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            Optional<Backend> backend,
            PlanNodeLogger logger
    ) {
        super(node, configuration, backendFactory, backend, logger);
    }

    /**
     * Notifies the result of the test execution to the EachTestNotifier.
     *
     * @param notifier The EachTestNotifier to notify the test result.
     */
    protected void notifyResult(EachTestNotifier notifier) {
        Exception notExecuted = new WakamitiException("Test case not executed due to unknown reasons");
        Optional<Result> result = getNode().result();
        if (result.isPresent()) {
            if (result.get() == Result.SKIPPED) {
                notifier.addFailure(new WakamitiSkippedException("Test case skipped"));
            } else if (result.get() != Result.PASSED) {
                Throwable error = getNode().errors().findFirst().orElse(notExecuted);
                if (error instanceof WakamitiSkippedException) {
                    notifier.fireTestIgnored();
                } else {
                    notifier.addFailure(error);
                }
            }
        } else {
            notifier.addFailure(notExecuted);
        }

    }

    /**
     * Runs the plan node and notifies the result to the RunNotifier.
     *
     * @param notifier The RunNotifier to notify the test execution.
     * @return The result of the test execution.
     */
    public Result run(RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, this.getDescription());
        testNotifier.fireTestStarted();
        Result result;
        try {
            result = super.runNode();
            notifyResult(testNotifier);
        } catch (Throwable e) {
            testNotifier.addFailure(e);
            result = Result.ERROR;
        } finally {
            testNotifier.fireTestFinished();
        }
        return result;
    }

    /**
     * Retrieves the description of the test case.
     *
     * @return The Description object representing the test case.
     */
    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createTestDescription("", getNode().displayName(), getUniqueId());
        }
        return description;
    }

}
