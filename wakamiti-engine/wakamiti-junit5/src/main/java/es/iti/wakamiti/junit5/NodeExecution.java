/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.plan.Result;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;


/**
 * Common contract for the Wakamiti plan-node runners that are able to
 * describe themselves to the JUnit Platform and to execute their node,
 * reporting the execution events to a given {@link EngineExecutionListener}.
 */
interface NodeExecution {

    /**
     * @return The descriptor representing this node in the test tree.
     */
    TestDescriptor descriptor();

    /**
     * Executes the node, firing the proper execution events.
     *
     * @param listener The listener to notify execution events to.
     * @return The result of the node execution.
     */
    Result execute(EngineExecutionListener listener);

}
