package iti.kukumo.core.backend;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.Backend;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoContributors;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.SetUp;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.annotations.TearDown;
import iti.kukumo.api.extensions.DataTypeContributor;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.util.ThrowableRunnable;

public class DefaultBackendFactory implements BackendFactory {

    private static final Logger LOGGER = Kukumo.LOGGER;
    private static final List<String> DEFAULT_MODULES = Collections.unmodifiableList(Arrays.asList(
        "core-types",
        "assertion-types"
    ));
    private static KukumoContributors kukumo = Kukumo.instance().contributors();




    @Override
    public Backend createBackend(PlanNode node, Configuration configuration) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "Creating backend for Test Case {}::'{}'",
                node.source(),
                node.displayName()
               );
        }
        return createBackend(configuration.appendFromMap(node.properties()));
    }




    protected Backend createBackend(Configuration configuration) {

        List<String> restrictedModules = new ArrayList<>(
            configuration.getList(KukumoConfiguration.MODULES,String.class)
        );
        List<StepContributor> stepContributors =
            createStepContributors(restrictedModules,configuration);

        Stream<DataTypeContributor> dataTypeContributors =
            resolveDataTypeContributors(restrictedModules);

        KukumoDataTypeRegistry typeRegistry = loadTypes(dataTypeContributors);
        List<RunnableStep> steps = createSteps(stepContributors, typeRegistry);
        Clock clock = Clock.systemUTC();
        return new DefaultBackend(
            configuration,
            typeRegistry,
            steps,
            getSetUpOperations(stepContributors),
            getTearDownOperations(stepContributors),
            clock
        );
    }


    private List<ThrowableRunnable> getSetUpOperations(List<StepContributor> stepContributors) {
        return loadMethods(stepContributors, SetUp.class, SetUp::order);
    }

    private List<ThrowableRunnable> getTearDownOperations(List<StepContributor> stepContributors) {
        return loadMethods(stepContributors, TearDown.class, TearDown::order);
    }



    protected List<StepContributor> createStepContributors(
        List<String> restrictedModules,
        Configuration configuration
    ) {

        List<StepContributor> stepContributors = new ArrayList<>();
        if (restrictedModules.isEmpty()) {
            stepContributors.addAll( kukumo.createAllStepContributors(configuration) );
        } else {
            List<String> modules = new ArrayList<>(restrictedModules);
            modules.addAll(DEFAULT_MODULES);
            stepContributors.addAll( kukumo.createStepContributors(modules,configuration) );
        }

        List<String> nonRegisteredContributorClasses
            = configuration.getList(KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS,String.class);
        List<StepContributor> nonRegisteredContributors
            = resolveNonRegisteredContributors(nonRegisteredContributorClasses,configuration);

        stepContributors.addAll(nonRegisteredContributors);

        if (stepContributors.isEmpty()) {
            logTipForNoStepContributors(restrictedModules);
            throw new KukumoException("Cannot build backend without step contributors");
        }

        stepContributors.forEach(stepContributor->kukumo.configure(stepContributor,configuration));
        return stepContributors;
    }




    protected Stream<DataTypeContributor> resolveDataTypeContributors(
        List<String> restrictedModules
    ) {
        if (restrictedModules.isEmpty()) {
            return kukumo.allDataTypeContributors();
        } else {
            List<String> modules = new ArrayList<>(restrictedModules);
            modules.addAll(DEFAULT_MODULES);
            return kukumo.dataTypeContributors(modules);
        }
    }



    protected void logTipForNoStepContributors(List<String> restrictedModules) {
        if (restrictedModules.isEmpty()) {
            LOGGER.error(
               "No step contributors found. You must either declare step modules with "+
               "property '{}' or non-registered step provider classes with property '{}'",
                KukumoConfiguration.MODULES,
                KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS
            );
        } else {
            String availableStepContributors = kukumo.allStepContributorMetadata()
                .map(Extension::name)
                .collect(Collectors.joining("\n\t"))
            ;
            if (availableStepContributors.isEmpty()) {
                LOGGER.error(
                    "No step contributors found. You must include at least one Kukumo plugin "+
                    "with a StepContributor in the classpath"
                );
            } else {
                LOGGER.error(
                    "No step contributors found for the modules {}, please check the spelling.\n"+
                    "The available step contributors are:\n\t{}",
                    restrictedModules,
                    availableStepContributors
                );
            }
        }
    }





    private <A extends Annotation> List<ThrowableRunnable> loadMethods(
        List<StepContributor> stepContributors,
        Class<A> annotation,
        ToIntFunction<A> orderGetter
    ) {
        LinkedHashMap<ThrowableRunnable,A> runnables = new LinkedHashMap<>();
        for (StepContributor stepContributor : stepContributors) {
            for (Method method : stepContributor.getClass().getMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    runnables.put(
                        args -> method.invoke(stepContributor),
                        method.getAnnotation(annotation)
                    );
                }
            }
        }

        Comparator<? super Entry<ThrowableRunnable, A>> sorter =
            Comparator.comparingInt(e->orderGetter.applyAsInt(e.getValue()));

        return runnables.entrySet().stream()
        .sorted(sorter)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    }



    protected KukumoDataTypeRegistry loadTypes(Stream<DataTypeContributor> contributors) {

        Map<String,KukumoDataType<?>> types = new HashMap<>();
        contributors.forEach(contributor -> {
            for (KukumoDataType<?> type : contributor.contributeTypes()) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(
                        "using type {resourceType}({}) provided by {contributor}",
                        type.getName(),
                        type.getJavaType(),
                        contributor.info()
                    );
                }
                KukumoDataType<?> replacedType = types.put(type.getName(), type);
                if (replacedType != null && LOGGER.isDebugEnabled()) {
                    LOGGER.warn(
                        "Module {contributor} overrides type {resourceType}",
                        contributor.info(),
                        replacedType.getName()
                    );
                }
            }
        });
        return new KukumoDataTypeRegistry(types);
    }




    protected List<RunnableStep> createSteps(
            List<StepContributor> stepContributors,
            KukumoDataTypeRegistry typeRegistry
    ) {
        ArrayList<RunnableStep> resultSteps = new ArrayList<>();
        for (Object stepContributor : stepContributors) {
            createContributorSteps(resultSteps,stepContributor,typeRegistry);
        }
        return resultSteps;
    }




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
                    kukumo.configure(newStepContributor,configuration);
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
                    "Cannot find non-registered step provider class: {}\n\tEnsure the class "+
                    "exists, is fully qualified, and its accesible from the main class loader",
                    nonRegisteredContributorClass
                );
            } catch (NoSuchMethodException e) {
                LOGGER.warn(
                    "Non-registered step provider class {} requieres empty constructor",
                    nonRegisteredContributorClass
                );
            } catch (ReflectiveOperationException e) {
                LOGGER.warn(
                    "Error loading non-registered step provider class {} : {}",
                    nonRegisteredContributorClass,
                    e.getLocalizedMessage()
                );
            }
        }
        return nonRegisteredContributors;
    }




    protected void createContributorSteps(
        List<RunnableStep> output,
        Object stepProvider,
        KukumoDataTypeRegistry typeRegistry
    ) {
        for (Method method : stepProvider.getClass().getMethods()) {
            if (method.isAnnotationPresent(Step.class)) {
                try {
                    RunnableStep step = createRunnableStep(stepProvider, method, typeRegistry);
                    output.add(step);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                               "using step <{}::'{}' {}>",
                            stepProvider.getClass().getSimpleName(),
                            step.getDefinitionKey(),
                            step.getArguments()
                        );
                    }
                } catch (Exception e) {
                    throw new KukumoException(e);
                }
            }
        }
    }







    protected RunnableStep createRunnableStep(
            Object runnableObject,
            Method runnableMethod,
            KukumoDataTypeRegistry typeRegistry
    ) {
        Class<?> stepContributorClass = runnableObject.getClass();
        I18nResource stepDefinitionFile = stepContributorClass.getAnnotation(I18nResource.class);
        if (stepDefinitionFile == null) {
            throw new KukumoException(
                "Class {} must be annotated with {}",
                runnableObject.getClass().getCanonicalName(),
                I18nResource.class.getCanonicalName()
            );
        }
        final Step stepDefinition = runnableMethod.getAnnotation(Step.class);
        if (stepDefinition == null) {
            throw new KukumoException(
                "Method {}::{} must be annotated with {}",
                runnableObject.getClass().getCanonicalName(),
                runnableMethod.getName(),
                Step.class.getCanonicalName()
            );
        }
        for (Class<?> methodArgumentType: runnableMethod.getParameterTypes()) {
            if (methodArgumentType.isPrimitive()) {
                throw new KukumoException(
                    "Method {}::{} must not use primitive argument type {}; "+
                    "use equivalent boxed type",
                    runnableObject.getClass().getCanonicalName(),
                    runnableMethod.getName(),
                    methodArgumentType.getName()
                );
            }
        }
        return new RunnableStep(
                stepDefinitionFile.value(),
                stepDefinition.value(),
                new BackendArguments(runnableObject.getClass(),runnableMethod,typeRegistry),
                (args -> runnableMethod.invoke(runnableObject,args))
        );
    }





}
