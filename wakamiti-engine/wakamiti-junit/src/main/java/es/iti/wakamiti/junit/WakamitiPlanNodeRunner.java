/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit;


import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.runner.PlanNodeRunner;
import org.junit.runner.Describable;
import org.junit.runner.notification.RunNotifier;


public interface WakamitiPlanNodeRunner<T extends PlanNodeRunner> extends Describable {

    Result run(RunNotifier notifier);

    PlanNode getNode();

}
