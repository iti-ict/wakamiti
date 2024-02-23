/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.Backend;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.util.LocaleLoader;
import imconfig.Configuration;
import org.slf4j.Logger;

import java.util.List;
import java.util.Locale;


/**
 * Abstract implementation of the {@link Backend} interface, providing common functionality
 * for backends without running capabilities.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public abstract class AbstractBackend implements Backend {

    protected static final Logger LOGGER = Wakamiti.LOGGER;

    protected final Configuration configuration;
    protected final WakamitiDataTypeRegistry typeRegistry;
    protected final List<RunnableStep> runnableSteps;
    protected final RunnableStepResolver resolver;
    protected final StepHinter hinter;


    protected AbstractBackend(
        Configuration configuration,
        WakamitiDataTypeRegistry typeRegistry,
        List<RunnableStep> steps
    ) {
        this.configuration = configuration;
        this.typeRegistry = typeRegistry;
        this.runnableSteps = steps;
        this.resolver = new RunnableStepResolver(typeRegistry, steps);
        this.hinter = new StepHinter(runnableSteps, configuration, resolver, typeRegistry);
    }



    @Override
    public WakamitiDataTypeRegistry getTypeRegistry() {
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
        return hinter.getHintsForInvalidStep(invalidStep, locale, locale, numberOfHints, includeVariations);
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
        Map<? extends WakamitiDataType<?>, Pattern> types
    ) {
        List<String> variants = new ArrayList<>();
        for (Map.Entry<? extends WakamitiDataType<?>, Pattern> type : types.entrySet()) {
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
            WakamitiConfiguration.DATA_FORMAT_LANGUAGE,
            configuration.get(WakamitiConfiguration.DATA_FORMAT_LANGUAGE, String.class).orElse(null)
        );
        return dataFormatLocale == null ?
            fallbackLocale :
            LocaleLoader.forLanguage(dataFormatLocale)
        ;
    }

}