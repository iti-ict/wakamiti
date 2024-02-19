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
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
public class WakamitiContributors {

    private ExtensionManager extensionManager = new ExtensionManager();

    public void setClassLoaders(ClassLoader... loaders) {
        this.extensionManager = new ExtensionManager(loaders);
    }

    private final List<StepContributor> stepContributors = new LinkedList<>();

    public Map<Class<?>,List<Contributor>> allContributors() {
        Class<?>[] contributorTypes = {
                ConfigContributor.class,
                DataTypeContributor.class,
                EventObserver.class,
                PlanBuilder.class,
                PlanTransformer.class,
                Reporter.class,
                ResourceType.class,
                StepContributor.class
        } ;
        Map<Class<?>,List<Contributor>> map = Stream.of(contributorTypes)
            .map(type -> new Pair<>(type, extensionManager.getExtensions(type)
                    .map(Contributor.class::cast)
                    .peek(this::checkVersion)
                    .collect(Collectors.toList()))
            )
            .collect(Collectors.toMap(Pair::key,Pair::value));
        map.get(StepContributor.class).addAll(stepContributors);
        return map;
    }

    public <T extends Contributor> T getContributor(Class<T> contributorClass) {
        return stepContributors.stream()
                .filter(c -> contributorClass.isAssignableFrom(c.getClass()))
                .map(contributorClass::cast)
                .findFirst()
                .orElseThrow(() -> new WakamitiException(String.format("Contributor [%s] not found", contributorClass)));
    }

    public void addStepContributors(List<StepContributor> contributor) {
        stepContributors.addAll(contributor);
    }

    public Stream<EventObserver> eventObservers() {
        return extensionManager.getExtensions(EventObserver.class)
                .peek(this::checkVersion);
    }


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
     * @return A list of all available resource types provided by contributors
     */
    public Stream<ResourceType<?>> availableResourceTypes() {
        return extensionManager.getExtensions(ResourceType.class)
                .peek(this::checkVersion)
                .map(x -> (ResourceType<?>) x);
    }


    public Optional<ResourceType<?>> resourceTypeByName(String name) {
        return availableResourceTypes().filter(
            resourceType -> resourceType.extensionMetadata().name().equals(name)
        ).findAny();
    }


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


    @SuppressWarnings("unchecked")
    public <T> Stream<ConfigContributor<T>> configuratorsFor(T contributor) {
        return extensionManager
            .getExtensionsThatSatisfy(ConfigContributor.class, c -> c.accepts(contributor))
            .peek(this::checkVersion)
            .map(c -> (ConfigContributor<T>) c);
    }



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

    private void checkVersion(Contributor contributor) {
        String regex = "^(\\d+\\.\\d+)(\\.\\d+.*)?$";
        double coreVersion = Optional.of(WakamitiAPI.instance().version())
                .map(version -> version.replaceAll(regex, "$1"))
                .map(Double::valueOf)
                .get();
        Optional.ofNullable(contributor.extensionMetadata().version())
                .map(version -> version.replaceAll(regex, "$1"))
                .map(Double::valueOf)
                .filter(version -> coreVersion < version)
                .ifPresent(version -> {
                    String message = String.format(
                            "Contributor '%s' is compatible with the minimal core version %s, but it is %s",
                            contributor.extensionMetadata().name(), version, coreVersion);
                    throw new UnsupportedClassVersionError(message);
                });
    }

}