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
import java.util.Locale;
import java.util.Map;

import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.annotations.Level;
import es.iti.wakamiti.api.extensions.StepContributor;
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
    private final Locale locale;

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
        this(
                configuration,
                typeRegistry,
                List.of(),
                setUpOperations,
                tearDownOperations,
                steps,
                Locale.ENGLISH
        );
    }

    /**
     * Creates a backend with backend-scoped contributors and lifecycle locale.
     *
     * @param configuration       effective backend configuration
     * @param typeRegistry        registry for Wakamiti data types, when required
     * @param stepContributors    contributor instances owned by this backend
     * @param setUpOperations     setup operations grouped by scope
     * @param tearDownOperations  teardown operations grouped by scope
     * @param steps               runnable steps associated with this backend
     * @param locale              locale exposed while lifecycle operations run
     */
    public LifecycleBackend(
            Configuration configuration,
            WakamitiDataTypeRegistry typeRegistry,
            List<StepContributor> stepContributors,
            Map<Level, List<ThrowableRunnable>> setUpOperations,
            Map<Level, List<ThrowableRunnable>> tearDownOperations,
            List<RunnableStep> steps,
            Locale locale
    ) {
        super(configuration, typeRegistry, stepContributors, steps);
        this.setUpOperations = setUpOperations;
        this.tearDownOperations = tearDownOperations;
        this.locale = locale;
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
        WakamitiException failure = null;
        for (ThrowableRunnable tearDownOperation : tearDownOperations.getOrDefault(level, List.of())) {
            try {
                runMethod(tearDownOperation, type);
            } catch (WakamitiException operationFailure) {
                if (failure == null) {
                    failure = operationFailure;
                } else {
                    failure.addSuppressed(operationFailure);
                }
            }
        }
        LOGGER.debug("tear-down {} finished", level);
        if (failure != null) {
            throw failure;
        }
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
     * <p>The base implementation binds this backend to the current execution
     * thread.</p>
     */
    protected void beforeLifecycleOperation() {
        WakamitiStepRunContext.set(
                new WakamitiStepRunContext(configuration, this, locale, locale)
        );
    }

    /**
     * Releases state installed for a lifecycle operation.
     *
     * <p>The base implementation clears the backend execution context.</p>
     */
    protected void afterLifecycleOperation() {
        WakamitiStepRunContext.clear();
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
