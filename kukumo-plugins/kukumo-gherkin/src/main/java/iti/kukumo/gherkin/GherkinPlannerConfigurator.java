package iti.kukumo.gherkin;

import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.extensions.Configurator;

/**
 * @author ITI
 *         Created by ITI on 12/03/19
 */
@Extension(provider = "iti.kukumo", name="gherkin-configurator", extensionPoint = "iti.kukumo.api.extensions.Configurator")
public class GherkinPlannerConfigurator implements Configurator<GherkinPlanner> {

    @Override
    public boolean accepts(Object contributor) {
        return GherkinPlanner.class.isAssignableFrom(contributor.getClass());
    }


    @Override
    public void configure(GherkinPlanner contributor, Configuration configuration) {
        contributor.configureFilterFromTagExpression(configuration);
        contributor.configureIdTagPattern(configuration);
        contributor.setRedefinitionEnabled(configuration.getBoolean(KukumoConfiguration.REDEFINITION_ENABLED)
            .orElse(Boolean.TRUE));
        contributor.setRedefinitionHelper(new GherkinPlanRedefiner(configuration,contributor));
    }

}
