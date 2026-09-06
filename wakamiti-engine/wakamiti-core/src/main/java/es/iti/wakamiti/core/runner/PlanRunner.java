/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.Level;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationFactory;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;


/**
 * Coordinates end-to-end execution of a constructed plan.
 * <p>
 * The runner assigns execution IDs, delegates node execution to
 * {@link PlanNodeRunner} children, and publishes plan-level start/finish events.
 * </p>
 */
public class PlanRunner {

    private static final ConfigurationFactory CONF_BUILDER = ConfigurationFactory.instance();
    private static final Logger LOGGER = Wakamiti.LOGGER;

    private final Wakamiti wakamiti;
    private final Configuration configuration;
    private final boolean stopExecutionOnError;

    private final PlanNodeLogger planNodeLogger;
    private final PlanNode plan;
    private List<PlanNodeRunner> children;
    private BackendFactory backendFactory;
    private Backend lifecycleBackend;

    /**
     * Creates an execution coordinator for a fully constructed plan.
     *
     * @param plan          root plan node whose children will be executed
     * @param configuration effective execution and reporting configuration
     */
    public PlanRunner(
            PlanNode plan,
            Configuration configuration
    ) {
        this.plan = plan;
        this.configuration = configuration;
        this.stopExecutionOnError = configuration.get(WakamitiConfiguration.STOP_EXECUTION_ON_ERROR, Boolean.class)
                .get();
        this.planNodeLogger = new PlanNodeLogger(Wakamiti.LOGGER, configuration, plan);
        this.wakamiti = Wakamiti.instance();
    }

    /**
     * Executes the plan in normal mode.
     *
     * @return root plan node after execution
     */
    public PlanNode run() {
        return runPlan(false);
    }

    /**
     * Executes the plan in dry-run mode.
     * <p>
     * Step definitions are resolved and validated, but step implementations are
     * not invoked.
     * </p>
     *
     * @return root plan node after validation
     */
    public PlanNode noRun() {
        return runPlan(true);
    }

    private PlanNode runPlan(
            boolean dryRun
    ) {
        plan.assignExecutionID(
                configuration.get(WakamitiConfiguration.EXECUTION_ID, String.class)
                        .orElse(UUID.randomUUID().toString())
        );
        wakamiti.publishEvent(Event.PLAN_RUN_STARTED, new PlanNodeSnapshot(plan));
        planNodeLogger.logTestPlanHeader(plan);
        List<PlanNodeRunner> runners = dryRun ? buildRunners(true) : getChildren();
        boolean hasImplementedSteps = plan.descendants().anyMatch(node -> node.nodeType() == NodeType.STEP);
        boolean lifecycleError = false;
        boolean stopChildren = false;
        if (!dryRun && hasImplementedSteps) {
            try {
                lifecycleBackend().setUp(Level.PLAN);
            } catch (WakamitiException e) {
                lifecycleError = true;
                stopChildren = stopExecutionOnError;
            }
        }
        if (stopChildren) {
            skipPendingChildren(runners, 0);
        } else {
            for (int i = 0; i < runners.size(); i++) {
                PlanNodeRunner child = runners.get(i);
                Result result = null;
                try {
                    result = child.runNode();
                } catch (Exception e) {
                    LOGGER.error("{error}", e.getMessage(), e);
                    if (child.getNode().result().isEmpty()) {
                        child.getNode().prepareExecution().markFinished(Instant.now(), Result.ERROR, e, null);
                    }
                    result = Result.ERROR;
                }
                if (stopExecutionOnError && result == Result.ERROR) {
                    skipPendingChildren(runners, i + 1);
                    break;
                }
            }
        }
        if (!dryRun && hasImplementedSteps) {
            try {
                lifecycleBackend().tearDown(Level.PLAN);
            } catch (WakamitiException e) {
                lifecycleError = true;
            }
        }
        if (lifecycleError) {
            plan.prepareExecution().markFinished(Instant.now(), Result.ERROR);
        }
        planNodeLogger.logTestPlanResult(plan);
        wakamiti.publishEvent(Event.PLAN_RUN_FINISHED, new PlanNodeSnapshot(plan));
        return plan;
    }

    /**
     * Gets the list of PlanNodeRunners representing the child nodes of the test plan.
     *
     * @return The list of PlanNodeRunners.
     */
    public List<PlanNodeRunner> getChildren() {
        if (children == null) {
            children = buildRunners(false);
        }
        return children;
    }

    /**
     * Builds one child runner per top-level plan child.
     *
     * @param dryRun whether child runners should execute in dry-run mode
     * @return top-level child runners
     */
    protected List<PlanNodeRunner> buildRunners(
            boolean dryRun
    ) {
        return plan.children().map(feature -> {
            Configuration childConfiguration = configuration.append(
                    CONF_BUILDER.fromMap(feature.properties())
            );
            return new PlanNodeRunner(feature, childConfiguration, backendFactory(), planNodeLogger, dryRun);
        }).collect(Collectors.toList());
    }

    private void skipPendingChildren(
            List<PlanNodeRunner> runners,
            int startIndex
    ) {
        for (int i = startIndex; i < runners.size(); i++) {
            runners.get(i).skipIfPending();
        }
    }

    private BackendFactory backendFactory() {
        if (backendFactory == null) {
            backendFactory = wakamiti.newBackendFactory();
        }
        return backendFactory;
    }

    private Backend lifecycleBackend() {
        if (lifecycleBackend == null) {
            lifecycleBackend = backendFactory().createLifecycleBackend(plan, configuration);
        }
        return lifecycleBackend;
    }

}
