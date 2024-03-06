/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package other

import groovy.util.logging.Slf4j
import es.iti.wakamiti.api.annotations.I18nResource
import es.iti.wakamiti.api.annotations.Step
import es.iti.wakamiti.api.extensions.StepContributor
import es.iti.wakamiti.groovy.GroovyStepContributor

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
        return Wakamiti.contributors().getContributor(GroovyStepContributor.class);
    }

}