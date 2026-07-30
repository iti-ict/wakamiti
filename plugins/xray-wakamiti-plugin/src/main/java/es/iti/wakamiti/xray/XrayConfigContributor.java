/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray;


import java.net.URL;
import java.util.HashSet;
import java.util.function.Consumer;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.wakamiti.xray.model.TestPlan;


/**
 * Applies Xray/Jira synchronization configuration to
 * {@link XRaySynchronizer}.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "xray-config",
        version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class XrayConfigContributor implements ConfigContributor<XRaySynchronizer> {

    /** Configuration key that enables the Xray synchronization observer. */
    public static final String XRAY_ENABLED = "xray.enabled";
    /** Configuration key for the Xray Cloud API base URL. */
    public static final String XRAY_BASE_URL = "xray.baseURL";
    /** Configuration key for the Jira project that owns synchronized Xray issues. */
    public static final String XRAY_PROJECT = "xray.project";
    /** Configuration key for the Xray Cloud API client identifier. */
    public static final String XRAY_CREDENTIALS_CLIENT_ID = "xray.auth.credentials.client-id";
    /** Configuration key for the Xray Cloud API client secret. */
    public static final String XRAY_CREDENTIALS_CLIENT_SECRET = "xray.auth.credentials.client-secret";
    /** Configuration key for Base64-encoded Jira Basic credentials. */
    public static final String JIRA_CREDENTIALS = "jira.auth.credentials";
    /** Configuration key for the Jira instance base URL. */
    public static final String JIRA_BASE_URL = "jira.baseURL";
    /** Parent configuration key containing the target Xray test-plan definition. */
    public static final String XRAY_PLAN = "xray.plan";
    /** Reserved configuration key for an Xray test-plan identifier. */
    public static final String XRAY_PLAN_ID = "xray.plan.id";
    /** Configuration key for the Jira summary used to locate the target test plan. */
    public static final String XRAY_PLAN_SUMMARY = "xray.plan.summary";
    /** Configuration key for the optional label used to filter synchronized tests. */
    public static final String XRAY_TAG = "xray.tag";
    /** Configuration key controlling automatic creation of a missing test plan. */
    public static final String XRAY_CREATE_ITEMS_IF_ABSENT = "xray.createItemsIfAbsent";
    /** Configuration key selecting feature-level instead of scenario-level tests. */
    public static final String XRAY_TEST_CASE_PER_FEATURE = "xray.testCasePerFeature";
    /** Configuration key for the source base removed from generated test-set paths. */
    public static final String XRAY_SUITE_BASE = "xray.suiteBase";
    /** Configuration key containing report-path globs to upload as attachments. */
    public static final String XRAY_ATTACHMENTS = "xray.attachments";

    @Override
    public boolean accepts(
            Object contributor
    ) {
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

    /**
     * Binds required and optional configuration properties to the synchronizer.
     *
     * @param synchronizer synchronizer instance to configure
     * @param configuration effective runtime configuration
     * @throws WakamitiException when required properties are missing
     */
    private void configure(
            XRaySynchronizer synchronizer,
            Configuration configuration
    ) {
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

    /**
     * Builds the target Xray test-plan descriptor from configuration.
     *
     * @param configuration effective runtime configuration
     * @return resolved test plan descriptor
     * @throws WakamitiException when the test plan section is missing
     */
    private TestPlan plan(
            Configuration configuration
    ) {
        if (configuration.inner(XRAY_PLAN).asMap().isEmpty()) {
            throw new WakamitiException("Property '{}' is required", XRAY_PLAN);
        }
        TestPlan plan = new TestPlan();
        requiredProperty(configuration, XRAY_PLAN_SUMMARY, String.class, s -> plan.getJira().summary(s));
        return plan;
    }

    /**
     * Resolves a required property and forwards its value to a setter.
     *
     * @param config configuration source
     * @param property required property key
     * @param type expected property type
     * @param setter consumer invoked with the resolved value
     * @param <T> property type
     * @throws WakamitiException when the property is absent
     */
    private <T> void requiredProperty(
            Configuration config,
            String property,
            Class<T> type,
            Consumer<T> setter
    ) {
        T value = config.get(property, type)
                .orElseThrow(() -> new WakamitiException("Property '{}' is required", property));
        setter.accept(value);
    }

}
