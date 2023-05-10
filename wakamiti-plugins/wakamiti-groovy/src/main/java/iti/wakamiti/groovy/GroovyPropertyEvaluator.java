/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.groovy;

import iti.commons.jext.Extension;
import iti.wakamiti.api.extensions.PropertyEvaluator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This {@link PropertyEvaluator} allows eval groovy script.
 *
 * <p> Pattern: {@code ${=[groovy script]}}
 *
 * <p> Examples:
 * <blockquote><pre>{@code
 *     ${=1+1}
 *     ${=new Date().format('yyyy-MM-dd')}
 * }</pre></blockquote>
 *
 * @author Maria Galbis Calomarde | mgalbis@iti.es
 */
@Extension(provider = "iti.wakamiti", name = "groovy-property-resolver",
        extensionPoint = "iti.wakamiti.api.extensions.PropertyEvaluator")
public class GroovyPropertyEvaluator extends PropertyEvaluator {

    @Override
    public Pattern pattern() {
        return Pattern.compile("\\$\\{=(?<name>(((?!\\$\\{).)+))\\}");
    }

    @Override
    public String evalProperty(String property, Matcher matcher) {
        return GroovyHelper.executeScript(matcher.group("name")).toString();
    }

}
