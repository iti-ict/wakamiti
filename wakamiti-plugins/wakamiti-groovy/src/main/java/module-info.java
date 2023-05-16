/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.LoaderContributor;
import es.iti.wakamiti.api.extensions.PropertyEvaluator;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.groovy.GroovyLoaderContributor;
import es.iti.wakamiti.groovy.GroovyPropertyEvaluator;
import es.iti.wakamiti.groovy.GroovyStepContributor;

module es.iti.wakamiti.groovy {

    exports es.iti.wakamiti.groovy;

    requires transitive es.iti.wakamiti.api;
    requires org.codehaus.groovy;
    requires iti.commons.jext;
//    requires junit;

    uses ConfigContributor;
    uses StepContributor;

    provides StepContributor with GroovyStepContributor;
    provides LoaderContributor with GroovyLoaderContributor;
    provides PropertyEvaluator with GroovyPropertyEvaluator;

}