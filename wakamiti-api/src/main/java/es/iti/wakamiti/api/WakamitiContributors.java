/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import es.iti.commons.jext.Extension;
import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.extensions.*;
import es.iti.wakamiti.api.util.Pair;
import imconfig.Configurable;
import imconfig.Configuration;
import imconfig.ConfigurationFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Manages contributors and extension points in Wakamiti API.
 * It handles various types of contributors, such as StepContributors, PlanBuilders, etc.
 * Provides methods to retrieve contributors, create instances, and perform configuration.
 * Acts as a central manager for contributors in Wakamiti API.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public class WakamitiContributors {

    private final List<StepContributor> stepContributors = new LinkedList<>();
    private ExtensionManager extensionManager = new ExtensionManager();

    public void setClassLoaders(ClassLoader... loaders) {
        this.extensionManager = new ExtensionManager(loaders);
    }

    /**
     * Retrieves all contributors of a specific type.
     *
     * @return A map containing contributor types and their
     * corresponding contributors.
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
     * Retrieves a contributor of a specific type.
     *
     * @param contributorClass The class of the contributor to retrieve.
     * @param <T>              The type of the contributor.
     * @return The contributor of the specified type.
     * @throws WakamitiException If the contributor is not found.
     */
    public <T extends Contributor> T getContributor(Class<T> contributorClass) {
        return stepContributors.stream()
                .filter(c -> contributorClass.isAssignableFrom(c.getClass()))
                .map(contributorClass::cast)
                .findFirst()
                .orElseThrow(() -> new WakamitiException(String.format("Contributor [%s] not found", contributorClass)));
    }

    /**
     * Adds StepContributors to the list.
     *
     * @param contributors List of StepContributors to add.
     */
    public void addStepContributors(List<StepContributor> contributors) {
        stepContributors.addAll(contributors);
    }

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
    public Optional<ResourceType<?>> resourceTypeByName(String name) {
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
    public Stream<DataTypeContributor> dataTypeContributors(List<String> modules) {
        Predicate<Extension> condition = extension -> modules.contains(extension.name());
        return extensionManager
                .getExtensionsThatSatisfyMetadata(DataTypeContributor.class, condition)
                .peek(this::checkVersion);
    }

    public Stream<DataTypeContributor> allDataTypeContributors() {
        return extensionManager.getExtensions(DataTypeContributor.class)
                .peek(this::checkVersion);
    }

    public Stream<LoaderContributor> allLoaderContributors() {
        return extensionManager.getExtensions(LoaderContributor.class)
                .peek(this::checkVersion);
    }

    /**
     * Creates a list of StepContributor instances based on the specified
     * modules and configuration.
     *
     * @param modules       List of module names.
     * @param configuration Configuration to be applied.
     * @return List of StepContributor instances.
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
     * Creates a list of all StepContributor instances with the specified
     * configuration.
     *
     * @param configuration Configuration to be applied.
     * @return List of StepContributor instances.
     */
    public List<StepContributor> createAllStepContributors(Configuration configuration) {
        return extensionManager
                .getExtensions(StepContributor.class)
                .peek(this::checkVersion)
                .peek(c -> configure(c, configuration))
                .collect(Collectors.toList());
    }

    public Stream<Extension> allStepContributorMetadata() {
        return extensionManager.getExtensionMetadata(StepContributor.class);
    }

    /**
     * Retrieves the configuration contributors for a specific contributor
     * type.
     *
     * @param <T>         The contributor type.
     * @param contributor The contributor instance.
     * @return Stream of ConfigContributor instances for the given
     * contributor type.
     */
    @SuppressWarnings("unchecked")
    public <T> Stream<ConfigContributor<T>> configuratorsFor(T contributor) {
        return extensionManager
                .getExtensionsThatSatisfy(ConfigContributor.class, c -> c.accepts(contributor))
                .peek(this::checkVersion)
                .map(c -> (ConfigContributor<T>) c);
    }

    /**
     * Configures a contributor using the provided configuration.
     *
     * @param contributor   The contributor to configure.
     * @param configuration The configuration to apply.
     * @param <T>           The type of the contributor.
     * @return The configured contributor.
     */
    public <T> T configure(T contributor, Configuration configuration) {
        if (contributor instanceof Configurable) {
            ((Configurable) contributor).configure(configuration);
        }
        configuratorsFor(contributor).forEach(configurator -> {
            configurator.configurer().configure(contributor, configurator.defaultConfiguration().append(configuration));
        });
        return contributor;
    }

    public Stream<PlanTransformer> planTransformers() {
        return extensionManager.getExtensions(PlanTransformer.class)
                .peek(this::checkVersion);
    }

    /**
     * Configures property resolvers with the provided configuration.
     *
     * @param configuration The configuration to use for property resolvers.
     */
    public void propertyResolvers(Configuration configuration) {
        extensionManager.getExtensions(PropertyEvaluator.class)
                .peek(this::checkVersion)
                .forEach(c -> configure(c, configuration));
    }

    public Stream<Reporter> reporters() {
        return extensionManager.getExtensions(Reporter.class)
                .peek(this::checkVersion);
    }

    public ExtensionManager extensionManager() {
        return extensionManager;
    }

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
    private void checkVersion(Contributor contributor) {
        String regex = "^(\\d+\\.\\d+)(\\.\\d+.*)?$";
        Optional<Double> coreVersionOptional = Optional.ofNullable(WakamitiAPI.instance().version())
                .map(version -> version.replaceAll(regex, "$1"))
                .map(Double::valueOf);
        coreVersionOptional.ifPresent(coreVersion ->
                Optional.ofNullable(contributor.extensionMetadata().version())
                        .map(version -> version.replaceAll(regex, "$1"))
                        .map(Double::valueOf)
                        .filter(version -> coreVersion < version)
                        .ifPresent(version -> {
                            String message = String.format(
                                    "Contributor '%s' is compatible with the minimal core version %s, but it is %s",
                                    contributor.extensionMetadata().name(), version, coreVersion);
                            throw new UnsupportedClassVersionError(message);
                        })
        );
    }

}