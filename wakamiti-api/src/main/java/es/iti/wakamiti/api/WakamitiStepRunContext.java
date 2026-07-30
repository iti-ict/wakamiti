/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import java.util.Locale;

import es.iti.wakamiti.api.imconfig.Configuration;


/**
 * Execution context used while resolving and running a single Wakamiti step.
 * <p>
 * The static accessors are backed by a {@link ThreadLocal}. Each execution
 * thread must install a context with {@link #set(WakamitiStepRunContext)} and
 * release it with {@link #clear()} to avoid leaking state between test runs.
 * </p>
 */
public class WakamitiStepRunContext {

    private static final ThreadLocal<WakamitiStepRunContext> SINGLETON = new ThreadLocal<>();
    private final Configuration configuration;
    private final Backend backend;
    private final Locale stepLocale;
    private final Locale dataLocale;

    /**
     * Creates the complete context required to convert arguments and execute a
     * step.
     *
     * @param configuration effective configuration for the current execution
     * @param backend       backend providing step definitions and data types
     * @param stepLocale    locale used to match localized step definitions
     * @param dataLocale    locale used to parse values embedded in step text
     */
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
     * Sets the current context for the calling thread.
     *
     * @param context context to bind to the current thread, or {@code null} to
     *                clear the association
     */
    public static void set(
            WakamitiStepRunContext context
    ) {
        SINGLETON.set(context);
    }

    /**
     * Gets the context currently bound to the calling thread.
     *
     * @return current thread context, or {@code null} when no context has been
     *         installed
     */
    public static WakamitiStepRunContext current() {
        return SINGLETON.get();
    }

    /**
     * Removes the context bound to the calling thread.
     * <p>
     * This method should typically be called in a {@code finally} block after
     * step execution to guarantee cleanup.
     * </p>
     */
    public static void clear() {
        SINGLETON.remove();
    }

    /**
     * Gets the effective configuration for this step execution.
     *
     * @return step configuration
     */
    public Configuration configuration() {
        return configuration;
    }

    /**
     * Gets the locale used to match localized step definitions.
     *
     * @return step locale
     */
    public Locale stepLocale() {
        return stepLocale;
    }

    /**
     * Gets the locale used to parse data values embedded in step text.
     *
     * @return data locale
     */
    public Locale dataLocale() {
        return dataLocale;
    }

    /**
     * Gets the data type registry exposed by the selected backend.
     *
     * @return backend data type registry
     */
    public WakamitiDataTypeRegistry typeRegistry() {
        return backend.getTypeRegistry();
    }

    /**
     * Gets the backend associated with this step execution.
     *
     * @return backend instance
     */
    public Backend backend() {
        return this.backend;
    }

}
