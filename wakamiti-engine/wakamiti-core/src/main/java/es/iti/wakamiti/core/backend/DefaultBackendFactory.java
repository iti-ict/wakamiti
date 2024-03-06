/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.Contributor;
import es.iti.wakamiti.api.extensions.DataTypeContributor;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.util.ThrowableRunnable;
import es.iti.wakamiti.core.Wakamiti;
import imconfig.Configuration;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Default implementation of the BackendFactory interface for creating backend instances.
 * This factory is responsible for creating backends based on test cases, configuration, and contributors.
 * It manages the loading of step contributors and data type contributors to support the creation of runnable steps.
 * The factory supports the creation of both RunnableBackend for executing test cases and NonRunnableBackend
 * for scenarios that don't involve test execution.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class DefaultBackendFactory implements BackendFactory {

    private static final Logger LOGGER = Wakamiti.LOGGER;
    private static final List<String> DEFAULT_MODULES = List.of("core-types", "assertion-types");

    private final WakamitiContributors contributors;

    public DefaultBackendFactory(WakamitiContributors contributors) {
        this.contributors = contributors;
    }

    /**
     * Creates a backend based on the provided test case and configuration.
     *
     * @param testCase      The plan node representing the test case.
     * @param configuration The configuration for the backend.
     * @return A Backend instance, either RunnableBackend or NonRunnableBackend.
     * @throws IllegalArgumentException If the provided plan node is not of type TEST_CASE.
     * @see RunnableBackend
     * @see NonRunnableBackend
     */
    @Override
    public Backend createBackend(PlanNode testCase, Configuration configuration) {
        if (testCase.nodeType() != NodeType.TEST_CASE) {
            throw new IllegalArgumentException("Plan node must be of type TEST_CASE");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Creating backend for Test Case {}::'{}'",
                    testCase.source(),
                    testCase.displayName()
            );
        }
        return doCreateBackend(testCase, configuration);
    }

    /**
     * Creates a non-runnable backend based on the provided configuration.
     *
     * @param configuration The configuration for the backend.
     * @return A NonRunnableBackend instance.
     * @see NonRunnableBackend
     */
    @Override
    public Backend createNonRunnableBackend(Configuration configuration) {
        return doCreateBackend(null, configuration);
    }

    /**
     * Creates a backend based on the provided test case and configuration.
     *
     * @param testCase      The test case for which the backend is created.
     * @param configuration The configuration for the backend.
     * @return A Backend instance (either RunnableBackend or NonRunnableBackend).
     * @see RunnableBackend
     * @see NonRunnableBackend
     */
    private Backend doCreateBackend(PlanNode testCase, Configuration configuration) {
        boolean runnableBackend = (testCase != null);

        List<String> restrictedModules = new ArrayList<>(
                configuration.getList(WakamitiConfiguration.MODULES, String.class)
        ).stream().flatMap(it -> Stream.of(it.split(",")))
                .map(String::strip)
                .collect(Collectors.toList());

        if (testCase != null) {
            configuration = Configuration.factory()
                    .merge(configuration, Configuration.factory().fromMap(testCase.properties()));
        }

        List<StepContributor> stepContributors = createStepContributors(
                restrictedModules,
                configuration,
                !runnableBackend
        );

        Stream<DataTypeContributor> dataTypeContributors = resolveDataTypeContributors(
                restrictedModules
        );

        WakamitiDataTypeRegistry typeRegistry = loadTypes(dataTypeContributors);
        List<RunnableStep> steps = createSteps(stepContributors, typeRegistry);
        Clock clock = Clock.systemUTC();
        if (runnableBackend) {
            return new RunnableBackend(
                    testCase,
                    configuration,
                    typeRegistry,
                    steps,
                    getSetUpOperations(stepContributors),
                    getTearDownOperations(stepContributors),
                    clock
            );
        } else {
            return new NonRunnableBackend(configuration, typeRegistry, steps);
        }
    }

    /**
     * Retrieves a list of setup operations from the provided StepContributors.
     *
     * @param stepContributors List of StepContributors to retrieve setup operations from.
     * @return List of setup operations represented as ThrowableRunnable.
     * @see SetUp
     * @see ThrowableRunnable
     */
    private List<ThrowableRunnable> getSetUpOperations(List<StepContributor> stepContributors) {
        return loadMethods(stepContributors, SetUp.class, SetUp::order);
    }

    /**
     * Retrieves a list of teardown operations from the provided StepContributors.
     *
     * @param stepContributors List of StepContributors to retrieve teardown operations from.
     * @return List of teardown operations represented as ThrowableRunnable.
     * @see TearDown
     * @see ThrowableRunnable
     */
    private List<ThrowableRunnable> getTearDownOperations(List<StepContributor> stepContributors) {
        return loadMethods(stepContributors, TearDown.class, TearDown::order);
    }

    /**
     * Creates a list of StepContributors based on the provided configuration and module restrictions.
     *
     * @param restrictedModules List of module names to restrict the creation of StepContributors.
     * @param configuration     Configuration object used to configure StepContributors.
     * @param allowEmptySteps   Boolean flag indicating whether empty step contributors are allowed.
     * @return List of StepContributors created based on the specified conditions.
     * @throws WakamitiException If no step contributors are found and allowEmptySteps is false.
     * @see StepContributor
     * @see WakamitiException
     */
    protected List<StepContributor> createStepContributors(
            List<String> restrictedModules,
            Configuration configuration,
            boolean allowEmptySteps
    ) {

        List<StepContributor> stepContributors = new ArrayList<>();
        if (restrictedModules.isEmpty()) {
            stepContributors.addAll(contributors.createAllStepContributors(configuration));
        } else {
            List<String> modules = new ArrayList<>(restrictedModules);
            modules.addAll(DEFAULT_MODULES);
            stepContributors.addAll(contributors.createStepContributors(modules, configuration));
        }

        List<String> nonRegisteredContributorClasses = configuration
                .getList(WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS, String.class);
        List<StepContributor> nonRegisteredContributors = resolveNonRegisteredContributors(
                nonRegisteredContributorClasses,
                configuration
        );

        stepContributors.addAll(nonRegisteredContributors);

        if (stepContributors.isEmpty() && !allowEmptySteps) {
            logTipForNoStepContributors(restrictedModules);
            throw new WakamitiException("Cannot build backend without step contributors");
        }

        Wakamiti.contributors().propertyResolvers(configuration);

        Wakamiti.contributors().addStepContributors(stepContributors);
        return stepContributors;
    }

    /**
     * Resolves DataTypeContributors based on the provided configuration and module restrictions.
     * If no modules are restricted, it retrieves all DataTypeContributors from the global contributors.
     * If modules are restricted, it retrieves DataTypeContributors only for the specified modules.
     *
     * @param restrictedModules List of module names to restrict the retrieval of DataTypeContributors.
     * @return Stream of DataTypeContributors based on the specified conditions.
     * @see DataTypeContributor
     */
    protected Stream<DataTypeContributor> resolveDataTypeContributors(
            List<String> restrictedModules
    ) {
        if (restrictedModules.isEmpty()) {
            return contributors.allDataTypeContributors();
        } else {
            List<String> modules = new ArrayList<>(restrictedModules);
            modules.addAll(DEFAULT_MODULES);
            return contributors.dataTypeContributors(modules);
        }
    }

    /**
     * Logs an error message providing tips when no step contributors are found.
     * If no modules are restricted, it suggests declaring step modules with the
     * property '{@code WakamitiConfiguration.MODULES}' or non-registered step
     * provider classes with the property '{@code WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS'}.
     * If modules are restricted, it suggests checking the spelling of the specified modules.
     *
     * @param restrictedModules List of module names used to restrict the retrieval of step contributors.
     * @see WakamitiConfiguration#MODULES
     * @see WakamitiConfiguration#NON_REGISTERED_STEP_PROVIDERS
     * @see Extension
     */
    protected void logTipForNoStepContributors(List<String> restrictedModules) {
        if (restrictedModules.isEmpty()) {
            LOGGER.error(
                    "No step contributors found. You must either declare step modules with "
                            + "property '{}' or non-registered step provider classes with property '{}'",
                    WakamitiConfiguration.MODULES,
                    WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS
            );
        } else {
            String availableStepContributors = contributors.allStepContributorMetadata()
                    .map(Extension::name)
                    .collect(Collectors.joining("\n\t"));
            if (availableStepContributors.isEmpty()) {
                LOGGER.error(
                        "No step contributors found. You must include at least one Wakamiti plugin "
                                + "with a StepContributor in the classpath"
                );
            } else {
                LOGGER.error(
                        "No step contributors found for the modules {}, please check the spelling.\n"
                                + "The available step contributors are:\n\t{}",
                        restrictedModules,
                        availableStepContributors
                );
            }
        }
    }

    /**
     * Loads methods annotated with a specific annotation from a list of StepContributors.
     * Creates a mapping of methods to their corresponding annotation instances, sorts them based on the provided order,
     * and returns a list of ThrowableRunnable instances that can invoke the methods.
     *
     * @param <A>              The type of the annotation.
     * @param stepContributors List of StepContributors to inspect for annotated methods.
     * @param annotation       The annotation class to search for on the methods.
     * @param orderGetter      Function to extract the order value from the annotation.
     * @return List of ThrowableRunnable instances representing annotated methods, sorted by the specified order.
     * @see StepContributor
     * @see Annotation
     * @see ThrowableRunnable
     */
    private <A extends Annotation> List<ThrowableRunnable> loadMethods(
            List<StepContributor> stepContributors,
            Class<A> annotation,
            ToIntFunction<A> orderGetter
    ) {
        LinkedHashMap<ThrowableRunnable, A> runnable = new LinkedHashMap<>();
        for (StepContributor stepContributor : stepContributors) {
            for (Method method : stepContributor.getClass().getMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    runnable.put(
                            args -> method.invoke(stepContributor),
                            method.getAnnotation(annotation)
                    );
                }
            }
        }

        Comparator<? super Entry<ThrowableRunnable, A>> sorter = Comparator
                .comparingInt(e -> orderGetter.applyAsInt(e.getValue()));

        return runnable.entrySet().stream()
                .sorted(sorter)
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Loads WakamitiDataType instances from a stream of DataTypeContributors.
     * Constructs a registry of WakamitiDataTypes, logs information about the
     * contributed types, and handles type overrides.
     *
     * @param contributors Stream of DataTypeContributors providing WakamitiDataType instances.
     * @return WakamitiDataTypeRegistry containing the loaded WakamitiDataTypes.
     * @see DataTypeContributor
     * @see WakamitiDataTypeRegistry
     */
    protected WakamitiDataTypeRegistry loadTypes(Stream<DataTypeContributor> contributors) {

        Map<String, WakamitiDataType<?>> types = new HashMap<>();
        contributors.forEach(contributor -> {
            for (WakamitiDataType<?> type : contributor.contributeTypes()) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(
                            "using type {resourceType}({}) provided by {contributor}",
                            type.getName(),
                            type.getJavaType(),
                            contributor.info()
                    );
                }
                WakamitiDataType<?> replacedType = types.put(type.getName(), type);
                if (replacedType != null && LOGGER.isDebugEnabled()) {
                    LOGGER.warn(
                            "Module {contributor} overrides type {resourceType}",
                            contributor.info(),
                            replacedType.getName()
                    );
                }
            }
        });
        return new WakamitiDataTypeRegistry(types);
    }

    /**
     * Creates RunnableStep instances by processing StepContributors.
     * Iterates through the given list of StepContributors, creating RunnableStep
     * instances for each step defined by the contributors.
     *
     * @param stepContributors List of StepContributors providing step definitions.
     * @param typeRegistry     WakamitiDataTypeRegistry containing information about
     *                         registered data types.
     * @return List of RunnableStep instances created from the stepContributors.
     * @see RunnableStep
     * @see StepContributor
     * @see WakamitiDataTypeRegistry
     */
    protected List<RunnableStep> createSteps(
            List<StepContributor> stepContributors,
            WakamitiDataTypeRegistry typeRegistry
    ) {
        ArrayList<RunnableStep> resultSteps = new ArrayList<>();
        for (Object stepContributor : stepContributors) {
            createContributorSteps(resultSteps, stepContributor, typeRegistry);
        }
        return resultSteps;
    }

    /**
     * Resolves and creates instances of non-registered StepContributors based on the provided class names.
     * Iterates through the list of non-registered step provider class names, loads the classes, and creates
     * instances of StepContributors.
     *
     * @param nonRegisteredContributorClasses List of non-registered step provider class names.
     * @param configuration                   Configuration object providing additional settings.
     * @return List of StepContributor instances created from the non-registered step provider classes.
     * @see StepContributor
     * @see Configuration
     */
    protected List<StepContributor> resolveNonRegisteredContributors(
            List<String> nonRegisteredContributorClasses,
            Configuration configuration
    ) {
        List<StepContributor> nonRegisteredContributors = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String nonRegisteredContributorClass : nonRegisteredContributorClasses) {
            try {
                Object newStepContributor = classLoader.loadClass(nonRegisteredContributorClass)
                        .getConstructor()
                        .newInstance();
                if (newStepContributor instanceof StepContributor) {
                    contributors.configure(newStepContributor, configuration);
                    nonRegisteredContributors.add((StepContributor) newStepContributor);
                } else {
                    LOGGER.warn(
                            "Class {} does not implement {}",
                            nonRegisteredContributorClass,
                            StepContributor.class
                    );
                }
            } catch (ClassNotFoundException e) {
                LOGGER.warn(
                        "Cannot find non-registered step provider class: {}\n\tEnsure the class "
                                + "exists, is fully qualified, and its accessible from the main class loader",
                        nonRegisteredContributorClass
                );
            } catch (NoSuchMethodException e) {
                LOGGER.warn(
                        "Non-registered step provider class {} requires empty constructor",
                        nonRegisteredContributorClass
                );
            } catch (ReflectiveOperationException e) {
                LOGGER.warn(
                        "Error loading non-registered step provider class {}: {}",
                        nonRegisteredContributorClass,
                        e.toString()
                );
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("<exception stack trace>", e);
                }
            }
        }
        return nonRegisteredContributors;
    }

    /**
     * Creates RunnableStep instances for each method annotated with @Step in the given stepProvider.
     * Iterates through the methods of the stepProvider, checks for the presence of @Step annotation,
     * and creates a RunnableStep for each annotated method.
     *
     * @param output       List to store the created RunnableStep instances.
     * @param stepProvider The object providing steps, which can be a StepContributor or any other object.
     * @param typeRegistry WakamitiDataTypeRegistry for handling data types.
     * @see RunnableStep
     * @see Step
     * @see StepContributor
     * @see WakamitiDataTypeRegistry
     */
    protected void createContributorSteps(
            List<RunnableStep> output,
            Object stepProvider,
            WakamitiDataTypeRegistry typeRegistry
    ) {
        String stepProviderName = (stepProvider instanceof Contributor) ?
                ((Contributor) stepProvider).info() :
                stepProvider.getClass().getCanonicalName();

        for (Method method : stepProvider.getClass().getMethods()) {
            if (method.isAnnotationPresent(Step.class)) {
                Step stepAnnotation = method.getAnnotation(Step.class);
                String stepClassifier = stepAnnotation.classifier();
                String info = (stepClassifier.isBlank() ? stepProviderName : stepClassifier);
                try {
                    RunnableStep step = createRunnableStep(stepProvider, method, typeRegistry, info);
                    output.add(step);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "using step <{}@{}::'{}' {}>",
                                stepProvider.getClass().getSimpleName(),
                                Integer.toHexString(stepProvider.hashCode()),
                                step.getDefinitionKey(),
                                step.getArguments()
                        );
                    }
                } catch (Exception e) {
                    throw new WakamitiException(e);
                }
            }
        }
    }

    /**
     * Creates a RunnableStep instance for the given method annotated with @Step in the provided runnableObject.
     * Validates the annotations and types of the method, and constructs a RunnableStep accordingly.
     *
     * @param runnableObject The object providing the step method.
     * @param runnableMethod The method annotated with @Step.
     * @param typeRegistry   WakamitiDataTypeRegistry for handling data types.
     * @param stepProvider   The identifier of the step provider.
     * @return A RunnableStep instance representing the annotated step method.
     * @throws WakamitiException If there is an issue with annotations or types during the creation of the RunnableStep.
     */
    protected RunnableStep createRunnableStep(
            Object runnableObject,
            Method runnableMethod,
            WakamitiDataTypeRegistry typeRegistry,
            String stepProvider
    ) {
        Class<?> stepContributorClass = runnableObject.getClass();
        I18nResource stepDefinitionFile = stepContributorClass.getAnnotation(I18nResource.class);
        if (stepDefinitionFile == null) {
            throw new WakamitiException(
                    "Class {} must be annotated with {}",
                    runnableObject.getClass().getCanonicalName(),
                    I18nResource.class.getCanonicalName()
            );
        }
        final Step stepDefinition = runnableMethod.getAnnotation(Step.class);
        if (stepDefinition == null) {
            throw new WakamitiException(
                    "Method {}::{} must be annotated with {}",
                    runnableObject.getClass().getCanonicalName(),
                    runnableMethod.getName(),
                    Step.class.getCanonicalName()
            );
        }
        for (Class<?> methodArgumentType : runnableMethod.getParameterTypes()) {
            if (methodArgumentType.isPrimitive()) {
                throw new WakamitiException(
                        "Method {}::{} must not use primitive argument type {}; " + "use equivalent boxed type",
                        runnableObject.getClass().getCanonicalName(),
                        runnableMethod.getName(),
                        methodArgumentType.getName()
                );
            }
        }
        return new RunnableStep(
                stepDefinitionFile.value(),
                stepDefinition.value(),
                new BackendArguments(runnableObject.getClass(), runnableMethod, typeRegistry),
                (args -> runnableMethod.invoke(runnableObject, args)),
                stepProvider
        );
    }

    /**
     * Creates a Hinter instance for providing auto-completion suggestions during
     * the development of Wakamiti test plans.
     *
     * @param configuration The configuration specifying the modules and settings for the Hinter.
     * @return A Hinter instance for auto-completion suggestions.
     */
    @Override
    public Hinter createHinter(Configuration configuration) {
        List<String> restrictedModules = new ArrayList<>(
                configuration.getList(WakamitiConfiguration.MODULES, String.class)
        );
        List<StepContributor> stepContributors = createStepContributors(
                restrictedModules,
                configuration,
                true
        );
        var dataTypeContributors = resolveDataTypeContributors(restrictedModules);
        var typeRegistry = loadTypes(dataTypeContributors);
        List<RunnableStep> steps = createSteps(stepContributors, typeRegistry);
        var stepResolver = new RunnableStepResolver(typeRegistry, steps);
        return new StepHinter(
                steps,
                configuration,
                stepResolver,
                typeRegistry
        );
    }

}