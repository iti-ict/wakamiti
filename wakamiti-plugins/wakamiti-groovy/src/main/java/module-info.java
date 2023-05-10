/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import iti.wakamiti.api.extensions.ConfigContributor;
import iti.wakamiti.api.extensions.LoaderContributor;
import iti.wakamiti.api.extensions.PropertyEvaluator;
import iti.wakamiti.api.extensions.StepContributor;
import iti.wakamiti.groovy.GroovyLoaderContributor;
import iti.wakamiti.groovy.GroovyPropertyEvaluator;
import iti.wakamiti.groovy.GroovyStepContributor;

module wakamiti.groovy {

    exports iti.wakamiti.groovy;

    requires transitive wakamiti.api;
    requires org.codehaus.groovy;
    requires iti.commons.jext;
//    requires junit;

    uses ConfigContributor;
    uses StepContributor;

    provides StepContributor with GroovyStepContributor;
    provides LoaderContributor with GroovyLoaderContributor;
    provides PropertyEvaluator with GroovyPropertyEvaluator;

}