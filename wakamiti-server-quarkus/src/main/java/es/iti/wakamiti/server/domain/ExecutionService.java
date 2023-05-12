/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server.domain;



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

import es.iti.wakamiti.server.domain.model.ExecutionCriteria;
import es.iti.wakamiti.server.domain.model.WakamitiExecution;
import es.iti.wakamiti.server.spi.ApplicationContext;
import es.iti.wakamiti.server.spi.ExecutionRepository;
import imconfig.Configuration;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.server.domain.model.*;
import es.iti.wakamiti.server.spi.*;


@ApplicationScoped
public class ExecutionService {

    private static Map<String,Map<String, PlanNode>> aliveExecutions = new ConcurrentHashMap<>();
    private static Set<String> runningExecutions = new ConcurrentSkipListSet<>();
    private static Set<String> finishedExecutions = new ConcurrentSkipListSet<>();

    @ConfigProperty(name = "wakamiti.executions.oldestAge")
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
    		if (property.startsWith(WakamitiConfiguration.PREFIX)) {
    			properties.put(property,quarkusConfig.getValue(property, String.class));
    		}
    	});
        this.defaultConfiguration = Wakamiti.defaultConfiguration().append(
        	Configuration.factory().fromMap(properties).inner(WakamitiConfiguration.PREFIX)
        );
    }


    @PostConstruct
    void fillFinishedExecutions() {
        executionRepository.removeOldExecutions(executionsOldestAge);
        finishedExecutions.addAll(executionRepository.getAllExecutionIDs(user()));
	}


    public WakamitiExecution runSingleResource(String resourceType, String content, boolean async) {
        return run(
            Configuration.factory().fromPairs(WakamitiConfiguration.RESOURCE_TYPES, resourceType),
            content,
            async
        );
    }


    public WakamitiExecution runMultipleResources(Map<String,String> contents, boolean async)
	throws IOException {
        Path temporalWorkspace = Files.createTempDirectory("wakamiti");
        for (var fileContent : contents.entrySet()) {
            Path file = temporalWorkspace.resolve(Path.of(fileContent.getKey()));
            Files.writeString(file, fileContent.getValue());
        }
        return runWorkspace(temporalWorkspace.toString(), async);
    }


    public WakamitiExecution runWorkspace(String workspace, boolean async) {
    	validateWorkspace(workspace);
        return run(
            Configuration.factory().fromPairs(WakamitiConfiguration.RESOURCE_PATH, workspace),
            null,
            async
        );
    }



    public PlanNodeSnapshot analyzeSingleResource(String resourceType, String content) {
        return analyze(
            Configuration.factory().fromPairs(WakamitiConfiguration.RESOURCE_TYPES, resourceType),
            content
        );
    }


    public PlanNodeSnapshot analyzeMultipleResources(Map<String,String> contents) throws IOException {
        Path temporalWorkspace = Files.createTempDirectory("wakamiti");
        for (var fileContent : contents.entrySet()) {
            Path file = temporalWorkspace.resolve(Path.of(fileContent.getKey()));
            Files.writeString(file, fileContent.getValue());
        }
        return analyzeWorkspace(temporalWorkspace.toString());
    }



    public PlanNodeSnapshot analyzeWorkspace(String workspace) {
    	validateWorkspace(workspace);
        return analyze(
            Configuration.factory().fromPairs(WakamitiConfiguration.RESOURCE_PATH, workspace),
            null
        );
    }





    private void validateWorkspace(String workspace) {
    	Path directory = Path.of(workspace);
    	if (!Files.exists(directory)) {
    		throw new WakamitiException("Workspace {} does not exist", workspace);
    	}
    	if (!Files.isDirectory(directory)) {
    		throw new WakamitiException("Workspace {} is not a directory", workspace);
    	}
    	if (!Files.isReadable(directory)) {
    		throw new WakamitiException("Workspace {} is not readable", workspace);
    	}
    }



    private WakamitiExecution run(Configuration configuration, @Null String content, boolean async) {

        String executionID = UUID.randomUUID().toString();
        runningExecutions.add(executionID);
        String owner = user();

    	Configuration effectiveConfiguration = this.defaultConfiguration
			.append(configuration)
	        .appendFromPairs(
	            WakamitiConfiguration.EXECUTION_ID, executionID,
	    		WakamitiConfiguration.GENERATE_OUTPUT_FILE, "false"
	        );

        var plan = content == null ?
            Wakamiti.instance().createPlanFromWorkspace(effectiveConfiguration) :
            Wakamiti.instance().createPlanFromContent(effectiveConfiguration, toInputStream(content))
        ;
        if (async) {
        	executorService.submit(()->run(owner, executionID, plan, effectiveConfiguration));
            var instant = executionRepository.prepareExecution(executionID, owner);
        	return WakamitiExecution.fromPlan(plan, executionID, instant.toString(), owner);
        } else {
        	return run(owner,executionID, plan, effectiveConfiguration);
        }

    }






	private WakamitiExecution run(String owner, String executionID, PlanNode plan, Configuration configuration) {
    	try {
            aliveExecutions(owner).put(executionID, plan);
            var result = Wakamiti.instance().executePlan(plan, configuration);
            var execution = WakamitiExecution.fromResult(result, owner);
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
            Wakamiti.instance().createPlanFromWorkspace(configuration) :
            Wakamiti.instance().createPlanFromContent(
        		configuration,
        		IOUtils.toInputStream(content, StandardCharsets.UTF_8)
    		)
        ;
        return new PlanNodeSnapshot(plan);
    }



    public Optional<WakamitiExecution> getExecution(String executionID) {
        if (runningExecutions.contains(executionID)) {
            return Optional.of(aliveExecution(executionID, user()));
        } else {
            return executionRepository.getExecution(user(), executionID);
        }
    }


    private WakamitiExecution aliveExecution(String executionID, String owner) {
        return WakamitiExecution.fromPlan(aliveExecutions.get(owner).get(executionID),owner);
    }


    public List<WakamitiExecution> searchExecutionHistory(ExecutionCriteria criteria) {
        return executionRepository.getExecutions(criteria);
    }


    public List<WakamitiExecution> getAliveExecutions() {
        return aliveExecutions(user()).values().stream()
	        .map(PlanNodeSnapshot::new)
	        .map(PlanNodeSnapshot::withoutChildren)
	        .map(snapshot -> WakamitiExecution.fromSnapshot(snapshot, user()))
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