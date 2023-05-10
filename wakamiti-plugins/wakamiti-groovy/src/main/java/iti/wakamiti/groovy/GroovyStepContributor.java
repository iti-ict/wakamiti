/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.groovy;

import iti.commons.jext.Extension;
import iti.wakamiti.api.WakamitiStepRunContext;
import iti.wakamiti.api.annotations.I18nResource;
import iti.wakamiti.api.annotations.Step;
import iti.wakamiti.api.extensions.StepContributor;
import iti.wakamiti.api.plan.Document;

/**
 * The groovy steps' definition.
 *
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
@I18nResource("iti_wakamiti_wakamiti-groovy")
@Extension(provider = "iti.wakamiti", name = "groovy-steps")
public class GroovyStepContributor implements StepContributor {

    /**
     * Runs the given groovy script with the available variables:
     * <ul>
     * <li>{@code log} - The {@link org.slf4j.Logger} groovy</li>
     * <li>{@code ctx} - The {@link WakamitiStepRunContext} with the current
     * scenario running information</li>
     * </ul>
     *
     * <p> Examples:
     * <blockquote><pre>{@code
     * When the following groovy code is executed:
     *   """groovy
     *   1+1
     *   """
     * And the following groovy code is executed:
     *   """groovy
     *   log.debug("Results: {}", ctx.backend().getResults())
     *   assert ctx.backend().getResults()[0] == 2
     *   """
     * }</pre></blockquote>
     *
     * @param document The script content
     * @return The script return object
     */
    @Step("groovy.execute")
    public Object execute(Document document) {
        return GroovyHelper.executeScript(document.getContent());
    }

}
