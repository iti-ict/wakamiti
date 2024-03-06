/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.util.Either;
import es.iti.wakamiti.api.util.Pair;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


/**
 * This class is responsible for resolving RunnableSteps during test execution.
 * It helps locate the appropriate RunnableStep based on a given step definition.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class RunnableStepResolver {

    private final WakamitiDataTypeRegistry typeRegistry;
    private final List<RunnableStep> runnableSteps;


    RunnableStepResolver(
            WakamitiDataTypeRegistry typeRegistry,
            List<RunnableStep> runnableSteps
    ) {
        this.typeRegistry = typeRegistry;
        this.runnableSteps = runnableSteps;
    }

    /**
     * Locates a RunnableStep and its corresponding matcher based on the
     * provided PlanNode step and StepHinter.
     *
     * @param step   The PlanNode step to locate.
     * @param hinter The StepHinter providing hints for step location.
     * @return A Pair containing the located RunnableStep and its Matcher.
     * @throws UndefinedStepException If the step cannot be matched with
     *                                any defined step or matches more than one defined step.
     */
    Pair<RunnableStep, Matcher> locateRunnableStep(PlanNode step, StepHinter hinter) {
        Locale stepLocale = Locale.forLanguageTag(step.language());
        Locale dataLocale = Locale.forLanguageTag(
                step.properties().getOrDefault(WakamitiConfiguration.DATA_FORMAT_LANGUAGE, step.language())
        );
        return locateRunnableStep(Either.of(step), stepLocale, dataLocale, hinter);
    }

    /**
     * Locates a RunnableStep and its corresponding matcher based on the
     * provided Either PlanNode or step definition string, locales, and
     * StepHinter.
     *
     * @param step       The Either PlanNode or step definition string to
     *                   locate.
     * @param stepLocale The locale for the step.
     * @param dataLocale The locale for the test data.
     * @param hinter     The StepHinter providing hints for step location.
     * @return A Pair containing the located RunnableStep and its Matcher.
     * @throws UndefinedStepException If the step cannot be matched with any
     *                                defined step or matches more than one defined step.
     */
    Pair<RunnableStep, Matcher> locateRunnableStep(
            String step,
            Locale stepLocale,
            Locale dataLocale,
            StepHinter hinter
    ) {
        return locateRunnableStep(Either.fallback(step), stepLocale, dataLocale, hinter);
    }

    /**
     * Locates a RunnableStep and its corresponding matcher based on
     * the provided step definition string, locales, and StepHinter.
     *
     * @param step       The step definition string to locate.
     * @param stepLocale The locale for the step.
     * @param dataLocale The locale for the test data.
     * @param hinter     The StepHinter providing hints for step location.
     * @return A Pair containing the located RunnableStep and its Matcher.
     * @throws UndefinedStepException If the step cannot be matched with any
     *                                defined step or matches more than one defined step.
     */
    private Pair<RunnableStep, Matcher> locateRunnableStep(
            Either<PlanNode, String> step,
            Locale stepLocale,
            Locale dataLocale,
            StepHinter hinter
    ) {

        Function<RunnableStep, Matcher> matcher = runnableStep -> runnableStep
                .matcher(step, stepLocale, dataLocale, typeRegistry);

        String stepName = step.mapValueOrFallback(PlanNode::name);

        List<Pair<RunnableStep, Matcher>> locatedSteps = runnableSteps.stream()
                .map(Pair.compute(matcher))
                .filter(pair -> pair.value().matches())
                .collect(Collectors.toList());

        if (locatedSteps.isEmpty()) {
            throw new UndefinedStepException(
                    step,
                    "Cannot match step with any defined step",
                    hinter.getHintFor(stepName, stepLocale, dataLocale)
            );
        }
        if (locatedSteps.size() > 1) {
            String locatedStepsInfo = locatedSteps.stream()
                    .map(Pair::key)
                    .map(locatedStep -> locatedStep.getTranslatedDefinition(stepLocale))
                    .collect(Collectors.joining("\n\t"));

            throw new UndefinedStepException(
                    stepName,
                    "Step matches more than one defined step",
                    locatedStepsInfo
            );
        }
        return locatedSteps.get(0);
    }

    /**
     * Obtains a RunnableStep by its definition and locale.
     *
     * @param stepDefinition The step definition string.
     * @param stepLocale     The locale for the step.
     * @return An Optional containing the located RunnableStep, or an empty
     * Optional if not found.
     */
    Optional<RunnableStep> obtainRunnableStepByDefinition(String stepDefinition, Locale stepLocale) {
        return runnableSteps.stream()
                .filter(runnableStep -> stepDefinition.equals(runnableStep.getTranslatedDefinition(stepLocale)))
                .findAny();
    }


}