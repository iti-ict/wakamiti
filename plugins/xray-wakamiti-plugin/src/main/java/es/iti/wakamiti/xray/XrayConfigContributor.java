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

import java.util.function.BiConsumer;


@Extension(
        provider = "es.iti.wakamiti",
        name = "xray-config",
        version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class XrayConfigContributor implements ConfigContributor<XRayReporter> {

    public static final String XRAY_DISABLED = "xray.disabled";
    public static final String XRAY_HOST = "xray.host";
    public static final String XRAY_PROJECT = "xray.project";
    public static final String XRAY_CREDENTIALS_CLIENT_ID = "xray.credentials.client-id";
    public static final String XRAY_CREDENTIALS_CLIENT_SECRET = "xray.credentials.client-secret";
    public static final String XRAY_TAG = "xray.tag";
    public static final String DEFAULT_XRAY_TAG = "XRay";


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof XRayReporter;
    }


    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                XRAY_DISABLED, "false",
                XRAY_HOST, "https://eu.xray.cloud.getxray.app",
                XRAY_PROJECT, "",
                XRAY_CREDENTIALS_CLIENT_ID, "",
                XRAY_CREDENTIALS_CLIENT_SECRET, "",
                XRAY_TAG, DEFAULT_XRAY_TAG
        );
    }


    @Override
    public Configurer<XRayReporter> configurer() {
        return this::configure;
    }


    private void configure(XRayReporter xRayReporter, Configuration configuration) {
        xRayReporter.setDisabled(configuration.get(XRAY_DISABLED, Boolean.class).orElse(Boolean.FALSE));
        xRayReporter.setCredentialsClientId(configuration.get(XRAY_CREDENTIALS_CLIENT_ID, String.class).orElse(""));
        xRayReporter.setCredentialsClientSecret(configuration.get(XRAY_CREDENTIALS_CLIENT_SECRET, String.class).orElse(""));
        xRayReporter.setXRayTag(configuration.get(XRAY_TAG, String.class).orElse(DEFAULT_XRAY_TAG));

        requiredProperty(configuration, xRayReporter, XRAY_HOST, XRayReporter::setHost);
        requiredProperty(configuration, xRayReporter, XRAY_PROJECT, XRayReporter::setProject);

    }

    private void requiredProperty(Configuration config, XRayReporter reporter, String property, BiConsumer<XRayReporter, String> setter) {
        String value = config.get(property, String.class).orElseThrow(() -> new WakamitiException("Property {} is required", property));
        setter.accept(reporter, value);
    }
}