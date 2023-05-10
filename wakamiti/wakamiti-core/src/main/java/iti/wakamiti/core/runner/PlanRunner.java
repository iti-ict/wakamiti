/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.core.runner;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import imconfig.Configuration;
import imconfig.ConfigurationFactory;
import iti.wakamiti.api.WakamitiConfiguration;
import org.slf4j.Logger;

import iti.wakamiti.api.BackendFactory;
import iti.wakamiti.core.Wakamiti;
import iti.wakamiti.api.event.Event;
import iti.wakamiti.api.plan.PlanNode;


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


    public PlanNode run() {
        wakamiti.configureLogger(configuration);
        wakamiti.configureEventObservers(configuration);
        plan.assignExecutionID(
             configuration.get(WakamitiConfiguration.EXECUTION_ID,String.class)
             .orElse(UUID.randomUUID().toString())
        );
        wakamiti.publishEvent(Event.PLAN_RUN_STARTED, plan);
        planNodeLogger.logTestPlanHeader(plan);
        for (PlanNodeRunner child : getChildren()) {
            try {
                child.runNode();
            } catch (Exception e) {
                LOGGER.error("{error}", e.getMessage(), e);
            }
        }
        planNodeLogger.logTestPlanResult(plan);
        wakamiti.publishEvent(Event.PLAN_RUN_FINISHED, plan);
        return plan;
    }


    public List<PlanNodeRunner> getChildren() {
        if (children == null) {
            children = buildRunners();
        }
        return children;
    }


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