package iti.kukumo.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.commons.jext.ExtensionManager;
import iti.kukumo.api.extensions.Configurable;
import iti.kukumo.api.extensions.Configurator;
import iti.kukumo.api.extensions.DataTypeContributor;
import iti.kukumo.api.extensions.EventObserver;
import iti.kukumo.api.extensions.PlanBuilder;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.api.extensions.Reporter;
import iti.kukumo.api.extensions.ResourceType;
import iti.kukumo.api.extensions.StepContributor;

public class KukumoContributors {

    private final ExtensionManager extensionManager = new ExtensionManager();

    public Stream<EventObserver> eventObservers() {
        return extensionManager.getExtensions(EventObserver.class);
    }

    public Optional<PlanBuilder> createPlanBuilderFor(ResourceType<?> resourceType, Configuration configuration) {
        Predicate<PlanBuilder> filter = planner->planner.acceptResourceType(resourceType);
        Optional<PlanBuilder> planBuilder
            = extensionManager.getExtensionThatSatisfy(PlanBuilder.class, filter);
        if (planBuilder.isPresent()) {
            configure(planBuilder.get(),configuration);
        }
        return planBuilder;
    }


    /**
     * @return A list of all available resource types provided by contributors
     */
    public Stream<ResourceType<?>> availableResourceTypes() {
        return extensionManager.getExtensions(ResourceType.class).map(x->(ResourceType<?>)x);
    }


    public Optional<ResourceType<?>> resourceTypeByName(String name) {
        return availableResourceTypes().filter(
                resourceType -> resourceType.extensionMetadata().name().equals(name)
        ).findAny();
    }


    public Stream<DataTypeContributor> dataTypeContributors(List<String> modules) {
        Predicate<Extension> condition = extension -> modules.contains(extension.name());
        return extensionManager
            .getExtensionsThatSatisfyMetadata(DataTypeContributor.class, condition);
    }



    public Stream<DataTypeContributor> allDataTypeContributors() {
        return extensionManager.getExtensions(DataTypeContributor.class);
    }



    public List<StepContributor> createStepContributors(
        List<String> modules,
        Configuration configuration
    ) {
        Predicate<Extension> condition = extension -> modules.contains(extension.name());
        return extensionManager
            .getExtensionsThatSatisfyMetadata(StepContributor.class,condition)
            .peek(c->configure(c,configuration))
            .collect(Collectors.toList());
    }



    public List<StepContributor> createAllStepContributors(Configuration configuration) {
        return extensionManager
            .getExtensions(StepContributor.class)
            .peek(c->configure(c,configuration))
            .collect(Collectors.toList());
    }


    public Stream<Extension> allStepContributorMetadata() {
        return extensionManager.getExtensionMetadata(StepContributor.class);
    }



    @SuppressWarnings("unchecked")
    public <T> Stream<Configurator<T>> configuratorsFor(T contributor) {
        return extensionManager
            .getExtensionsThatSatisfy(Configurator.class, c->c.accepts(contributor))
            .map(c->(Configurator<T>) c);
    }


    public <T> T configure(T contributor, Configuration configuration) {
        if (contributor instanceof Configurable) {
            ((Configurable)contributor).configure(configuration);
        }
        configuratorsFor(contributor).forEach(
            configurator -> configurator.configure(contributor, configuration)
        );
        return contributor;
    }


    public Stream<PlanTransformer> planTransformers() {
        return extensionManager.getExtensions(PlanTransformer.class);
    }


    public Stream<Reporter> reporters() {
        return extensionManager.getExtensions(Reporter.class);
    }


    public ExtensionManager extensionManager() {
        return extensionManager;
    }

}
