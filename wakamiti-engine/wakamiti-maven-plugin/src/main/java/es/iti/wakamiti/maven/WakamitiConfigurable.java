/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven;


import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;
import org.apache.maven.plugin.logging.Log;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;


/**
 * Interface for configuring Wakamiti within a Maven environment.
 */
public interface WakamitiConfigurable {

    /**
     * Reads the configuration from the provided files and properties.
     *
     * @param confFiles  List of configuration files.
     * @param properties Map of configuration properties.
     * @return The merged configuration.
     * @throws ConfigurationException If there is an issue with the configuration.
     */
    default Configuration readConfiguration(
            List<String> confFiles,
            Map<String, String> properties
    ) throws ConfigurationException {
        Configuration configuration = Wakamiti.defaultConfiguration();
        if (!confFiles.isEmpty()) {
            for (String confFile : confFiles) {
                configuration = Configuration.factory().merge(configuration,
                        Configuration.factory().fromPath(Path.of(confFile)).inner("wakamiti"));
            }
        }
        if (!properties.isEmpty()) {
            configuration = configuration.appendFromMap(properties);
        }
        if (configuration.isEmpty()) {
            getLog().warn("configuration is empty");
        } else {
            getLog().debug("using the following configuration\n" + configuration);
        }

        return configuration;
    }

    /**
     * Retrieves the Maven logger.
     *
     * @return The Maven logger.
     */
    Log getLog();
}