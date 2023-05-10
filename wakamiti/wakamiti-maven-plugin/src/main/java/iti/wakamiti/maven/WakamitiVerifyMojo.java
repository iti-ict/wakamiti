/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.maven;


import imconfig.Configuration;
import iti.wakamiti.api.WakamitiException;
import iti.wakamiti.api.plan.PlanNode;
import iti.wakamiti.api.plan.Result;
import iti.wakamiti.core.Wakamiti;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Mojo(name = "verify", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class WakamitiVerifyMojo extends AbstractMojo implements WakamitiConfigurable {

    @Parameter
    public boolean skipTests;

    @Parameter
    public Map<String, String> properties = new LinkedHashMap<>();

    @Parameter
    public List<String> configurationFiles = new LinkedList<>();

    @Parameter(defaultValue = "info")
    public String logLevel;

    @Parameter(property = "maven.test.failure.ignore", defaultValue = "false")
    public boolean testFailureIgnore;

    /**
     * The current build session instance.
     */
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;


    @Override
    public void execute() {

        System.setProperty("org.slf4j.simpleLogger.log.iti.wakamiti", logLevel);

        if (skipTests) {
            info("Wakamiti tests skipped");
            return;
        }

        Configuration configuration;
        try {
            Wakamiti wakamiti = Wakamiti.instance();
            // replace null properties for empty values
            for (String key : properties.keySet()) {
                properties.putIfAbsent(key, "");
            }

            configuration = readConfiguration(configurationFiles, properties);
            PlanNode plan = wakamiti.createPlanFromConfiguration(configuration);
            if (!plan.hasChildren()) {
                warn("Test Plan is empty!");
            } else {
                wakamiti.executePlan(plan, configuration).result()
                        .filter(result -> !testFailureIgnore)
                        .filter(result -> result != Result.PASSED)
                        .ifPresent(result ->
                                session.getResult().addException(new MojoFailureException("Wakamiti Test Plan not passed")));
            }
        } catch (WakamitiException e) {
            if (!testFailureIgnore)
                session.getResult().addException(new MojoFailureException("Wakamiti error: " + e.getMessage(), e));
        } catch (Exception e) {
            if (!testFailureIgnore)
                session.getResult().addException(new MojoExecutionException("Wakamiti configuration error: " + e.getMessage(), e));
        }

    }

}