/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven;


import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.core.Wakamiti;


/**
 * Maven plugin mojo for generating Wakamiti reports.
 */
@Mojo(name = "report", defaultPhase = LifecyclePhase.VERIFY)
public class WakamitiReporterMojo extends AbstractMojo implements WakamitiConfigurable {

    @Parameter
    Map<String, String> properties = new LinkedHashMap<>();

    @Parameter
    List<String> configurationFiles = new LinkedList<>();

    @Parameter
    boolean testFailureIgnore;

    /**
     * The current build session instance.
     */
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;

    /**
     * Executes the plugin.
     *
     * @throws MojoExecutionException If an unexpected problem occurs during execution.
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            Configuration configuration = readConfiguration(configurationFiles, properties);
            getLog().info("invoking reports...");
            Wakamiti.instance().generateReports(configuration);
        } catch (Throwable e) {
            getLog().error(e);
            if (!testFailureIgnore) {
                throw new MojoExecutionException("Wakamiti configuration error: " + e.getMessage(), e);
            }
        }
    }

}
