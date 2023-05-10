/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import iti.wakamiti.api.extensions.ConfigContributor;
import iti.wakamiti.api.extensions.StepContributor;
import iti.wakamiti.rest.RestConfigContributor;
import iti.wakamiti.rest.RestStepContributor;

module wakamiti.rest {

    exports iti.wakamiti.rest;
    exports iti.wakamiti.rest.oauth;

    requires transitive wakamiti.api;
    requires transitive rest.assured;
    requires transitive xml.path;
    requires org.json;
    requires json.path;
    requires com.fasterxml.jackson.databind;
    requires iti.commons.jext;
    requires junit;
    requires com.fasterxml.jackson.dataformat.xml;
    requires org.apache.commons.lang3;
    requires org.hamcrest;
    requires org.everit.json.schema;
    requires java.xml;
    requires plexus.utils;
    requires xmlbeans;

    uses ConfigContributor;
    uses StepContributor;

    provides ConfigContributor with RestConfigContributor;
    provides StepContributor with RestStepContributor;

}