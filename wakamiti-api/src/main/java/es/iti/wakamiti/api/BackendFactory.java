/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;


/**
 * Factory for backend-related runtime services.
 * <p>
 * Implementations normally create one runnable {@link Backend} per test case
 * execution to keep scenario state isolated.
 * </p>
 */
public interface BackendFactory {

    /**
     * Creates a runnable backend for one test case node.
     *
     * @param node          test-case node, typically of type
     *                      {@link NodeType#TEST_CASE}
     * @param configuration effective execution configuration for that test case
     * @return runnable backend bound to the supplied node/context
     */
    Backend createBackend(
            PlanNode node,
            Configuration configuration
    );

    /**
     * Creates a backend for a plan- or feature-level lifecycle scope.
     * <p>
     * The returned backend is intended to be retained for both setup and
     * teardown of the supplied scope, so contributor state is preserved.
     * </p>
     *
     * @param scope         lifecycle node
     * @param configuration effective execution configuration for that lifecycle
     * @return backend bound to the supplied node/context
     */
    Backend createLifecycleBackend(
            PlanNode scope,
            Configuration configuration
    );

    /**
     * Creates a non-runnable backend for metadata and discovery use cases.
     * <p>
     * Returned instances are intended for listing steps, hints and data types
     * without executing plan steps.
     * </p>
     *
     * @param configuration runtime configuration
     * @return non-runnable backend instance
     */
    Backend createNonRunnableBackend(
            Configuration configuration
    );

    /**
     * Creates a hinter used to generate suggestions for invalid steps.
     *
     * @param configuration runtime configuration for hint generation
     * @return hinter instance
     */
    Hinter createHinter(
            Configuration configuration
    );

}
