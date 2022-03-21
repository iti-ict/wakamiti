/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.*;
import iti.kukumo.util.StringDistance;




public class StepHinter implements Hinter {


    private final List<RunnableStep> runnableSteps;
    private final List<String> properties;
    private final RunnableStepResolver stepResolver;
    private final KukumoDataTypeRegistry typeRegistry;
    private final Locale defaultTextLocale;
    private final Locale defaultDataLocale;


    public StepHinter(
        List<RunnableStep> runnableSteps,
        Configuration configuration,
        RunnableStepResolver stepResolver,
        KukumoDataTypeRegistry typeRegistry
    ) {
        this.runnableSteps = runnableSteps;
        this.properties = configuration.keyStream().collect(toList());
        this.stepResolver = stepResolver;
        this.typeRegistry = typeRegistry;
        this.defaultTextLocale =
            configuration.get(KukumoConfiguration.LANGUAGE,String.class)
            .map(Locale::forLanguageTag)
            .orElse(Locale.ENGLISH)
        ;
        this.defaultDataLocale =
            configuration.get(KukumoConfiguration.DATA_FORMAT_LANGUAGE,String.class)
            .map(Locale::forLanguageTag)
            .orElse(this.defaultTextLocale)
        ;
    }



    @Override
    public List<String> getExpandedAvailableSteps() {
        return getAvailableSteps(true);
    }


    @Override
    public List<String> getCompactAvailableSteps() {
        return getAvailableSteps(false);
    }

    @Override
    public List<String> getAvailableProperties() {
        return properties;
    }

    @Override
    public boolean isValidStep(String step) {
        return isValidStep(step, defaultTextLocale, defaultDataLocale);
    }


    @Override
    public String getStepProviderByDefinition(String step) {
    	return getStepProviderByDefinition(step, defaultTextLocale);
    }


    @Override
    public List<String> getHintsForInvalidStep(
        String invalidStep,
        int numberOfHints,
        boolean includeVariations
    ) {
        return getHintsForInvalidStep(
            invalidStep, defaultTextLocale, defaultDataLocale, numberOfHints, includeVariations
        );
    }


    public List<String> getAvailableSteps(boolean includeVariations) {
        return getHintsForInvalidStep("",-1,includeVariations);
    }


    public boolean isValidStep(String stepLiteral, Locale textLocale, Locale dataLocale) {
        try {
            return stepResolver.locateRunnableStep(stepLiteral, textLocale, dataLocale, this) != null;
        } catch (UndefinedStepException e) {
            return false;
        }
    }


    public String getStepProviderByDefinition(String step, Locale textLocale) {
        return stepResolver
        	.obtainRunnableStepByDefinition(step, textLocale)
        	.map(RunnableStep::getProvider)
        	.orElse("");
    }



    public List<String> getHintsForInvalidStep(
        String invalidStep,
        Locale textLocale,
        Locale dataLocale,
        int numberOfHints,
        boolean includeVariations
    ) {
        Set<String> stepHints = new HashSet<>();
        Map<? extends KukumoDataType<?>, Pattern> types = typeRegistry.getTypes()
            .stream()
            .collect(Collectors.toMap(
                x -> x,
                type -> Pattern.compile("\\{[^:]*:?" + type.getName() + "\\}")
            ));
        for (RunnableStep runnableStep : runnableSteps) {
            String stepHint = runnableStep.getTranslatedDefinition(textLocale);
            if (includeVariations) {
                stepHints.addAll(populateStepHintWithTypeHints(stepHint, dataLocale, types));
            } else {
                stepHints.add(stepHint);
            }
        }
        return StringDistance.closerStrings(invalidStep, stepHints, numberOfHints);
    }





    public String getHintFor(String invalidStep, Locale textLocale, Locale dataLocale) {
        int maxSuggestions = 5;
        StringBuilder hint = new StringBuilder(
            "Perhaps you mean one of the following:\n\t----------\n\t"
        );
        var allSuggestions = getHintsForInvalidStep(
            invalidStep, textLocale, dataLocale, -1, true
        );
        if (allSuggestions.size() > maxSuggestions) {
            allSuggestions = getHintsForInvalidStep(
                invalidStep, textLocale, dataLocale, maxSuggestions, false
            );
        }
        for (String stepHint : allSuggestions) {
            hint.append(stepHint).append("\n\t");
        }
        return hint.toString();
    }



    private List<String> populateStepHintWithTypeHints(
        String stepHint,
        Locale dataLocale,
        Map<? extends KukumoDataType<?>, Pattern> types
    ) {
        List<String> variants = new ArrayList<>();
        for (var type : types.entrySet()) {
            if (type.getValue().matcher(stepHint).find()) {
                for (String typeHint : type.getKey().getHints(dataLocale)) {
                    String variant = stepHint.replaceFirst(type.getValue().pattern(), typeHint);
                    variants.addAll(populateStepHintWithTypeHints(variant, dataLocale, types));
                }
            }
        }
        if (variants.isEmpty()) {
            variants.add(stepHint);
        }
        return variants;
    }


}