/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.plan;


import java.util.List;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.core.plan.PlanNodeBuilderRules.PlanNodeBuilderRule;



public abstract class RuleBasedPlanTransformer implements PlanTransformer {

    protected abstract List<PlanNodeBuilderRule> createRules(Configuration configuration);


    @Override
    public PlanNodeBuilder transform(PlanNodeBuilder plan, Configuration configuration) {
        for (PlanNodeBuilderRule rule : createRules(configuration)) {
            rule.apply(plan);
        }
        return plan;
    }
}
