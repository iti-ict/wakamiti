/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.launcher;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;

import java.net.URISyntaxException;


/**
 * Utility class to run the Wakamiti application based on the provided command-line arguments.
 *
 * <p>This class initializes Wakamiti with the provided configuration and executes the test plan.
 * It also retrieves and displays contributions if needed.</p>
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiRunner {

    private final CliArguments arguments;

    /**
     * Constructs a WakamitiRunner instance.
     *
     * @param arguments The command-line arguments provided to the application.
     */
    public WakamitiRunner(CliArguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Retrieves contributions and formats them as a string.
     *
     * @return A string containing the contributions.
     */
    public String getContributions() {
        StringBuilder string = new StringBuilder();
        Wakamiti.contributors().allContributors().forEach((type, contributions) -> {
            string.append(type.getSimpleName()).append(" :\n");
            contributions.forEach(it -> string.append("    ").append(it.info()).append("\n"));
        });
        return string.toString();
    }

    /**
     * Executes the Wakamiti application based on the provided command-line arguments.
     *
     * @return True if the test plan passes, false otherwise.
     */
    public boolean run() {
        Configuration configuration;
        Wakamiti wakamiti = Wakamiti.instance();
        try {
            configuration = readConfiguration(arguments);
            PlanNode plan = wakamiti.createPlanFromConfiguration(configuration);
            if (!plan.hasChildren()) {
                WakamitiLauncher.logger().warn("Test Plan is empty!");
            } else {
                wakamiti.executePlan(plan, configuration);
            }
            return plan.result().map(Result::isPassed).orElse(false);
        } catch (ConfigurationException e) {
            WakamitiLauncher.logger()
                    .error("Error reading configuration: {}", e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new WakamitiException(e);
        }
        return false;
    }

    /**
     * Reads the configuration from the provided command-line arguments.
     *
     * @param arguments The command-line arguments provided to the application.
     * @return The configuration for Wakamiti.
     * @throws URISyntaxException If there is an issue with URI syntax.
     */
    private Configuration readConfiguration(CliArguments arguments) throws URISyntaxException {
        Configuration argumentConfiguration = arguments.wakamitiConfiguration();
        Configuration configuration = Wakamiti.defaultConfiguration()
                .append(argumentConfiguration);
        if (WakamitiLauncher.logger().isDebugEnabled()) {
            WakamitiLauncher.logger().debug("Using the following {}", configuration);
        }
        return configuration;
    }

}