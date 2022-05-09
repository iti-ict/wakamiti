/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;

import iti.kukumo.api.KukumoAPI;
import iti.kukumo.api.util.Either;
import iti.kukumo.api.util.Pair;
import iti.kukumo.api.util.ResourceLoader;
import iti.kukumo.api.util.ThrowableRunnable;
import iti.kukumo.core.Kukumo;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;



public class RunnableStep {

    private final String definitionFile;
    private final String definitionKey;
    private final Map<Locale, String> translatedDefinitions = new HashMap<>();
    private final BackendArguments arguments;
    private final ThrowableRunnable executor;
    private final ResourceLoader resourceLoader = new ResourceLoader();
    private final String stepProvider;


    public RunnableStep(
        String definitionFile,
        String definitionKey,
        BackendArguments arguments,
        ThrowableRunnable stepExecutor,
        String stepProvider
    ) {
        this.definitionFile = definitionFile;
        this.definitionKey = definitionKey;
        this.arguments = arguments;
        this.executor = stepExecutor;
        this.stepProvider = stepProvider;
    }


    public String getTranslatedDefinition(Locale locale) {
        String translatedDefinition = translatedDefinitions.get(locale);
        if (translatedDefinition == null) {
            ResourceBundle resourceBundle = resourceLoader.resourceBundle(definitionFile, locale);
            if (resourceBundle == null) {
                throw new KukumoException(
                    "Cannot find step definition file {} for locale {}",
                    definitionFile,
                    locale
                );
            }
            translatedDefinition = resourceBundle.getString(definitionKey).trim();
            if (translatedDefinition == null) {
                throw new KukumoException(
                    "Cannot find step definition entry '{}' in file '{}' for locale {}",
                    definitionKey,
                    definitionFile,
                    locale
                );
            }
            translatedDefinitions.put(locale, translatedDefinition);
        }
        return translatedDefinition;
    }


    public Matcher matcher(
        Either<PlanNode,String> modelStep,
        Locale stepLocale,
        Locale dataLocale,
        KukumoDataTypeRegistry typeRegistry
    ) {
        String translatedDefinition = getTranslatedDefinition(stepLocale);
        return ExpressionMatcher
            .matcherFor(translatedDefinition, typeRegistry, dataLocale, modelStep);
    }


    public void run(Map<String, Object> invokeArguments) {

        boolean error = false;
        // re-arrange argument order
        if (invokeArguments.size() != this.arguments.size()) {
            error = true;
        }
        Object[] argumentArray = new Object[this.arguments.size()];
        for (int i = 0; i < argumentArray.length; i++) {
            Pair<String, String> argument = this.arguments.get(i);
            Object invokeArgument = invokeArguments.get(argument.key());
            if (invokeArgument == null) {
                error = true;
                break;
            } else {
                argumentArray[i] = invokeArgument;
            }
        }
        if (error) {
            throw new KukumoException(
                "Cannot run step: wrong arguments (expected {} but received {})",
                arguments,
                invokeArguments
            );
        } else {
            try {
                executor.run(argumentArray);
            } catch (InvocationTargetException e) {
                var originalException = e.getTargetException();
                if (originalException instanceof AssertionError) {
                    throw (AssertionError)originalException;
                } else {
                    throw new KukumoException(originalException);
                }
            } catch (Exception e) {
                throw new KukumoException(e);
            }
        }
    }


    public BackendArguments getArguments() {
        return arguments;
    }


    public String getDefinitionKey() {
        return definitionKey;
    }


	public String getProvider() {
		return stepProvider;
	}
}