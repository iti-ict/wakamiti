/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.properties;


import es.iti.wakamiti.api.imconfig.Configurable;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.PropertyEvaluator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Allows eval wakamiti configuration properties using a
 * regular expression pattern to match property
 * placeholders in a string.
 *
 * <p> Pattern: {@code ${[property name]}}
 *
 * <p> Examples:
 * <blockquote><pre>
 *     ${credential.name}
 *     ${credential.password}
 * </pre></blockquote>
 *
 * @author Maria Galbis Calomarde | mgalbis@iti.es
 * @see PropertyEvaluator
 */
@Extension(
        provider =  "es.iti.wakamiti", name = "global-property-resolver",
        extensionPoint =  "es.iti.wakamiti.api.extensions.PropertyEvaluator", priority = 1
)
public class GlobalPropertyEvaluator extends PropertyEvaluator implements Configurable {

    private Configuration configuration;

    /**
     * Configures the evaluator with the provided {@link Configuration}.
     *
     * @param configuration The configuration to use for property resolution.
     */
    @Override
    public void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Defines the pattern to identify property placeholders in a string.
     *
     * @return A {@link Pattern} object representing the property pattern.
     */
    @Override
    public Pattern pattern() {
        return Pattern.compile("\\$\\{(?<name>[\\w\\d-]+(\\.[\\w\\d-]+)*)\\}"); //NOSONAR
    }

    /**
     * Evaluates a property using the configured {@link Configuration}.
     *
     * @param property The property to evaluate.
     * @param matcher  The {@link Matcher} object containing the matched property name.
     * @return The resolved value of the property.
     * @throws WakamitiException If the property cannot be resolved.
     */
    @Override
    public String evalProperty(String property, Matcher matcher) {
        return configuration.get(matcher.group("name"), String.class)
                .orElseThrow(() -> new WakamitiException("Not resolvable property: " + property));
    }
}
