/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.allure;


import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;


@I18nResource("sample-steps")
public class SampleSteps implements StepContributor {

    @Step("step.pass")
    public void pass() {
        // no-op
    }

    @Step("step.fail")
    public void fail() {
        throw new AssertionError("Synthetic failure for Allure");
    }

    @Step("step.error")
    public void error() {
        throw new IllegalStateException("Synthetic error for Allure");
    }
}
