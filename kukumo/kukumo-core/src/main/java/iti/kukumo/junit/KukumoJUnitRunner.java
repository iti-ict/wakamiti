package iti.kukumo.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.BackendFactory;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.core.runner.PlanNodeLogger;


public class KukumoJUnitRunner extends Runner {

    /** Configuration property to set if the steps are treated as tests. False by default */
    public static final String TREAT_STEPS_AS_TESTS = "junit.treatStepsAsTests";

    protected static final Logger LOGGER = Kukumo.LOGGER;
    protected static final ConfigurationBuilder confBuilder = ConfigurationBuilder.instance();

    protected final Configuration configuration;
    protected final Class<?> configurationClass;
    protected final PlanNodeLogger planNodeLogger;
    protected final boolean treatStepsAsTests;
    protected final Kukumo kukumo;
    private PlanNode plan;
    private List<JUnitPlanNodeRunner> children;
    private Description description;




    public KukumoJUnitRunner(Class<?> configurationClass) throws InitializationError {
        this.kukumo = Kukumo.instance();
        this.configurationClass = configurationClass;
        this.configuration = retrieveConfiguration(configurationClass);
        this.planNodeLogger = new PlanNodeLogger(LOGGER,configuration,getPlan());
        this.treatStepsAsTests = configuration.get(TREAT_STEPS_AS_TESTS, Boolean.class).orElse(Boolean.FALSE);
        validateAnnotatedMethod(configurationClass, BeforeClass.class);
        validateAnnotatedMethod(configurationClass, AfterClass.class);
    }





    @Override
    public void run(RunNotifier notifier) {
        kukumo.configureLogger(configuration);
        kukumo.configureEventObservers(configuration);
        kukumo.publishEvent(Event.PLAN_RUN_STARTED,getPlan().obtainDescriptor());
        planNodeLogger.logTestPlanHeader(plan);
        executeAnnotatedMethod(configurationClass, BeforeClass.class);

        for (JUnitPlanNodeRunner child: getChildren()) {
            try {
                child.runNode(notifier,false);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }

        // refactor this line in the future when multithreading is supported
        executeAnnotatedMethod(configurationClass, AfterClass.class);

        planNodeLogger.logTestPlanResult(plan);
        kukumo.publishEvent(Event.PLAN_RUN_FINISHED,getPlan().obtainDescriptor());
        kukumo.writeOutputFile(plan, configuration);
        kukumo.generateReports(configuration);
    }


    public List<JUnitPlanNodeRunner> getChildren() {
        if (children == null) {
            children = buildRunners();
        }
        return children;
    }



    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription("Kukumo Test Plan",UUID.randomUUID().toString());
            for (JUnitPlanNodeRunner child : getChildren()) {
                description.addChild(child.getDescription());
            }
        }
        return description;
    }


    protected PlanNode getPlan() {
        if (plan == null) {
            plan = kukumo.createPlanFromConfiguration(configuration);
        }
        return plan;
    }


    protected List<JUnitPlanNodeRunner> buildRunners()  {
        return getPlan().children().map(node -> {
            Configuration featureConfiguration = configuration.append(
                confBuilder.buildFromMap(node.properties())
            );
            BackendFactory backendFactory = kukumo.getBackendFactory().setConfiguration(featureConfiguration);
            return treatStepsAsTests ?
                    new JUnitPlanNodeStepRunner(node,backendFactory,planNodeLogger):
                    new JUnitPlanNodeRunner(node,backendFactory,planNodeLogger);
        }).collect(Collectors.toList());
    }




    private static Configuration retrieveConfiguration(Class<?> testedClass) throws InitializationError {
        try {
            return KukumoConfiguration.defaultConfiguration().appendFromAnnotation(testedClass);
        } catch (ConfigurationException e) {
            LOGGER.error("Error loading configuration from {} : {}", testedClass, e.getMessage(), e);
            throw new InitializationError(e);
        }
    }




    private void validateAnnotatedMethod(Class<?> configurationClass, Class<? extends Annotation> annotation)
    throws InitializationError {
        for (Method method : configurationClass.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                     throw new InitializationError("Method "+method.getName()+" annotated with "+annotation+
                             " should be static");
                }
                if (method.getParameterCount() > 0) {
                    throw new InitializationError("Method "+method.getName()+" annotated with "+annotation+
                            " should have no parameters");
                }
            }
        }
    }

    private void executeAnnotatedMethod(Class<?> configurationClass, Class<? extends Annotation> annotation) {
        for (Method method : configurationClass.getMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                try {
                    method.invoke(null);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
