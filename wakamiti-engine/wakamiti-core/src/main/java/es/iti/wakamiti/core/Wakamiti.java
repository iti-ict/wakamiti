/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core;


import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.event.EventDispatcher;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.extensions.PlanTransformer;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.extensions.ResourceType;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.util.PathUtil;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.util.ThrowableFunction;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.backend.DefaultBackendFactory;
import es.iti.wakamiti.core.runner.PlanRunner;
import es.iti.wakamiti.core.util.TagFilter;
import es.iti.wakamiti.api.imconfig.Configuration;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static es.iti.wakamiti.core.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_FEATURE;


/**
 * The main class for managing and executing test plans in Wakamiti.
 * It serves as a central hub for configuration, plan creation,
 * execution, and report generation.
 * This class uses contributors and extensions for extensibility.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class Wakamiti {

    public static final Logger LOGGER = WakamitiLogger.forClass(Wakamiti.class);
    private static final AtomicBoolean instantiated = new AtomicBoolean();
    private static final ResourceLoader resourceLoader = new ResourceLoader();
    private static final WakamitiContributors contributors = new WakamitiContributors();
    private static final PlanSerializer planSerializer = new JsonPlanSerializer();
    private static final EventDispatcher eventDispatcher = new EventDispatcher();
    private static final WakamitiFetcher artifactFetcher = new WakamitiFetcher();

    private static final String IMPORTANT = "{important}";

    private static Wakamiti instance;


    private Wakamiti() {
        WakamitiLogger.configure(WakamitiConfiguration.DEFAULTS);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("{logo}", WakamitiLogger.logo());
        }
        contributors.eventObservers().forEach(eventDispatcher::addObserver);
    }

    /**
     * Gets the singleton instance of Wakamiti.
     *
     * @return The Wakamiti instance.
     */
    public static Wakamiti instance() {
        if (!instantiated.getAndSet(true)) {
            instance = new Wakamiti();
        }
        return instance;
    }

    /**
     * Gets the resource loader instance used by Wakamiti.
     *
     * @return The ResourceLoader instance.
     */
    public static ResourceLoader resourceLoader() {
        return resourceLoader;
    }

    /**
     * The contributors managing various extensions in Wakamiti.
     *
     * @return The WakamitiContributors instance.
     */
    public static WakamitiContributors contributors() {
        return contributors;
    }

    /**
     * The plan serializer used by Wakamiti.
     *
     * @return The plan serializer.
     */
    public static PlanSerializer planSerializer() {
        return planSerializer;
    }

    /**
     * The extension manager used by Wakamiti.
     *
     * @return The extension manager.
     */
    public static ExtensionManager extensionManager() {
        return contributors.extensionManager();
    }

    /**
     * The artifact fetcher used by Wakamiti.
     *
     * @return The artifact fetcher.
     */
    public static WakamitiFetcher artifactFetcher() {
        return artifactFetcher;
    }

    /**
     * The default configuration for Wakamiti. Any specific configuration should be
     * derived from this default configuration.
     *
     * @return The default configuration.
     */
    public static Configuration defaultConfiguration() {
        return WakamitiConfiguration.DEFAULTS;
    }

    /**
     * Gets the working directory for a given configuration.
     *
     * @param configuration The configuration for which the working directory is obtained.
     * @return The working directory path.
     */
    public static Path workingDir(Configuration configuration) {
        return Path.of(configuration.get(WORKING_DIR, String.class).orElse("")).toAbsolutePath();
    }

    /**
     * Configures the logger based on the provided configuration.
     *
     * @param configuration The configuration to use for logger configuration.
     */
    public void configureLogger(Configuration configuration) {
        WakamitiLogger.configure(configuration);
    }

    /**
     * Attempt to create a plan using the resource type and the feature path
     * defined in the received configuration.
     *
     * @param configuration The configuration for creating the test plan.
     * @return A new test plan ready to be executed.
     */
    public PlanNode createPlanFromConfiguration(Configuration configuration) {

        LOGGER.info(IMPORTANT, "Creating the Test Plan...");

        resourceLoader.setWorkingDir(workingDir(configuration));

        List<String> discoveryPaths = configuration.getList(RESOURCE_PATH, String.class);
        if (discoveryPaths.isEmpty()) {
            discoveryPaths = List.of(".");
        }

        loadClasses(discoveryPaths)
                .forEach(c -> LOGGER.info("Class [{}] loaded", c));

        List<String> resourceTypeNames = configuration.getList(RESOURCE_TYPES, String.class);
        if (resourceTypeNames.isEmpty()) {
            throw new WakamitiException("No resource types configured\nConfiguration was:\n{}", configuration);
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


        if (configuration.get(STRICT_TEST_CASE_ID, Boolean.class).orElse(Boolean.FALSE)) {
            validateUniqueTestCaseID(plan, configuration);
        }
        publishEvent(Event.PLAN_CREATED, new PlanNodeSnapshot(plan));
        return plan;
    }

    /**
     * Attempt to create a plan using the resource path and the feature path
     * defined in the received configuration.
     *
     * @param configuration The configuration for creating the test plan.
     * @return A new test plan ready to be executed.
     */
    public PlanNode createPlanFromWorkspace(Configuration configuration) {

        resourceLoader.setWorkingDir(workingDir(configuration));

        List<String> discoveryPaths = configuration.getList(RESOURCE_PATH, String.class);
        if (discoveryPaths.isEmpty()) {
            discoveryPaths = List.of(".");
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
     *
     * @param configuration The configuration for creating the test plan.
     * @return A new test plan ready to be executed.
     */
    public PlanNode createPlanFromContent(Configuration configuration, InputStream inputStream) {
        LOGGER.info(IMPORTANT, "Creating the Test Plan...");
        resourceLoader.setWorkingDir(workingDir(configuration));
        String resourceTypeName = configuration.get(RESOURCE_TYPES, String.class)
                .orElseThrow(() -> new WakamitiException("No resource types configured\nConfiguration was:\n{}", configuration));
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
                resourceType -> resourceLoader().discoverResources(discoveryPaths, resourceType),
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
                resourceType -> List.of(resourceLoader().fromInputStream(resourceType, inputStream)),
                configuration
        );
    }


    private Optional<PlanNode> createPlanForResourceType(
            String resourceTypeName,
            Function<ResourceType<?>, List<Resource<?>>> resourceSupplier,
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
        if (planBuilder.isEmpty()) {
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

    /**
     * Creates a new {@link TagFilter} instance based on the provided tag expression.
     *
     * @param tagExpression The tag expression used for filtering.
     * @return A new {@link TagFilter} instance.
     */
    public TagFilter createTagFilter(String tagExpression) {
        return new TagFilter(tagExpression);
    }

    /**
     * Gets the event dispatcher.
     *
     * @return The event dispatcher instance.
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Configures event observers based on the provided configuration.
     *
     * @param configuration The configuration used for configuring event observers.
     */
    public void configureEventObservers(Configuration configuration) {
        getEventDispatcher().observers()
                .forEach(observer -> contributors.configure(observer, configuration));
    }

    /**
     * Adds an event observer to the event dispatcher.
     *
     * @param observer The event observer to be added.
     */
    public void addEventDispatcherObserver(EventObserver observer) {
        getEventDispatcher().addObserver(observer);
    }

    /**
     * Removes an event observer from the event dispatcher.
     *
     * @param observer The event observer to be removed.
     */
    public void removeEventDispatcherObserver(EventObserver observer) {
        getEventDispatcher().removeObserver(observer);
    }

    /**
     * Publishes an event with the specified type and data.
     *
     * @param eventType The type of the event.
     * @param data      The data associated with the event.
     */
    public void publishEvent(String eventType, Object data) {
        getEventDispatcher().publishEvent(eventType, data);
    }

    /**
     * Executes the specified test plan using the provided configuration.
     *
     * @param plan          The test plan to execute.
     * @param configuration The configuration for plan execution.
     * @return The result of the test plan execution.
     */
    public PlanNode executePlan(PlanNode plan, Configuration configuration) {
        PlanNode result = new PlanRunner(plan, configuration).run();
        writeOutputFile(plan, configuration);
        if (configuration.get(WakamitiConfiguration.REPORT_GENERATION, Boolean.class).orElse(true)) {
            generateReports(configuration, new PlanNodeSnapshot(plan));
        }
        return result;
    }

    /**
     * Generates the report of the specified test plan using the provided configuration.
     *
     * @param plan          The test plan to execute.
     * @param configuration The configuration for plan execution.
     */
    public void generateExecutionPlan(PlanNode plan, Configuration configuration) {
        writeOutputFile(plan, configuration);
        if (configuration.get(WakamitiConfiguration.REPORT_GENERATION, Boolean.class).orElse(true)) {
            generateReports(configuration, new PlanNodeSnapshot(plan));
        }
    }

    /**
     * Creates a new {@link BackendFactory} instance using the default backend factory implementation.
     *
     * @return A new {@link BackendFactory} instance.
     */
    public BackendFactory newBackendFactory() {
        return new DefaultBackendFactory(contributors);
    }

    /**
     * Writes the output file for the specified plan and configuration.
     *
     * @param plan          The plan for which the output file is generated.
     * @param configuration The configuration used for writing the output file.
     * @return The path of the written output file or {@code null} if generation is disabled.
     */
    public Path writeOutputFile(PlanNode plan, Configuration configuration) {
        List<String> toHide = configuration.getList(WakamitiConfiguration.PROPERTIES_HIDDEN, String.class)
                .stream().map(p -> "\\$\\{" + p.trim() + "(\\.[\\w\\d-]+)*\\}")
                .collect(Collectors.toList());
        plan.resolveProperties(e -> toHide.stream().noneMatch(h -> e.getKey().trim().matches(h)));

        if (!configuration
                .get(WakamitiConfiguration.GENERATE_OUTPUT_FILE, Boolean.class)
                .orElse(Boolean.TRUE)) {
            return null;
        }

        try {

            publishEvent(Event.BEFORE_WRITE_OUTPUT_FILES, null);

            Path standardOutputFile = writeStandardOutputFile(plan, configuration);

            if (configuration.get(WakamitiConfiguration.OUTPUT_FILE_PER_TEST_CASE, Boolean.class).orElse(Boolean.FALSE)) {
                writeOutputFilesPerTestCase(plan, configuration);
            }

            publishEvent(Event.AFTER_WRITE_OUTPUT_FILES, null);

            return standardOutputFile;

        } catch (IOException e) {
            LOGGER.error(
                    "Error writing output file : {}",
                    e.getMessage(),
                    e
            );
            return null;
        }

    }

    private Path writeStandardOutputFile(PlanNode plan, Configuration configuration) throws IOException {
        String outputPath = configuration.get(WakamitiConfiguration.OUTPUT_FILE_PATH, String.class).orElseThrow();
        Path path = resourceLoader.absolutePath(
                PathUtil.replacePlaceholders(Paths.get(outputPath), plan)
        );
        Path parentPath = path.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
        try (Writer writer = new FileWriter(path.toFile())) {
            planSerializer().write(writer, plan);
            LOGGER.info("Generated result output file {uri}", path);
        }
        publishEvent(Event.STANDARD_OUTPUT_FILE_WRITTEN, path);

        return path;
    }

    private void writeOutputFilesPerTestCase(PlanNode plan, Configuration configuration) throws IOException {
        String outputPath = configuration.get(OUTPUT_FILE_PER_TEST_CASE_PATH, String.class).orElseThrow();
        Path path = resourceLoader.absolutePath(
                PathUtil.replacePlaceholders(Paths.get(outputPath), plan)
        );
        Files.createDirectories(path);

        List<PlanNode> testCases = plan
                .descendants()
                .filter(node -> node.nodeType() == NodeType.TEST_CASE)
                .collect(Collectors.toList());

        for (PlanNode testCase : testCases) {
            String testCaseId = Objects.requireNonNull(testCase.id(), "test case have no id");
            Path testCasePath = path.resolve(testCaseId + ".json");
            try (Writer writer = new FileWriter(testCasePath.toFile())) {
                planSerializer().write(writer, new PlanNodeSnapshot(testCase).withoutChildren());
                LOGGER.info("Generated result output file {uri}", testCasePath);
            }
            publishEvent(Event.TEST_CASE_OUTPUT_FILE_WRITTEN, testCasePath);
        }
    }

    /**
     * Generates reports based on the provided configuration.
     *
     * @param configuration The configuration for report generation.
     */
    public void generateReports(Configuration configuration) {
        String reportSource = configuration.get(REPORT_SOURCE, String.class)
                .orElse(configuration.get(OUTPUT_FILE_PATH, String.class).orElse(null));
        if (reportSource == null) {
            throw new WakamitiException(
                    "The report source file/folder is not defined.\n" + "Perhaps you may set the property {}",
                    REPORT_SOURCE
            );
        }
        generateReports(configuration, resourceLoader.absolutePath(Path.of(reportSource)));
    }

    /**
     * Generates reports based on the configuration and the provided report source path.
     *
     * @param configuration The configuration for generating reports.
     * @param reportSource  The path to the report source file/folder.
     * @throws WakamitiException If the report source file/folder does not exist.
     */
    public void generateReports(Configuration configuration, Path reportSource) {
        List<Reporter> reporters = contributors.reporters().collect(Collectors.toList());
        if (reporters.isEmpty()) {
            return;
        }
        LOGGER.info(IMPORTANT, "Generating reports...");

        Path sourceFolder = reportSource.toAbsolutePath();
        if (!sourceFolder.toFile().exists()) {
            throw new WakamitiException(
                    "The defined report source file/folder {} does not exist!",
                    sourceFolder
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
        generateReports(configuration, plans);

    }

    /**
     * Generates reports based on the configuration and the provided plan node snapshot.
     *
     * @param configuration The configuration for generating reports.
     * @param plan          The plan node snapshot for generating reports.
     */
    public void generateReports(Configuration configuration, PlanNodeSnapshot plan) {
        generateReports(configuration, new PlanNodeSnapshot[]{plan});
    }

    /**
     * Generates reports based on the provided configuration and plans.
     *
     * @param configuration The configuration for report generation.
     * @param plans         The plans for which reports are generated.
     */
    public void generateReports(Configuration configuration, PlanNodeSnapshot[] plans) {
        List<Reporter> reporters = contributors.reporters().collect(Collectors.toList());
        if (reporters.isEmpty()) {
            return;
        }
        LOGGER.info(IMPORTANT, "Generating reports...");
        PlanNodeSnapshot rootNode = PlanNodeSnapshot.group(plans);
        publishEvent(Event.BEFORE_WRITE_OUTPUT_FILES, null);
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
        publishEvent(Event.AFTER_WRITE_OUTPUT_FILES, null);

    }

    /**
     * Creates a Hinter instance for providing suggestions based on the configuration.
     *
     * @param configuration The configuration for Hinter creation.
     * @return A Hinter instance.
     */
    public Hinter createHinterFor(Configuration configuration) {
        var backendFactory = newBackendFactory();
        return backendFactory.createHinter(configuration);
    }


    private void validateUniqueTestCaseID(PlanNode plan, Configuration configuration) {
        if (plan == null) {
            return;
        }
        Map<String, AtomicInteger> ids = new HashMap<>();
        String idTagPattern = configuration.get(ID_TAG_PATTERN, String.class)
                .orElseThrow(() -> new WakamitiException("The '{}' property is required."));
        plan
                .descendants()
                .filter(node -> node.nodeType() == NodeType.TEST_CASE)
                .map(node -> node.id().matches(idTagPattern) ? node.id() : null)
                .forEach(id -> ids.computeIfAbsent(id, x -> new AtomicInteger()).incrementAndGet());

        if (ids.get(null) != null) {
            throw new WakamitiException("There is one or more test cases without a valid ID");
        }
        ids.forEach((id, count) -> {
            if (count.get() > 1) {
                throw new WakamitiException("The ID {} is used in {} test cases", id, count);
            }
        });

        ids.clear();

        plan.descendants()
                .filter(node -> node.properties().get("gherkinType").equals(GHERKIN_TYPE_FEATURE))
                .filter(node -> !node.id().startsWith("#"))
                .map(node -> node.id().matches(idTagPattern) ? node.id() : null)
                .forEach(id -> ids.computeIfAbsent(id, x -> new AtomicInteger()).incrementAndGet());

        if (ids.get(null) != null) {
            throw new WakamitiException("There is one or more features without a valid ID");
        }
        ids.forEach((id, count) -> {
            if (count.get() > 1) {
                throw new WakamitiException("The ID {} is used in {} features", id, count);
            }
        });
    }

}