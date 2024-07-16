/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import es.iti.wakamiti.api.imconfig.Configuration;

import java.util.Locale;


/**
 * Represents the context in which a Wakamiti step is run. This context includes information
 * such as the configuration, backend, step locale, data locale, and the type registry.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiStepRunContext {

    private static final ThreadLocal<WakamitiStepRunContext> singleton = new ThreadLocal<>();
    private final Configuration configuration;
    private final Backend backend;
    private final Locale stepLocale;
    private final Locale dataLocale;

    public WakamitiStepRunContext(
            Configuration configuration,
            Backend backend,
            Locale stepLocale,
            Locale dataLocale
    ) {
        this.configuration = configuration;
        this.backend = backend;
        this.stepLocale = stepLocale;
        this.dataLocale = dataLocale;
    }

    /**
     * Sets the current WakamitiStepRunContext for the current thread.
     *
     * @param context The WakamitiStepRunContext to set.
     */
    public static void set(WakamitiStepRunContext context) {
        singleton.set(context);
    }

    /**
     * Retrieves the current WakamitiStepRunContext for the current thread.
     *
     * @return The current WakamitiStepRunContext.
     */
    public static WakamitiStepRunContext current() {
        return singleton.get();
    }

    /**
     * Clears the WakamitiStepRunContext for the current thread.
     */
    public static void clear() {
        singleton.remove();
    }

    /**
     * Gets the configuration associated with this step run.
     *
     * @return The configuration.
     */
    public Configuration configuration() {
        return configuration;
    }

    /**
     * Gets the locale associated with the step.
     *
     * @return The step locale.
     */
    public Locale stepLocale() {
        return stepLocale;
    }

    /**
     * Gets the locale associated with the data.
     *
     * @return The data locale.
     */
    public Locale dataLocale() {
        return dataLocale;
    }

    /**
     * Gets the type registry associated with the backend.
     *
     * @return The type registry.
     */
    public WakamitiDataTypeRegistry typeRegistry() {
        return backend.getTypeRegistry();
    }

    /**
     * Gets the backend associated with this step run.
     *
     * @return The backend.
     */
    public Backend backend() {
        return this.backend;
    }

}