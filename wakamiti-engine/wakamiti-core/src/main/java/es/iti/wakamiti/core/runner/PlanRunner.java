/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.runner;


import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import imconfig.Configuration;
import imconfig.ConfigurationFactory;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Executes a test plan represented by a PlanNode. It manages
 * the execution of child nodes using PlanNodeRunners and provides
 * logging and event handling.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class PlanRunner {

    private static final ConfigurationFactory confBuilder = ConfigurationFactory.instance();
    private static final Logger LOGGER = Wakamiti.LOGGER;

    private final Wakamiti wakamiti;
    private final Configuration configuration;

    private final PlanNodeLogger planNodeLogger;
    private final PlanNode plan;
    private List<PlanNodeRunner> children;

    public PlanRunner(PlanNode plan, Configuration configuration) {
        this.plan = plan;
        this.configuration = configuration;
        this.planNodeLogger = new PlanNodeLogger(Wakamiti.LOGGER, configuration, plan);
        this.wakamiti = Wakamiti.instance();
    }

    /**
     * Runs the test plan, executing each child node using PlanNodeRunners.
     *
     * @return The root PlanNode after the execution of the test plan.
     */
    public PlanNode run() {
        wakamiti.configureLogger(configuration);
        wakamiti.configureEventObservers(configuration);
        plan.assignExecutionID(
                configuration.get(WakamitiConfiguration.EXECUTION_ID, String.class)
                        .orElse(UUID.randomUUID().toString())
        );
        wakamiti.publishEvent(Event.PLAN_RUN_STARTED, new PlanNodeSnapshot(plan));
        planNodeLogger.logTestPlanHeader(plan);
        for (PlanNodeRunner child : getChildren()) {
            try {
                child.runNode();
            } catch (Exception e) {
                LOGGER.error("{error}", e.getMessage(), e);
                if (child.getNode().result().isEmpty())
                    child.getNode().prepareExecution().markFinished(Instant.now(), Result.ERROR, e, null);
            }
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
            children = buildRunners();
        }
        return children;
    }

    /**
     * Builds and returns a list of PlanNodeRunners for the child nodes of the test plan.
     *
     * @return The list of PlanNodeRunners.
     */
    protected List<PlanNodeRunner> buildRunners() {
        BackendFactory backendFactory = wakamiti.newBackendFactory();
        return plan.children().map(feature -> {
            Configuration childConfiguration = configuration.append(
                    confBuilder.fromMap(feature.properties())
            );
            return new PlanNodeRunner(feature, childConfiguration, backendFactory, planNodeLogger);
        }).collect(Collectors.toList());
    }

}