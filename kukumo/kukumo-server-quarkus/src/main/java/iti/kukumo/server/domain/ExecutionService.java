package iti.kukumo.server.domain;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.Null;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.server.domain.model.ExecutionCriteria;
import iti.kukumo.server.domain.model.KukumoExecution;
import iti.kukumo.server.spi.ExecutionRepository;

@ApplicationScoped
public class ExecutionService {

    private static Map<String, PlanNode> aliveExecutions = new ConcurrentHashMap<>();
    private static Set<String> runningExecutions = new ConcurrentSkipListSet<>();
    private static Set<String> finishedExecutions = new ConcurrentSkipListSet<>();

    @ConfigProperty(name = "kukumo.executions.oldestAge")
    int executionsOldestAge;

    @Inject
    ExecutionRepository executionRepository;

    private Configuration defaultConfiguration;



    @PostConstruct
    void buildDefaultConfiguration() {
    	Map<String,String> properties = new HashMap<>();
    	Config quarkusConfig = ConfigProvider.getConfig();
    	quarkusConfig.getPropertyNames().forEach(property -> {
    		if (property.startsWith(KukumoConfiguration.PREFIX)) {
    			properties.put(property,quarkusConfig.getValue(property, String.class));
    		}
    	});
        this.defaultConfiguration = Kukumo.defaultConfiguration().append(
        	Configuration.fromMap(properties).inner(KukumoConfiguration.PREFIX)
        );
    }


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
    	validateWorkspace(workspace);
        return run(
            Configuration.fromPairs(KukumoConfiguration.RESOURCE_PATH, workspace),
            null
        );
    }



    public PlanNodeSnapshot analyzeSingleResource(String resourceType, String content) {
        return analyze(
            Configuration.fromPairs(KukumoConfiguration.RESOURCE_TYPES, resourceType),
            content
        );
    }


    public PlanNodeSnapshot analyzeMultipleResources(Map<String,String> contents) throws IOException {
        Path temporalWorkspace = Files.createTempDirectory("kukumo");
        for (var fileContent : contents.entrySet()) {
            Path file = temporalWorkspace.resolve(Path.of(fileContent.getKey()));
            Files.writeString(file, fileContent.getValue());
        }
        return analyzeWorkspace(temporalWorkspace.toString());
    }



    public PlanNodeSnapshot analyzeWorkspace(String workspace) {
    	validateWorkspace(workspace);
        return analyze(
            Configuration.fromPairs(KukumoConfiguration.RESOURCE_PATH, workspace),
            null
        );
    }





    private void validateWorkspace(String workspace) {
    	Path directory = Path.of(workspace);
    	if (!Files.exists(directory)) {
    		throw new KukumoException("Workspace {} does not exist", workspace);
    	}
    	if (!Files.isDirectory(directory)) {
    		throw new KukumoException("Workspace {} is not a directory", workspace);
    	}
    	if (!Files.isReadable(directory)) {
    		throw new KukumoException("Workspace {} is not readable", workspace);
    	}
    }



    private KukumoExecution run(Configuration configuration, @Null String content) {
        String executionID = UUID.randomUUID().toString();
        runningExecutions.add(executionID);
        try {
        	configuration = this.defaultConfiguration
			.append(configuration)
            .appendFromPairs(
                KukumoConfiguration.EXECUTION_ID, executionID,
        		KukumoConfiguration.GENERATE_OUTPUT_FILE, "false"
            );
            var plan = content == null ?
                Kukumo.instance().createPlanFromConfiguration(configuration) :
                Kukumo.instance().createPlanFromContent(configuration, IOUtils.toInputStream(content, StandardCharsets.UTF_8))
            ;
            aliveExecutions.put(executionID,plan);
            var result = Kukumo.instance().executePlan(plan, configuration);
            var execution = new KukumoExecution(new PlanNodeSnapshot(result));
            executionRepository.saveExecution(execution);
            return execution;
        } finally {
            runningExecutions.remove(executionID);
            finishedExecutions.add(executionID);
        }
    }



    private PlanNodeSnapshot analyze(Configuration configuration, @Null String content) {
       	configuration = this.defaultConfiguration.append(configuration);
        var plan = content == null ?
            Kukumo.instance().createPlanFromConfiguration(configuration) :
            Kukumo.instance().createPlanFromContent(configuration, IOUtils.toInputStream(content, StandardCharsets.UTF_8))
        ;
        return new PlanNodeSnapshot(plan);
    }



    public Optional<KukumoExecution> getExecution(String executionID) throws IOException {
        if (runningExecutions.contains(executionID)) {
            return Optional.of(aliveExecution(executionID));
        } else {
            return executionRepository.getExecution(executionID);
        }
    }


    private KukumoExecution aliveExecution(String executionID) {
        return new KukumoExecution(new PlanNodeSnapshot(aliveExecutions.get(executionID)));
    }


    public List<KukumoExecution> searchExecutionHistory(ExecutionCriteria criteria) {
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
