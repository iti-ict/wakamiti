/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import static iti.kukumo.api.KukumoConfiguration.*;

import java.io.*;
import java.nio.file.*;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.*;

import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.jext.ExtensionManager;
import iti.kukumo.api.event.*;
import iti.kukumo.api.extensions.*;
import iti.kukumo.api.plan.*;
import iti.kukumo.core.backend.DefaultBackendFactory;
import iti.kukumo.core.runner.PlanRunner;
import iti.kukumo.util.*;


public class Kukumo {

    private static final AtomicBoolean instantiated = new AtomicBoolean();

    public static final Logger LOGGER = KukumoLogger.forClass(Kukumo.class);

    private static final ResourceLoader resourceLoader = new ResourceLoader();
    private static final KukumoContributors contributors = new KukumoContributors();
    private static final PlanSerializer planSerializer = new PlanSerializer();
    private static final EventDispatcher eventDispatcher = new EventDispatcher();
    private static final KukumoFetcher artifactFetcher = new KukumoFetcher();

    private static Kukumo instance;


    public static Kukumo instance() {
        if (!instantiated.getAndSet(true)) {
            instance = new Kukumo();
        }
        return instance;
    }


    public static ResourceLoader resourceLoader() {
        return resourceLoader;
    }


    public static KukumoContributors contributors() {
        return contributors;
    }


    public static PlanSerializer planSerializer() {
        return planSerializer;
    }


    public static ExtensionManager extensionManager() {
        return contributors.extensionManager();
    }


    public static KukumoFetcher artifactFetcher() {
        return artifactFetcher;
    }


    private Kukumo() {
        KukumoLogger.configure(KukumoConfiguration.DEFAULTS);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{logo}", KukumoLogger.logo());
        }
        contributors.eventObservers().forEach(eventDispatcher::addObserver);
    }


    /** @return The default configuration. Any configuration should be derived from this one */
    public static Configuration defaultConfiguration() {
        return KukumoConfiguration.DEFAULTS;
    }


    /**
     * Configure the logger
     *
     * @param configuration
     */
    public void configureLogger(Configuration configuration) {
        KukumoLogger.configure(configuration);
    }


    /**
     * Attempt to create a plan using the resource type and the feature path
     * defined in the received configuration.
     *
     * @param configuration
     * @return A new plan ready to be executed
     * @throws KukumoException if the plan was not created
     */
    public PlanNode createPlanFromConfiguration(Configuration configuration) {

        LOGGER.info("{important}", "Creating the Test Plan...");

        List<String> discoveryPaths = configuration.getList(RESOURCE_PATH, String.class);
        if (discoveryPaths.isEmpty()) {
            discoveryPaths = Arrays.asList(".");
        }

        List<String> resourceTypeNames = configuration.getList(RESOURCE_TYPES, String.class);
        if (resourceTypeNames.isEmpty()) {
            new KukumoException("No resource types configured\nConfiguration was:\n{}",configuration);
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
            throw new KukumoException("No test plans created");
        }
        PlanNode plan = mergePlans(plans);
        publishEvent(Event.PLAN_CREATED, plan);
        return plan;
    }


    /**
     * Attempt to create a plan using the resource path and the feature path
     * defined in the received configuration.
     *
     * @param configuration
     * @return A new plan ready to be executed
     * @throws KukumoException if the plan was not created
     */
    public PlanNode createPlanFromWorkspace(Configuration configuration) {

        List<String> discoveryPaths = configuration.getList(RESOURCE_PATH, String.class);
        if (discoveryPaths.isEmpty()) {
            discoveryPaths = Arrays.asList(".");
        }

        // try to load a kukumo.yaml file if exists
        for (String discoveryPath : discoveryPaths) {
        	LOGGER.debug("Looking for configuration file {} in {}...", DEFAULT_CONF_FILE, discoveryPath);
            Path confFile = Path.of(discoveryPath, DEFAULT_CONF_FILE);
            if (Files.exists(confFile)) {
                Configuration confFromFile = Configuration.fromPath(confFile).inner(PREFIX);
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
     * @throws KukumoException if the plan was not created
     */
    public PlanNode createPlanFromContent(Configuration configuration, InputStream inputStream) {
        LOGGER.info("{important}", "Creating the Test Plan...");
        String resourceTypeName = configuration.get(RESOURCE_TYPES, String.class)
            .orElseThrow(()->new KukumoException("No resource types configured\nConfiguration was:\n{}",configuration));
        Optional<PlanNode> plan = createPlanForResourceType(
             resourceTypeName,
             inputStream,
             configuration
        );
        if (plan.isEmpty()) {
            throw new KukumoException("No test plans created");
        } else {
            publishEvent(Event.PLAN_CREATED, plan.get());
            return plan.get();
        }
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
        configuration.ifPresent(TEST_PLAN_NAME, String.class, planNodeBuilder::setName);

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


    public void publishEvent(String eventType, PlanNode data) {
        getEventDispatcher().publishEvent(eventType, new PlanNodeSnapshot(data));
    }


    public PlanNode executePlan(PlanNode plan, Configuration configuration) {
        PlanNode result = new PlanRunner(plan, configuration).run();
        writeOutputFile(plan, configuration);
        if (configuration.get(KukumoConfiguration.REPORT_GENERATION, Boolean.class).orElse(true)) {
            generateReports(configuration);
        }
        return result;
    }


    public BackendFactory newBackendFactory() {
        return new DefaultBackendFactory(contributors);
    }


    public void writeOutputFile(PlanNode plan, Configuration configuration) {

    	if (!configuration
			.get(KukumoConfiguration.GENERATE_OUTPUT_FILE, Boolean.class)
			.orElse(Boolean.TRUE)) {
    		return;
    	}

        Optional<String> outputPath = configuration
            .get(KukumoConfiguration.OUTPUT_FILE_PATH, String.class);
        if (outputPath.isPresent()) {
            try {
                Path path = Paths.get(outputPath.get()).toAbsolutePath();
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                try (Writer writer = new FileWriter(outputPath.get())) {
                    planSerializer().write(writer, plan);
                    LOGGER.info("Generated result output file {uri}", path);
                }
            } catch (IOException e) {
                LOGGER.error(
                    "Error writing output file {} : {}",
                    outputPath.get(),
                    e.getMessage(),
                    e
                );
            }
        }
    }


    public void generateReports(Configuration configuration) {
        List<Reporter> reporters = contributors.reporters().collect(Collectors.toList());
        if (reporters.isEmpty()) {
            return;
        }
        LOGGER.info("{important}", "Generating reports...");
        String reportSource = configuration.get(REPORT_SOURCE, String.class)
            .orElse(configuration.get(OUTPUT_FILE_PATH, String.class).orElse(null));
        Path sourceFolder = Paths.get(reportSource).toAbsolutePath();
        if (!sourceFolder.toFile().exists()) {
            throw new KukumoException(
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
            throw new KukumoException("Error searching source file/folder", e1);
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



}
