/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package other

import groovy.util.logging.Slf4j
import es.iti.wakamiti.api.annotations.I18nResource
import es.iti.wakamiti.api.extensions.StepContributor

@Slf4j
@I18nResource("customs")
class ErrorSteps implements StepContributor {

    def whatever(Integer x, Integer y) {
        def result = x + y
        log.info("{} + {} = {}", x, y, result)
    }

    def something(String name);

}