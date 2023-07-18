/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.util.*;
import es.iti.wakamiti.core.Wakamiti;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
public class RunnableStep {

    private static final Logger LOGGER = Wakamiti.LOGGER;
    private final String definitionFile;
    private final String definitionKey;
    private final Map<Locale, String> translatedDefinitions = new HashMap<>();
    private final BackendArguments arguments;
    private final ThrowableRunnable executor;
    private final ResourceLoader resourceLoader = WakamitiAPI.instance().resourceLoader();
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
                throw new WakamitiException(
                        "Cannot find step definition file {} for locale {}",
                        definitionFile,
                        locale
                );
            }
            try {
                translatedDefinition = resourceBundle.getString(definitionKey).trim();
            } catch (MissingResourceException e) {
                throw new WakamitiException(
                        "Cannot find step definition entry '{}' in file '{}' for locale {}",
                        definitionKey,
                        definitionFile,
                        locale,
                        e
                );
            }
            translatedDefinitions.put(locale, translatedDefinition);
        }
        return translatedDefinition;
    }


    public Matcher matcher(
            Either<PlanNode, String> modelStep,
            Locale stepLocale,
            Locale dataLocale,
            WakamitiDataTypeRegistry typeRegistry
    ) {
        String translatedDefinition = getTranslatedDefinition(stepLocale);
        return ExpressionMatcher
                .matcherFor(translatedDefinition, typeRegistry, dataLocale, modelStep);
    }


    public Object run(Map<String, Argument> invokeArguments) {

        boolean error = invokeArguments.size() != this.arguments.size();
        // re-arrange argument order
        Object[] argumentArray = new Object[this.arguments.size()];
        for (int i = 0; i < argumentArray.length; i++) {
            Pair<String, String> argument = this.arguments.get(i);
            Object invokeArgument = invokeArguments.get(argument.key()).resolve();
            if (invokeArgument == null) {
                error = true;
                break;
            } else {
                argumentArray[i] = invokeArgument;
            }
        }
        if (error) {
            throw new WakamitiException(
                    "Cannot run step: wrong arguments (expected {} but received {})",
                    arguments,
                    invokeArguments
            );
        } else {
            try {
                LOGGER.trace("Calling step [{}] with {}", this.definitionKey, Arrays.deepToString(argumentArray));
                Object result = executor.run(argumentArray);
                LOGGER.trace("Returning [{}]", result);
                return result;
            } catch (InvocationTargetException e) {
                var originalException = e.getTargetException();
                if (originalException instanceof AssertionError) {
                    throw (AssertionError) originalException;
                } else {
                    throw new WakamitiException(originalException);
                }
            } catch (Exception e) {
                throw new WakamitiException(e);
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