/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.xray;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.wakamiti.xray.model.TestPlan;

import java.net.URL;
import java.util.HashSet;
import java.util.function.Consumer;


@Extension(
        provider = "es.iti.wakamiti",
        name = "xray-config",
        version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class XrayConfigContributor implements ConfigContributor<XRaySynchronizer> {

    public static final String XRAY_ENABLED = "xray.enabled";
    public static final String XRAY_BASE_URL = "xray.baseURL";
    public static final String XRAY_PROJECT = "xray.project";
    public static final String XRAY_CREDENTIALS_CLIENT_ID = "xray.auth.credentials.client-id";
    public static final String XRAY_CREDENTIALS_CLIENT_SECRET = "xray.auth.credentials.client-secret";
    public static final String JIRA_CREDENTIALS = "jira.auth.credentials";
    public static final String JIRA_BASE_URL = "jira.baseURL";
    public static final String XRAY_PLAN = "xray.plan";
    public static final String XRAY_PLAN_ID = "xray.plan.id";
    public static final String XRAY_PLAN_SUMMARY = "xray.plan.summary";
    public static final String XRAY_TAG = "xray.tag";
    public static final String XRAY_CREATE_ITEMS_IF_ABSENT = "xray.createItemsIfAbsent";
    public static final String XRAY_TEST_CASE_PER_FEATURE = "xray.testCasePerFeature";
    public static final String XRAY_SUITE_BASE = "xray.suiteBase";
    public static final String XRAY_ATTACHMENTS = "xray.attachments";

    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof XRaySynchronizer;
    }

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                XRAY_ENABLED, "false",
                XRAY_BASE_URL, "https://eu.xray.cloud.getxray.app",
                XRAY_PROJECT, "",
                XRAY_CREDENTIALS_CLIENT_ID, "",
                XRAY_CREDENTIALS_CLIENT_SECRET, "",
                XRAY_TAG, ""
        );
    }

    @Override
    public Configurer<XRaySynchronizer> configurer() {
        return this::configure;
    }

    private void configure(XRaySynchronizer synchronizer, Configuration configuration) {
        requiredProperty(configuration, XRAY_ENABLED, Boolean.class, synchronizer::enabled);
        requiredProperty(configuration, XRAY_BASE_URL, URL.class, synchronizer::xRayBaseURL);
        requiredProperty(configuration, JIRA_BASE_URL, URL.class, synchronizer::jiraBaseURL);
        requiredProperty(configuration, XRAY_PROJECT, String.class, synchronizer::project);
        requiredProperty(configuration, XRAY_CREDENTIALS_CLIENT_ID, String.class, synchronizer::xRayclientId);
        requiredProperty(configuration, XRAY_CREDENTIALS_CLIENT_SECRET, String.class, synchronizer::xRayclientSecret);
        requiredProperty(configuration, JIRA_CREDENTIALS, String.class, synchronizer::jiraCredentials);
        synchronizer.attachments(new HashSet<>(configuration.getList(XRAY_ATTACHMENTS, String.class)));

        synchronizer.testPlan(plan(configuration));

        configuration.get(XRAY_SUITE_BASE, String.class).ifPresent(synchronizer::testSet);
        requiredProperty(configuration, XRAY_TEST_CASE_PER_FEATURE, Boolean.class, synchronizer::testCasePerFeature);

        configuration.get(XRAY_TAG, String.class).ifPresent(synchronizer::tag);
        configuration.get(XRAY_CREATE_ITEMS_IF_ABSENT, Boolean.class).ifPresent(synchronizer::createItemsIfAbsent);
    }

    private TestPlan plan(Configuration configuration) {
        if (configuration.inner(XRAY_PLAN).asMap().isEmpty()) {
            throw new WakamitiException("Property '{}' is required", XRAY_PLAN);
        }
        TestPlan plan = new TestPlan();
        requiredProperty(configuration, XRAY_PLAN_SUMMARY, String.class, s -> plan.getJira().summary(s));
        return plan;
    }

    private <T> void requiredProperty(Configuration config, String property, Class<T> type, Consumer<T> setter) {
        T value = config.get(property, type)
                .orElseThrow(() -> new WakamitiException("Property '{}' is required", property));
        setter.accept(value);
    }
}
