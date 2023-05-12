/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core;


import static es.iti.wakamiti.api.WakamitiConfiguration.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.*;

import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.core.backend.DefaultBackendFactory;
import es.iti.wakamiti.core.runner.PlanRunner;
import es.iti.wakamiti.core.util.TagFilter;
import imconfig.Configuration;
import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.event.EventDispatcher;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.extensions.PlanTransformer;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.extensions.ResourceType;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.api.util.PathUtil;
import org.slf4j.Logger;

import es.iti.commons.jext.ExtensionManager;


public class Wakamiti {

    private static final AtomicBoolean instantiated = new AtomicBoolean();

    public static final Logger LOGGER = WakamitiLogger.forClass(Wakamiti.class);

    private static final ResourceLoader resourceLoader = new ResourceLoader();
    private static final WakamitiContributors contributors = new WakamitiContributors();
    private static final PlanSerializer planSerializer = new JsonPlanSerializer();
    private static final EventDispatcher eventDispatcher = new EventDispatcher();
    private static final WakamitiFetcher artifactFetcher = new WakamitiFetcher();

    private static final String IMPORTANT = "{important}";

    private static Wakamiti instance;


    public static Wakamiti instance() {
        if (!instantiated.getAndSet(true)) {
            instance = new Wakamiti();
        }
        return instance;
    }


    public static ResourceLoader resourceLoader() {
        return resourceLoader;
    }


    public static WakamitiContributors contributors() {
        return contributors;
    }


    public static PlanSerializer planSerializer() {
        return planSerializer;
    }


    public static ExtensionManager extensionManager() {
        return contributors.extensionManager();
    }


    public static WakamitiFetcher artifactFetcher() {
        return artifactFetcher;
    }


    private Wakamiti() {
        WakamitiLogger.configure(WakamitiConfiguration.DEFAULTS);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{logo}", WakamitiLogger.logo());
        }
        contributors.eventObservers().forEach(eventDispatcher::addObserver);
    }


    /** @return The default configuration. Any configuration should be derived from this one */
    public static Configuration defaultConfiguration() {
        return WakamitiConfiguration.DEFAULTS;
    }


    /**
     * Configure the logger
     *
     * @param configuration
     */
    public void configureLogger(Configuration configuration) {
        WakamitiLogger.configure(configuration);
    }


    /**
     * Attempt to create a plan using the resource type and the feature path
     * defined in the received configuration.
     *
     * @param configuration
     * @return A new plan ready to be executed
     * @throws WakamitiException if the plan was not created
     */
    public PlanNode createPlanFromConfiguration(Configuration configuration) {

        LOGGER.info(IMPORTANT, "Creating the Test Plan...");

        List<String> discoveryPaths = configuration.getList(RESOURCE_PATH, String.class);
        if (discoveryPaths.isEmpty()) {
            discoveryPaths = Arrays.asList(".");
        }

        loadClasses(discoveryPaths)
                .forEach(c -> LOGGER.info("Class [{}] loaded", c));

        List<String> resourceTypeNames = configuration.getList(RESOURCE_TYPES, String.class);
        if (resourceTypeNames.isEmpty()) {
            throw new WakamitiException("No resource types configured\nConfiguration was:\n{}",configuration);
        }
        List<PlanNode> plans = new ArrayList<>();
        for (String resourceTypeName : resourceTypeNames) {
        	LOGGER.debug("Creating plan for resource type {resourceType}..., resourceTypeName");
            Optional<PlanNode> plan = createPlanForResourceType(
                resourceTypeName,
                discoveryPaths,
                configuration
            );
            plan.ifPresent(plans::add);
        }
        if (plans.isEmpty()) {
            throw new WakamitiException("No test plans created");
        }
        PlanNode plan = mergePlans(plans);


        if (configuration.get(STRICT_TEST_CASE_ID,Boolean.class).orElse(Boolean.FALSE)) {
            validateUniqueTestCaseID(plan);
        }
        publishEvent(Event.PLAN_CREATED, new PlanNodeSnapshot(plan));
        return plan;
    }




    /**
     * Attempt to create a plan using the resource path and the feature path
     * defined in the received configuration.
     *
     * @param configuration
     * @return A new plan ready to be executed
     * @throws WakamitiException if the plan was not created
     */
    public PlanNode createPlanFromWorkspace(Configuration configuration) {

        List<String> discoveryPaths = configuration.getList(RESOURCE_PATH, String.class);
        if (discoveryPaths.isEmpty()) {
            discoveryPaths = Arrays.asList(".");
        }

        // try to load a wakamiti.yaml file if exists
        for (String discoveryPath : discoveryPaths) {
        	LOGGER.debug("Looking for configuration file {} in {}...", DEFAULT_CONF_FILE, discoveryPath);
            Path confFile = Path.of(discoveryPath, DEFAULT_CONF_FILE);
            if (Files.exists(confFile)) {
                Configuration confFromFile = Configuration.factory().fromPath(confFile).inner(PREFIX);
                configuration = configuration.append(confFromFile);
                LOGGER.debug("Found {}, applying new {}", confFile, configuration);
            }
        }

        LOGGER.debug("Using final {}", configuration);

        return createPlanFromConfiguration(configuration);
    }


    /**
     * Attempt to create a plan using the resource type defined in the received configuration,
     * but using the given content instead of discovering resources
     * @param configuration
     * @param inputStream
     * @return a new plan ready to be executed
     * @throws WakamitiException if the plan was not created
     */
    public PlanNode createPlanFromContent(Configuration configuration, InputStream inputStream) {
        LOGGER.info(IMPORTANT, "Creating the Test Plan...");
        String resourceTypeName = configuration.get(RESOURCE_TYPES, String.class)
            .orElseThrow(()->new WakamitiException("No resource types configured\nConfiguration was:\n{}",configuration));
        Optional<PlanNode> plan = createPlanForResourceType(
             resourceTypeName,
             inputStream,
             configuration
        );
        if (plan.isEmpty()) {
            throw new WakamitiException("No test plans created");
        } else {
            publishEvent(Event.PLAN_CREATED, new PlanNodeSnapshot(plan.get()));
            return plan.get();
        }
    }

    private Stream<? extends Class<?>> loadClasses(
            List<String> discoveryPaths
    ) {
        return contributors.allLoaderContributors()
                .flatMap(c -> c.load(discoveryPaths));
    }

    private Optional<PlanNode> createPlanForResourceType(
        String resourceTypeName,
        List<String> discoveryPaths,
        Configuration configuration
    ) {
       return createPlanForResourceType(
           resourceTypeName,
           resourceType->resourceLoader().discoverResources(discoveryPaths, resourceType),
           configuration
       );
    }



    private Optional<PlanNode> createPlanForResourceType(
        String resourceTypeName,
        InputStream inputStream,
        Configuration configuration
    ) {
        return createPlanForResourceType(
            resourceTypeName,
            resourceType->List.of(resourceLoader().fromInputStream(resourceType,inputStream)),
            configuration
        );
    }




    private Optional<PlanNode> createPlanForResourceType(
            String resourceTypeName,
            Function<ResourceType<?>,List<Resource<?>>> resourceSupplier,
            Configuration configuration
    ) {
        var resourceType = contributors.resourceTypeByName(resourceTypeName);
        if (!resourceType.isPresent()) {
            LOGGER.warn(
                    "Resource type {resourceType} is not provided by any contributor",
                    resourceTypeName
            );
            return Optional.empty();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Creating plan for resources of type {resourceType} provided by {contributor}...",
                    resourceTypeName,
                    resourceType.get().info()
            );
        }
        var resources = resourceSupplier.apply(resourceType.get());
        if (resources.isEmpty()) {
            LOGGER.warn("No resources of type {resourceType}", resourceTypeName);
            return Optional.empty();
        }

        var planBuilder = contributors.createPlanBuilderFor(resourceType.get(), configuration);
        if (!planBuilder.isPresent()) {
            LOGGER.warn(
                    "No plan builder suitable for resource type {resourceType} has been found",
                    resourceTypeName
            );
            return Optional.empty();
        }

        PlanNodeBuilder planNodeBuilder = planBuilder.get().createPlan(resources);

        List<PlanTransformer> planTransformers = contributors.planTransformers()
                .collect(Collectors.toList());
        for (var planTransformer : planTransformers) {
            planNodeBuilder = planTransformer.transform(planNodeBuilder, configuration);
        }

        return Optional.ofNullable(planNodeBuilder.build());
    }





    private PlanNode mergePlans(List<PlanNode> plans) {
        if (plans.isEmpty()) {
            return null;
        }
        if (plans.size() == 1) {
            return plans.get(0);
        }
        return new PlanNode(NodeType.AGGREGATOR, plans);
    }


    public TagFilter createTagFilter(String tagExpression) {
        return new TagFilter(tagExpression);
    }


    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }


    public void configureEventObservers(Configuration configuration) {
        getEventDispatcher().observers()
            .forEach(observer -> contributors.configure(observer, configuration));
    }


    public void addEventDispatcherObserver(EventObserver observer) {
        getEventDispatcher().addObserver(observer);
    }


    public void removeEventDispatcherObserver(EventObserver observer) {
        getEventDispatcher().removeObserver(observer);
    }


    public void publishEvent(String eventType, Object data) {
        getEventDispatcher().publishEvent(eventType, data);
    }


    public PlanNode executePlan(PlanNode plan, Configuration configuration) {
        PlanNode result = new PlanRunner(plan, configuration).run();
        writeOutputFile(plan, configuration);
        if (configuration.get(WakamitiConfiguration.REPORT_GENERATION, Boolean.class).orElse(true)) {
            generateReports(configuration);
        }
        return result;
    }


    public BackendFactory newBackendFactory() {
        return new DefaultBackendFactory(contributors);
    }


    public void writeOutputFile(PlanNode plan, Configuration configuration) {
        List<String> toHide = configuration.getList(WakamitiConfiguration.PROPERTIES_HIDDEN, String.class)
                .stream().map(p -> "\\$\\{" + p.trim() + "(\\.[\\w\\d-]+)*\\}")
                .collect(Collectors.toList());
        plan.resolveProperties(e -> toHide.stream().noneMatch(h -> e.getKey().trim().matches(h)));

    	if (!configuration
			.get(WakamitiConfiguration.GENERATE_OUTPUT_FILE, Boolean.class)
			.orElse(Boolean.TRUE)) {
    		return;
    	}

        try {

            publishEvent(Event.BEFORE_WRITE_OUTPUT_FILES,null);

            writeStandardOutputFile(plan, configuration);

            if (configuration.get(WakamitiConfiguration.OUTPUT_FILE_PER_TEST_CASE, Boolean.class).orElse(Boolean.FALSE)){
                writeOutputFilesPerTestCase(plan, configuration);
            }

            publishEvent(Event.AFTER_WRITE_OUTPUT_FILES,null);

        } catch (IOException e) {
            LOGGER.error(
                "Error writing output file : {}",
                e.getMessage(),
                e
            );
        }

    }



    private String writeStandardOutputFile(PlanNode plan, Configuration configuration) throws IOException {
        String outputPath = configuration.get(WakamitiConfiguration.OUTPUT_FILE_PATH, String.class).orElseThrow();
        Path path = PathUtil.replacePlaceholders(Paths.get(outputPath).toAbsolutePath(), plan);
        Path parentPath = path.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
        try (Writer writer = new FileWriter(path.toFile())) {
            planSerializer().write(writer, plan);
            LOGGER.info("Generated result output file {uri}", path);
        }
        publishEvent(Event.OUTPUT_FILE_WRITTEN, path);

        return outputPath;
    }


    private void writeOutputFilesPerTestCase(PlanNode plan, Configuration configuration) throws IOException {
        String outputPath = configuration.get(OUTPUT_FILE_PER_TEST_CASE_PATH, String.class).orElseThrow();
        Path path = PathUtil.replacePlaceholders(Paths.get(outputPath).toAbsolutePath(), plan);
        Files.createDirectories(path);

        List<PlanNode> testCases = plan
            .descendants()
            .filter(node -> node.nodeType() == NodeType.TEST_CASE)
            .collect(Collectors.toList());

        for (PlanNode testCase : testCases) {
            String testCaseId = Objects.requireNonNull(testCase.id(),"test case have no id");
            Path testCasePath = path.resolve(testCaseId+".json");
            try (Writer writer = new FileWriter(testCasePath.toFile())) {
                planSerializer().write(writer, new PlanNodeSnapshot(testCase).withoutChildren());
                LOGGER.info("Generated result output file {uri}", testCasePath);
            }
            publishEvent(Event.OUTPUT_FILE_PER_TEST_CASE_WRITTEN, testCasePath);
        }
    }



    public void generateReports(Configuration configuration) {
        List<Reporter> reporters = contributors.reporters().collect(Collectors.toList());
        if (reporters.isEmpty()) {
            return;
        }
        LOGGER.info(IMPORTANT, "Generating reports...");
        String reportSource = configuration.get(REPORT_SOURCE, String.class)
            .orElse(configuration.get(OUTPUT_FILE_PATH, String.class).orElse(null));
        Path sourceFolder = Paths.get(reportSource).toAbsolutePath();
        if (!sourceFolder.toFile().exists()) {
            throw new WakamitiException(
                "The report source file/folder {} does not exist.\n" + "Perhaps you may set the property {} to the path defined by the property {}:{}",
                sourceFolder,
                REPORT_SOURCE,
                OUTPUT_FILE_PATH,
                configuration.get(OUTPUT_FILE_PATH, String.class).orElse("<undefined>")
            );
        }
        PlanSerializer deserializer = planSerializer();
        PlanNodeSnapshot[] plans;
        try (Stream<Path> walker = Files.walk(sourceFolder)) {
            plans = walker
                .map(Path::toFile)
                .filter(File::exists)
                .filter(File::isFile)
                .map(ThrowableFunction.unchecked(deserializer::read))
                .toArray(PlanNodeSnapshot[]::new);
        } catch (IOException e1) {
            throw new WakamitiException("Error searching source file/folder", e1);
        }
        PlanNodeSnapshot rootNode = PlanNodeSnapshot.group(plans);
        for (Reporter reporter : reporters) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                        "Generating report provided by plugin {contributor}...",
                        reporter.info()
                    );
                }
                contributors.configure(reporter, configuration).report(rootNode);
            } catch (Exception e) {
                LOGGER.error(
                    "{error} {contributor} : {error}",
                    "Error running reporter",
                    reporter.info(),
                    e.getMessage(),
                    e
                );
            }
        }

    }


    public Hinter createHinterFor(Configuration configuration) {
    	var backendFactory = newBackendFactory();
        return backendFactory.createHinter(configuration);
    }


    private void validateUniqueTestCaseID(PlanNode plan) {
        if (plan == null) {
            return;
        }
        Map<String, AtomicInteger> ids = new HashMap<>();
        plan
            .descendants()
            .filter(node -> node.nodeType() == NodeType.TEST_CASE)
            .forEach(node -> ids.computeIfAbsent(node.id(), x->new AtomicInteger()).incrementAndGet());
        if (ids.get(null) != null) {
            throw new WakamitiException("There is one or more test cases withouth a valid ID");
        }
        ids.forEach((id, count)->{
            if (count.get() > 1) {
                throw new WakamitiException("The ID {} is used in {} test cases",id,count);
            }
        });
    }

}