package iti.kukumo.gherkin;

import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioOutline;
import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.core.plan.PlanNodeBuilder;
import iti.kukumo.core.plan.PlanNodeBuilderRules.*;
import iti.kukumo.core.plan.RuleBasedPlanTransformer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static iti.kukumo.core.plan.PlanNodeBuilderRules.*;
import static iti.kukumo.gherkin.GherkinPlanBuilder.*;

/**
 * @author ITI
 * Created by ITI on 7/10/19
 */
@Extension(provider = "iti.kukumo", name = "gherkin-redefinition-transformer")
public class GherkinRedefinitionPlanTransformer extends RuleBasedPlanTransformer implements PlanTransformer {


    @Override
    protected List<PlanNodeBuilderRule> createRules(Configuration configuration) {

        GherkinPlanBuilder gherkinPlanBuilder = new GherkinPlanBuilder();
        gherkinPlanBuilder.configure(configuration);
        String implementationTag = implementationTag(configuration);
        String definitionTag = definitionTag(configuration);

        return Arrays.asList(

           // remove background of definition features
           forEachNode(ofTypeBackground().and(withTag(definitionTag)))
              .perform(node -> node.parent().ifPresent(parent -> parent.removeChild(node))),

           // remove scenarios from implementation scenario outlines
           forEachNode(ofTypeScenarioOutline().and(withTag(implementationTag)))
              .perform(PlanNodeBuilder::clearChildren),

            // repopulate implementation scenario outlines using definition examples
            populateImplementationScenarioOutlinesWithDefinitionExamples(
                implementationTag,definitionTag,gherkinPlanBuilder
            ),

            // recreate the implementation background in each implementation scenario
            // within scenario outlines
            attachImplemantationBackgroundToCreatedScenarios(implementationTag,gherkinPlanBuilder),

           // change node type of definition steps from step to step_aggregator
           forEachNode(withType(NodeType.STEP).and(childOf(anyNode(withTag(definitionTag)))))
              .perform(node -> node.setNodeType(NodeType.STEP_AGGREGATOR)),

           // move implementation steps to definition as children of the original definition steps
           forEachNode(
                   withType(NodeType.STEP_AGGREGATOR).and(childOf(anyNode(withTag(definitionTag))))
           ).given(anyOtherNode(
                   withType(NodeType.TEST_CASE).and(withTag(implementationTag)),
                   withSame(PlanNodeBuilder::parent,Optional::of,PlanNodeBuilder::id)
              ))
              .perform(this::attachImplementationSteps),

           // move background steps to definition as the first children of the definition scenario
           forEachNode(ofTypeBackground().and(withTag(implementationTag)))
              .given(anyOtherNode(
                  ofTypeScenario().and(withTag(definitionTag)),
                  withSame(PlanNodeBuilder::parent,Optional::of,PlanNodeBuilder::id)
              ))
              .perform((impBackground,defScenario)->defScenario.addFirstChild(
                 impBackground
                         .setName("<preparation>")
                         .setKeyword(null)
                         .setDisplayNamePattern("{name}")
              )),

           // change node type of any definition step without children, from step_aggregator to virtual_step
           forEachNode(withType(NodeType.STEP_AGGREGATOR).and(withoutChildren()))
              .perform(node -> node.setNodeType(NodeType.VIRTUAL_STEP)),

           // copy node properties of implementation scenarios to definition scenarios
           forEachNode(ofTypeScenario().and(withTag(definitionTag)))
              .given(anyOtherNode(ofTypeScenario().and(withTag(implementationTag)),withSame(PlanNodeBuilder::id)))
              .perform((defScenario,impScenario)->defScenario.addProperties(impScenario.properties())),

           // remove totally the implementation features
           forEachNode(ofTypeFeature().and(withTag(implementationTag)))
              .perform(node->node.parent().ifPresent(parent -> parent.removeChild(node)))

        );
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
            forEachNode(
                ofTypeScenario().and(
                withTag(implementationTag)).and(
                childOf(anyNode(ofTypeScenarioOutline())))
            )
            .given(feature())
            .perform((scenarioNode,featureNode)-> {
                gherkinPlanBuilder.createBackgroundSteps(
                    (Feature)featureNode.getUnderlyingModel(),
                    scenarioNode.source(),
                    scenarioNode
                ).ifPresent(scenarioNode::addFirstChild);
            });
    }



    private void attachImplementationSteps(PlanNodeBuilder defStepNode, PlanNodeBuilder impScenarioNode) {
        int[] stepMap = computeStepMap(defStepNode.parent().map(PlanNodeBuilder::numChildren).orElse(0),impScenarioNode);
        for (int i=0;i<stepMap[defStepNode.positionInParent()];i++) {
            impScenarioNode
                .children(withType(NodeType.STEP))
                .findFirst()
                .ifPresent(defStepNode::addChild);
        }
    }



    private String definitionTag(Configuration configuration) {
        return configuration
            .get(KukumoConfiguration.REDEFINITION_DEFINITION_TAG,String.class)
            .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_DEFINITION_TAG);
    }


    private String implementationTag(Configuration configuration) {
        return configuration
                .get(KukumoConfiguration.REDEFINITION_IMPLEMENTATION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_IMPLEMENTATION_TAG);
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
        return withGherkinType(GHERKIN_TYPE_FEATURE);
    }

    private Predicate<PlanNodeBuilder> ofTypeBackground() {
        return withGherkinType(GHERKIN_TYPE_BACKGROUND);
    }


    private int[] computeStepMap(int numDefChildren, PlanNodeBuilder implNode) {
        int[] stepMap = new int[numDefChildren];
        String stepMapProperty = implNode.properties().get(KukumoConfiguration.REDEFINITION_STEP_MAP);
        try {
            if (stepMapProperty != null) {
                String[] stepMapArray = stepMapProperty.split("-");
                for (int i = 0; i < stepMapArray.length; i++) {
                    stepMap[i] = Integer.valueOf(stepMapArray[i]);
                }
            } else {
                Arrays.fill(stepMap, 1);
            }
            return stepMap;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new KukumoException("Bad definition of step map in {} : {}",implNode.source(),stepMapProperty,e);
        }
    }


}
