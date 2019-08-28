package iti.kukumo.core.backend;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.*;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.api.plan.Result;
import iti.kukumo.util.LocaleLoader;
import iti.kukumo.util.Pair;
import iti.kukumo.util.StringDistance;
import iti.kukumo.util.ThrowableRunnable;
import org.slf4j.Logger;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultBackend implements Backend {

    public static final Logger LOGGER = Kukumo.LOGGER;
    public static final String UNNAMED_ARG = "unnamed";
    public static final String DOCUMENT_ARG = "document";
    public static final String DATATABLE_ARG = "datatable";

    private final Configuration configuration;
    private final KukumoDataTypeRegistry typeRegistry;
    private final List<RunnableStep> runnableSteps;
    private final Clock clock;
    private final List<ThrowableRunnable> setUpOperations;
    private final List<ThrowableRunnable> tearDownOperations;

    public DefaultBackend(
            Configuration configuration,
            KukumoDataTypeRegistry typeRegistry,
            List<RunnableStep> steps,
            List<ThrowableRunnable> setUpOperations,
            List<ThrowableRunnable> tearDownOperations,
            Clock clock
    ) {
        this.configuration = configuration;
        this.typeRegistry = typeRegistry;
        this.runnableSteps = steps;
        this.setUpOperations = setUpOperations;
        this.tearDownOperations = tearDownOperations;
        this.clock = clock;
    }


    @Override
    public void runStep(PlanStep modelStep) {
        if (modelStep.isVoid()) {
            Instant now = clock.instant();
            modelStep.markStarted(now);
            performRunVoidStep(modelStep,now);
        } else {
            modelStep.markStarted(clock.instant());
            try {
                Locale stepLocale = LocaleLoader.forLanguage(modelStep.language());
                Locale dataLocale = dataLocale(modelStep, stepLocale);
                Pair<RunnableStep, Matcher> runnableStepData = locateRunnableStep(modelStep, stepLocale, dataLocale);
                RunnableStep runnableStep = runnableStepData.key();
                Matcher stepMatcher = runnableStepData.value();
                Map<String, Object> invokingArguments = buildInvokingArguments(modelStep, runnableStep, stepMatcher, dataLocale);
                KukumoStepRunContext.set(new KukumoStepRunContext(configuration, this, stepLocale, dataLocale));
                performRunStep(modelStep, runnableStep, invokingArguments);
                KukumoStepRunContext.clear();
            } catch (Exception e) {
                fillErrorState(modelStep, e);
            }
        }
    }


    @Override
    public void skipStep(PlanStep modelStep) {
        Instant now = clock.instant();
        modelStep.markStarted(now);
        modelStep.markFailure(
            now,
            Result.SKIPPED,
            null
        );
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
            runMethod(tearDownOperation,"tear-down");
        }
    }


    private void runMethod(ThrowableRunnable operation, String type) {
        try {
            operation.run();
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() != e) {
                e = (Exception) e.getCause();
            }
            LOGGER.error("Error running "+type+" operation", e);
        }
    }



    protected void performRunStep(PlanStep modelStep, RunnableStep runnableStep, Map<String,Object> invokingArguments) {
        try {
            Kukumo.publishEvent(Event.BEFORE_RUN_BACKEND_STEP, this);
            runnableStep.run(invokingArguments);
            modelStep.markPassed(clock.instant());
        } catch (Throwable e) {
            fillErrorState(modelStep, e);
        } finally {
            Kukumo.publishEvent(Event.AFTER_RUN_BACKEND_STEP, this);
        }
    }

    protected void performRunVoidStep(PlanStep voidStep, Instant fixedInstant) {
        voidStep.markPassed(fixedInstant);
    }


    protected void fillErrorState(PlanStep modelStep, Throwable e) {
        modelStep.markFailure(clock.instant(), resultFromThrowable(e), e);
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



    protected Pair<RunnableStep,Matcher> locateRunnableStep(PlanStep modelStep, Locale stepLocale, Locale dataLocale) {

        List<Pair<RunnableStep,Matcher>> locatedSteps = runnableSteps.stream()
                .map(Pair.computeValue(step->step.matcher(modelStep,stepLocale,dataLocale,typeRegistry)))
                .filter(pair -> pair.value().matches())
                .collect(Collectors.toList());

        if (locatedSteps.isEmpty()) {
            throw new UndefinedStepException(
                modelStep,
                "Cannot match step with any defined step",
                getHint(modelStep.name(),dataLocale)
            );
        }
        if (locatedSteps.size() > 1) {
            String locatedStepsInfo =  locatedSteps.stream().map(Pair::key)
                    .map(step -> step.getTranslatedDefinition(stepLocale)).collect(Collectors.joining("\n\t"));
            throw new UndefinedStepException(
                modelStep,
                "Step matches more than one defined step",
                locatedStepsInfo
            );
        }
        return locatedSteps.get(0);
    }




    protected Map<String,Object> buildInvokingArguments(PlanStep modelStep, RunnableStep runnableStep, Matcher stepMatcher, Locale locale) {
        Map<String,Object> invokingArguments = new HashMap<>();
        for (Pair<String,String> definedArgument : runnableStep.getArguments()) {
            String argName = definedArgument.key();
            String argType = definedArgument.value();
            String argValue = null;

            if (argType.equals(DOCUMENT_ARG)) {
                invokingArguments.put(DOCUMENT_ARG, modelStep.getDocument().orElseThrow(
                    ()->new KukumoException("[{}] Incomplete step '{} {}': a document was expected",
                            modelStep.source(), modelStep.keyword(), modelStep.name())
                ));
            } else if (argType.equals(DATATABLE_ARG)) {
                invokingArguments.put(DATATABLE_ARG, modelStep.getDataTable().orElseThrow(
                  ()->new KukumoException("[{}] Incomplete step '{} {}': a data table was expected",
                        modelStep.source(), modelStep.keyword(), modelStep.name())
                ));
            } else {
                argValue = stepMatcher.group(argName);
                invokingArguments.put(argName, typeRegistry.getType(argType).parse(locale, argValue));
            }
        }
        return invokingArguments;
    }



    public String getHint(String wrongStep, Locale locale) {
        StringBuilder hint = new StringBuilder("Perhaps you mean one of the following:\n\t----------\n\t");
        Set<String> stepHints = new HashSet<>();
        Map<? extends KukumoDataType<?>, Pattern> types = typeRegistry.getTypes().stream()
                .collect(Collectors.toMap(x -> x, type -> Pattern.compile("\\{[^:]*:?" + type.getName() + "\\}")));

        for (RunnableStep runnableStep : runnableSteps) {
            String stepHint = runnableStep.getTranslatedDefinition(locale);
            stepHints.addAll( populateStepHintWithTypeHints(stepHint,locale,types));
        }
        for (String stepHint : StringDistance.closerStrings(wrongStep, stepHints,5)) {
            hint.append(stepHint).append("\n\t");
        }

        return hint.toString();
    }




    private List<String> populateStepHintWithTypeHints(String stepHint, Locale locale, Map<? extends KukumoDataType<?>, Pattern> types) {
        List<String> variants = new ArrayList<>();
        for (Map.Entry<? extends KukumoDataType<?>,Pattern> type : types.entrySet()) {
            if (type.getValue().matcher(stepHint).find()) {
                for (String typeHint : type.getKey().getHints(locale)) {
                    String variant = stepHint.replaceFirst(type.getValue().pattern(),typeHint);
                    variants.addAll( populateStepHintWithTypeHints(variant,locale,types));
                }
            }
        }
        if (variants.isEmpty()) {
            variants.add(stepHint);
        }
        return variants;
    }





    protected Locale dataLocale(PlanStep modelStep, Locale fallbackLocale) {
        String dataFormatLocale = modelStep.properties().getOrDefault(
                KukumoConfiguration.DATA_FORMAT_LANGUAGE,
                configuration.get(KukumoConfiguration.DATA_FORMAT_LANGUAGE,String.class).orElse(null)
        );
        return dataFormatLocale == null ? fallbackLocale : LocaleLoader.forLanguage(dataFormatLocale);

    }


}
