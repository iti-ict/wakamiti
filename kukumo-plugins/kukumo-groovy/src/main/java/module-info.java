/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.api.extensions.LoaderContributor;
import iti.kukumo.api.extensions.PropertyEvaluator;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.groovy.GroovyLoaderContributor;
import iti.kukumo.groovy.GroovyPropertyEvaluator;
import iti.kukumo.groovy.GroovyStepContributor;

module kukumo.groovy {

    exports iti.kukumo.groovy;

    requires transitive kukumo.api;
    requires org.codehaus.groovy;
    requires iti.commons.jext;
//    requires junit;

    uses ConfigContributor;
    uses StepContributor;

    provides StepContributor with GroovyStepContributor;
    provides LoaderContributor with GroovyLoaderContributor;
    provides PropertyEvaluator with GroovyPropertyEvaluator;

}