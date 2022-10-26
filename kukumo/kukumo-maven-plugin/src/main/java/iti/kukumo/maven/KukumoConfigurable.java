/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.maven;


import imconfig.Configuration;
import imconfig.ConfigurationException;
import iti.kukumo.core.Kukumo;
import org.apache.maven.plugin.logging.Log;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;


public interface KukumoConfigurable {

    default Configuration readConfiguration(
            List<String> confFiles,
            Map<String, String> properties
    ) throws ConfigurationException {
        Configuration configuration = Kukumo.defaultConfiguration();
        if (!confFiles.isEmpty()) {
            for (String confFile : confFiles) {
                configuration = Configuration.factory().merge(configuration,
                        Configuration.factory().fromPath(Path.of(confFile)).inner("kukumo"));
            }
        }
        if (!properties.isEmpty()) {
            configuration = configuration.appendFromMap(properties);
        }
        if (configuration.isEmpty()) {
            warn("configuration is empty");
        }
        return configuration;
    }


    default void info(String message) {
        getLog().info(message);
    }


    default void warn(String message) {
        getLog().warn(message);
    }


    Log getLog();
}