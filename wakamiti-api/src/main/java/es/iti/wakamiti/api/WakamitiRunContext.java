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
import es.iti.wakamiti.api.util.WakamitiLogger;
import imconfig.Configuration;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;


public class WakamitiRunContext {

    private static Logger LOGGER = WakamitiLogger.forClass(WakamitiRunContext.class);

    private static final ThreadLocal<WakamitiRunContext> singleton = new ThreadLocal<>();


    public static void set(WakamitiRunContext context) {
        singleton.set(context);
    }


    public static WakamitiRunContext current() {
        return singleton.get();
    }


    public static void clear() {
        singleton.remove();
    }


    private final Configuration configuration;
    private final ResourceLoader resourceLoader;
    private final Path workingDir;


    public WakamitiRunContext(Configuration configuration) {
        this.configuration = configuration;
        this.workingDir = WakamitiAPI.instance().workingDir(configuration);
        this.resourceLoader = WakamitiAPI.instance().resourceLoader(workingDir.toFile());
        LOGGER.info("Workding directory is {}",workingDir);
    }


    public Configuration configuration() {
        return configuration;
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