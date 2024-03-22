/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.Hinter;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.core.util.StringDistance;
import imconfig.Configuration;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


/**
 * Provides suggestions and information related to available steps and properties for Wakamiti.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class StepHinter implements Hinter {


    private final List<RunnableStep> runnableSteps;
    private final List<String> properties;
    private final RunnableStepResolver stepResolver;
    private final WakamitiDataTypeRegistry typeRegistry;
    private final Locale defaultTextLocale;
    private final Locale defaultDataLocale;


    public StepHinter(
            List<RunnableStep> runnableSteps,
            Configuration configuration,
            RunnableStepResolver stepResolver,
            WakamitiDataTypeRegistry typeRegistry
    ) {
        this.runnableSteps = runnableSteps;
        this.properties = configuration.keyStream().collect(toList());
        this.stepResolver = stepResolver;
        this.typeRegistry = typeRegistry;
        this.defaultTextLocale =
                configuration.get(WakamitiConfiguration.LANGUAGE, String.class)
                        .map(Locale::forLanguageTag)
                        .orElse(Locale.ENGLISH)
        ;
        this.defaultDataLocale =
                configuration.get(WakamitiConfiguration.DATA_FORMAT_LANGUAGE, String.class)
                        .map(Locale::forLanguageTag)
                        .orElse(this.defaultTextLocale)
        ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getExpandedAvailableSteps() {
        return getAvailableSteps(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getCompactAvailableSteps() {
        return getAvailableSteps(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAvailableProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidStep(String step) {
        return isValidStep(step, defaultTextLocale, defaultDataLocale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStepProviderByDefinition(String step) {
        return getStepProviderByDefinition(step, defaultTextLocale);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Retrieves the available steps with or without variations.
     *
     * @param includeVariations If true, includes variations; otherwise, returns only unique steps.
     * @return List of available steps.
     */
    public List<String> getAvailableSteps(boolean includeVariations) {
        return getHintsForInvalidStep("", -1, includeVariations);
    }

    /**
     * Checks if a step is valid for a given locale.
     *
     * @param stepLiteral The step to check.
     * @param textLocale  The text locale.
     * @param dataLocale  The data locale.
     * @return True if the step is valid, false otherwise.
     */
    public boolean isValidStep(String stepLiteral, Locale textLocale, Locale dataLocale) {
        try {
            return stepResolver.locateRunnableStep(stepLiteral, textLocale, dataLocale, this) != null;
        } catch (UndefinedStepException e) {
            return false;
        }
    }

    /**
     * Retrieves the provider for a given step definition and text locale.
     *
     * @param step       The step definition.
     * @param textLocale The text locale.
     * @return The step provider.
     */
    public String getStepProviderByDefinition(String step, Locale textLocale) {
        return stepResolver
                .obtainRunnableStepByDefinition(step, textLocale)
                .map(RunnableStep::getProvider)
                .orElse("");
    }

    /**
     * Retrieves hints for an invalid step based on the specified locales, number of hints, and variations.
     *
     * @param invalidStep       The invalid step.
     * @param textLocale        The text locale.
     * @param dataLocale        The data locale.
     * @param numberOfHints     The number of hints to retrieve.
     * @param includeVariations If true, includes variations; otherwise, returns only unique hints.
     * @return List of hints for the invalid step.
     */
    public List<String> getHintsForInvalidStep(
            String invalidStep,
            Locale textLocale,
            Locale dataLocale,
            int numberOfHints,
            boolean includeVariations
    ) {
        Set<String> stepHints = new HashSet<>();
        Map<? extends WakamitiDataType<?>, Pattern> types = typeRegistry.getTypes()
                .stream()
                .collect(Collectors.toMap(
                        x -> x,
                        type -> Pattern.compile("\\{[^:]*:?" + type.getName() + "}")
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

    /**
     * Gets a hint for an invalid step for a given text locale and data locale.
     *
     * @param invalidStep The invalid step.
     * @param textLocale  The text locale.
     * @param dataLocale  The data locale.
     * @return The hint for the invalid step.
     */
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


    /**
     * Populates a list of step hints with variations based on WakamitiDataType hints.
     *
     * @param stepHint   The original step hint.
     * @param dataLocale The locale for which hints should be generated.
     * @param types      A map of WakamitiDataType patterns to their corresponding regex patterns.
     * @return A list of step hints with variations based on WakamitiDataType hints.
     */
    private List<String> populateStepHintWithTypeHints(
            String stepHint,
            Locale dataLocale,
            Map<? extends WakamitiDataType<?>, Pattern> types
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