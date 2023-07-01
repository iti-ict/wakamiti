/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.groovy;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.Document;

/**
 * The groovy steps' definition.
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
@I18nResource("iti_wakamiti_wakamiti-groovy")
@Extension(provider =  "es.iti.wakamiti", name = "groovy-steps")
public class GroovyStepContributor implements StepContributor {

    /**
     * Runs the given groovy script with the available variables:
     * <ul>
     * <li>{@code log} - The {@link org.slf4j.Logger} groovy</li>
     * <li>{@code ctx} - The {@link java.util.Map} context with the current
     * scenario running information</li>
     * </ul>
     *
     * <p> Examples:
     * <blockquote><pre>
     * <code>@ID-01</code>
     * Scenario: Example scenario
     *   When the following groovy code is executed:
     *     """groovy
     *     ctx['a'] = 'something'
     *     1+1
     *     """
     *   And the following groovy code is executed:
     *     """groovy
     *     log.debug("Context: {}", ctx)
     *     assert ctx.results[0] == 2
     *     assert ctx.a == 'something'
     *     assert ctx.id == 'ID-01'
     *     """
     * </pre></blockquote>
     *
     *
     * @param document The script content
     * @return The script return object
     */
    @Step("groovy.execute")
    public Object execute(Document document) {
        return GroovyHelper.executeScript(document.getContent());
    }

}
