/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.gherkin;


import static iti.kukumo.core.plan.PlanNodeBuilderRules.anyNode;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.anyOtherNode;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.childOf;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.copyProperties;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.forEachNode;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.removeNode;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.sharing;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.withProperty;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.withTag;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.withType;
import static iti.kukumo.core.plan.PlanNodeBuilderRules.withoutChildren;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_PROPERTY;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_BACKGROUND;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_FEATURE;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_SCENARIO;
import static iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_SCENARIO_OUTLINE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import iti.commons.configurer.Configuration;
import iti.commons.gherkin.Examples;
import iti.commons.gherkin.Feature;
import iti.commons.gherkin.ScenarioOutline;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNodeBuilder;
import iti.kukumo.core.plan.PlanNodeBuilderRules.PlanNodeBuilderRule;
import iti.kukumo.core.plan.RuleBasedPlanTransformer;



@Extension(provider = "iti.kukumo", name = "gherkin-redefinition-transformer")
public class GherkinRedefinitionPlanTransformer extends RuleBasedPlanTransformer
                implements PlanTransformer {

    @Override
    protected List<PlanNodeBuilderRule> createRules(Configuration configuration) {
        GherkinRedefinitionRules rules = new GherkinRedefinitionRules(configuration);
        return Arrays.asList(

            forEachNode(rules.definitionBackground())
                .perform(removeNode()),

            forEachNode(rules.implementationScenarioOutline())
                .perform(PlanNodeBuilder::clearChildren),

            forEachNode(rules.implementationScenarioOutline())
                .given(rules.definitionScenarioOutlineSharingId())
                .perform(rules::populateImplementationScenarioOutlinesWithExamples),

            forEachNode(rules.implementationScenarioChildOfScenarioOutline())
                .given(rules.parentFeature())
                .perform(rules::populateImplementationBackground),

            forEachNode(rules.definitionStep())
                .perform(node -> node.setNodeType(NodeType.STEP_AGGREGATOR)),

            forEachNode(rules.definitionStepAggregator())
                .given(rules.implementationScenarioSharingIdWithParent())
                .perform(rules::attachImplementationSteps),

            forEachNode(rules.implementationBackground())
                .given(rules.definitionScenarioSharingIdWithParent())
                .perform(rules::attachImplementationBackgroundToDefinitionScenario),

            forEachNode(rules.stepAggregatorWithoutChildren())
                .perform(node -> node.setNodeType(NodeType.VIRTUAL_STEP)),

            forEachNode(rules.definitionScenario())
                .given(rules.implementationScenarioSharingId())
                .perform(copyProperties()),

            forEachNode(rules.implementationFeature())
                .perform(removeNode())

        );
    }


    private class GherkinRedefinitionRules {

        private final GherkinPlanBuilder gherkinPlanBuilder;
        private final String implementationTag;
        private final String definitionTag;


        public GherkinRedefinitionRules(Configuration configuration) {
            gherkinPlanBuilder = new GherkinPlanBuilder();
            gherkinPlanBuilder.configure(configuration);
            implementationTag = implementationTag(configuration);
            definitionTag = definitionTag(configuration);
        }


        public Predicate<PlanNodeBuilder> definitionBackground() {
            return ofTypeBackground().and(withTag(definitionTag));
        }


        public Predicate<PlanNodeBuilder> definitionScenario() {
            return ofTypeScenario().and(withTag(definitionTag));
        }


        private Predicate<PlanNodeBuilder> definitionStep() {
            return withType(NodeType.STEP).and(childOf(anyNode(withTag(definitionTag))));
        }


        private Predicate<PlanNodeBuilder> definitionStepAggregator() {
            return withType(NodeType.STEP_AGGREGATOR).and(childOf(anyNode(withTag(definitionTag))));
        }


        public Predicate<PlanNodeBuilder> implementationBackground() {
            return ofTypeBackground().and(withTag(implementationTag));
        }


        public Predicate<PlanNodeBuilder> implementationScenarioOutline() {
            return ofTypeScenarioOutline().and(withTag(implementationTag));
        }


        public Predicate<PlanNodeBuilder> implementationScenario() {
            return ofTypeScenario().and(withTag(implementationTag));
        }


        public Predicate<PlanNodeBuilder> implementationScenarioChildOfScenarioOutline() {
            return implementationScenario().and(childOf(anyNode(ofTypeScenarioOutline())));
        }


        public Predicate<PlanNodeBuilder> implementationFeature() {
            return ofTypeFeature().and(withTag(implementationTag));
        }


        public Predicate<PlanNodeBuilder> stepAggregatorWithoutChildren() {
            return withType(NodeType.STEP_AGGREGATOR).and(withoutChildren());
        }


        private Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> parentFeature() {
            return node -> node.ancestors().filter(ofTypeFeature()).findFirst();
        }


        public Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> definitionScenarioOutlineSharingId() {
            return anyOtherNode(
                ofTypeScenarioOutline().and(withTag(definitionTag)),
                sharing(PlanNodeBuilder::id)
            );
        }


        public Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> implementationScenarioSharingIdWithParent() {
            return anyOtherNode(
                withType(NodeType.TEST_CASE).and(withTag(implementationTag)),
                sharing(PlanNodeBuilder::parent, Optional::of, PlanNodeBuilder::id)
            );
        }


        public Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> definitionScenarioSharingIdWithParent() {
            return anyOtherNode(
                ofTypeScenario().and(withTag(definitionTag)),
                sharing(PlanNodeBuilder::parent, Optional::of, PlanNodeBuilder::id)
            );
        }


        public Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> implementationScenarioSharingId() {
            return anyOtherNode(
                ofTypeScenario().and(withTag(implementationTag)),
                sharing(PlanNodeBuilder::id)
            );
        }


        public void populateImplementationScenarioOutlinesWithExamples(
            PlanNodeBuilder scenarioOutlineNode,
            PlanNodeBuilder scenarioOutlineNodeWithExamples
        ) {
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
        }


        public void populateImplementationBackground(
            PlanNodeBuilder scenarioNode,
            PlanNodeBuilder featureNode
        ) {
            gherkinPlanBuilder.createBackgroundSteps(
                (Feature) featureNode.getUnderlyingModel(),
                scenarioNode.source(),
                scenarioNode
            ).ifPresent(scenarioNode::addFirstChild);
        }


        public void attachImplementationBackgroundToDefinitionScenario(
            PlanNodeBuilder impBackground,
            PlanNodeBuilder defScenario
        ) {
            defScenario.addFirstChild(
                impBackground
                    .setName("<preparation>")
                    .setKeyword(null)
                    .setDisplayNamePattern("{name}")
            );
        }


        public void attachImplementationSteps(
            PlanNodeBuilder defStepNode,
            PlanNodeBuilder impScenarioNode
        ) {
            int[] stepMap = computeStepMap(
                defStepNode.parent().map(PlanNodeBuilder::numChildren).orElse(0),
                impScenarioNode
            );
            for (int i = 0; i < stepMap[defStepNode.positionInParent()]; i++) {
                impScenarioNode
                    .children(withType(NodeType.STEP))
                    .findFirst()
                    .ifPresent(defStepNode::addChild);
            }
        }


        private String definitionTag(Configuration configuration) {
            return configuration
                .get(KukumoConfiguration.REDEFINITION_DEFINITION_TAG, String.class)
                .orElseThrow();
        }


        private String implementationTag(Configuration configuration) {
            return configuration
                .get(KukumoConfiguration.REDEFINITION_IMPLEMENTATION_TAG, String.class)
                .orElseThrow();
        }


        private Predicate<PlanNodeBuilder> withGherkinType(String gherkinType) {
            return withProperty(GHERKIN_PROPERTY, gherkinType);
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
            String stepMapProperty = implNode.properties()
                .get(KukumoConfiguration.REDEFINITION_STEP_MAP);
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
                throw new KukumoException(
                    "Bad definition of step map in {} : {}",
                    implNode.source(),
                    stepMapProperty,
                    e
                );
            }
        }

    }

}
