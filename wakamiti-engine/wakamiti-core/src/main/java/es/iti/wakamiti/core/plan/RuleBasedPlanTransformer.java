/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.plan;


import java.util.List;

import imconfig.Configuration;
import es.iti.wakamiti.api.extensions.PlanTransformer;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;
import es.iti.wakamiti.core.plan.PlanNodeBuilderRules.PlanNodeBuilderRule;



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