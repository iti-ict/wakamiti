/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.test;


import static es.iti.wakamiti.api.WakamitiConfiguration.NON_REGISTERED_STEP_PROVIDERS;
import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_PATH;
import static es.iti.wakamiti.api.WakamitiConfiguration.RESOURCE_TYPES;
import static es.iti.wakamiti.xray.XrayConfigContributor.JIRA_BASE_URL;
import static es.iti.wakamiti.xray.XrayConfigContributor.JIRA_CREDENTIALS;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_ATTACHMENTS;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_BASE_URL;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_CREATE_ITEMS_IF_ABSENT;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_CREDENTIALS_CLIENT_ID;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_CREDENTIALS_CLIENT_SECRET;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_ENABLED;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_PLAN;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_PLAN_ID;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_PLAN_SUMMARY;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_PROJECT;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_SUITE_BASE;
import static es.iti.wakamiti.xray.XrayConfigContributor.XRAY_TEST_CASE_PER_FEATURE;

import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;


@AnnotatedConfiguration({
        @Property(key = NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.xray.test.MockSteps"),
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources"),
        @Property(key = XRAY_ENABLED, value = "true"),
        @Property(key = XRAY_PROJECT, value = "W2"),
        @Property(key = XRAY_PLAN, value = ""),
        @Property(key = XRAY_PLAN_ID, value = ""),
        @Property(key = XRAY_PLAN_SUMMARY, value = "Sincronización con plugin Wakamiti"),
        @Property(key = XRAY_SUITE_BASE, value = "features"),
        @Property(key = XRAY_TEST_CASE_PER_FEATURE, value = "true"),
        @Property(key = XRAY_CREATE_ITEMS_IF_ABSENT, value = "true"),
        @Property(key = XRAY_ATTACHMENTS, value = "**/*.html"),
        @Property(key = XRAY_BASE_URL, value = "https://eu.xray.cloud.getxray.app"),
        @Property(key = JIRA_BASE_URL, value = "XXX"),
        @Property(key = XRAY_CREDENTIALS_CLIENT_ID, value = "XXX"),
        @Property(key = XRAY_CREDENTIALS_CLIENT_SECRET, value = "XXX"),
        @Property(key = JIRA_CREDENTIALS, value = "XXX")
})
//@RunWith(WakamitiJUnitRunner.class)
public class TestRunConfig {

}
