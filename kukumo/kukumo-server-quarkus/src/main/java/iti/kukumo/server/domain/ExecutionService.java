package iti.kukumo.server.domain;



import iti.commons.configurer.Configuration;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.server.domain.model.ExecutionCriteria;
import iti.kukumo.server.domain.model.KukumoExecution;
import iti.kukumo.server.spi.ExecutionRepository;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@ApplicationScoped
public class ExecutionService {

    private static Map<String, PlanNode> aliveExecutions = new ConcurrentHashMap<>();
    private static Set<String> runningExecutions = new ConcurrentSkipListSet<>();
    private static Set<String> finishedExecutions = new ConcurrentSkipListSet<>();

    @ConfigProperty(name = "kukumo.executions.path")
    String executionsPath;

    @ConfigProperty(name = "kukumo.executions.oldestAge")
    int executionsOldestAge;

    @Inject
    ExecutionRepository executionRepository;

    @PostConstruct
    void fillFinishedExecutions() {
        executionRepository.removeOldExecutions(executionsOldestAge);
        finishedExecutions.addAll(executionRepository.getAllExecutionIDs());
    }


    public KukumoExecution runSingleResource(String resourceType, String content) {
        return run(
            Configuration.fromPairs(KukumoConfiguration.RESOURCE_TYPES, resourceType),
            content
        );
    }


    public KukumoExecution runMultipleResources(Map<String,String> contents) throws IOException {
        Path temporalWorkspace = Files.createTempDirectory("kukumo");
        for (var fileContent : contents.entrySet()) {
            Path file = temporalWorkspace.resolve(Path.of(fileContent.getKey()));
            Files.writeString(file, fileContent.getValue());
        }
        return runWorkspace(temporalWorkspace.toString());
    }


    public KukumoExecution runWorkspace(String workspace) {
        return run(
            Configuration.fromPairs(KukumoConfiguration.RESOURCE_PATH, workspace),
            null
        );
    }






    private KukumoExecution run(Configuration configuration, @Null String content) {
        String executionID = UUID.randomUUID().toString();
        runningExecutions.add(executionID);
        try {
            configuration = Kukumo.defaultConfiguration()
            .append(configuration)
            .appendFromPairs(
                KukumoConfiguration.OUTPUT_FILE_PATH, Path.of(executionsPath,executionID, "kukumo.json").toString(),
                KukumoConfiguration.EXECUTION_ID, executionID
            );
            var plan = content == null ?
                Kukumo.instance().createPlanFromConfiguration(configuration) :
                Kukumo.instance().createPlanFromContent(configuration, IOUtils.toInputStream(content, StandardCharsets.UTF_8))
            ;
            aliveExecutions.put(executionID,plan);
            var result = Kukumo.instance().executePlan(plan, configuration);
            return new KukumoExecution(configuration.asMap(),new PlanNodeSnapshot(plan));
        } finally {
            runningExecutions.remove(executionID);
            finishedExecutions.add(executionID);
        }
    }


    public Optional<KukumoExecution> getExecution(String executionID) throws IOException {
        if (runningExecutions.contains(executionID)) {
            return Optional.of(aliveExecution(executionID));
        } else {
            return executionRepository.getExecution(executionID);
        }
    }


    private KukumoExecution aliveExecution(String executionID) {
        return new KukumoExecution(
            Map.of("executionId",executionID),
            new PlanNodeSnapshot(aliveExecutions.get(executionID))
        );
    }


    public List<KukumoExecution> searchExecutionHistory(ExecutionCriteria criteria) throws IOException {
        return executionRepository.getExecutions(criteria);
    }


    public List<KukumoExecution> getAliveExecutions() {
        return aliveExecutions.values().stream()
                .map(PlanNodeSnapshot::new)
                .map(PlanNodeSnapshot::withoutChildren)
                .map(KukumoExecution::new)
                .collect(Collectors.toList());
    }


}
