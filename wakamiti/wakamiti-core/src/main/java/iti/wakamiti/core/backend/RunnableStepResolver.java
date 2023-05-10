/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.core.backend;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import iti.wakamiti.api.*;
import iti.wakamiti.api.WakamitiConfiguration;
import iti.wakamiti.api.WakamitiDataTypeRegistry;
import iti.wakamiti.api.plan.PlanNode;
import iti.wakamiti.api.util.Either;
import iti.wakamiti.api.util.Pair;


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



    Pair<RunnableStep, Matcher> locateRunnableStep(PlanNode step, StepHinter hinter) {
        Locale stepLocale = Locale.forLanguageTag(step.language());
        Locale dataLocale = Locale.forLanguageTag(
            step.properties().getOrDefault(WakamitiConfiguration.DATA_FORMAT_LANGUAGE,step.language())
        );
        return locateRunnableStep(Either.of(step), stepLocale, dataLocale, hinter);
    }


    Pair<RunnableStep, Matcher> locateRunnableStep(
        String step,
        Locale stepLocale,
        Locale dataLocale,
        StepHinter hinter
    ) {
        return locateRunnableStep(Either.fallback(step), stepLocale, dataLocale, hinter);
    }


    private Pair<RunnableStep, Matcher> locateRunnableStep(
        Either<PlanNode,String> step,
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



    Optional<RunnableStep> obtainRunnableStepByDefinition(String stepDefinition, Locale stepLocale) {
    	return runnableSteps.stream()
	        .filter(runnableStep -> stepDefinition.equals(runnableStep.getTranslatedDefinition(stepLocale)))
	        .findAny();
    }



}