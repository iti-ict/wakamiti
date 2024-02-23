/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;

import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiSkippedException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.model.ExecutionState;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeData;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Argument;
import es.iti.wakamiti.api.util.Either;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.ThrowableRunnable;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.util.LocaleLoader;
import imconfig.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
public class RunnableBackend extends AbstractBackend {

    public static final Logger LOGGER = Wakamiti.LOGGER;
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
    private final Map<String, Object> extraProperties;
    private final List<PlanNode> stepsWithErrors;


    public RunnableBackend(
            PlanNode testCase,
            Configuration configuration,
            WakamitiDataTypeRegistry typeRegistry,
            List<RunnableStep> steps,
            List<ThrowableRunnable> setUpOperations,
            List<ThrowableRunnable> tearDownOperations,
            Clock clock
    ) {
        super(configuration, typeRegistry, steps);
        this.testCase = testCase;
        this.setUpOperations = setUpOperations;
        this.tearDownOperations = tearDownOperations;
        this.clock = clock;
        this.stepBackendData = new HashMap<>();
        this.extraProperties = new ContextMap();
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
            throw new WakamitiException(
                    "Plan node of type {} cannot be executed",
                    step.nodeType()
            );
        }
        if (!testCase.hasDescendant(step)) {
            throw new WakamitiException(
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
    public WakamitiDataTypeRegistry getTypeRegistry() {
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
        } catch (Exception | Error e) {
            Throwable tr = e;
            while (StringUtils.isBlank(tr.getMessage())) {
                tr = tr.getCause();
            }
            LOGGER.error("Error running {} operation: {}", type, tr.getMessage());
            LOGGER.debug(tr.getMessage(), e);

            if (e instanceof WakamitiException) {
                throw (WakamitiException) e;
            } else {
                throw new WakamitiException(e);
            }
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
        Pair<RunnableStep, Matcher> runnableStepData = resolver.locateRunnableStep(step, hinter);
        RunnableStep runnableStep = runnableStepData.key();
        Matcher stepMatcher = runnableStepData.value();
        Map<String, Argument> invokingArguments = buildInvokingArguments(
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
                invokingArguments,
                runnableStep.getProvider()
        );
    }


    @SuppressWarnings("unchecked")
    protected void runStep(PlanNode step, Instant instant) {
        step.prepareExecution().markStarted(instant);
        StepBackendData stepBackend = stepBackendData.get(step);
        WakamitiStepRunContext.set(
                new WakamitiStepRunContext(
                        configuration,
                        this,
                        stepBackend.stepLocale(),
                        stepBackend.dataLocale()
                )
        );
        try {
            if (stepBackend.exception() != null) {
                throw stepBackend.exception();
            }
            Map<String, Argument> arguments = stepBackend.invokingArguments();
            step.arguments().addAll(arguments.values());
            Object result = stepBackend.runnableStep().run(arguments);
            ((List<Object>) extraProperties.get(ContextMap.RESULTS_PROP)).add(result);
            step.prepareExecution().markFinished(clock.instant(), Result.PASSED);
        } catch (Throwable e) {
            fillErrorState(step, instant, e, stepBackend.classifier());
        } finally {
            WakamitiStepRunContext.clear();
        }
    }


    protected void fillErrorState(PlanNode modelStep, Instant instant, Throwable e, String errorClassifier) {
        modelStep.prepareExecution().markFinished(instant, resultFromThrowable(e), e, errorClassifier);
        stepsWithErrors.add(modelStep);
    }


    protected Result resultFromThrowable(Throwable e) {
        Result result;
        if (e instanceof AssertionError) {
            result = Result.FAILED;
        } else if (e instanceof UndefinedStepException) {
            result = Result.UNDEFINED;
        } else if (e instanceof WakamitiSkippedException) {
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


    protected Map<String, Argument> buildInvokingArguments(
            PlanNode modelStep,
            RunnableStep runnableStep,
            Matcher stepMatcher,
            Locale locale
    ) {
        Map<String, Argument> invokingArguments = new HashMap<>();
        for (Pair<String, String> definedArgument : runnableStep.getArguments()) {
            String argName = definedArgument.key();
            String argType = definedArgument.value();
            if (DATA_ARG_ALTERNATIVES.contains(argType)) {
                PlanNodeData data = modelStep.data().orElseThrow(
                        () -> new WakamitiException("[{}] Incomplete step '{} {}': a {} was expected",
                                modelStep.source(), modelStep.keyword(), modelStep.name(), argType
                        ));
                invokingArguments.put(
                        argType, new Argument() {
                            @Override
                            public Object doResolve() {
                                return data.copyReplacingVariables(this::resolveForEach);
                            }
                        }
                );
            } else {
                String argValue = stepMatcher.group(argName);
                Argument parsedValue = Argument.of(argValue, value -> typeRegistry.getType(argType).parse(locale, value));
                invokingArguments.put(argName, parsedValue);
            }
        }
        return invokingArguments;
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    public class ContextMap extends LinkedHashMap<String, Object> {

        public static final String RESULTS_PROP = "results";
        public static final String ID_PROP = "id";

        ContextMap() {
            super.put(ID_PROP, testCase.id());
            super.put(RESULTS_PROP, new LinkedList<>());
        }

        @Override
        public Object put(String key, Object value) {
            if (Arrays.asList(RESULTS_PROP, ID_PROP).contains(key)) {
                throw new IllegalArgumentException(key);
            } else {
                return super.put(key, value);
            }
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            m.remove(ID_PROP);
            m.remove(RESULTS_PROP);
            super.putAll(m);
        }
    }
}