package iti.kukumo.launcher;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;

/**
 * @author ITI
 * Created by ITI on 2/04/19
 */
public class KukumoVerifier {

    private final Arguments arguments;

    public KukumoVerifier(Arguments arguments) {
        this.arguments = arguments;
    }



    public void verify() {
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
             KukumoLauncher.logger().error("Error reading configuration: {}", e.getLocalizedMessage(), e);
         } catch (KukumoException e) {
             KukumoLauncher.logger().error(e.getMessage());
         } catch (Exception e) {
             KukumoLauncher.logger().error("Fatal error: {}",e.getLocalizedMessage(),e);
         }
    }



    private Configuration readConfiguration(Arguments arguments) {
        Configuration argumentConfiguration = arguments.kukumoConfiguration();
        Configuration configuration = KukumoConfiguration.defaultConfiguration().append(argumentConfiguration);
        if (KukumoLauncher.logger().isDebugEnabled()) {
            KukumoLauncher.logger().debug("Using the following {}", configuration);
        }
        return configuration;
    }

}