/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import imconfig.Configuration;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


/**
 *
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.INTEGRATION_TEST,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
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
     * Use project dependencies.
     * E.g.:
     *
     * <blockquote><pre>{@code
     *   <configuration>
     *     <useProjectDependencies>true</useProjectDependencies>
     *   </configuration>
     * }</pre></blockquote>
     * <p>
     * Default value is {@code false}
     */
    @Parameter(defaultValue = "false")
    public boolean includeProjectDependencies;

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

    @Parameter(defaultValue = "${mojoExecution}", required = true, readonly = true)
    private MojoExecution mojoExecution;

    @Parameter(defaultValue = "${project.compileClasspathElements}", required = true, readonly = true)
    private List<String> projectDependencies;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        System.setProperty("log4j2.loggerContextFactory", "org.apache.logging.log4j.simple.SimpleLoggerContextFactory");
        System.setProperty("org.slf4j.simpleLogger.log.es.iti.wakamiti", logLevel);

        if (skipTests) {
            getLog().info("Wakamiti tests skipped");
            return;
        }

        Configuration configuration;
        try {
            if (includeProjectDependencies) {
                resolvePluginDependencies();
            }

            Wakamiti wakamiti = Wakamiti.instance();
            // replace null properties for empty values
            for (String key : properties.keySet()) {
                properties.putIfAbsent(key, "");
            }

            configuration = readConfiguration(configurationFiles, properties);

            PlanNode plan = wakamiti.createPlanFromConfiguration(configuration);
            if (!plan.hasChildren()) {
                getLog().warn("Test Plan is empty!");
            } else {
                Optional<Result> planResult = wakamiti.executePlan(plan, configuration).result()
                        .filter(result -> !testFailureIgnore)
                        .filter(result -> result != Result.PASSED);
                if (planResult.isPresent()) {
                    throw new WakamitiException("Wakamiti Test Plan not passed: " + planResult.get());
                }
            }
        } catch (WakamitiException e) {
            getLog().error(e);
            if (testFailureIgnore) return;
            MojoFailureException exception = new MojoFailureException("Wakamiti error: " + e.getMessage(), e);
            if (mojoExecution.getPlugin().getExecutions().stream()
                    .noneMatch(execution -> execution.getGoals().contains("control"))) {
                throw exception;
            }
            MojoResult.setError(exception);
        } catch (Throwable e) {
            getLog().error(e);
            if (testFailureIgnore) return;
            MojoExecutionException exception = new MojoExecutionException("Wakamiti configuration error: " + e.getMessage(), e);
            if (mojoExecution.getPlugin().getExecutions().stream()
                    .noneMatch(execution -> execution.getGoals().contains("control"))) {
                throw exception;
            }
            MojoResult.setError(new MojoExecutionException("Wakamiti configuration error: " + e.getMessage(), e));
        }

    }

    private void resolvePluginDependencies() {
        try {
            Set<URL> urls = new HashSet<>();
            for (String element : projectDependencies) {
                urls.add(new File(element).toURI().toURL());
            }
            ClassLoader contextClassLoader = URLClassLoader.newInstance(
                    urls.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        getLog().info("Project dependencies included");
    }

}