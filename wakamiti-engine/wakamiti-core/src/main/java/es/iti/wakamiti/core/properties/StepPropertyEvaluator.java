/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.properties;


import com.fasterxml.jackson.databind.JsonNode;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.extensions.PropertyEvaluator;
import es.iti.wakamiti.api.util.JsonUtils;
import es.iti.wakamiti.api.util.XmlUtils;
import es.iti.wakamiti.core.backend.RunnableBackend;
import org.apache.xmlbeans.XmlObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Allows eval previous steps results.
 *
 * <p> Pattern: {@code ${[step number]#[xpath/jsonpath expression]}}
 *
 * <p> Examples:
 * <blockquote><pre>
 *     ${4#$.body.items[0].id}
 *     ${2#}
 * </pre></blockquote>
 *
 * @author Maria Galbis Calomarde | mgalbis@iti.es
 * @see PropertyEvaluator
 */
@Extension(provider = "es.iti.wakamiti", name = "step-property-resolver",
        extensionPoint = "es.iti.wakamiti.api.extensions.PropertyEvaluator", priority = 2)
public class StepPropertyEvaluator extends PropertyEvaluator {

    /**
     * Defines the pattern to identify property placeholders in a string.
     *
     * @return A {@link Pattern} object representing the property pattern.
     */
    @Override
    public Pattern pattern() {
        return Pattern.compile("\\$\\{(?<name>((-?\\d+)#((?!\\$\\{|\\}).)*))\\}");
    }

    /**
     * Evaluates the property by extracting information from the results
     * of previous steps.
     *
     * @param property The property to evaluate.
     * @param matcher  The matcher containing the property information.
     * @return The evaluated property value.
     * @throws WakamitiException If the property cannot be resolved.
     */
    @Override
    public String evalProperty(String property, Matcher matcher) {
        String name = matcher.group("name");
        WakamitiStepRunContext context = WakamitiStepRunContext.current();
        int step = Integer.parseInt(matcher.group(3));
        List<?> steps = Optional.ofNullable(context.backend().getExtraProperties()
                        .get(RunnableBackend.ContextMap.RESULTS_PROP))
                .map(List.class::cast)
                .orElse(new LinkedList<>());
        Object result = steps.get(step < 0 ? steps.size() + step : step - 1);
        String fragment = name.replaceAll("^-?\\d+#", "").trim();
        String evaluation = Objects.toString(result);
        if (!fragment.isBlank()) {
            try {
                if (isJson(result)) {
                    evaluation = JsonUtils.readStringValue(JsonUtils.json(result.toString()), fragment);
                } else if (isXml(result)) {
                    evaluation = XmlUtils.readStringValue(XmlUtils.xml(result.toString()), fragment);
                } else {
                    throw new IllegalArgumentException(fragment);
                }
            } catch (Exception e) {
                throw new WakamitiException("Not resolvable property: " + property, e);
            }
        }
        return evaluation;
    }

    /**
     * Checks if the given object is in JSON format.
     *
     * @param object The object to check.
     * @return {@code true} if the object is in JSON format, {@code false} otherwise.
     */
    private boolean isJson(Object object) {
        return object instanceof JsonNode || (object instanceof String && isJson((String) object));
    }

    /**
     * Checks if the given string is in JSON format.
     *
     * @param string The string to check.
     * @return {@code true} if the string is in JSON format, {@code false} otherwise.
     */
    private boolean isJson(String string) {
        try {
            JsonUtils.json(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the given object is in XML format.
     *
     * @param object The object to check.
     * @return {@code true} if the object is in XML format, {@code false} otherwise.
     */
    private boolean isXml(Object object) {
        return object instanceof XmlObject || (object instanceof String && isXml((String) object));
    }

    /**
     * Checks if the given string is in XML format.
     *
     * @param string The string to check.
     * @return {@code true} if the string is in XML format, {@code false} otherwise.
     */
    private boolean isXml(String string) {
        try {
            XmlUtils.xml(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
