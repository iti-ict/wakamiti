package iti.kukumo.server.domain;



import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.Null;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.*;
import iti.kukumo.api.plan.*;
import iti.kukumo.server.domain.model.*;
import iti.kukumo.server.spi.*;


@ApplicationScoped
public class ExecutionService {

    private static Map<String,Map<String, PlanNode>> aliveExecutions = new ConcurrentHashMap<>();
    private static Set<String> runningExecutions = new ConcurrentSkipListSet<>();
    private static Set<String> finishedExecutions = new ConcurrentSkipListSet<>();

    @ConfigProperty(name = "kukumo.executions.oldestAge")
    int executionsOldestAge;

    @Inject
    ExecutionRepository executionRepository;

    @Inject
    ApplicationContext context;


    private final ExecutorService executorService = Executors.newCachedThreadPool();

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
        finishedExecutions.addAll(executionRepository.getAllExecutionIDs(user()));
	}


    public KukumoExecution runSingleResource(String resourceType, String content, boolean async) {
        return run(
            Configuration.fromPairs(KukumoConfiguration.RESOURCE_TYPES, resourceType),
            content,
            async
        );
    }


    public KukumoExecution runMultipleResources(Map<String,String> contents, boolean async)
	throws IOException {
        Path temporalWorkspace = Files.createTempDirectory("kukumo");
        for (var fileContent : contents.entrySet()) {
            Path file = temporalWorkspace.resolve(Path.of(fileContent.getKey()));
            Files.writeString(file, fileContent.getValue());
        }
        return runWorkspace(temporalWorkspace.toString(), async);
    }


    public KukumoExecution runWorkspace(String workspace, boolean async) {
    	validateWorkspace(workspace);
        return run(
            Configuration.fromPairs(KukumoConfiguration.RESOURCE_PATH, workspace),
            null,
            async
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



    private KukumoExecution run(Configuration configuration, @Null String content, boolean async) {

        String executionID = UUID.randomUUID().toString();
        runningExecutions.add(executionID);
        String owner = user();

    	Configuration effectiveConfiguration = this.defaultConfiguration
			.append(configuration)
	        .appendFromPairs(
	            KukumoConfiguration.EXECUTION_ID, executionID,
	    		KukumoConfiguration.GENERATE_OUTPUT_FILE, "false"
	        );

        var plan = content == null ?
            Kukumo.instance().createPlanFromConfiguration(effectiveConfiguration) :
            Kukumo.instance().createPlanFromContent(effectiveConfiguration, toInputStream(content))
        ;
        if (async) {
        	executorService.submit(()->run(owner, executionID, plan, effectiveConfiguration));
            var instant = executionRepository.prepareExecution(executionID, owner);
        	return KukumoExecution.fromPlan(plan, executionID, instant.toString(), owner);
        } else {
        	return run(owner,executionID, plan, effectiveConfiguration);
        }

    }






	private KukumoExecution run(String owner, String executionID, PlanNode plan, Configuration configuration) {
    	try {
            aliveExecutions(owner).put(executionID, plan);
            var result = Kukumo.instance().executePlan(plan, configuration);
            var execution = KukumoExecution.fromResult(result, owner);
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
            Kukumo.instance().createPlanFromContent(
        		configuration,
        		IOUtils.toInputStream(content, StandardCharsets.UTF_8)
    		)
        ;
        return new PlanNodeSnapshot(plan);
    }



    public Optional<KukumoExecution> getExecution(String executionID) {
        if (runningExecutions.contains(executionID)) {
            return Optional.of(aliveExecution(executionID, user()));
        } else {
            return executionRepository.getExecution(user(), executionID);
        }
    }


    private KukumoExecution aliveExecution(String executionID, String owner) {
        return KukumoExecution.fromPlan(aliveExecutions.get(owner).get(executionID),owner);
    }


    public List<KukumoExecution> searchExecutionHistory(ExecutionCriteria criteria) {
        return executionRepository.getExecutions(criteria);
    }


    public List<KukumoExecution> getAliveExecutions() {
        return aliveExecutions(user()).values().stream()
	        .map(PlanNodeSnapshot::new)
	        .map(PlanNodeSnapshot::withoutChildren)
	        .map(snapshot -> KukumoExecution.fromSnapshot(snapshot, user()))
	        .collect(Collectors.toList());
    }


    private InputStream toInputStream(@Null String content) {
    	return IOUtils.toInputStream(content, StandardCharsets.UTF_8);
	}


	private String user() {
        return context.user().orElse("anonymous");
    }


	private Map<String, PlanNode> aliveExecutions(String owner) {
		return aliveExecutions.computeIfAbsent(owner, it -> new HashMap<>() );
	}

}