/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.plan;


import es.iti.wakamiti.api.extensions.PlanTransformer;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;
import es.iti.wakamiti.core.plan.PlanNodeBuilderRules.PlanNodeBuilderRule;
import es.iti.wakamiti.api.imconfig.Configuration;

import java.util.List;


/**
 * An abstract implementation of the {@link PlanTransformer} interface that applies a set
 * of rules to transform a {@link PlanNodeBuilder}.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public abstract class RuleBasedPlanTransformer implements PlanTransformer {

    /**
     * Creates a list of rules based on the provided configuration.
     *
     * @param configuration The configuration used to create rules.
     * @return A list of {@link PlanNodeBuilderRule} instances.
     */
    protected abstract List<PlanNodeBuilderRule> createRules(Configuration configuration);

    /**
     * Transforms a {@link PlanNodeBuilder} using a set of rules defined by the implementation.
     *
     * @param plan          The original plan to transform.
     * @param configuration The configuration used to determine transformation rules.
     * @return The transformed {@link PlanNodeBuilder}.
     */
    @Override
    public PlanNodeBuilder transform(PlanNodeBuilder plan, Configuration configuration) {
        for (PlanNodeBuilderRule rule : createRules(configuration)) {
            rule.apply(plan);
        }
        return plan;
    }
}