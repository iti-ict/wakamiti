/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import imconfig.Configuration;

import java.util.Locale;



public class KukumoStepRunContext {

    private static final ThreadLocal<KukumoStepRunContext> singleton = new ThreadLocal<>();


    public static void set(KukumoStepRunContext context) {
        singleton.set(context);
    }


    public static KukumoStepRunContext current() {
        return singleton.get();
    }


    public static void clear() {
        singleton.remove();
    }


    private final Configuration configuration;
    private final Backend backend;
    private final Locale stepLocale;
    private final Locale dataLocale;


    public KukumoStepRunContext(
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


    public Configuration configuration() {
        return configuration;
    }


    public Locale stepLocale() {
        return stepLocale;
    }


    public Locale dataLocale() {
        return dataLocale;
    }


    public KukumoDataTypeRegistry typeRegistry() {
        return backend.getTypeRegistry();
    }

    public Backend backend() {
        return this.backend;
    }

}