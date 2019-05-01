package iti.kukumo.launcher;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.Result;

import java.io.File;
import java.util.Optional;

/**
 * @author ITI
 * Created by ITI on 2/04/19
 */
public class KukumoVerifier {

    private static final File DEFAULT_CONF_FILE = new File("kukumo.yaml");
    
    public void verify(Arguments arguments) {

        /*
          Restrict modules that will be located by Kukumo.
          (this is only necessary from the launcher; when executed by Maven, the dependencies are already
          configured in the pom)
         */
        // TODO instead of restrict modules by name, we should restrit by jar, otherwise the plugin architecture will be
        // way to rigid
        // Kukumo.restrictModules(arguments.modules());


        Configuration configuration;
         try {
             configuration = readConfiguration(arguments); 
             KukumoLauncher.logger().info("building test plan");
             PlanNode plan = Kukumo.createPlanFromConfiguration(configuration);
             if (!plan.hasChildren()) {
                 KukumoLauncher.logger().warn("Test Plan is empty!");
             } else {
                 KukumoLauncher.logger().info("running test plan");
                 Optional<Result> result = Kukumo.executePlan(plan, configuration).computeResult();
                 if (result.isPresent()) {
                     if (result.get() == Result.PASSED) {
                         KukumoLauncher.logger().info("Test Plan passed");
                     } else {
                         KukumoLauncher.logger().error("Test Plan not passed");
                     }
                 }
             }
         } catch (ConfigurationException e) {
             KukumoLauncher.logger().error("Error reading configuration: {}",e.getLocalizedMessage(),e);
         } catch (Exception e) {
             KukumoLauncher.logger().error("Fatal error: {}",e.getLocalizedMessage(),e);
         }
    }

    
    
    private Configuration readConfiguration(Arguments arguments) throws ConfigurationException {
        Configuration configuration = KukumoConfiguration.defaultConfiguration();
        File confFile = arguments.confFile().map(File::new).orElse(DEFAULT_CONF_FILE);
        if (confFile.exists()) {
            configuration = configuration.appendFromPath(confFile.getAbsolutePath(), "kukumo");
        }
        if (!arguments.kukumoProperties().isEmpty()) {
            configuration = configuration.appendFromMap(arguments.kukumoProperties());
        }    
        return configuration;
    }

}