package iti.kukumo.core.plan;

import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import iti.kukumo.api.plan.PlanNodeBuilder;
import iti.kukumo.api.plan.PlanNodeData;
import iti.kukumo.core.plan.PlanNodeBuilderRules.PlanNodeBuilderRule;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
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