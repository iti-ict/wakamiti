/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher;


import java.net.URISyntaxException;


import imconfig.Configuration;
import imconfig.ConfigurationException;
import iti.kukumo.core.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;



public class KukumoRunner {

    private final CliArguments arguments;


    public KukumoRunner(CliArguments arguments) {
        this.arguments = arguments;
    }

    public String getContributions() {
        StringBuilder string = new StringBuilder();
        Kukumo.contributors().allContributors().forEach((type, contributions)-> {
            string.append(type.getSimpleName()).append(" :\n");
            contributions.forEach(it -> string.append("    ").append(it.info()).append("\n"));
        });
        return string.toString();
    }


    public void run() {
        Configuration configuration;
        Kukumo kukumo = Kukumo.instance();
        try {
            configuration = readConfiguration(arguments);
            PlanNode plan = kukumo.createPlanFromConfiguration(configuration);
            if (!plan.hasChildren()) {
                KukumoLauncher.logger().warn("Test Plan is empty!");
            } else {
                kukumo.executePlan(plan, configuration);
            }
        } catch (ConfigurationException e) {
            KukumoLauncher.logger()
                .error("Error reading configuration: {}", e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new KukumoException(e);
        }
    }



    private Configuration readConfiguration(CliArguments arguments) throws URISyntaxException {
        Configuration argumentConfiguration = arguments.kukumoConfiguration();
        Configuration configuration = Kukumo.defaultConfiguration()
            .append(argumentConfiguration);
        if (KukumoLauncher.logger().isDebugEnabled()) {
            KukumoLauncher.logger().debug("Using the following {}", configuration);
        }
        return configuration;
    }

}