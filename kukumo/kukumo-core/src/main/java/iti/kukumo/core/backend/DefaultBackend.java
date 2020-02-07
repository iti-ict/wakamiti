/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.Backend;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.KukumoSkippedException;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.Result;
import iti.kukumo.core.model.ExecutionState;
import iti.kukumo.util.LocaleLoader;
import iti.kukumo.util.Pair;
import iti.kukumo.util.StringDistance;
import iti.kukumo.util.ThrowableRunnable;


public class DefaultBackend implements Backend {

    public static final Logger LOGGER = Kukumo.LOGGER;
    public static final String UNNAMED_ARG = "unnamed";
    public static final String DOCUMENT_ARG = "document";
    public static final String DATATABLE_ARG = "datatable";

    private static final List<String> DATA_ARG_ALTERNATIVES = Arrays.asList(
        DOCUMENT_ARG,
        DATATABLE_ARG
    );

    private final PlanNode testCase;
    private final Configuration configuration;
    private final KukumoDataTypeRegistry typeRegistry;
    private final List<RunnableStep> runnableSteps;
    private final Clock clock;
    private final List<ThrowableRunnable> setUpOperations;
    private final List<ThrowableRunnable> tearDownOperations;
    private final Map<PlanNode, StepBackendData> stepBackendData;
    private final List<PlanNode> stepsWithErrors;


    public DefaultBackend(
                    PlanNode testCase,
                    Configuration configuration,
                    KukumoDataTypeRegistry typeRegistry,
                    List<RunnableStep> steps,
                    List<ThrowableRunnable> setUpOperations,
                    List<ThrowableRunnable> tearDownOperations,
                    Clock clock
    ) {
        this.testCase = testCase;
        this.configuration = configuration;
        this.typeRegistry = typeRegistry;
        this.runnableSteps = steps;
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
            .matcher(modelStep, stepLocale, dataLocale, typeRegistry);

        List<Pair<RunnableStep, Matcher>> locatedSteps = runnableSteps.stream()
            .map(Pair.computeValue(matcher))
            .filter(pair -> pair.value().matches())
            .collect(Collectors.toList());

        if (locatedSteps.isEmpty()) {
            throw new UndefinedStepException(
                modelStep,
                "Cannot match step with any defined step",
                getHint(modelStep.name(), dataLocale)
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


    public String getHint(String wrongStep, Locale locale) {
        StringBuilder hint = new StringBuilder("Perhaps you mean one of the following:\n")
            .append("\t----------\n\t");
        Set<String> stepHints = new HashSet<>();
        Map<? extends KukumoDataType<?>, Pattern> types = typeRegistry.getTypes().stream()
            .collect(
                Collectors.toMap(
                    x -> x,
                    type -> Pattern.compile("\\{[^:]*:?" + type.getName() + "\\}")
                )
            );

        for (RunnableStep runnableStep : runnableSteps) {
            String stepHint = runnableStep.getTranslatedDefinition(locale);
            stepHints.addAll(populateStepHintWithTypeHints(stepHint, locale, types));
        }
        for (String stepHint : StringDistance.closerStrings(wrongStep, stepHints, 5)) {
            hint.append(stepHint).append("\n\t");
        }

        return hint.toString();
    }


    private List<String> populateStepHintWithTypeHints(
        String stepHint,
        Locale locale,
        Map<? extends KukumoDataType<?>, Pattern> types
    ) {
        List<String> variants = new ArrayList<>();
        for (Map.Entry<? extends KukumoDataType<?>, Pattern> type : types.entrySet()) {
            if (type.getValue().matcher(stepHint).find()) {
                for (String typeHint : type.getKey().getHints(locale)) {
                    String variant = stepHint.replaceFirst(type.getValue().pattern(), typeHint);
                    variants.addAll(populateStepHintWithTypeHints(variant, locale, types));
                }
            }
        }
        if (variants.isEmpty()) {
            variants.add(stepHint);
        }
        return variants;
    }


    protected Locale dataLocale(PlanNode modelStep, Locale fallbackLocale) {
        String dataFormatLocale = modelStep.properties().getOrDefault(
            KukumoConfiguration.DATA_FORMAT_LANGUAGE,
            configuration.get(KukumoConfiguration.DATA_FORMAT_LANGUAGE, String.class).orElse(null)
        );
        return dataFormatLocale == null ? fallbackLocale
                        : LocaleLoader.forLanguage(dataFormatLocale);
    }

}
