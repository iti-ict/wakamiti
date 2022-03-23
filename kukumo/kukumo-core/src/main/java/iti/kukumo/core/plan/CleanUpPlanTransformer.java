/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.core.plan;

import imconfig.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.api.plan.PlanNodeData;

import java.util.List;
import java.util.regex.Pattern;

import static iti.kukumo.core.plan.PlanNodeBuilderRules.forEachNode;


@Extension(provider = "iti.kukumo", name = "cleanup-transformer", version = "1.1", priority = 1)
public class CleanUpPlanTransformer extends RuleBasedPlanTransformer implements PlanTransformer {


    private static Pattern propertySubstitution = Pattern.compile("\\$\\{([^}]+)}");


    @Override
    protected List<PlanNodeBuilderRules.PlanNodeBuilderRule> createRules(Configuration configuration) {

        return List.of(
            forEachNode()
                .perform( node -> node.setName(substituteProperties(node.name(),configuration))),
            forEachNode( node -> node.data().isPresent() )
                .perform( node -> node.setData( substituteProperties(node.data().orElseThrow(), configuration)))
        );

    }



    private String substituteProperties(String value, Configuration configuration) {
        return propertySubstitution
            .matcher(value)
            .replaceAll(result ->
                configuration.get(result.group(1), String.class).orElse(result.group(0))
            );
    }


    private PlanNodeData substituteProperties(PlanNodeData data, Configuration configuration) {
        return data.copyReplacingVariables(value -> substituteProperties(value, configuration));
    }




}