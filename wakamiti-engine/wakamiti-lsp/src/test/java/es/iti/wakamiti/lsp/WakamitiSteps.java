/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package es.iti.wakamiti.lsp;


import es.iti.wakamiti.api.annotations.*;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.StepContributor;


@I18nResource("test-wakamiti-steps")
public class WakamitiSteps implements StepContributor {


    @Step(value = "given.set.of.numbers")
    public void setOfNumbers() {

    }


    @Step(value = "simple.step.with.multiple.asserts", args = { "a:integer-assertion", "b:integer",
                    "c:text-assertion" })
    public void simpleStepWithMultipleAsserts(Assertion<Integer> a, Long b, Assertion<String> c) {
        // nothing
    }

}