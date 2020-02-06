package iti.kukumo.server;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.server.app.PlanNodeWrapper;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@Component
public class ExecutionManager {


    private static Map<String, PlanNode> executions = new HashMap<>();
    private static Set<String> runningExecutions = new HashSet<>();
    private static Set<String> finishedExecutions = new HashSet<>();

    private String newID() {
        return UUID.randomUUID().toString();
    }


    public PlanNodeWrapper run(String contentType, InputStream content) {
        String executionID = newID();
        runningExecutions.add(executionID);
        try {
            Configuration configuration = KukumoConfiguration.defaultConfiguration().appendFromMap(Map.of(
                    KukumoConfiguration.RESOURCE_TYPES, contentType,
                    KukumoConfiguration.OUTPUT_FILE_PATH, "/tmp/" + executionID + "/kukumo.json"
            ));
            var plan = Kukumo.instance().createPlanFromContent(configuration, content);
            executions.put(executionID,plan);
            var result = Kukumo.instance().executePlan(plan, configuration);
            return new PlanNodeWrapper(Map.of("executionId",executionID),new PlanNodeSnapshot(plan));
        } finally {
            runningExecutions.remove(executionID);
            finishedExecutions.add(executionID);
        }
    }


    public PlanNodeWrapper getExecutionData(String executionID) throws IOException {
        PlanNodeSnapshot plan;
        if (runningExecutions.contains(executionID)) {
            plan = new PlanNodeSnapshot(executions.get(executionID));
        } else {
            plan = Kukumo.planSerializer().read(Path.of("/tmp/"+executionID+"/kukumo.json"));
        }
        return new PlanNodeWrapper(Map.of("executionId",executionID),plan);
    }

}
