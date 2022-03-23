/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.maven;


import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import imconfig.Configuration;
import imconfig.ConfigurationException;
import org.apache.maven.plugin.logging.Log;


import iti.kukumo.api.Kukumo;


public interface KukumoConfigurable {

    default Configuration readConfiguration(
        List<String> confFiles,
        Map<String, String> properties
    ) throws ConfigurationException {
        Configuration configuration = Kukumo.defaultConfiguration();
        if (confFiles != null) {
            for (String confFile : confFiles) {
                configuration = configuration.appendFromPath(Path.of(confFile))
                    .inner("kukumo");
            }
        }
        if (properties != null) {
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