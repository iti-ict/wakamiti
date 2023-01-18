/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.groovy;

import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoStepRunContext;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.Document;

/**
 *
 */
@I18nResource("iti_kukumo_kukumo-groovy")
@Extension(provider = "iti.kukumo", name = "groovy-steps")
public class GroovyStepContributor implements StepContributor {

    /**
     * Run the given groovy script with the available variables:
     * <ul>
     *  <li>{@code log} - The {@link org.slf4j.Logger} groovy</li>
     *  <li>{@code ctx} - The {@link KukumoStepRunContext} with the current scenario running information</li>
     * </ul>
     *
     * <p> Examples:
     * <blockquote><pre>
     * When the following groovy code is executed:
     *   """groovy
     *   1+1
     *   """
     * Then the following groovy code is executed:
     *   """groovy
     *   log.debug("Results: {}", ctx.backend().getResults())
     *   assert ctx.backend().getResults()[0] == 2
     *   """
     * </pre></blockquote>
     *
     * @param document The script content
     * @return The script return object
     */
    @Step("groovy.execute")
    public Object execute(Document document) {
        return GroovyHelper.executeScript(document.getContent());
    }
}
