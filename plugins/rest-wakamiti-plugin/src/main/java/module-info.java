/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.rest.RestConfigContributor;
import es.iti.wakamiti.rest.RestStepContributor;

module es.iti.wakamiti.rest {

    exports es.iti.wakamiti.rest;
    exports es.iti.wakamiti.rest.oauth;

    requires transitive es.iti.wakamiti.api;
    requires transitive rest.assured;
    requires transitive xml.path;
    requires json.path;
    requires com.fasterxml.jackson.databind;
    requires iti.commons.jext;
    requires junit;
    requires com.fasterxml.jackson.dataformat.xml;
    requires org.apache.commons.lang3;
    requires org.hamcrest;
    requires org.everit.json.schema;
    requires java.xml;
    requires org.apache.xmlbeans;
    requires json;

    uses ConfigContributor;
    uses StepContributor;

    provides ConfigContributor with RestConfigContributor;
    provides StepContributor with RestStepContributor;

}