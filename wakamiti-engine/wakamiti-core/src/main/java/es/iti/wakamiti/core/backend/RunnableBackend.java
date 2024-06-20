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
import java.util.regex.Matcher;


/**
 * Implementation of the Backend interface that allows running tests.
 * It provides the capability to execute individual test steps and
 * handle the setup and teardown operations.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class RunnableBackend extends AbstractBackend {

    public static final Logger LOGGER = Wakamiti.LOGGER;
    private static final List<String> DATA_ARG_ALTERNATIVES = List.of(DOCUMENT_ARG, DATATABLE_ARG);

    private final PlanNode testCase;
    private final Clock clock;
    private final List<ThrowableRunnable> setUpOperations;
    private final List<ThrowableRunnable> tearDownOperations;
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

    /**
     * {@inheritDoc}
     * This implementation validates the given test step, fetches the associated backend data, and executes the step.
     *
     * @param step The plan node representing the step to run.
     */
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

    /**
     * Validates that the given step is of type {@code STEP} or {@code VIRTUAL_STEP}
     * and is a descendant of the test case.
     *
     * @param step The plan node representing the step to validate.
     * @throws WakamitiException If the step is not of the expected type or not a
     *                           descendant of the test case.
     */
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

    /**
     * Checks if there are other steps with errors that should cause the current
     * step to be skipped.
     *
     * @param modelStep The plan node representing the step.
     * @return {@code true} if other steps have errors, {@code false} otherwise.
     */
    private boolean otherStepsHasErrors(PlanNode modelStep) {
        return (!stepsWithErrors.isEmpty() && !stepsWithErrors.contains(modelStep));
    }

    /**
     * Marks the given step as skipped.
     *
     * @param modelStep The plan node representing the step to be skipped.
     * @param now       The timestamp when the skipping occurs.
     */
    private void skipStep(PlanNode modelStep, Instant now) {
        ExecutionState<Result> execution = modelStep.prepareExecution();
        execution.markStarted(now);
        execution.markFinished(now, Result.SKIPPED);
    }

    /**
     * {@inheritDoc}
     * This implementation executes the setup operations associated with this backend.
     */
    @Override
    public void setUp() {
        String type = "set-up";
        LOGGER.debug("Performing {} operations...", type);
        for (ThrowableRunnable setUpOperation : setUpOperations) {
            runMethod(setUpOperation, type);
        }
        LOGGER.debug("{} finished", type);
    }

    /**
     * {@inheritDoc}
     * This implementation executes the teardown operations associated with this backend.
     */
    @Override
    public void tearDown() {
        String type = "tear-down";
        LOGGER.debug("Performing {} operations...", type);
        for (ThrowableRunnable tearDownOperation : tearDownOperations) {
            runMethod(tearDownOperation, type);
        }
        LOGGER.debug("{} finished", type);
    }

    /**
     * Runs the specified {@link ThrowableRunnable} operation and handles any exceptions or errors.
     *
     * @param operation The operation to be executed.
     * @param type      The type of the operation for logging purposes.
     * @throws WakamitiException If an exception or error occurs during the execution of the operation.
     */
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

    /**
     * Fetches backend data associated with each step.
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

    /**
     * Runs a test step.
     *
     * @param step    The test step to be executed.
     * @param instant The current instant.
     */
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

    /**
     * Fills the error state for a test step.
     *
     * @param modelStep       The test step.
     * @param instant         The instant when the error occurred.
     * @param e               The thrown exception.
     * @param errorClassifier The error classifier.
     */
    protected void fillErrorState(PlanNode modelStep, Instant instant, Throwable e, String errorClassifier) {
        modelStep.prepareExecution().markFinished(instant, resultFromThrowable(e), e, errorClassifier);
        stepsWithErrors.add(modelStep);
    }

    /**
     * Gets the result type based on the thrown exception.
     *
     * @param e The thrown exception.
     * @return The result type.
     */
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
     * Gets the extra properties associated with this backend.
     *
     * @return The extra properties.
     */
    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }


    /**
     * The {@code ContextMap} class is a specialized map used to store
     * extra properties associated with the backend.
     * It prevents certain keys (like "results" and "id") from being
     * used and allows cleaning them before putAll.
     */
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
            m.entrySet().stream()
                    .filter(e -> !List.of(ID_PROP, RESULTS_PROP).contains(e.getKey()))
                    .forEach(e -> put(e.getKey(), e.getValue()));
        }
    }
}