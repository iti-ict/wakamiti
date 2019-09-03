package iti.kukumo.core.backend;

import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.*;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.SetUp;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.annotations.TearDown;
import iti.kukumo.api.extensions.DataTypeContributor;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.util.ThrowableRunnable;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Extension(provider="iti.kukumo", name="defaultBackendFactory", version="1.0")
public class DefaultBackendFactory implements BackendFactory {

    private static final Logger LOGGER = Kukumo.LOGGER;
    private static final List<String> DEFAULT_MODULES = Collections.unmodifiableList(Arrays.asList(
            "core-types",
            "assertion-types"
    ));


    private Clock clock = Clock.systemUTC();
    private Configuration configuration;


    @Override
    public BackendFactory setClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    @Override
    public BackendFactory setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }



    @Override
    public Backend createBackend(PlanNode node) {
        if (clock == null) {
            throw new IllegalStateException("Clock must be set first");
        }
        if (configuration == null) {
            throw new IllegalStateException("Configuration must be set first");
        }
        LOGGER.debug("Creating backend for Test Case {}::'{}'", node.source(), node.displayName());
        Backend backend =  createBackend(configuration.appendFromMap(node.properties()));
        return backend;
    }




    protected Backend createBackend(Configuration configuration) {
        // each backend uses a new instance of each step contributor in order to ease parallelization
        List<String> restrictedModules = new ArrayList<>(configuration.getList(KukumoConfiguration.MODULES,String.class));
        List<StepContributor> stepContributors = resolveStepContributors(restrictedModules);
        stepContributors.forEach(stepContributor -> Kukumo.configure(stepContributor,configuration));
        List<DataTypeContributor> dataTypeContributors = resolveDataTypeContributors(restrictedModules);
        KukumoDataTypeRegistry typeRegistry = loadTypes(dataTypeContributors);
        List<RunnableStep> steps = loadSteps(stepContributors, typeRegistry);
        List<ThrowableRunnable> setUpOperations = loadMethods(stepContributors, SetUp.class, SetUp::order);
        List<ThrowableRunnable> tearDownOperations = loadMethods(stepContributors, TearDown.class, TearDown::order);
        return new DefaultBackend(configuration,typeRegistry,steps,setUpOperations,tearDownOperations,clock);
    }




    protected List<StepContributor> resolveStepContributors(List<String> restrictedModules) {

        List<StepContributor> stepContributors = new ArrayList<>();
        if (restrictedModules.isEmpty()) {
            stepContributors.addAll( Kukumo.loadAllStepContributors(configuration) );
        } else {
            List<String> modules = new ArrayList<>(restrictedModules);
            modules.addAll(DEFAULT_MODULES);
            stepContributors.addAll( Kukumo.loadSpecificStepContributors(modules,configuration) );
        }

        List<String> nonRegisteredContributorClasses = configuration.getList(KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS,String.class);
        stepContributors.addAll(resolveNonRegisteredContributors(nonRegisteredContributorClasses));
        if (stepContributors.isEmpty()) {
            logTipForNoStepContributors(restrictedModules);
            throw new KukumoException("Cannot build backend without step contributors");
        }
        return stepContributors;
    }




    protected List<DataTypeContributor> resolveDataTypeContributors(List<String> restrictedModules) {
        if (restrictedModules.isEmpty()) {
            return Kukumo.getAllDataTypeContributors();
        } else {
            List<String> modules = new ArrayList<>(restrictedModules);
            modules.addAll(DEFAULT_MODULES);
            return Kukumo.getSpecificDataTypeContributors(restrictedModules);
        }
    }



    protected void logTipForNoStepContributors(List<String> restrictedModules) {
        if (restrictedModules.isEmpty()) {
            LOGGER.error(
               "No step contributors found. You must either declare step modules with property '{}' or " +
               "non-registered step provider classes with property '{}'",
                KukumoConfiguration.MODULES, KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS
            );
        } else {
            String availableStepContributors = Kukumo.getAllStepContributorMetadata().stream()
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
        Function<A,Integer> orderProvider
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
        return runnables.entrySet().stream()
        .sorted( (e1,e2)->orderProvider.apply(e1.getValue()).compareTo(orderProvider.apply(e2.getValue())) )
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    }



    protected KukumoDataTypeRegistry loadTypes(List<DataTypeContributor> contributors) {

        Map<String,KukumoDataType<?>> types = new HashMap<>();
        for (DataTypeContributor contributor: contributors) {
            for (KukumoDataType<?> type : contributor.contributeTypes()) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("using type {resourceType}({}) provided by {contributor}", type.getName(), type.getJavaType(), contributor.info());
                }
                KukumoDataType<?> replacedType = types.put(type.getName(), type);
                if (replacedType != null && LOGGER.isDebugEnabled()) {
                    LOGGER.warn("Module {contributor} overrides type {resourceType}", contributor.info(), replacedType.getName());
                }
            }
        }
        return new KukumoDataTypeRegistry(types);
    }




    protected List<RunnableStep> loadSteps(
            List<StepContributor> stepContributors,
            KukumoDataTypeRegistry typeRegistry
    ) {
        ArrayList<RunnableStep> resultSteps = new ArrayList<>();
        for (Object stepContributor : stepContributors) {
            loadContributorSteps(resultSteps,stepContributor,typeRegistry);
        }
        return resultSteps;
    }




    protected List<StepContributor> resolveNonRegisteredContributors(List<String> nonRegisteredContributorClasses) {
        List<StepContributor> nonRegisteredContributors = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String nonRegisteredContributorClass : nonRegisteredContributorClasses) {
            try {
                Object newStepContributor = classLoader.loadClass(nonRegisteredContributorClass).getConstructor().newInstance();
                if (newStepContributor instanceof StepContributor) {
                    Kukumo.configure(newStepContributor,configuration);
                    nonRegisteredContributors.add((StepContributor) newStepContributor);
                } else {
                    LOGGER.warn("Class {} does not implement {}", nonRegisteredContributorClass, StepContributor.class);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Cannot find non-registered step provider class: {}\n\t{}",
                        nonRegisteredContributorClass,
                        "Ensure the class exists, is fully qualified, and its accesible from the main class loader");
            } catch (NoSuchMethodException e) {
                LOGGER.warn("Non-registered step provider class {} requieres empty constructor", nonRegisteredContributorClass);
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("Error loading non-registered step provider class {} : {}", nonRegisteredContributorClass, e.getLocalizedMessage());
            }
        }
        return nonRegisteredContributors;
    }




    protected void loadContributorSteps(List<RunnableStep> output, Object stepProvider, KukumoDataTypeRegistry typeRegistry) {
        for (Method method : stepProvider.getClass().getMethods()) {
            if (method.isAnnotationPresent(Step.class)) {
                try {
                    RunnableStep step = createRunnableStep(stepProvider, method, typeRegistry);
                    output.add(step);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("using step <{}::'{}' {}>",
                                stepProvider.getClass().getSimpleName(), step.getDefinitionKey(), step.getArguments());
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
        final I18nResource stepDefinitionFile = runnableObject.getClass().getAnnotation(I18nResource.class);
        if (stepDefinitionFile == null) {
            throw new KukumoException("Class {} must be annotated with {}",
                    runnableObject.getClass().getCanonicalName(),
                    I18nResource.class.getCanonicalName());
        }
        final Step stepDefinition = runnableMethod.getAnnotation(Step.class);
        if (stepDefinition == null) {
            throw new KukumoException("Method {}::{} must be annotated with {}",
                    runnableObject.getClass().getCanonicalName(), runnableMethod.getName(), Step.class.getCanonicalName());
        }
        for (Class<?> methodArgumentType: runnableMethod.getParameterTypes()) {
            if (methodArgumentType.isPrimitive()) {
                throw new KukumoException("Method {}::{} must not use primitive argument type {}; use analogous boxed type",
                        runnableObject.getClass().getCanonicalName(), runnableMethod.getName(), methodArgumentType.getName());
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
