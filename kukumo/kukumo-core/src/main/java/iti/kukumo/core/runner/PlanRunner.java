package iti.kukumo.core.runner;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeDescriptor;


public class PlanRunner  {

    private static final ConfigurationBuilder confBuilder = ConfigurationBuilder.instance();
    private static final Logger LOGGER = Kukumo.LOGGER;

    private final Kukumo kukumo;
    private final Configuration configuration;

    private PlanNodeLogger planNodeLogger;
    private PlanNode plan;
    private List<PlanNodeRunner> children;



    public PlanRunner(PlanNode plan, Configuration configuration)  {
        this.plan = plan;
        this.configuration = configuration;
        this.planNodeLogger = new PlanNodeLogger(Kukumo.LOGGER,configuration,plan);
        this.kukumo = Kukumo.instance();
    }



    public PlanNode run() {
        kukumo.configureLogger(configuration);
        kukumo.configureEventObservers(configuration);
        kukumo.publishEvent(Event.PLAN_RUN_STARTED,new PlanNodeDescriptor(plan));
        planNodeLogger.logTestPlanHeader(plan);
        for (PlanNodeRunner child: getChildren()) {
            try {
                child.runNode(false);
            } catch (Exception e) {
                LOGGER.error("{error}",e.getMessage(),e);
            }
        }
        planNodeLogger.logTestPlanResult(plan);
        kukumo.publishEvent(Event.PLAN_RUN_FINISHED,new PlanNodeDescriptor(plan));
        return plan;
    }





    public List<PlanNodeRunner> getChildren() {
        if (children == null) {
            children = buildRunners();
        }
        return children;
    }



    protected List<PlanNodeRunner> buildRunners()  {
        return plan.children().map(feature -> {
            Configuration childConfiguration = configuration.append(
                confBuilder.buildFromMap(feature.properties())
            );
            BackendFactory backendFactory = kukumo.getBackendFactory().setConfiguration(childConfiguration);
            return new PlanNodeRunner(feature, backendFactory, planNodeLogger);
        }).collect(Collectors.toList());
    }






}
