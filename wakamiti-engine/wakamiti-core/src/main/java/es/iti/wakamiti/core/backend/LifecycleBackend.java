/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.Map;

import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.Level;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.util.ThrowableRunnable;


/**
 * Backend base class that executes operations registered for lifecycle scopes.
 *
 * <p>Subclasses can install execution context around every operation by
 * overriding the lifecycle-context hooks.</p>
 */
public class LifecycleBackend extends AbstractBackend {

    private final Map<Level, List<ThrowableRunnable>> setUpOperations;
    private final Map<Level, List<ThrowableRunnable>> tearDownOperations;

    /**
     * Creates a backend with operations grouped by lifecycle scope.
     *
     * @param configuration      effective backend configuration
     * @param typeRegistry       registry for Wakamiti data types, when required
     * @param setUpOperations    setup operations grouped by scope
     * @param tearDownOperations teardown operations grouped by scope
     * @param steps              runnable steps associated with this backend
     */
    public LifecycleBackend(
            Configuration configuration,
            WakamitiDataTypeRegistry typeRegistry,
            Map<Level, List<ThrowableRunnable>> setUpOperations,
            Map<Level, List<ThrowableRunnable>> tearDownOperations,
            List<RunnableStep> steps
    ) {
        super(configuration, typeRegistry, steps);
        this.setUpOperations = setUpOperations;
        this.tearDownOperations = tearDownOperations;
    }

    /**
     * {@inheritDoc}
     * This implementation executes the setup operations associated with this backend.
     */
    @Override
    public void setUp(
            Level level
    ) {
        String type = "set-up";
        LOGGER.debug("Performing set-up {} operations...", level);
        for (ThrowableRunnable setUpOperation : setUpOperations.getOrDefault(level, List.of())) {
            runMethod(setUpOperation, type);
        }
        LOGGER.debug("set-up {} finished", level);
    }

    /**
     * {@inheritDoc}
     * This implementation executes the teardown operations associated with this backend.
     */
    @Override
    public void tearDown(
            Level level
    ) {
        String type = "tear-down";
        LOGGER.debug("Performing tear-down {} operations...", level);
        for (ThrowableRunnable tearDownOperation : tearDownOperations.getOrDefault(level, List.of())) {
            runMethod(tearDownOperation, type);
        }
        LOGGER.debug("tear-down {} finished", level);
    }

    @Override
    public void runStep(
            PlanNode modelStep
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Installs state required before a lifecycle operation runs.
     *
     * <p>The base implementation requires no additional state.</p>
     */
    protected void beforeLifecycleOperation() {
        // nothing
    }

    /**
     * Releases state installed for a lifecycle operation.
     *
     * <p>The base implementation requires no cleanup.</p>
     */
    protected void afterLifecycleOperation() {
        // nothing
    }

    /**
     * Runs the specified {@link ThrowableRunnable} operation and handles any exceptions or errors.
     *
     * @param operation The operation to be executed.
     * @param type      The type of the operation for logging purposes.
     * @throws WakamitiException If an exception or error occurs during the execution of the operation.
     */
    private void runMethod(
            ThrowableRunnable operation,
            String type
    ) {
        try {
            beforeLifecycleOperation();
            operation.run();
        } catch (Exception | Error e) {
            Throwable tr = e;
            while (isBlank(tr.getMessage())) {
                if (tr.getCause() == null) {
                    break;
                }
                tr = tr.getCause();
            }
            LOGGER.error("Error running {} operation: {}", type, tr.getMessage());
            LOGGER.debug(tr.getMessage(), e);

            if (e instanceof WakamitiException) {
                throw (WakamitiException) e;
            } else {
                throw new WakamitiException(e);
            }
        } finally {
            afterLifecycleOperation();
        }
    }

}
