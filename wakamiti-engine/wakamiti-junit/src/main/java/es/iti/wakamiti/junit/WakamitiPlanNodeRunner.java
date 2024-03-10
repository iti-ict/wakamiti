/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.plan.Result;
import org.junit.runner.Describable;
import org.junit.runner.notification.RunNotifier;


/**
 * An interface representing a runner for Wakamiti test plans.
 *
 * <p>Implementations of this interface should provide the ability to run a Wakamiti test plan and
 * return the result of the execution.</p>
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public interface WakamitiPlanNodeRunner extends Describable {

    /**
     * Runs the Wakamiti test plan and notifies the provided RunNotifier.
     *
     * @param notifier The RunNotifier to notify during the test execution.
     * @return The Result of the test plan execution.
     */
    Result run(RunNotifier notifier);

}
