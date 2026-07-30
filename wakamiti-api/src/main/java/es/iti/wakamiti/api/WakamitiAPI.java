/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import java.nio.file.Path;
import java.util.ServiceLoader;

import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.api.util.ResourceLoader;


/**
 * Provides access to various components and services within the
 * Wakamiti application.
 */
public interface WakamitiAPI {

    /**
     * Locates the active API implementation through Java's service-provider
     * mechanism.
     *
     * @return the first available Wakamiti API provider
     * @throws java.util.NoSuchElementException if no provider is visible to the
     *                                         current service loader
     */
    static WakamitiAPI instance() {
        return ServiceLoader.load(WakamitiAPI.class).findFirst().orElseThrow();
    }

    /**
     * Returns the facade used to discover and configure contributors.
     *
     * @return the contributor registry
     */
    WakamitiContributors contributors();

    /**
     * Returns the extension manager underlying contributor discovery.
     *
     * @return the active extension manager
     */
    ExtensionManager extensionManager();

    /**
     * Returns the serializer for executable plans and result snapshots.
     *
     * @return the configured plan serializer
     */
    PlanSerializer planSerializer();

    /**
     * Returns the resource loader scoped to the current Wakamiti runtime.
     *
     * @return the configured resource loader
     */
    ResourceLoader resourceLoader();

    /**
     * Publishes a runtime event to observers that accept its type.
     *
     * @param eventType event identifier, normally one of the constants in
     *                  {@link es.iti.wakamiti.api.event.Event}
     * @param data      event-specific payload, or {@code null}
     */
    void publishEvent(
            String eventType,
            Object data
    );

    /**
     * Returns the running Wakamiti core version.
     *
     * @return the version string used for contributor compatibility checks
     */
    String version();

    /**
     * Resolves the effective working directory from a configuration.
     *
     * @param configuration runtime configuration containing any directory
     *                      override
     * @return the normalized working directory
     */
    Path workingDir(
            Configuration configuration
    );

    /**
     * Returns the baseline configuration contributed by the runtime and all
     * installed configuration contributors.
     *
     * @return the effective default configuration
     */
    Configuration defaultConfiguration();

}
