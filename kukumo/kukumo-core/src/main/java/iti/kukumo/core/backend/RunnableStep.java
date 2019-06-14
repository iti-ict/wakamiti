package iti.kukumo.core.backend;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.util.Pair;
import iti.kukumo.util.ThrowableRunnable;

public class RunnableStep {

    private final String definitionFile;
    private final String definitionKey;
    private final Map<Locale,String> translatedDefinitions = new HashMap<>();
    private final BackendArguments arguments;
    private final ThrowableRunnable executor;


    public RunnableStep(
            String definitionFile,
            String definitionKey,
            BackendArguments arguments,
            ThrowableRunnable stepExecutor
    ) {
        this.definitionFile = definitionFile;
        this.definitionKey = definitionKey;
        this.arguments = arguments;
        this.executor = stepExecutor;
    }



    public String getTranslatedDefinition(Locale locale) {
        String translatedDefinition = translatedDefinitions.get(locale);
        if (translatedDefinition == null) {
            ResourceBundle resourceBundle = Kukumo.getResourceLoader().resourceBundle(definitionFile,locale);
            if (resourceBundle == null) {
                throw new KukumoException("Cannot find step definition file {} for locale {}",
                        definitionFile,locale);
            }
            translatedDefinition = resourceBundle.getString(definitionKey).trim();
            if (translatedDefinition == null) {
                throw new KukumoException("Cannot find step definition entry '{}' in file '{}' for locale {}",
                        definitionKey,definitionFile,locale);
            }
            translatedDefinitions.put(locale, translatedDefinition);
        }
        return translatedDefinition;
    }



    public Matcher matcher(PlanStep modelStep, Locale stepLocale, Locale dataLocale, KukumoDataTypeRegistry typeRegistry) {
        String translatedDefinition = getTranslatedDefinition(stepLocale);
        return new ExpressionMatcher(translatedDefinition,typeRegistry,dataLocale).matcher(modelStep);
    }



    public void run (Map<String,Object> invokeArguments) throws Throwable {

        boolean error = false;
        // re-arrange argument order
        if (invokeArguments.size() != this.arguments.size()) {
            error = true;
        }
        Object[] argumentArray = new Object[this.arguments.size()];
        for (int i=0;i<argumentArray.length;i++) {
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
            throw new KukumoException("Cannot run step: wrong arguments (expected {} but received {})",
                    arguments, invokeArguments);
        } else {
            try {
                executor.run(argumentArray);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }


    public BackendArguments getArguments() {
        return arguments;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }
}
