/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.launcher;


import java.net.URISyntaxException;


import imconfig.Configuration;
import imconfig.ConfigurationException;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;



public class WakamitiRunner {

    private final CliArguments arguments;


    public WakamitiRunner(CliArguments arguments) {
        this.arguments = arguments;
    }

    public String getContributions() {
        StringBuilder string = new StringBuilder();
        Wakamiti.contributors().allContributors().forEach((type, contributions)-> {
            string.append(type.getSimpleName()).append(" :\n");
            contributions.forEach(it -> string.append("    ").append(it.info()).append("\n"));
        });
        return string.toString();
    }


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