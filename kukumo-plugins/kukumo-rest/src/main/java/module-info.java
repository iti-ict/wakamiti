/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.rest.RestConfigContributor;
import iti.kukumo.rest.RestStepContributor;

module kukumo.rest {

    exports iti.kukumo.rest;

    requires transitive kukumo.api;
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

    uses ConfigContributor;
    uses StepContributor;

    provides ConfigContributor with RestConfigContributor;
    provides StepContributor with RestStepContributor;

}