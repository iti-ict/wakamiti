/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiSkippedException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.annotations.Level;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.model.ExecutionState;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeData;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.api.util.Argument;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.ThrowableRunnable;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.util.LocaleLoader;


/**
 * Runnable {@link es.iti.wakamiti.api.Backend} implementation for test-case
 * execution.
 * <p>
 * This backend resolves runnable steps, executes setup/teardown hooks,
 * propagates per-step execution state and stores scenario-scoped extra
 * properties.
 * </p>
 */
public class RunnableBackend extends LifecycleBackend {

    /** Engine logger shared by runnable backends for execution diagnostics. */
    public static final Logger LOGGER = Wakamiti.LOGGER;
    private static final List<String> DATA_ARG_ALTERNATIVES = List.of(DOCUMENT_ARG, DATATABLE_ARG);

    private final PlanNode testCase;
    private final Clock clock;
    private final Map<PlanNode, StepBackendData> stepBackendData;
    private final Map<String, Object> extraProperties;
    private final List<PlanNode> stepsWithErrors;

    /**
     * Constructs a {@code RunnableBackend} with the specified test case,
     * configuration, type registry, list of runnable steps, setup operations,
     * teardown operations, and clock.
     *
     * @param testCase           The test case associated with this backend.
     * @param configuration      The configuration for the backend.
     * @param typeRegistry       The WakamitiDataTypeRegistry for type information.
     * @param steps              The list of runnable steps available in this backend.
     * @param setUpOperations    The list of setup operations to be executed before
     *                           running the test steps.
     * @param tearDownOperations The list of teardown operations to be executed
     *                           after running the test steps.
     * @param clock              The clock used to record timestamps.
     */
    public RunnableBackend(
            PlanNode testCase,
            Configuration configuration,
            WakamitiDataTypeRegistry typeRegistry,
            List<RunnableStep> steps,
            Map<Level, List<ThrowableRunnable>> setUpOperations,
            Map<Level, List<ThrowableRunnable>> tearDownOperations,
            Clock clock
    ) {
        super(configuration, typeRegistry, setUpOperations, tearDownOperations, steps);
        this.testCase = testCase;
        this.clock = clock;
        this.stepBackendData = new HashMap<>();
        this.extraProperties = new ContextMap();
        this.stepsWithErrors = new ArrayList<>();
    }

    @Override
    protected void beforeLifecycleOperation() {
        Locale locale = LocaleLoader.forLanguage(testCase.language());
        WakamitiStepRunContext.set(
                new WakamitiStepRunContext(configuration, this, locale, locale)
        );
    }

    @Override
    protected void afterLifecycleOperation() {
        WakamitiStepRunContext.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLifecycleHook() {
        return testCase.nodeType() == NodeType.LIFECYCLE_HOOK;
    }

    /**
     * {@inheritDoc}
     * This implementation validates the given test step, fetches the associated backend data, and executes the step.
     *
     * @param step The plan node representing the step to run.
     */
    @Override
    public void runStep(
            PlanNode step
    ) {
        runStep(step, () -> {
            Instant now = clock.instant();
            if (otherStepsHasErrors(step)) {
                skipStep(step, now);
            } else {
                runStep(step, now);
            }
        });
    }

    /**
     * {@inheritDoc}
     * This implementation validates and resolves the given step without
     * executing the underlying step method.
     *
     * @param step The plan node representing the step to validate.
     */
    @Override
    public void dryRunStep(
            PlanNode step
    ) {
        runStep(step, () -> {
            Instant now = clock.instant();
            StepBackendData stepBackend = stepBackendData.get(step);
            step.prepareExecution().markStarted(now);
            if (stepBackend.exception() != null) {
                fillErrorState(step, now, stepBackend.exception(), stepBackend.classifier());
            } else {
                step.prepareExecution().markFinished(clock.instant(), Result.PASSED);
            }
        });
    }

    private void runStep(
            PlanNode step,
            Runnable runnable
    ) {
        validateStepFromTestCase(step);
        fetchStepBackendData();
        if (step.nodeType() == NodeType.VIRTUAL_STEP) {
            // virtual steps are never executed, but must be accounted as valid.
            Instant now = clock.instant();
            ExecutionState<Result> executionData = step.prepareExecution();
            executionData.markStarted(now);
            executionData.markFinished(now, Result.PASSED);
        } else if (step.nodeType() == NodeType.STEP) {
            runnable.run();
        }
    }

    /**
     * Validates that the given step is of type {@code STEP} or {@code VIRTUAL_STEP}
     * and is a descendant of the test case.
     *
     * @param step The plan node representing the step to validate.
     * @throws WakamitiException If the step is not of the expected type or not a
     *                           descendant of the test case.
     */
    private void validateStepFromTestCase(
            PlanNode step
    ) {
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

    /**
     * Checks if there are other steps with errors that should cause the current
     * step to be skipped.
     *
     * @param modelStep The plan node representing the step.
     * @return {@code true} if other steps have errors, {@code false} otherwise.
     */
    private boolean otherStepsHasErrors(
            PlanNode modelStep
    ) {
        return (!stepsWithErrors.isEmpty() && !stepsWithErrors.contains(modelStep));
    }

    /**
     * Marks the given step as skipped.
     *
     * @param modelStep The plan node representing the step to be skipped.
     * @param now       The timestamp when the skipping occurs.
     */
    private void skipStep(
            PlanNode modelStep,
            Instant now
    ) {
        ExecutionState<Result> execution = modelStep.prepareExecution();
        execution.markStarted(now);
        execution.markFinished(now, Result.SKIPPED);
    }

    /**
     * Resolves and caches backend execution metadata for every step in the test
     * case.
     * <p>
     * The cache is built lazily once; step-resolution errors are recorded so
     * affected steps are marked with errors during execution.
     * </p>
     */
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

    /**
     * Fetches backend data associated with each step.
     *
     * @param step The test step for which backend data is fetched.
     * @return The backend data for the given step.
     */
    private StepBackendData fetchStepBackendData(
            PlanNode step
    ) {
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

    /**
     * Executes a resolved step implementation.
     * <p>
     * A {@link WakamitiStepRunContext} is installed for the current thread
     * during execution and cleared in {@code finally}.
     * </p>
     *
     * @param step    test step to execute
     * @param instant execution start timestamp
     */
    @SuppressWarnings("unchecked")
    protected void runStep(
            PlanNode step,
            Instant instant
    ) {
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
            ((Map<String, Object>) extraProperties.get(ContextMap.RESULTS_PROP))
                    .put(step.properties().getOrDefault("id", step.id()), result);
            step.prepareExecution().markFinished(
                    clock.instant(),
                    Result.PASSED,
                    null,
                    null,
                    result == null ? null : result.toString()
            );
        } catch (Throwable e) {
            fillErrorState(step, instant, e, stepBackend.classifier());
        } finally {
            WakamitiStepRunContext.clear();
        }
    }

    /**
     * Fills the error state for a test step.
     *
     * @param modelStep       The test step.
     * @param instant         The instant when the error occurred.
     * @param e               The thrown exception.
     * @param errorClassifier The error classifier.
     */
    protected void fillErrorState(
            PlanNode modelStep,
            Instant instant,
            Throwable e,
            String errorClassifier
    ) {
        modelStep.prepareExecution().markFinished(instant, resultFromThrowable(e), e, errorClassifier);
        stepsWithErrors.add(modelStep);
    }

    /**
     * Gets the result type based on the thrown exception.
     *
     * @param e The thrown exception.
     * @return The result type.
     */
    protected Result resultFromThrowable(
            Throwable e
    ) {
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

    /**
     * Builds invoking arguments for a test step.
     *
     * @param modelStep    The test step.
     * @param runnableStep The runnable step.
     * @param stepMatcher  The step matcher.
     * @param locale       The locale for the test data.
     * @return A map of invoking arguments.
     */
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

    /**
     * Returns the mutable scenario context map.
     * <p>
     * The map always contains reserved keys {@code id} and {@code results}.
     * </p>
     *
     * @return scenario extra properties map
     */
    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    /**
     * Scenario context map with reserved system keys.
     * <p>
     * Clients may store custom values except for {@link #ID_PROP} and
     * {@link #RESULTS_PROP}, which are managed by the backend.
     * </p>
     */
    public class ContextMap extends LinkedHashMap<String, Object> {

        /** Plan-node property that carries examples or execution-result rows. */
        public static final String RESULTS_PROP = "results";
        /** Plan-node property that carries the stable identifier of an executable node. */
        public static final String ID_PROP = "id";

        ContextMap() {
            super.put(ID_PROP, testCase.id());
            super.put(RESULTS_PROP, new LinkedHashMap<>());
        }

        @Override
        public Object put(
                String key,
                Object value
        ) {
            if (Arrays.asList(RESULTS_PROP, ID_PROP).contains(key)) {
                throw new IllegalArgumentException(key);
            } else {
                return super.put(key, value);
            }
        }

        @Override
        public void putAll(
                Map<? extends String, ?> m
        ) {
            m.entrySet().stream()
                    .filter(e -> !List.of(ID_PROP, RESULTS_PROP).contains(e.getKey()))
                    .forEach(e -> put(e.getKey(), e.getValue()));
        }

    }

}
