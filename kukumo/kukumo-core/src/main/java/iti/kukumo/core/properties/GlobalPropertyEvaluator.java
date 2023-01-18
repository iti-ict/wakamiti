/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.core.properties;

import imconfig.Configurable;
import imconfig.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.extensions.PropertyEvaluator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This {@link PropertyEvaluator} allows eval kukumo configuration
 * properties.
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
 */
@Extension(
        provider = "iti.kukumo", name = "global-property-resolver",
        extensionPoint = "iti.kukumo.api.extensions.PropertyEvaluator", priority = 1
)
public class GlobalPropertyEvaluator extends PropertyEvaluator implements Configurable {

    private Configuration configuration;

    @Override
    public void configure(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Pattern pattern() {
        return Pattern.compile("\\$\\{(?<name>[\\w\\d-]+(\\.[\\w\\d-]+)*)\\}"); //NOSONAR
    }

    @Override
    public String evalProperty(String property, Matcher matcher) {
        return configuration.get(matcher.group("name"), String.class)
                .orElseThrow(() -> new KukumoException("Not resolvable property: " + property));
    }
}
