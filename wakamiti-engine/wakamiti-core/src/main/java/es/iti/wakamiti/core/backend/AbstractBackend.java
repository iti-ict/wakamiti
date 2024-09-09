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
import es.iti.wakamiti.api.imconfig.Configuration;
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

    /**
     * Constructs an abstract backend with the provided configuration, type registry,
     * and list of runnable steps.
     *
     * @param configuration The configuration for this backend.
     * @param typeRegistry  The registry for Wakamiti data types.
     * @param steps         The list of runnable steps associated with this backend.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public WakamitiDataTypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAvailableSteps(Locale locale, boolean includeVariations) {
        return getSuggestionsForInvalidStep("", locale, -1, includeVariations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSuggestionsForInvalidStep(
            String invalidStep,
            Locale locale,
            int numberOfHints,
            boolean includeVariations
    ) {
        return hinter.getHintsForInvalidStep(invalidStep, locale, locale, numberOfHints, includeVariations);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Retrieves the locale for data associated with the given model step.
     *
     * @param modelStep      The model step.
     * @param fallbackLocale The fallback locale.
     * @return The locale for data associated with the model step.
     */
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