/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.imconfig.Configuration;


/**
 * Factory interface for creating instances of Backend. The factory
 * is responsible for creating backends based on test case nodes
 * and configurations.
 * <p>It also provides the ability to create a non-runnable
 * backend for exposing information without executing tests.</p>
 * <p>Additionally, it can create a hinter for providing
 * suggestions and hints.</p>
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public interface BackendFactory {

    /**
     * Create a new backend for a node of type {@link NodeType#TEST_CASE}
     * and a given configuration.
     *
     * @param node          The test case node.
     * @param configuration The test case configuration.
     * @return A new backend.
     */
    Backend createBackend(PlanNode node, Configuration configuration);

    /**
     * Create a new <b>non-runnable</b> backend based on a given
     * configuration, limited to step-related data providing. It
     * is useful to expose that information to third-party
     * components (like completion tools) without compromising
     * the test plan execution.
     * The resulting backend would throw {@link UnsupportedOperationException}
     * if a test case is attempted to be run.
     *
     * @param configuration The configuration for the non-runnable
     *                      backend.
     * @return A non-runnable backend.
     */
    Backend createNonRunnableBackend(Configuration configuration);

    /**
     * Create a hinter for providing suggestions and hints based
     * on the given configuration.
     *
     * @param configuration The configuration for the hinter.
     * @return A hinter instance.
     */
    Hinter createHinter(Configuration configuration);

}