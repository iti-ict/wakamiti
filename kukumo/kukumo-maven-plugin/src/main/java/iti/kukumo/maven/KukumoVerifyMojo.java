/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.maven;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.Result;


@Mojo(name = "verify", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class KukumoVerifyMojo extends AbstractMojo implements KukumoConfigurable {

    @Parameter
    public boolean skipTests;

    @Parameter
    public Map<String, String> properties;

    @Parameter
    public List<String> configurationFiles;

    @Parameter(defaultValue = "info")
    public String logLevel;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        System.setProperty("org.slf4j.simpleLogger.log.iti.kukumo", logLevel);

        if (skipTests) {
            info("Kukumo tests skipped");
            return;
        }

        Configuration configuration;
        try {
            Kukumo kukumo = Kukumo.instance();
            // replace null properties for empty values
            for (String key : properties.keySet()) {
                properties.putIfAbsent(key, "");
            }

            configuration = readConfiguration(configurationFiles, properties);
            PlanNode plan = kukumo.createPlanFromConfiguration(configuration);
            if (!plan.hasChildren()) {
                warn("Test Plan is empty!");
            } else {
                Optional<Result> result = kukumo.executePlan(plan, configuration).result();
                if (result.isPresent()) {
                    if (result.get() == Result.PASSED) {
                    } else {
                        throw new MojoFailureException("Kukumo Test Plan not passed");
                    }
                }
            }
        } catch (KukumoException e) {
            throw new MojoFailureException(e.getMessage());
        } catch (ConfigurationException e) {
            throw new MojoExecutionException("Kukumo configuration error: " + e.getMessage(), e);
        }

    }

}