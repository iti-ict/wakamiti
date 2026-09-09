/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package steps


import groovy.util.logging.Slf4j
import es.iti.wakamiti.api.annotations.I18nResource
import es.iti.wakamiti.api.annotations.Step
import es.iti.wakamiti.api.extensions.StepContributor

@Slf4j
@I18nResource("customs")
class CustomSteps implements StepContributor {


    @Step(value = "number.addition", args = ["x:int", "y:int"])
    def whatever(Integer x, Integer y) {
        def result = x + y
        log.info("{} + {} = {}", x, y, result)
    }


}