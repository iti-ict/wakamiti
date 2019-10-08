package iti.kukumo.gherkin;

import gherkin.ast.ScenarioOutline;
import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.core.plan.PlanNodeBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static iti.kukumo.core.plan.PlanNodeBuilderRules.*;
import static iti.kukumo.gherkin.GherkinPlanBuilder.*;

/**
 * @author ITI
 * Created by ITI on 7/10/19
 */
@Extension(provider = "iti.kukumo", name = "gherkin-redefinition-transformer")
public class GherkinRedefinitionPlanTransformer implements PlanTransformer {


    private final GherkinPlanBuilder gherkinPlanBuilder = new GherkinPlanBuilder();


    @Override
    public PlanNodeBuilder transform(PlanNodeBuilder plan, Configuration configuration) {

        String definitionTag = configuration
                .get(KukumoConfiguration.REDEFINITION_DEFINITION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_DEFINITION_TAG);
        String implementationTag = configuration
                .get(KukumoConfiguration.REDEFINITION_IMPLEMENTATION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_IMPLEMENTATION_TAG);

        List<PlanNodeBuilderRule> rules = Arrays.asList(

                forEachNode(ofTypeScenarioOutline())
                .perform(node -> System.out.println("HOLA SOY UN OUTLINE "+node.displayName())),

                forEachNode(ofTypeScenarioOutline().and(withTag(implementationTag)))
                .perform(PlanNodeBuilder::clearChildren)

        //        forEachNode(ofTypeScenarioOutline().and(withTag(implementationTag)))
        //        .given(anyOtherNode(
        //                ofTypeScenarioOutline().and(withTag(definitionTag)),
        //                withSame(PlanNodeBuilder::id)
        //        ))
        //        .perform(this::createScenariosFromExamples)
        );
        rules.forEach(rule -> rule.apply(plan));
        return plan;
    }



    private Predicate<PlanNodeBuilder> withGherkinType(String gherkinType) {
        return withProperty(GHERKIN_PROPERTY,gherkinType);
    }

    private Predicate<PlanNodeBuilder> ofTypeScenarioOutline() {
        return withGherkinType(GHERKIN_TYPE_SCENARIO_OUTLINE);
    }



    private List<PlanNodeBuilder> createScenariosFromExamples(
            PlanNodeBuilder scenarioOutlineNode,
            PlanNodeBuilder scenarioOutlineNodeExamples
    ) {
        return gherkinPlanBuilder.createScenariosFromExamples(
            (ScenarioOutline) scenarioOutlineNode.getUnderlyingModel(),
            ((ScenarioOutline) scenarioOutlineNodeExamples.getUnderlyingModel()).getExamples().get(0),
            scenarioOutlineNode,
            scenarioOutlineNode.root().children()
               .filter(node -> GHERKIN_TYPE_BACKGROUND.equals(node.properties().get(GHERKIN_PROPERTY)))
               .findFirst()
            ,
            scenarioOutlineNode.language(),
            scenarioOutlineNode.source()
        );
    }


}
