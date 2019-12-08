/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import static iti.kukumo.api.KukumoConfiguration.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.jext.ExtensionManager;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.event.EventDispatcher;
import iti.kukumo.api.extensions.EventObserver;
import iti.kukumo.api.extensions.PlanBuilder;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.api.extensions.Reporter;
import iti.kukumo.api.extensions.ResourceType;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeDescriptor;
import iti.kukumo.api.plan.PlanSerializer;
import iti.kukumo.core.backend.DefaultBackendFactory;
import iti.kukumo.core.plan.PlanNodeBuilder;
import iti.kukumo.core.runner.PlanRunner;
import iti.kukumo.util.KukumoLogger;
import iti.kukumo.util.ResourceLoader;
import iti.kukumo.util.TagFilter;
import iti.kukumo.util.ThrowableFunction;


public class Kukumo {

    private static final AtomicBoolean instantiated = new AtomicBoolean();

    public static final Logger LOGGER = KukumoLogger.forClass(Kukumo.class);

    private static final ResourceLoader resourceLoader = new ResourceLoader();
    private static final KukumoContributors contributors = new KukumoContributors();
    private static final PlanSerializer planSerializer = new PlanSerializer();
    private static final EventDispatcher eventDispatcher = new EventDispatcher();

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


    private Kukumo() {
        KukumoLogger.configure(KukumoConfiguration.defaultConfiguration());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{logo}", KukumoLogger.logo());
        }
        contributors.eventObservers().forEach(eventDispatcher::addObserver);
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
     * defined in then received configuration.
     *
     * @param configuration
     * @return A new plan ready to be executed
     * @throws KukumoException if the plan couldn't be created
     */
    public PlanNode createPlanFromConfiguration(Configuration configuration) {

        LOGGER.info("{important}", "Creating the Test Plan...");
        List<String> resourceTypeNames = configuration.getList(RESOURCE_TYPES, String.class);
        if (resourceTypeNames.isEmpty()) {
            throw new KukumoException("No resource types configured");
        }
        List<String> discoveryPaths = configuration.getList(RESOURCE_PATH, String.class);
        if (discoveryPaths.isEmpty()) {
            discoveryPaths = Arrays.asList(".");
        }
        List<PlanNode> plans = new ArrayList<>();
        for (String resourceTypeName : resourceTypeNames) {
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


    private Optional<PlanNode> createPlanForResourceType(
        String resourceTypeName,
        List<String> discoveryPaths,
        Configuration configuration
    ) {
        Optional<ResourceType<?>> resourceType = contributors.resourceTypeByName(resourceTypeName);
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
        List<Resource<?>> resources = resourceLoader()
            .discoverResources(discoveryPaths, resourceType.get());

        if (resources.isEmpty()) {
            LOGGER.warn("No resources of type {resourceType}", resourceTypeName);
            return Optional.empty();
        }

        Optional<PlanBuilder> planBuilder = contributors
            .createPlanBuilderFor(resourceType.get(), configuration);

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
        for (PlanTransformer planTransformer : planTransformers) {
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


    public <T> void publishEvent(String eventType, T data) {
        getEventDispatcher().publishEvent(eventType, data);
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
        PlanNodeDescriptor[] plans;
        try (Stream<Path> walker = Files.walk(sourceFolder)) {
            plans = walker
                .map(Path::toFile)
                .filter(File::exists)
                .filter(File::isFile)
                .map(ThrowableFunction.unchecked(deserializer::read))
                .toArray(PlanNodeDescriptor[]::new);
        } catch (IOException e1) {
            throw new KukumoException("Error searching source file/folder", e1);
        }
        PlanNodeDescriptor rootNode = PlanNodeDescriptor.group(plans);
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


    
}
