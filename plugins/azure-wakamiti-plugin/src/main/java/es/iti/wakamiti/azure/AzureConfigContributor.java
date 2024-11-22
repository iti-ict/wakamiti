/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.api.model.TestPlan;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import static es.iti.wakamiti.api.WakamitiConfiguration.ID_TAG_PATTERN;
import static es.iti.wakamiti.api.WakamitiConfiguration.STRICT_TEST_CASE_ID;


@Extension(provider = "es.iti.wakamiti", name = "azure-config", version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor")
public class AzureConfigContributor implements ConfigContributor<AzureSynchronizer> {

    public static final String AZURE_ENABLED = "azure.enabled";
    public static final String AZURE_BASE_URL = "azure.baseURL";
    public static final String AZURE_AUTH_USERNAME = "azure.auth.username";
    public static final String AZURE_AUTH_PASSWORD = "azure.auth.password";
    public static final String AZURE_AUTH_TOKEN = "azure.auth.token";
    public static final String AZURE_API_VERSION = "azure.apiVersion";
    public static final String AZURE_ORGANIZATION = "azure.organization";
    public static final String AZURE_PROJECT = "azure.project";
    public static final String AZURE_CONFIGURATION = "azure.configuration";
    public static final String AZURE_PLAN = "azure.plan";
    public static final String AZURE_PLAN_NAME = "azure.plan.name";
    public static final String AZURE_PLAN_AREA = "azure.plan.area";
    public static final String AZURE_PLAN_ITERATION = "azure.plan.iteration";
    public static final String AZURE_SUITE_BASE = "azure.suiteBase";
    public static final String AZURE_TAG = "azure.tag";
    public static final String AZURE_TEST_CASE_PER_FEATURE = "azure.testCasePerFeature";
    public static final String AZURE_CREATE_ITEMS_IF_ABSENT = "azure.createItemsIfAbsent";

    public static final String DEFAULT_AZURE_API_VERSION = "6.0-preview";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                AZURE_ENABLED, Boolean.TRUE.toString(),
                AZURE_API_VERSION, DEFAULT_AZURE_API_VERSION,
                AZURE_TEST_CASE_PER_FEATURE, Boolean.FALSE.toString(),
                AZURE_CREATE_ITEMS_IF_ABSENT, Boolean.TRUE.toString()
        );
    }

    @Override
    public Configurer<AzureSynchronizer> configurer() {
        return this::configure;
    }

    private void configure(AzureSynchronizer synchronizer, Configuration configuration) {
        requiredProperty(configuration, AZURE_ENABLED, Boolean.class, synchronizer::enabled);
        requiredProperty(configuration, AZURE_BASE_URL, URL.class, synchronizer::baseURL);

        credentials(configuration).ifPresent(c -> synchronizer.setCredentialsAuthenticator(c.key(), c.value()));
        configuration.get(AZURE_AUTH_TOKEN, String.class).ifPresent(synchronizer::setTokenAuthenticator);

        requiredProperty(configuration, AZURE_ORGANIZATION, String.class, synchronizer::organization);
        requiredProperty(configuration, AZURE_PROJECT, String.class, synchronizer::project);
        requiredProperty(configuration, AZURE_API_VERSION, String.class, synchronizer::version);
        configuration.get(AZURE_CONFIGURATION, String.class).ifPresent(synchronizer::configuration);
        synchronizer.testPlan(plan(configuration));
        configuration.get(AZURE_SUITE_BASE, String.class).ifPresent(synchronizer::suiteBase);
        configuration.get(AZURE_TAG, String.class).ifPresent(synchronizer::tag);
        requiredProperty(configuration, AZURE_TEST_CASE_PER_FEATURE, Boolean.class, v -> {
            if (v && !configuration.get(STRICT_TEST_CASE_ID, Boolean.class).orElse(false)) {
                throw new WakamitiException("The property '{}' must be enabled", STRICT_TEST_CASE_ID);
            }
            synchronizer.testCasePerFeature(v);
        });
        requiredProperty(configuration, AZURE_CREATE_ITEMS_IF_ABSENT, Boolean.class, synchronizer::createItemsIfAbsent);
        requiredProperty(configuration, ID_TAG_PATTERN, String.class, synchronizer::idTagPattern);
    }

    private Optional<Pair<String, String>> credentials(Configuration configuration) {
        String password = configuration.get(AZURE_AUTH_PASSWORD, String.class).orElse("");
        return configuration.get(AZURE_AUTH_USERNAME, String.class).map(user -> new Pair<>(user, password));
    }

    private TestPlan plan(Configuration configuration) {
        if (configuration.inner(AZURE_PLAN).asMap().isEmpty()) {
            throw new WakamitiException("Property '{}' is required", AZURE_PLAN);
        }
        TestPlan plan = new TestPlan();
        requiredProperty(configuration, AZURE_PLAN_NAME, String.class, plan::name);
        requiredProperty(configuration, AZURE_PLAN_AREA, Path.class, plan::area);
        requiredProperty(configuration, AZURE_PLAN_ITERATION, Path.class, plan::iteration);
        return plan;
    }

    private <T> void requiredProperty(Configuration config, String property, Class<T> type, Consumer<T> setter) {
        T value = config.get(property, type)
                .orElseThrow(() -> new WakamitiException("Property '{}' is required", property));
        setter.accept(value);
    }

}
