package iti.kukumo.core.plan;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.core.plan.PlanNodeBuilderRules.PlanNodeBuilderRule;

import java.util.List;

/**
 * @author ITI
 * Created by ITI on 10/10/19
 */
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
