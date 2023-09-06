/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;

import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;


@I18nResource("mock-steps")
public class MockSteps implements StepContributor {


    @Override
    public String info() {
        return "test";
    }

    @Step(value = "step.ok")
    public void ok() {

    }

    @Step(value = "step.fail")
    public void fail() {
        throw new AssertionError("step failed");
    }


}