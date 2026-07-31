/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import java.util.List;

import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNode;


/**
 * Implementation of the Backend interface that does not allow running tests.
 * Its main purpose is to provide information about available steps to
 * third-party components, such as completion tools.
 */
public class NonRunnableBackend extends AbstractBackend {

    /**
     * Creates a discovery-only backend.
     *
     * @param configuration effective backend configuration
     * @param typeRegistry  data types used to parse step expressions
     * @param steps         runnable-step descriptors exposed for matching and
     *                      completion, although execution remains disabled
     */
    public NonRunnableBackend(
            Configuration configuration,
            WakamitiDataTypeRegistry typeRegistry,
            List<RunnableStep> steps
    ) {
        super(configuration, typeRegistry, steps);
    }

    /**
     * {@inheritDoc}
     * This implementation throws an {@code UnsupportedOperationException}, as
     * {@code NonRunnableBackend} does not support running steps.
     *
     * @param step The plan node representing the step to run.
     * @throws UnsupportedOperationException Always thrown, as running steps is not supported.
     */
    @Override
    public void runStep(
            PlanNode step
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * This implementation does nothing, as there is no setup required for a non-runnable steps.
     */
    @Override
    public void setUp() {
        // nothing
    }

    /**
     * {@inheritDoc}
     * This implementation does nothing, as there is no teardown required for a non-runnable steps.
     */
    @Override
    public void tearDown() {
        // nothing
    }

}
