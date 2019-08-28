package iti.kukumo.maven;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.KukumoConfiguration;
import org.apache.maven.plugin.logging.Log;

import java.util.List;
import java.util.Map;

public interface KukumoConfigurable {

    default Configuration readConfiguration(List<String> confFiles, Map<String, String> properties)
            throws ConfigurationException {
        Configuration configuration = KukumoConfiguration.defaultConfiguration();
        if (confFiles != null) {
            for (String confFile : confFiles) {
                configuration = configuration.appendFromClasspathResourceOrURI(confFile).inner("kukumo");
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
