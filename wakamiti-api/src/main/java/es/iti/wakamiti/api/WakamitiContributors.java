/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.iti.commons.jext.Extension;
import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.Contributor;
import es.iti.wakamiti.api.extensions.DataTypeContributor;
import es.iti.wakamiti.api.extensions.EventObserver;
import es.iti.wakamiti.api.extensions.LoaderContributor;
import es.iti.wakamiti.api.extensions.PlanBuilder;
import es.iti.wakamiti.api.extensions.PlanTransformer;
import es.iti.wakamiti.api.extensions.PropertyEvaluator;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.extensions.ResourceType;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.imconfig.Configurable;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationFactory;
import es.iti.wakamiti.api.util.Pair;


/**
 * Discovers, validates and configures contributor extensions.
 * <p>
 * This facade centralizes extension lookup across contributor types
 * (steps, plan builders, data types, reporters, transformers and observers),
 * applies core-version compatibility checks and wires configuration into
 * discovered contributors.
 * </p>
 * <p>
 * Instances are mutable because they keep an internal extension manager and
 * an additional list of manually supplied step contributors.
 * </p>
 */
public class WakamitiContributors {

    private static final AtomicBoolean VERSION_WARNED = new AtomicBoolean(false);
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.\\d+.*)?$");
    private final List<StepContributor> stepContributors = new LinkedList<>();
    private ExtensionManager extensionManager = new ExtensionManager();

    /**
     * Replaces the extension discovery scope with the supplied class loaders.
     * Previously discovered manager state is discarded.
     *
     * @param loaders class loaders searched for extension providers
     */
    public void setClassLoaders(
            ClassLoader... loaders
    ) {
        this.extensionManager = new ExtensionManager(loaders);
    }

    /**
     * Discovers contributors grouped by contributor contract type.
     * <p>
     * The map includes built-in extension-based contributors and any extra
     * step contributors previously added through
     * {@link #addStepContributors(List)}.
     * </p>
     *
     * @return contributor lists keyed by contributor interface
     */
    public Map<Class<?>, List<Contributor>> allContributors() {
        Class<?>[] contributorTypes = {
                ConfigContributor.class,
                DataTypeContributor.class,
                EventObserver.class,
                PlanBuilder.class,
                PlanTransformer.class,
                Reporter.class,
                ResourceType.class,
                StepContributor.class
        };
        Map<Class<?>, List<Contributor>> map = Stream.of(contributorTypes)
                .map(type -> new Pair<>(type, extensionManager.getExtensions(type)
                        .map(Contributor.class::cast)
                        .peek(this::checkVersion)
                        .collect(Collectors.toList()))
                )
                .collect(Collectors.toMap(Pair::key, Pair::value));
        map.get(StepContributor.class).addAll(stepContributors);
        return map;
    }

    /**
     * Retrieves one manually added step contributor assignable to the requested
     * type.
     *
     * @param contributorClass contributor API type to match
     * @param <T>              contributor type
     * @return first matching manually added contributor
     * @throws WakamitiException when no manually added contributor matches
     */
    public <T extends Contributor> T getContributor(
            Class<T> contributorClass
    ) {
        return stepContributors.stream()
                .filter(c -> contributorClass.isAssignableFrom(c.getClass()))
                .map(contributorClass::cast)
                .findFirst()
                .orElseThrow(() -> new WakamitiException(String.format("Contributor [%s] not found", contributorClass)));
    }

    /**
     * Registers pre-instantiated step contributors in addition to discovered
     * extensions.
     *
     * @param contributors step contributors to append
     */
    public void addStepContributors(
            List<StepContributor> contributors
    ) {
        stepContributors.addAll(contributors);
    }

    /**
     * Discovers event observers after validating their minimum core versions.
     *
     * @return a lazy stream of compatible observers
     */
    public Stream<EventObserver> eventObservers() {
        return extensionManager.getExtensions(EventObserver.class)
                .peek(this::checkVersion);
    }

    /**
     * Creates a PlanBuilder for the given ResourceType and configuration.
     *
     * @param resourceType  The ResourceType for which the PlanBuilder is created.
     * @param configuration The Configuration to be used for configuration.
     * @return Optional containing the PlanBuilder if available.
     */
    public Optional<PlanBuilder> createPlanBuilderFor(
            ResourceType<?> resourceType,
            Configuration configuration
    ) {
        Optional<PlanBuilder> planBuilder = extensionManager
                .getExtensionThatSatisfy(PlanBuilder.class, planner -> planner.acceptResourceType(resourceType));
        planBuilder.ifPresent(this::checkVersion);
        planBuilder.ifPresent(builder -> configure(builder, configuration));
        return planBuilder;
    }

    /**
     * Retrieves a Stream of available ResourceType instances.
     *
     * @return Stream of available ResourceTypes.
     */
    public Stream<ResourceType<?>> availableResourceTypes() {
        return extensionManager.getExtensions(ResourceType.class)
                .peek(this::checkVersion)
                .map(x -> (ResourceType<?>) x);
    }

    /**
     * Retrieves an optional ResourceType instance by its name.
     *
     * @param name The name of the ResourceType to retrieve.
     * @return Optional containing the ResourceType with the specified
     * name, or empty if not found.
     */
    public Optional<ResourceType<?>> resourceTypeByName(
            String name
    ) {
        return availableResourceTypes().filter(
                resourceType -> resourceType.extensionMetadata().name().equals(name)
        ).findAny();
    }

    /**
     * Retrieves a stream of DataTypeContributor instances based on the
     * specified modules.
     *
     * @param modules The list of module names.
     * @return Stream of DataTypeContributor instances satisfying the
     * specified modules.
     */
    public Stream<DataTypeContributor> dataTypeContributors(
            List<String> modules
    ) {
        Predicate<Extension> condition = extension -> modules.contains(extension.name());
        return extensionManager
                .getExtensionsThatSatisfyMetadata(DataTypeContributor.class, condition)
                .peek(this::checkVersion);
    }

    /**
     * Discovers every data-type contributor visible to the extension manager.
     *
     * @return a lazy stream of version-compatible contributors
     */
    public Stream<DataTypeContributor> allDataTypeContributors() {
        return extensionManager.getExtensions(DataTypeContributor.class)
                .peek(this::checkVersion);
    }

    /**
     * Discovers all contributors capable of loading external resources or
     * runtime components.
     *
     * @return a lazy stream of version-compatible loader contributors
     */
    public Stream<LoaderContributor> allLoaderContributors() {
        return extensionManager.getExtensions(LoaderContributor.class)
                .peek(this::checkVersion);
    }

    /**
     * Creates step contributors whose extension names are listed in
     * {@code modules}.
     * <p>
     * Each selected contributor is compatibility-checked and then configured
     * through {@link #configure(Object, Configuration)}.
     * </p>
     *
     * @param modules       extension names to load
     * @param configuration execution configuration to apply
     * @return configured step contributors matching the requested modules
     */
    public List<StepContributor> createStepContributors(
            List<String> modules,
            Configuration configuration
    ) {
        Predicate<Extension> condition = extension -> modules.contains(extension.name());
        return extensionManager
                .getExtensionsThatSatisfyMetadata(StepContributor.class, condition)
                .peek(this::checkVersion)
                .peek(c -> configure(c, configuration))
                .collect(Collectors.toList());
    }

    /**
     * Creates and configures every discoverable step contributor.
     *
     * @param configuration execution configuration to apply
     * @return configured step contributors
     */
    public List<StepContributor> createAllStepContributors(
            Configuration configuration
    ) {
        return extensionManager
                .getExtensions(StepContributor.class)
                .peek(this::checkVersion)
                .peek(c -> configure(c, configuration))
                .collect(Collectors.toList());
    }

    /**
     * Returns extension metadata for every discoverable step contributor
     * without instantiating the contributors themselves.
     *
     * @return a stream of step-contributor annotations
     */
    public Stream<Extension> allStepContributorMetadata() {
        return extensionManager.getExtensionMetadata(StepContributor.class);
    }

    /**
     * Resolves configuration contributors that accept the supplied target
     * contributor instance.
     *
     * @param contributor contributor instance to configure
     * @param <T>         contributor type
     * @return compatible configuration contributors
     */
    @SuppressWarnings("unchecked")
    public <T> Stream<ConfigContributor<T>> configuratorsFor(
            T contributor
    ) {
        return extensionManager
                .getExtensionsThatSatisfy(ConfigContributor.class, c -> c.accepts(contributor))
                .peek(this::checkVersion)
                .map(c -> (ConfigContributor<T>) c);
    }

    /**
     * Applies configuration to one contributor.
     * <p>
     * If the contributor implements {@link Configurable}, its
     * {@code configure(Configuration)} method is invoked first. Then every
     * matching {@link ConfigContributor} is applied using
     * {@code defaultConfiguration().append(configuration)}.
     * </p>
     *
     * @param contributor   contributor to configure
     * @param configuration execution configuration
     * @param <T>           contributor type
     * @return same contributor instance after configuration
     */
    public <T> T configure(
            T contributor,
            Configuration configuration
    ) {
        if (contributor instanceof Configurable configurable) {
            configurable.configure(configuration);
        }
        configuratorsFor(contributor).forEach(configurator ->
                configurator.configurer().configure(contributor, configurator.defaultConfiguration().append(configuration)));
        return contributor;
    }

    /**
     * Discovers transformations to apply to constructed execution plans.
     *
     * @return a lazy stream of version-compatible plan transformers
     */
    public Stream<PlanTransformer> planTransformers() {
        return extensionManager.getExtensions(PlanTransformer.class)
                .peek(this::checkVersion);
    }

    /**
     * Configures every discoverable property evaluator.
     *
     * @param configuration configuration passed to each evaluator
     */
    public void propertyResolvers(
            Configuration configuration
    ) {
        extensionManager.getExtensions(PropertyEvaluator.class)
                .peek(this::checkVersion)
                .forEach(c -> configure(c, configuration));
    }

    /**
     * Discovers contributors that generate reports from execution snapshots.
     *
     * @return a lazy stream of version-compatible reporters
     */
    public Stream<Reporter> reporters() {
        return extensionManager.getExtensions(Reporter.class)
                .peek(this::checkVersion);
    }

    /**
     * Returns the manager currently responsible for extension discovery.
     *
     * @return the active extension manager
     */
    public ExtensionManager extensionManager() {
        return extensionManager;
    }

    /**
     * Merges default configurations from every configuration contributor.
     * Defaults are appended in extension-discovery order.
     *
     * @return the combined global defaults, or an empty configuration when no
     * contributor supplies defaults
     */
    public Configuration globalDefaultConfiguration() {
        return extensionManager.getExtensions(ConfigContributor.class)
                .peek(this::checkVersion)
                .map(ConfigContributor::defaultConfiguration)
                .reduce(ConfigurationFactory.instance().empty(), Configuration::append);
    }

    /**
     * Checks the compatibility of a contributor's version with the core version.
     *
     * @param contributor The contributor to check.
     */
    private void checkVersion(
            Contributor contributor
    ) {
        String coreVersion = WakamitiAPI.instance().version();
        Optional<Pair<Integer, Integer>> coreVersionOptional = Optional.ofNullable(coreVersion)
                .flatMap(this::extractVersion);
        coreVersionOptional.ifPresentOrElse(core ->
                        Optional.ofNullable(contributor.extensionMetadata().version())
                                .flatMap(this::extractVersion)
                                .filter(minimalCore -> compareVersions(core, minimalCore) < 0)
                                .ifPresent(minimalCore -> {
                                    String message = String.format(
                                            "Contributor '%s' is compatible with the minimal core version %s, but it is %s",
                                            contributor.extensionMetadata().name(),
                                            formatVersion(minimalCore),
                                            formatVersion(core));
                                    throw new UnsupportedClassVersionError(message);
                                }),
                () -> {
                    if (!VERSION_WARNED.getAndSet(true)) {
                        System.err.println("WARNING: Core version is not in correct format: " + coreVersion);
                    }
                }
        );
    }

    private Optional<Pair<Integer, Integer>> extractVersion(
            String version
    ) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            return Optional.of(new Pair<>(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2))
            ));
        }
        return Optional.empty();
    }

    private int compareVersions(
            Pair<Integer, Integer> left,
            Pair<Integer, Integer> right
    ) {
        int majorComparison = Integer.compare(left.key(), right.key());
        if (majorComparison != 0) {
            return majorComparison;
        }
        return Integer.compare(left.value(), right.value());
    }

    private String formatVersion(
            Pair<Integer, Integer> version
    ) {
        return version.key() + "." + version.value();
    }

}
