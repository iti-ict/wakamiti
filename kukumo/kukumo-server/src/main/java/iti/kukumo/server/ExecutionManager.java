package iti.kukumo.server;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.server.spi.ExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Component
public class ExecutionManager {

    private static Map<String, PlanNode> aliveExecutions = new ConcurrentHashMap<>();
    private static Set<String> runningExecutions = new ConcurrentSkipListSet<>();
    private static Set<String> finishedExecutions = new ConcurrentSkipListSet<>();

    @Value("${kukumo.executions.path}")
    private String executionsPath;

    @Value("${kukumo.executions.oldestAge}")
    private int executionsOldestAge;

    @Autowired
    private ExecutionRepository executionRepository;

    @PostConstruct
    private void fillFinishedExecutions() {
        executionRepository.removeOldExecutions(executionsOldestAge);
        finishedExecutions.addAll(executionRepository.getAllExecutionIDs());
    }


    public KukumoExecution run(String contentType, InputStream content) {
        String executionID = UUID.randomUUID().toString();
        runningExecutions.add(executionID);
        try {
            Configuration configuration = Kukumo.defaultConfiguration().appendFromMap(Map.of(
                    KukumoConfiguration.RESOURCE_TYPES, contentType,
                    KukumoConfiguration.OUTPUT_FILE_PATH, executionsPath + executionID + "/kukumo.json",
                    KukumoConfiguration.EXECUTION_ID, executionID
            ));
            var plan = Kukumo.instance().createPlanFromContent(configuration, content);
            aliveExecutions.put(executionID,plan);
            var result = Kukumo.instance().executePlan(plan, configuration);
            return new KukumoExecution(configuration.asMap(),new PlanNodeSnapshot(plan));
        } finally {
            runningExecutions.remove(executionID);
            finishedExecutions.add(executionID);
        }
    }


    public KukumoExecution getExecution(String executionID) throws IOException {
        PlanNodeSnapshot plan;
        if (runningExecutions.contains(executionID)) {
            plan = new PlanNodeSnapshot(aliveExecutions.get(executionID));
        } else {
            plan = Kukumo.planSerializer().read(Path.of(executionsPath + executionID+"/kukumo.json"));
        }
        return new KukumoExecution(Map.of("executionId",executionID),plan);
    }


    public List<KukumoExecution> searchExecutionHistory(ExecutionCriteria criteria) throws IOException {
        return executionRepository.getExecutions(criteria);
    }


    public List<KukumoExecution> getAliveExecutions() {
        return aliveExecutions.values().stream()
            .map(PlanNodeSnapshot::new)
            .map(KukumoExecution::new)
            .collect(Collectors.toList());
    }


}
