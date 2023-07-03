/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.api;


import es.iti.wakamiti.api.util.ResourceLoader;
import imconfig.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;



public class WakamitiStepRunContext {

    private static final ThreadLocal<WakamitiStepRunContext> singleton = new ThreadLocal<>();


    public static void set(WakamitiStepRunContext context) {
        singleton.set(context);
    }


    public static WakamitiStepRunContext current() {
        return singleton.get();
    }


    public static void clear() {
        singleton.remove();
    }


    private final Configuration configuration;
    private final Backend backend;
    private final Locale stepLocale;
    private final Locale dataLocale;
    private final ResourceLoader resourceLoader;
    private final Path workingDir;


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
        this.workingDir = WakamitiAPI.instance().workingDir(configuration);
        this.resourceLoader = WakamitiAPI.instance().resourceLoader(workingDir.toFile());
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


    public WakamitiDataTypeRegistry typeRegistry() {
        return backend.getTypeRegistry();
    }

    public Backend backend() {
        return this.backend;
    }


    public ResourceLoader resourceLoader() {
        return resourceLoader;
    }

    public Path workingDir() {
        return workingDir;
    }


    public File absolutePath(File file) {
        if (file.isAbsolute()) {
            return file;
        }
        return new File(workingDir.toFile(),file.toString());
    }


    public Path absolutePath(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        return workingDir.resolve(path);
    }
}