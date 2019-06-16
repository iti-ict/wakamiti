package iti.kukumo.maven;

import java.io.IOException;
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
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.Result;

@Mojo(name = "verify", defaultPhase = LifecyclePhase.VERIFY)
public class KukumoVerifyMojo extends AbstractMojo implements KukumoConfigurable {

    @Parameter
    public Map<String, String> properties;

    @Parameter
    public List<String> configurationfiles;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Configuration configuration;
        try {
            configuration = readConfiguration(configurationfiles, properties);
            info("building test plan");
            PlanNode plan = Kukumo.createPlanFromConfiguration(configuration);
            if (!plan.hasChildren()) {
                warn("Test Plan is empty!");
            } else {
                info("running test plan");
                Optional<Result> result = Kukumo.executePlan(plan, configuration).computeResult();
                if (result.isPresent()) {
                    if (result.get() == Result.PASSED) {
                        info("Test Plan passed");
                    } else {
                        throw new MojoFailureException("[kukumo] Test Plan NOT passed");
                    }
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Kukumo reporting error: " + e.getMessage(), e);
        } catch (ConfigurationException e) {
            throw new MojoExecutionException("Kukumo configuration error: " + e.getMessage(), e);
        }

    }

}
