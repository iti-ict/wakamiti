/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@I18nResource("steps/test-wakamiti-steps")
public class WakamitiSteps implements StepContributor {

    private static final Logger LOGGER = LoggerFactory.getLogger("es.iti.wakamiti.test");

    @Override
    public String info() {
        return "test";
    }

    @Step(value = "dummy.step")
    public void dummyStep() {
        LOGGER.info("This is a test");
    }

}