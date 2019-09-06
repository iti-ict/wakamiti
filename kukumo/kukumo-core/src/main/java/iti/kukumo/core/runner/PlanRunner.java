package iti.kukumo.core.runner;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanNode;
import org.slf4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class PlanRunner  {

    private static final ConfigurationBuilder confBuilder = ConfigurationBuilder.instance();

    private final String uniqueId;
    private final Configuration configuration;
    private final Logger LOGGER = Kukumo.LOGGER;
    private PlanNodeLogger planNodeLogger;
    private PlanNode plan;
    private List<PlanNodeRunner> children;


    public PlanRunner(PlanNode plan, Configuration configuration)  {
        this.uniqueId = "kukumo";
        this.plan = plan;
        this.configuration = configuration;
        this.planNodeLogger = new PlanNodeLogger(LOGGER,configuration,plan.numTestCases());
    }


    
    public PlanNode run() {
        Kukumo.configureLogger(configuration);
        Kukumo.configureEventObservers(configuration);
        Kukumo.publishEvent(Event.PLAN_RUN_STARTED,plan.obtainDescriptor());
        planNodeLogger.logTestPlanHeader(plan);
        for (PlanNodeRunner child: getChildren()) {
            try {
                child.runNode(false);
            } catch (Exception e) {
                LOGGER.error("{error}",e.getMessage(),e);
            }
        }
        planNodeLogger.logTestPlanResult(plan);
        Kukumo.publishEvent(Event.PLAN_RUN_FINISHED,plan.obtainDescriptor());
        writeOutputFile();
        return plan;
    }




    private void writeOutputFile() {
        configuration
        .get(KukumoConfiguration.OUTPUT_FILE_PATH,String.class)
        .map(Paths::get)
        .ifPresent(outputPath -> {
            try {
                Files.createDirectories(outputPath.toAbsolutePath().getParent());
                try (Writer writer = new FileWriter(outputPath.toAbsolutePath().toFile())) {
                    Kukumo.getPlanSerializer().write(writer, plan);
                    LOGGER.info("Raw result data stored in {uri}", outputPath);
                }
            } catch (IOException e) {
                LOGGER.error("{error} {uri}","Error writing output file", outputPath, e.getMessage(), e);
            }
        });
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
            return new PlanNodeRunner(uniqueId, feature, backendFactory, planNodeLogger);
        }).collect(Collectors.toList());
    }






}
