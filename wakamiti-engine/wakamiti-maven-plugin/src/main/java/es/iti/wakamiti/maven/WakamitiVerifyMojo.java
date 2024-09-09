/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.imconfig.Configuration;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


/**
 * Maven plugin mojo for verifying tests using Wakamiti.
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
     * <p>
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
     * <p>
     * <p>
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
     * <p>
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

    @Parameter(defaultValue = "${project.runtimeClasspathElements}", required = true, readonly = true)
    private List<String> projectDependencies;

    /**
     * Executes the plugin.
     *
     * @throws MojoExecutionException If an unexpected problem occurs during execution.
     * @throws MojoFailureException   If a failure is encountered during execution.
     */
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
            resolvePluginDependencies();

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
                wakamiti.executePlan(plan, configuration).result()
                        .filter(result -> !testFailureIgnore)
                        .filter(result -> result != Result.PASSED)
                        .ifPresent(result -> plan.errors().findFirst().ifPresentOrElse(e -> {
                            throw new WakamitiException("Wakamiti Test Plan not passed: {}", result, e);
                        }, () -> {
                            throw new WakamitiException("Wakamiti Test Plan not passed: {}", result);
                        }));
            }
        } catch (WakamitiException e) {
            getLog().error(e);
            MojoFailureException exception = new MojoFailureException("Wakamiti error: " + e.getMessage(), e);
            errorControl(exception);
        } catch (Throwable e) {
            getLog().error(e);
            MojoExecutionException exception =
                    new MojoExecutionException("Wakamiti configuration error: " + e.getMessage(), e);
            errorControl(exception);
        }

    }

    private void errorControl(AbstractMojoExecutionException exception)
            throws MojoExecutionException, MojoFailureException {
        if (testFailureIgnore) return;
        if (mojoExecution.getPlugin().getExecutions().stream()
                .noneMatch(execution -> execution.getGoals().contains("control"))) {
            if (exception instanceof MojoExecutionException) throw (MojoExecutionException) exception;
            if (exception instanceof MojoFailureException) throw (MojoFailureException) exception;
        }
        MojoResult.setError(exception);
    }

    private void resolvePluginDependencies() {
        if (!includeProjectDependencies) return;
        try {
            Set<URL> urls = new HashSet<>();
            for (String element : projectDependencies) {
                urls.add(new File(element).toURI().toURL());
                getLog().debug(element + " loaded");
            }
            ClassLoader contextClassLoader = URLClassLoader.newInstance(
                    urls.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            WakamitiAPI.instance().contributors().setClassLoaders(Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new WakamitiException(e);
        }
        getLog().info("Project dependencies included");
    }

}