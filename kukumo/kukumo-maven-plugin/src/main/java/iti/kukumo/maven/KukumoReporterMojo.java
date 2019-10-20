/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.maven;


import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.Kukumo;


@Mojo(name = "report", defaultPhase = LifecyclePhase.VERIFY)
public class KukumoReporterMojo extends AbstractMojo implements KukumoConfigurable {

    @Parameter
    public Map<String, String> properties;

    @Parameter
    public List<String> configurationfiles;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Configuration configuration = readConfiguration(configurationfiles, properties);
            info("invoking reports...");
            Kukumo.instance().generateReports(configuration);
        } catch (ConfigurationException e) {
            throw new MojoExecutionException("Kukumo configuration error: " + e.getMessage(), e);
        }
    }

}
