/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import imconfig.Configuration;
import org.slf4j.Logger;

import iti.kukumo.api.Backend;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.util.LocaleLoader;
import iti.kukumo.util.StringDistance;

/*
 * Partial implementation of Backend, without running capabilities
 */
public abstract class AbstractBackend implements Backend {

    protected static final Logger LOGGER = Kukumo.LOGGER;

    protected final Configuration configuration;
    protected final KukumoDataTypeRegistry typeRegistry;
    protected final List<RunnableStep> runnableSteps;


    protected AbstractBackend(
        Configuration configuration,
        KukumoDataTypeRegistry typeRegistry,
        List<RunnableStep> steps
    ) {
        this.configuration = configuration;
        this.typeRegistry = typeRegistry;
        this.runnableSteps = steps;
    }



    @Override
    public KukumoDataTypeRegistry getTypeRegistry() {
        return typeRegistry;
    }



    @Override
    public List<String> getAvailableSteps(Locale locale, boolean includeVariations) {
        return getSuggestionsForInvalidStep("",locale,-1,includeVariations);
    }



    @Override
    public List<String> getSuggestionsForInvalidStep(
        String invalidStep,
        Locale locale,
        int numberOfHints,
        boolean includeVariations
    ) {
        Set<String> stepHints = new HashSet<>();
        Map<? extends KukumoDataType<?>, Pattern> types = typeRegistry.getTypes().stream()
            .collect(Collectors.toMap(
                x -> x,
                type -> Pattern.compile("\\{[^:]*:?" + type.getName() + "\\}")
            ));
        for (RunnableStep runnableStep : runnableSteps) {
            String stepHint = runnableStep.getTranslatedDefinition(locale);
            if (includeVariations) {
                stepHints.addAll(populateStepHintWithTypeHints(stepHint, locale, types));
            } else {
                stepHints.add(stepHint);
            }
        }
        return StringDistance.closerStrings(invalidStep, stepHints, numberOfHints);
    }


    @Override
    public String getHintFor(String invalidStep, Locale locale) {
        int maxSuggestions = 5;
        StringBuilder hint = new StringBuilder(
            "Perhaps you mean one of the following:\n\t----------\n\t"
        );
        var allSuggestions = getSuggestionsForInvalidStep(invalidStep, locale, -1, true);
        if (allSuggestions.size() > maxSuggestions) {
            allSuggestions = getSuggestionsForInvalidStep(invalidStep, locale, maxSuggestions, false);
        }
        for (String stepHint : allSuggestions) {
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
        return dataFormatLocale == null ?
            fallbackLocale :
            LocaleLoader.forLanguage(dataFormatLocale)
        ;
    }

}