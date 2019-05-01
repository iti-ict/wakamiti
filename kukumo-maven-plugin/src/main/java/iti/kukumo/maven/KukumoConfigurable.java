package iti.kukumo.maven;

import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.KukumoConfiguration;

public interface KukumoConfigurable {

    default Configuration readConfiguration(List<String> confFiles, Map<String, String> properties)
            throws ConfigurationException {
        Configuration configuration = KukumoConfiguration.defaultConfiguration();
        if (confFiles != null) {
            for (String confFile : confFiles) {
                configuration = configuration.appendFromPath(confFile, "kukumo");
            }
        }
        if (properties != null) {
            configuration = configuration.appendFromMap(properties);
        }
        if (configuration.isEmpty()) {
            warn("configuration is empty");
        } else {
            info("configured properties:\n" + configuration);
        }
        return configuration;
    }

    default void info(String message) {
        getLog().info(format(message));
    }

    default void warn(String message) {
        getLog().warn(format(message));
    }

    default String format(String message) {
        return "[kukumo] " + message;
    }

    Log getLog();
}
