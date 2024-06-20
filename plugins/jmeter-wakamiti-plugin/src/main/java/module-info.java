/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.plugins.jmeter.JMeterConfigContributor;
import es.iti.wakamiti.plugins.jmeter.JMeterStepContributor;

module es.iti.wakamiti.plugins.jmeter {

    exports es.iti.wakamiti.plugins.jmeter;
    exports es.iti.wakamiti.plugins.jmeter.datatypes;
    exports es.iti.wakamiti.plugins.jmeter.dsl;

    requires es.iti.wakamiti.api;
    requires jmeter.java.dsl;
    requires org.apache.httpcomponents.httpcore;
    requires org.hamcrest;
    requires org.apache.commons.lang3;
    requires ApacheJMeter.core;

    uses ConfigContributor;
    uses StepContributor;

    provides ConfigContributor with JMeterConfigContributor;
    provides StepContributor with JMeterStepContributor;

}