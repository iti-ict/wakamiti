package iti.kukumo.gherkin;

import static iti.kukumo.core.plan.PlanNodeBuilderRules.anyNode;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.anyOtherNode;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.childOf;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.forEachNode;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.withProperty;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.withSame;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.withTag;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_PROPERTY;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_BACKGROUND;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_SCENARIO;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_SCENARIO_OUTLINE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioOutline;
import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.core.plan.PlanNodeBuilder;
import iti.kukumo.core.plan.PlanNodeBuilderRules.PlanNodeBuilderRule;

/**
 * @author ITI
 * Created by ITI on 7/10/19
 */
@Extension(provider = "iti.kukumo", name = "gherkin-redefinition-transformer")
public class GherkinRedefinitionPlanTransformer implements PlanTransformer {



    @Override
    public PlanNodeBuilder transform(PlanNodeBuilder plan, Configuration configuration) {

        GherkinPlanBuilder gherkinPlanBuilder = new GherkinPlanBuilder();
        gherkinPlanBuilder.configure(configuration);

        String definitionTag = configuration
                .get(KukumoConfiguration.REDEFINITION_DEFINITION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_DEFINITION_TAG);
        String implementationTag = configuration
                .get(KukumoConfiguration.REDEFINITION_IMPLEMENTATION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_IMPLEMENTATION_TAG);

        List<PlanNodeBuilderRule> rules = Arrays.asList(
            clearChildrenOfImplementationScenarioOutlines(implementationTag),
            populateImplementationScenarioOutlinesWithDefinitionExamples(
                implementationTag,definitionTag,gherkinPlanBuilder
            ),
            attachImplemantationBackgroundToCreatedScenarios(implementationTag,gherkinPlanBuilder)
        );
        rules.forEach(rule -> rule.apply(plan));
        return plan;
    }



    private PlanNodeBuilderRule clearChildrenOfImplementationScenarioOutlines(String implementationTag) {
        return
                forEachNode(ofTypeScenarioOutline().and(withTag(implementationTag)))
                .perform(PlanNodeBuilder::clearChildren);
    }


    private PlanNodeBuilderRule populateImplementationScenarioOutlinesWithDefinitionExamples(
            String implementationTag,
            String definitionTag,
            GherkinPlanBuilder gherkinPlanBuilder
    ) {
        BiConsumer<PlanNodeBuilder, PlanNodeBuilder> action =
               (scenarioOutlineNode, scenarioOutlineNodeWithExamples) -> {
                Examples examples = ((ScenarioOutline) scenarioOutlineNodeWithExamples
                        .getUnderlyingModel()).getExamples().get(0);
                List<PlanNodeBuilder> scenarios = gherkinPlanBuilder.createScenariosFromExamples(
                    (ScenarioOutline) scenarioOutlineNode.getUnderlyingModel(),
                    examples,
                    scenarioOutlineNode,
                    Optional.empty(),
                    scenarioOutlineNode.language(),
                    scenarioOutlineNode.source()
                );
                scenarioOutlineNode.addChildren(scenarios);
        };
        return
            forEachNode(ofTypeScenarioOutline().and(withTag(implementationTag)))
            .given(anyOtherNode(
                ofTypeScenarioOutline().and(withTag(definitionTag)),
                withSame(PlanNodeBuilder::id)
            ))
            .perform(action);
    }



    private PlanNodeBuilderRule attachImplemantationBackgroundToCreatedScenarios(
        String implementationTag,
        GherkinPlanBuilder gherkinPlanBuilder
    ) {
        return
            forEachNode(ofTypeScenario().and(
                childOf(anyNode(ofTypeScenarioOutline().and(withTag(implementationTag))))
            ))
            .given(feature())
            .perform((scenarioNode,featureNode)-> {
                gherkinPlanBuilder.createBackgroundSteps(
                    (Feature)featureNode.getUnderlyingModel(),
                    scenarioNode.source(),
                    scenarioNode
                ).ifPresent(scenarioNode::addFirstChild);
            });
    }





    private Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> feature() {
        return node -> node.ancestors().filter(ofTypeFeature()).findFirst();
    }


    private Predicate<PlanNodeBuilder> withGherkinType(String gherkinType) {
        return withProperty(GHERKIN_PROPERTY,gherkinType);
    }

    private Predicate<PlanNodeBuilder> ofTypeScenarioOutline() {
        return withGherkinType(GHERKIN_TYPE_SCENARIO_OUTLINE);
    }

    private Predicate<PlanNodeBuilder> ofTypeScenario() {
        return withGherkinType(GHERKIN_TYPE_SCENARIO);
    }

    private Predicate<PlanNodeBuilder> ofTypeFeature() {
        return withGherkinType(GHERKIN_TYPE_SCENARIO);
    }

    private Predicate<PlanNodeBuilder> ofTypeBackground() {
        return withGherkinType(GHERKIN_TYPE_BACKGROUND);
    }


}
