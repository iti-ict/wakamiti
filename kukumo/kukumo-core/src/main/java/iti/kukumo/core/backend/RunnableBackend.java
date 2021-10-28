/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.*;
import iti.kukumo.api.plan.*;
import iti.kukumo.core.model.ExecutionState;
import iti.kukumo.util.*;


public class RunnableBackend extends AbstractBackend {

    public static final Logger LOGGER = Kukumo.LOGGER;
    public static final String UNNAMED_ARG = "unnamed";
    public static final String DOCUMENT_ARG = "document";
    public static final String DATATABLE_ARG = "datatable";

    private static final List<String> DATA_ARG_ALTERNATIVES = Arrays.asList(
        DOCUMENT_ARG,
        DATATABLE_ARG
    );

    private final PlanNode testCase;
    private final Clock clock;
    private final List<ThrowableRunnable> setUpOperations;
    private final List<ThrowableRunnable> tearDownOperations;
    private final Map<PlanNode, StepBackendData> stepBackendData;
    private final List<PlanNode> stepsWithErrors;


    public RunnableBackend(
        PlanNode testCase,
        Configuration configuration,
        KukumoDataTypeRegistry typeRegistry,
        List<RunnableStep> steps,
        List<ThrowableRunnable> setUpOperations,
        List<ThrowableRunnable> tearDownOperations,
        Clock clock
    ) {
        super(configuration,typeRegistry,steps);
        this.testCase = testCase;
        this.setUpOperations = setUpOperations;
        this.tearDownOperations = tearDownOperations;
        this.clock = clock;
        this.stepBackendData = new HashMap<>();
        this.stepsWithErrors = new ArrayList<>();
    }



    @Override
    public void runStep(PlanNode step) {
        validateStepFromTestCase(step);
        fetchStepBackendData();
        Instant now = clock.instant();
        if (step.nodeType() == NodeType.VIRTUAL_STEP) {
            // virtual steps are not executed, marked as passed directly
            ExecutionState<Result> executionData = step.prepareExecution();
            executionData.markStarted(now);
            executionData.markFinished(now, Result.PASSED);
        } else if (step.nodeType() == NodeType.STEP) {
            if (otherStepsHasErrors(step)) {
                skipStep(step, now);
            } else {
                runStep(step, now);
            }
        }
    }


    private void validateStepFromTestCase(PlanNode step) {
        if (step.nodeType().isNoneOf(NodeType.STEP, NodeType.VIRTUAL_STEP)) {
            throw new KukumoException(
                "Plan node of type {} cannot be executed",
                step.nodeType()
            );
        }
        if (!testCase.hasDescendant(step)) {
            throw new KukumoException(
                "Step {} is not descendant of {}",
                step.displayName(),
                testCase.displayName()
            );
        }
    }


    private boolean otherStepsHasErrors(PlanNode modelStep) {
        return (!stepsWithErrors.isEmpty() && !stepsWithErrors.contains(modelStep));
    }


    private void skipStep(PlanNode modelStep, Instant now) {
        ExecutionState<Result> execution = modelStep.prepareExecution();
        execution.markStarted(now);
        execution.markFinished(now, Result.SKIPPED);
    }


    @Override
    public KukumoDataTypeRegistry getTypeRegistry() {
        return typeRegistry;
    }


    @Override
    public void setUp() {
        for (ThrowableRunnable setUpOperation : setUpOperations) {
            runMethod(setUpOperation, "set-up");
        }
    }


    @Override
    public void tearDown() {
        for (ThrowableRunnable tearDownOperation : tearDownOperations) {
            runMethod(tearDownOperation, "tear-down");
        }
    }


    private void runMethod(ThrowableRunnable operation, String type) {
        try {
            operation.run();
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() != e) {
                e = (Exception) e.getCause();
            }
            LOGGER.error("Error running {} operation: {}", type, e.getMessage());
            LOGGER.debug(e.getMessage(), e);
        }
    }


    private void fetchStepBackendData() {
        if (stepBackendData.isEmpty()) {
            testCase.descendants().filter(node -> node.nodeType() == NodeType.STEP)
                .forEach(step -> {
                    try {
                        stepBackendData.put(step, fetchStepBackendData(step));
                    } catch (Exception e) {
                        stepBackendData.put(step, new StepBackendData(step, e));
                        stepsWithErrors.add(step);
                    }
                });
        }
    }


    private StepBackendData fetchStepBackendData(PlanNode step) {
        Locale stepLocale = LocaleLoader.forLanguage(step.language());
        Locale dataLocale = dataLocale(step, stepLocale);
        Pair<RunnableStep, Matcher> runnableStepData = locateRunnableStep(
            step,
            stepLocale,
            dataLocale
        );
        RunnableStep runnableStep = runnableStepData.key();
        Matcher stepMatcher = runnableStepData.value();
        Map<String, Object> invokingArguments = buildInvokingArguments(
            step,
            runnableStep,
            stepMatcher,
            dataLocale
        );
        return new StepBackendData(
            step,
            stepLocale,
            dataLocale,
            runnableStep,
            stepMatcher,
            invokingArguments
        );
    }


    protected void runStep(PlanNode step, Instant instant) {
        try {
            step.prepareExecution().markStarted(instant);
            StepBackendData stepBackend = stepBackendData.get(step);
            KukumoStepRunContext.set(
                new KukumoStepRunContext(
                    configuration,
                    this,
                    stepBackend.stepLocale(),
                    stepBackend.dataLocale()
                )
            );
            if (stepBackend.exception() != null) {
                throw stepBackend.exception();
            }
            stepBackend.runnableStep().run(stepBackend.invokingArguments());
            step.prepareExecution().markFinished(clock.instant(), Result.PASSED);
        } catch (Throwable e) {
            fillErrorState(step, instant, e);
        } finally {
            KukumoStepRunContext.clear();
        }
    }


    protected void fillErrorState(PlanNode modelStep, Instant instant, Throwable e) {
        modelStep.prepareExecution().markFinished(instant, resultFromThrowable(e), e);
        stepsWithErrors.add(modelStep);
    }


    protected Result resultFromThrowable(Throwable e) {
        Result result;
        if (e instanceof AssertionError) {
            result = Result.FAILED;
        } else if (e instanceof UndefinedStepException) {
            result = Result.UNDEFINED;
        } else if (e instanceof KukumoSkippedException) {
            result = Result.SKIPPED;
        } else {
            result = Result.ERROR;
        }
        return result;
    }


    protected Pair<RunnableStep, Matcher> locateRunnableStep(
        PlanNode modelStep,
        Locale stepLocale,
        Locale dataLocale
    ) {
        Function<RunnableStep, Matcher> matcher = runnableStep -> runnableStep
            .matcher(Either.of(modelStep), stepLocale, dataLocale, typeRegistry);

        List<Pair<RunnableStep, Matcher>> locatedSteps = runnableSteps.stream()
            .map(Pair.computeValue(matcher))
            .filter(pair -> pair.value().matches())
            .collect(Collectors.toList());

        if (locatedSteps.isEmpty()) {
            throw new UndefinedStepException(
                modelStep,
                "Cannot match step with any defined step",
                getHintFor(modelStep.name(), dataLocale)
            );
        }
        if (locatedSteps.size() > 1) {
            String locatedStepsInfo = locatedSteps.stream()
                .map(Pair::key)
                .map(step -> step.getTranslatedDefinition(stepLocale))
                .collect(Collectors.joining("\n\t"));
            throw new UndefinedStepException(
                modelStep,
                "Step matches more than one defined step",
                locatedStepsInfo
            );
        }
        return locatedSteps.get(0);
    }


    protected Map<String, Object> buildInvokingArguments(
        PlanNode modelStep,
        RunnableStep runnableStep,
        Matcher stepMatcher,
        Locale locale
    ) {
        Map<String, Object> invokingArguments = new HashMap<>();
        for (Pair<String, String> definedArgument : runnableStep.getArguments()) {
            String argName = definedArgument.key();
            String argType = definedArgument.value();
            String argValue = null;
            if (DATA_ARG_ALTERNATIVES.contains(argType)) {
                invokingArguments.put(
                    argType,
                    modelStep.data().orElseThrow(
                        () -> new KukumoException(
                            "[{}] Incomplete step '{} {}': a {} was expected",
                            modelStep.source(), modelStep.keyword(), modelStep.name(), argType
                        )
                    )
                );
            } else {
                argValue = stepMatcher.group(argName);
                Object parsedValue = typeRegistry.getType(argType).parse(locale, argValue);
                invokingArguments.put(argName, parsedValue);
            }
        }
        return invokingArguments;
    }



}
