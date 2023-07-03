/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven;


import es.iti.wakamiti.core.Wakamiti;
import imconfig.Configuration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
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


/**
 *
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class WakamitiVerifyMojo extends AbstractMojo implements WakamitiConfigurable {

    /**
     * Skip the plugin execution.
     * E.g.:
     *
     * <blockquote><pre>{@code
     *   <configuration>
     *     <skipTests>true</skipTests>
     *   </configuration>
     * }</pre></blockquote>
     *
     * Default value is {@code false}
     */
    @Parameter(defaultValue = "false")
    public boolean skipTests;

    /**
     * Sets wakamiti properties as {@link Map}.
     * E.g.:
     *
     * <blockquote><pre>{@code
     *   <configuration>
     *     <properties>
     *         <key>value</key>
     *     </properties>
     *   </configuration>
     * }</pre></blockquote>
     *
     */
    @Parameter
    public Map<String, String> properties = new LinkedHashMap<>();

    /**
     * Sets wakamiti configuration files.
     * E.g.:
     *
     * <blockquote><pre>{@code
     *   <configuration>
     *     <configurationFiles>file1,file2</configurationFiles>
     *   </configuration>
     * }</pre></blockquote>
     *
     */
    @Parameter
    public List<String> configurationFiles = new LinkedList<>();

    /**
     * Sets wakamiti log level.
     * E.g.:
     *
     * <blockquote><pre>{@code
     *   <configuration>
     *     <logLevel>debug</logLevel>
     *   </configuration>
     * }</pre></blockquote>
     *
     *
     * Default value is {@code info}
     */
    @Parameter(defaultValue = "info")
    public String logLevel;

    /**
     * Set this to {@code true} to ignore a failure during testing. Its use is
     * NOT RECOMMENDED, but quite convenient on occasion.
     * E.g.:
     *
     * <blockquote><pre>{@code
     *   <configuration>
     *     <testFailureIgnore>true</testFailureIgnore>
     *   </configuration>
     * }</pre></blockquote>
     *
     * Default value is {@code false}
     */
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