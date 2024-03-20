/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.PlanTransformer;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;
import es.iti.wakamiti.core.gherkin.parser.Examples;
import es.iti.wakamiti.core.gherkin.parser.Feature;
import es.iti.wakamiti.core.gherkin.parser.ScenarioOutline;
import es.iti.wakamiti.core.plan.RuleBasedPlanTransformer;
import imconfig.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static es.iti.wakamiti.core.gherkin.GherkinPlanBuilder.*;
import static es.iti.wakamiti.core.plan.PlanNodeBuilderRules.*;


/**
 * Represents a transformer for Gherkin-based test plans that allows
 * redefining certain aspects of the plan structure.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
@Extension(provider = "es.iti.wakamiti", name = "gherkin-redefinition-transformer", version = "1.1")
public class GherkinRedefinitionPlanTransformer extends RuleBasedPlanTransformer
        implements PlanTransformer {

    /**
     * {@inheritDoc}
     * <p>
     * This method creates a list of rules for transforming Gherkin-based test plans.
     *
     * @param configuration The configuration for the Gherkin redefinition transformer.
     * @return A list of plan node builder rules.
     */
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

                forEachNode(rules.backgroundStepsOfScenarioWithoutChildren())
                        .perform(node -> node.setNodeType(NodeType.VIRTUAL_STEP)),

                forEachNode(rules.definitionScenario())
                        .given(rules.implementationScenarioSharingId())
                        .perform(copyProperties()),

                forEachNode(rules.implementationFeature())
                        .perform(removeNode())

        );
    }

    /**
     * Defines rules for Gherkin-based test plan transformation.
     */
    private class GherkinRedefinitionRules {

        private final GherkinPlanBuilder gherkinPlanBuilder;
        private final String implementationTag;
        private final String definitionTag;

        /**
         * Constructs a set of rules for Gherkin redefinition.
         *
         * @param configuration The configuration for Gherkin redefinition.
         */
        public GherkinRedefinitionRules(Configuration configuration) {
            gherkinPlanBuilder = new GherkinPlanBuilder();
            gherkinPlanBuilder.configure(configuration);
            implementationTag = implementationTag(configuration);
            definitionTag = definitionTag(configuration);
        }

        /**
         * Defines a rule predicate for identifying definition background nodes.
         *
         * @return The rule predicate for definition background nodes.
         */
        public Predicate<PlanNodeBuilder> definitionBackground() {
            return ofTypeBackground().and(withTag(definitionTag));
        }

        /**
         * Defines a rule predicate for identifying definition scenario nodes.
         *
         * @return The rule predicate for definition scenario nodes.
         */
        public Predicate<PlanNodeBuilder> definitionScenario() {
            return ofTypeScenario().and(withTag(definitionTag));
        }

        /**
         * Defines a rule predicate for identifying definition step nodes.
         *
         * @return The rule predicate for definition step nodes.
         */
        private Predicate<PlanNodeBuilder> definitionStep() {
            return withType(NodeType.STEP).and(childOf(anyNode(withTag(definitionTag))));
        }

        /**
         * Defines a rule predicate for identifying definition step aggregator nodes.
         *
         * @return The rule predicate for definition step aggregator nodes.
         */
        private Predicate<PlanNodeBuilder> definitionStepAggregator() {
            return withType(NodeType.STEP_AGGREGATOR).and(childOf(anyNode(withTag(definitionTag))));
        }

        /**
         * Defines a rule predicate for identifying implementation background nodes.
         *
         * @return The rule predicate for implementation background nodes.
         */
        public Predicate<PlanNodeBuilder> implementationBackground() {
            return ofTypeBackground().and(withTag(implementationTag));
        }

        /**
         * Defines a rule predicate for identifying implementation scenario outline nodes.
         *
         * @return The rule predicate for an implementation scenario outline nodes.
         */
        public Predicate<PlanNodeBuilder> implementationScenarioOutline() {
            return ofTypeScenarioOutline().and(withTag(implementationTag));
        }

        /**
         * Defines a rule predicate for identifying implementation scenario nodes.
         *
         * @return The rule predicate for implementation scenario nodes.
         */
        public Predicate<PlanNodeBuilder> implementationScenario() {
            return ofTypeScenario().and(withTag(implementationTag));
        }

        /**
         * Defines a rule predicate for identifying implementation scenario child of scenario outline nodes.
         *
         * @return The rule predicate for implementation scenario child of scenario outline nodes.
         */
        public Predicate<PlanNodeBuilder> implementationScenarioChildOfScenarioOutline() {
            return implementationScenario().and(childOf(anyNode(ofTypeScenarioOutline())));
        }

        /**
         * Defines a rule predicate for identifying implementation feature nodes.
         *
         * @return The rule predicate for implementation feature nodes.
         */
        public Predicate<PlanNodeBuilder> implementationFeature() {
            return ofTypeFeature().and(withTag(implementationTag));
        }

        /**
         * Defines a rule predicate for identifying step aggregator without children nodes.
         *
         * @return The rule predicate for step aggregator without children nodes.
         */
        public Predicate<PlanNodeBuilder> stepAggregatorWithoutChildren() {
            return withType(NodeType.STEP_AGGREGATOR).and(withoutChildren());
        }

        /**
         * Defines a rule predicate for identifying background steps of scenario without children nodes.
         *
         * @return The rule predicate for background steps of scenario without children nodes.
         */
        public Predicate<PlanNodeBuilder> backgroundStepsOfScenarioWithoutChildren() {
            return withType(NodeType.STEP).and(withAnyAncestor(withGherkinType(GHERKIN_TYPE_BACKGROUND)))
                    .and(withAnyAncestor(
                            withType(NodeType.TEST_CASE).and(
                                    withNoneDescendant(
                                            withType(NodeType.STEP)
                                                    .and(withNoneAncestor(withGherkinType(GHERKIN_TYPE_BACKGROUND)))
                                    )
                            )
                    ));
        }

        /**
         * Retrieves the parent feature of a node.
         *
         * @return A function to retrieve the parent feature of a node.
         */
        private Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> parentFeature() {
            return node -> node.ancestors().filter(ofTypeFeature()).findFirst();
        }

        /**
         * Defines a function for identifying definition scenario outline sharing ID.
         *
         * @return A function for identifying definition scenario outline sharing ID.
         */
        public Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> definitionScenarioOutlineSharingId() {
            return anyOtherNode(
                    ofTypeScenarioOutline().and(withTag(definitionTag)),
                    sharing(PlanNodeBuilder::id)
            );
        }

        /**
         * Defines a function for identifying implementation scenario sharing ID with parent.
         *
         * @return A function for identifying implementation scenario sharing ID with parent.
         */
        public Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> implementationScenarioSharingIdWithParent() {
            return anyOtherNode(
                    withType(NodeType.TEST_CASE).and(withTag(implementationTag)),
                    sharing(PlanNodeBuilder::parent, Optional::of, PlanNodeBuilder::id)
            );
        }

        /**
         * Defines a function for identifying definition scenario sharing ID with parent.
         *
         * @return A function for identifying definition scenario sharing ID with parent.
         */
        public Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> definitionScenarioSharingIdWithParent() {
            return anyOtherNode(
                    ofTypeScenario().and(withTag(definitionTag)),
                    sharing(PlanNodeBuilder::parent, Optional::of, PlanNodeBuilder::id)
            );
        }

        /**
         * Defines a function for identifying implementation scenario sharing ID.
         *
         * @return A function for identifying implementation scenario sharing ID.
         */
        public Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> implementationScenarioSharingId() {
            return anyOtherNode(
                    ofTypeScenario().and(withTag(implementationTag)),
                    sharing(PlanNodeBuilder::id)
            );
        }

        /**
         * Populates implementation scenario outlines with examples.
         *
         * @param scenarioOutlineNode             The scenario outline node.
         * @param scenarioOutlineNodeWithExamples The scenario outline node with examples.
         */
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

        /**
         * Populates an implementation background for a scenario node.
         *
         * @param scenarioNode The scenario node.
         * @param featureNode  The feature node.
         */
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

        /**
         * Attaches implementation background to definition scenario.
         *
         * @param impBackground The implementation background node.
         * @param defScenario   The definition scenario node.
         */
        public void attachImplementationBackgroundToDefinitionScenario(
                PlanNodeBuilder impBackground,
                PlanNodeBuilder defScenario
        ) {
            defScenario.addFirstChild(impBackground);
        }

        /**
         * Attaches implementation steps to definition step node.
         *
         * @param defStepNode     The definition step node.
         * @param impScenarioNode The implementation scenario node.
         */
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

        /**
         * Retrieves the definition tag from the configuration.
         *
         * @param configuration The configuration for Gherkin redefinition.
         * @return The definition tag.
         */
        private String definitionTag(Configuration configuration) {
            return configuration
                    .get(WakamitiConfiguration.REDEFINITION_DEFINITION_TAG, String.class)
                    .orElseThrow();
        }

        /**
         * Retrieves the implementation tag from the configuration.
         *
         * @param configuration The configuration for Gherkin redefinition.
         * @return The implementation tag.
         */
        private String implementationTag(Configuration configuration) {
            return configuration
                    .get(WakamitiConfiguration.REDEFINITION_IMPLEMENTATION_TAG, String.class)
                    .orElseThrow();
        }

        /**
         * Checks if a node has a specific Gherkin type.
         *
         * @param gherkinType The Gherkin type to check.
         * @return A predicate for checking Gherkin type.
         */
        private Predicate<PlanNodeBuilder> withGherkinType(String gherkinType) {
            return withProperty(GHERKIN_PROPERTY, gherkinType);
        }

        /**
         * Checks if a node is of type scenario outline.
         *
         * @return A predicate for checking if a node is of type scenario outline.
         */
        private Predicate<PlanNodeBuilder> ofTypeScenarioOutline() {
            return withGherkinType(GHERKIN_TYPE_SCENARIO_OUTLINE);
        }

        /**
         * Checks if a node is of a type scenario.
         *
         * @return A predicate for checking if a node is of a type scenario.
         */
        private Predicate<PlanNodeBuilder> ofTypeScenario() {
            return withGherkinType(GHERKIN_TYPE_SCENARIO);
        }

        /**
         * Checks if a node is of type feature.
         *
         * @return A predicate for checking if a node is of type feature.
         */
        private Predicate<PlanNodeBuilder> ofTypeFeature() {
            return withGherkinType(GHERKIN_TYPE_FEATURE);
        }

        /**
         * Checks if a node is of a type background.
         *
         * @return A predicate for checking if a node is of a type background.
         */
        private Predicate<PlanNodeBuilder> ofTypeBackground() {
            return withGherkinType(GHERKIN_TYPE_BACKGROUND);
        }

        /**
         * Computes the step map for attaching implementation steps.
         *
         * @param numDefChildren The number of children in the definition.
         * @param implNode       The implementation node.
         * @return The step map.
         */
        private int[] computeStepMap(int numDefChildren, PlanNodeBuilder implNode) {
            int[] stepMap = new int[numDefChildren];
            String stepMapProperty = implNode.properties()
                    .get(WakamitiConfiguration.REDEFINITION_STEP_MAP);
            try {
                if (stepMapProperty != null) {
                    String[] stepMapArray = stepMapProperty.split("-");
                    for (int i = 0; i < stepMapArray.length; i++) {
                        stepMap[i] = Integer.parseInt(stepMapArray[i]);
                    }
                } else {
                    Arrays.fill(stepMap, 1);
                }
                return stepMap;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new WakamitiException(
                        "Bad definition of step map in {} : {}",
                        implNode.source(),
                        stepMapProperty,
                        e
                );
            }
        }

    }

}