package iti.kukumo.core.runner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanNode;


public class PlanRunner  {

    private static final Logger LOGGER = Kukumo.LOGGER;
    private static final ConfigurationBuilder confBuilder = ConfigurationBuilder.instance();

    private final String uniqueId;
    private final Configuration configuration;
    private PlanNode plan;
    private List<PlanNodeRunner> children;


    public PlanRunner(PlanNode plan, Configuration configuration)  {
        this.uniqueId = "kukumo";
        this.plan = plan;
        this.configuration = configuration;
    }


    
    public PlanNode run() {
        Kukumo.configureEventObservers(configuration);
        Kukumo.publishEvent(Event.PLAN_RUN_STARTED,plan.obtainDescriptor());
        for (PlanNodeRunner child: getChildren()) {
            try {
                child.runNode(false);
            } catch (Exception e) {
                LOGGER.error("{}",e.getMessage(),e);
            }
        }
        Kukumo.publishEvent(Event.PLAN_RUN_FINISHED,plan.obtainDescriptor());
        writeOutputFile();
        return plan;
    }




    private void writeOutputFile() {
        Optional<String> outputPath = configuration.get(KukumoConfiguration.OUTPUT_FILE_PATH,String.class);
        if (outputPath.isPresent()) {
            try(Writer writer = new FileWriter(outputPath.get())) {
                Kukumo.getPlanSerializer().write(writer, plan);
            } catch (IOException e) {
                LOGGER.error("Error writing output file {} : {}", outputPath.get(), e.getMessage(), e);
            }
        }
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
            BackendFactory backendFactory = Kukumo.getBackendFactory().setConfiguration(childConfiguration);
            return new PlanNodeRunner(uniqueId, feature, backendFactory);
        }).collect(Collectors.toList());
    }






}
