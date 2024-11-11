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
import es.iti.wakamiti.api.util.ThrowableFunction;

import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;

import static es.iti.wakamiti.api.WakamitiConfiguration.ID_TAG_PATTERN;
import static es.iti.wakamiti.api.WakamitiConfiguration.STRICT_TEST_CASE_ID;


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
    public static final String XRAY_TAG = "xray.tag";
    public static final String XRAY_CREATE_ITEMS_IF_ABSENT = "xray.createItemsIfAbsent";
    public static final String XRAY_TIME_ZONE_ADJUSTMENT = "xray.timeZoneAdjustment";

    public static final String DEFAULT_XRAY_TAG = "XRay";


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof XRayReporter;
    }


    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                XRAY_ENABLED, "false",
                XRAY_BASE_URL, "https://eu.xray.cloud.getxray.app",
                XRAY_PROJECT, "",
                XRAY_CREDENTIALS_CLIENT_ID, "",
                XRAY_CREDENTIALS_CLIENT_SECRET, "",
                XRAY_TAG, DEFAULT_XRAY_TAG
        );
    }


    @Override
    public Configurer<XRaySynchronizer> configurer() {
        return this::configure;
    }


    private void configure(XRaySynchronizer synchronizer, Configuration configuration) {
        requiredProperty(configuration, XRAY_ENABLED, Boolean.class, synchronizer::enabled);
        requiredProperty(configuration, XRAY_BASE_URL, URL.class, synchronizer::baseURL);
        requiredProperty(configuration, XRAY_PROJECT, String.class, synchronizer::project);

        synchronizer.setCredentialsClientId(configuration.get(XRAY_CREDENTIALS_CLIENT_ID, String.class).orElse(""));
        synchronizer.setCredentialsClientSecret(configuration.get(XRAY_CREDENTIALS_CLIENT_SECRET, String.class).orElse(""));
        synchronizer.setXRayTag(configuration.get(XRAY_TAG, String.class).orElse(DEFAULT_XRAY_TAG));
        synchronizer.setCreateItemsIfAbsent(configuration.get(XRAY_CREATE_ITEMS_IF_ABSENT, Boolean.class).orElse(Boolean.TRUE));
        synchronizer.setTimeZoneAdjustment(configuration.get(XRAY_TIME_ZONE_ADJUSTMENT, Integer.class).orElse(0));

    }

    private void configure2(AzureSynchronizer synchronizer, Configuration configuration) {
        requiredProperty(configuration, AZURE_ENABLED, Boolean.class, synchronizer::enabled);
        requiredProperty(configuration, AZURE_BASE_URL, URL.class, synchronizer::baseURL);

        credentials(configuration).ifPresent(c -> synchronizer.setCredentialsAuthenticator(c.key(), c.value()));
        configuration.get(AZURE_AUTH_TOKEN, String.class).ifPresent(synchronizer::setTokenAuthenticator);

        requiredProperty(configuration, AZURE_ORGANIZATION, String.class, synchronizer::organization);
        requiredProperty(configuration, AZURE_PROJECT, String.class, synchronizer::project);
        requiredProperty(configuration, AZURE_API_VERSION, String.class, synchronizer::version);
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
        configuration.getList(AZURE_ATTACHMENTS, String.class).stream()
                .map((ThrowableFunction<String, Set<Path>>) Util::findFiles)
                .forEach(synchronizer::attachments);
        requiredProperty(configuration, ID_TAG_PATTERN, String.class, synchronizer::idTagPattern);
    }
    private void requiredProperty(Configuration config, XRayReporter reporter, String property, BiConsumer<XRayReporter, String> setter) {
        String value = config.get(property, String.class).orElseThrow(() -> new WakamitiException("Property {} is required", property));
        setter.accept(reporter, value);
    }
}