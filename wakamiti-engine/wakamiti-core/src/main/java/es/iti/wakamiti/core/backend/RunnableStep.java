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
 * Represents a RunnableStep used in test execution.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
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

    /**
     * Gets the translated definition for a specific locale.
     *
     * @param locale The locale for which the definition is required.
     * @return The translated definition.
     * @throws WakamitiException If the definition file or key is not found for the given locale.
     */
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

    /**
     * Creates and returns a matcher for the step definition.
     *
     * @param modelStep    The model step.
     * @param stepLocale   The locale for the step.
     * @param dataLocale   The locale for the test data.
     * @param typeRegistry The data type registry.
     * @return The created matcher.
     */
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

    /**
     * Runs the step with the given invoking arguments.
     *
     * @param invokeArguments The arguments to be passed to the step.
     * @return The result of running the step.
     * @throws WakamitiException If there is an error while running the step.
     */
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

    /**
     * Gets the arguments associated with the step.
     *
     * @return The arguments. They represent the input parameters required by the step.
     */
    public BackendArguments getArguments() {
        return arguments;
    }

    /**
     * Gets the definition key for the step.
     *
     * @return The definition key. It uniquely identifies the step's definition within the definition file.
     */
    public String getDefinitionKey() {
        return definitionKey;
    }

    /**
     * Gets the step provider.
     *
     * @return The step provider. It indicates the source or origin of the step.
     */
    public String getProvider() {
        return stepProvider;
    }

}