/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven;


import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;
import es.iti.wakamiti.core.Wakamiti;


/**
 * Interface for configuring Wakamiti within a Maven environment.
 */
public interface WakamitiConfigurable {

    /**
     * Builds the effective plugin configuration.
     * <p>
     * Merge precedence is:
     * default configuration < configuration files < explicit plugin properties.
     * Configuration files are read as {@code inner("wakamiti")} namespaces.
     * </p>
     *
     * @param confFiles  ordered configuration-file paths
     * @param properties explicit key/value overrides from plugin configuration
     * @return merged effective configuration
     * @throws ConfigurationException when any file cannot be parsed or loaded
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
