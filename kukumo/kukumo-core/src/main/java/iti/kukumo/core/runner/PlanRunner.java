/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.runner;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import iti.kukumo.api.KukumoConfiguration;
import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeSnapshot;


public class PlanRunner {

    private static final ConfigurationBuilder confBuilder = ConfigurationBuilder.instance();
    private static final Logger LOGGER = Kukumo.LOGGER;

    private final Kukumo kukumo;
    private final Configuration configuration;

    private final PlanNodeLogger planNodeLogger;
    private final PlanNode plan;
    private List<PlanNodeRunner> children;


    public PlanRunner(PlanNode plan, Configuration configuration) {
        this.plan = plan;
        this.configuration = configuration;
        this.planNodeLogger = new PlanNodeLogger(Kukumo.LOGGER, configuration, plan);
        this.kukumo = Kukumo.instance();
    }


    public PlanNode run() {
        kukumo.configureLogger(configuration);
        kukumo.configureEventObservers(configuration);
        plan.assignExecutionID(
             configuration.get(KukumoConfiguration.EXECUTION_ID,String.class)
             .orElse(UUID.randomUUID().toString())
        );
        kukumo.publishEvent(Event.PLAN_RUN_STARTED, plan);
        planNodeLogger.logTestPlanHeader(plan);
        for (PlanNodeRunner child : getChildren()) {
            try {
                child.runNode();
            } catch (Exception e) {
                LOGGER.error("{error}", e.getMessage(), e);
            }
        }
        planNodeLogger.logTestPlanResult(plan);
        kukumo.publishEvent(Event.PLAN_RUN_FINISHED, plan);
        return plan;
    }


    public List<PlanNodeRunner> getChildren() {
        if (children == null) {
            children = buildRunners();
        }
        return children;
    }


    protected List<PlanNodeRunner> buildRunners() {
        BackendFactory backendFactory = kukumo.newBackendFactory();
        return plan.children().map(feature -> {
            Configuration childConfiguration = configuration.append(
                confBuilder.buildFromMap(feature.properties())
            );
            return new PlanNodeRunner(feature, childConfiguration, backendFactory, planNodeLogger);
        }).collect(Collectors.toList());
    }

}