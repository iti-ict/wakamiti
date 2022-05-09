/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.custom

import groovy.util.logging.Slf4j
import iti.kukumo.core.Kukumo
import iti.kukumo.api.annotations.I18nResource
import iti.kukumo.api.annotations.Step
import iti.kukumo.api.extensions.StepContributor
import iti.kukumo.test.gherkin.KukumoSteps

@Slf4j
@I18nResource("customs")
class CustomSteps implements StepContributor {

    @Step(value = "number.addition", args = ["x:int", "y:int"])
    def whatever(Integer x, Integer y) {
        def result = x + y
        log.info("{} + {} = {}", x, y, result)
    }

    @Step(value = "something", args = ["name:word"])
    def something(String name) {
        def contributor = getContributor();
        log.info("Contributor: {}", contributor.info())
        int month = new Date().format('MM') as int
        log.info("Month of year: {}", month)
        def result = month % 2 == 0 ? 'TABLA_PAR' : 'TABLA_IMPAR';
        log.info("Table {}: {}", name, result)
    }

    private def getContributor() {
        return Kukumo.contributors().getContributor(KukumoSteps.class);
    }
}