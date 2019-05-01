package iti.kukumo.gherkin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioOutline;
import iti.commons.configurer.Configuration;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeTypes;
import iti.kukumo.core.plan.DefaultPlanNode;
import iti.kukumo.core.plan.DefaultPlanStep;

public class GherkinPlanRedefiner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GherkinPlanRedefiner.class);

    private final String definitionTag;
    private final String implementationTag;
    private final GherkinPlanner factory;

    public GherkinPlanRedefiner(Configuration configuration, GherkinPlanner factory) {
        this.factory = factory;
        this.definitionTag = configuration
                .getString(KukumoConfiguration.REDEFINITION_DEFINITION_TAG)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_DEFINITION_TAG);
        this.implementationTag = configuration
                .getString(KukumoConfiguration.REDEFINITION_IMPLEMENTATION_TAG)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_IMPLEMENTATION_TAG);
    }



    public void arrangeRedefinitions(PlanNode plan) {
        removeScenariosFromScenarioOutlines(plan);
        arrangeScenarioRedefinitions(plan);
        arrangeScenarioOutlineRedefinitions(plan);
        plan.removeChildrenIf(this::hasImplementationTag);
    }




    protected void arrangeScenarioRedefinitions(PlanNode plan) {

        Map<String,DefaultPlanNode<?>> definitionScenarios = collectDefinitionScenarios(plan);
        Map<String, DefaultPlanNode<?>> implementationScenarios = collectImplementationScenarios(plan);

        for (PlanNode definitionScenario : definitionScenarios.values()) {
            PlanNode implementationScenario = implementationScenarios.get(definitionScenario.id());
            if (implementationScenario == null) {
                throw new KukumoException("Problem in {} '{}'\n\tNo implementation scenario with id '{}'",
                        definitionScenario.source(), definitionScenario.displayName(), definitionScenario.id());
            }
            arrangeScenarioImplIntoDefinition(definitionScenario, implementationScenario);
        }

    }



    protected void arrangeScenarioOutlineRedefinitions(PlanNode plan) {

        Map<String,DefaultPlanNode<?>> defScenarioOutlines = collectDefinitionScenarioOutlines(plan);
        Map<String, DefaultPlanNode<?>> implScenarioOutlines = collectImplementationScenarioOutlines(plan);

        for (DefaultPlanNode<?> defScenarioOutline : defScenarioOutlines.values()) {

            DefaultPlanNode<?> implScenarioOutline = implScenarioOutlines.get(defScenarioOutline.id());
            if (implScenarioOutline == null) {
                throw new KukumoException(
                        "Cannot redefine scenario outline <{}>::'{}'\n\tNo implementation scenario outline with id '{}'",
                        defScenarioOutline.source(), defScenarioOutline.displayName(), defScenarioOutline.id());
            }
            DefaultPlanNode<?> implFeature = findParentFeature(plan, implScenarioOutline)
                    .orElseThrow(IllegalStateException::new);  // never should throw this exception

            Examples defExamples = ((ScenarioOutline)defScenarioOutline.getGherkinModel()).getExamples().get(0);

            List<DefaultPlanStep> implBackgroundSteps = factory.createBackgroundSteps(
                    (Feature) implFeature.getGherkinModel(),
                    implFeature.source()
            );

            List<DefaultPlanNode<?>> implScenarios = factory.createScenariosFromExamples(
                    (ScenarioOutline)implScenarioOutline.getGherkinModel(),
                    defExamples,
                    implScenarioOutline,
                    implBackgroundSteps,
                    implScenarioOutline.language(),
                    implScenarioOutline.source()
            );

            for (int i=0;i<defScenarioOutline.numChildren();i++) {
                arrangeScenarioImplIntoDefinition(defScenarioOutline.child(i), implScenarios.get(i));
            }

        }

    }



    private void removeScenariosFromScenarioOutlines(PlanNode plan) {
        for (PlanNode scenarioOutline : collectNodesById(
                PlanNodeTypes.FEATURE,
                PlanNodeTypes.SCENARIO_OUTLINE,
                plan.children().filter(this::hasImplementationTag),
                new HashMap<>())
                .values()) {
            scenarioOutline.clearChildren();
        }
    }




    private Map<String, DefaultPlanNode<?>> collectDefinitionScenarios(PlanNode plan) {
        return collectNodesById(
            PlanNodeTypes.FEATURE,
            PlanNodeTypes.SCENARIO,
            plan.children().filter(this::hasDefinitionTag),
            new HashMap<>());
    }


    private Map<String, DefaultPlanNode<?>> collectImplementationScenarios(PlanNode plan) {
        return collectNodesById(
            PlanNodeTypes.FEATURE,
            PlanNodeTypes.SCENARIO,
            plan.children().filter(this::hasImplementationTag),
            new HashMap<>());
    }



    private Map<String, DefaultPlanNode<?>> collectDefinitionScenarioOutlines(PlanNode plan) {
        return collectNodesById(
            PlanNodeTypes.FEATURE,
            PlanNodeTypes.SCENARIO_OUTLINE,
            plan.children().filter(this::hasDefinitionTag),
            new HashMap<>());
    }


    private Map<String, DefaultPlanNode<?>> collectImplementationScenarioOutlines(PlanNode plan) {
        return collectNodesById(
            PlanNodeTypes.FEATURE,
            PlanNodeTypes.SCENARIO_OUTLINE,
            plan.children().filter(this::hasImplementationTag),
            new HashMap<>());
    }


    private Optional<DefaultPlanNode<?>> findParentFeature(PlanNode plan, PlanNode child) {
        return plan.children()
            .filter(feature -> feature.containsChild(child))
            .findFirst()
            .map(DefaultPlanNode.class::cast);
    }



    protected void arrangeScenarioImplIntoDefinition(PlanNode defScenario, PlanNode implScenario) {

            List<DefaultPlanStep> nonBackgroundDefSteps =
                    stepList(defScenario, not(DefaultPlanStep::isBackgroundStep));
            List<DefaultPlanStep> backgroundDefSteps =
                    stepList(defScenario, DefaultPlanStep::isBackgroundStep);
            List<DefaultPlanStep> nonBackgroundImplSteps =
                    stepList(implScenario, not(DefaultPlanStep::isBackgroundStep));
            List<DefaultPlanStep> backgroundImplSteps =
                    stepList(implScenario, DefaultPlanStep::isBackgroundStep);

            defScenario.clearChildren();

            if (!backgroundImplSteps.isEmpty()) {
                DefaultPlanNode<?> virtualBackgroundNode = new DefaultPlanNode<>(PlanNodeTypes.STEP)
                        .setKeyword("*")
                        .setName("<background>");
                backgroundImplSteps.forEach(virtualBackgroundNode::addChild);
                defScenario.addChild(virtualBackgroundNode);
            }
            backgroundDefSteps.forEach(defScenario::addChild);

            int[] stepMap = computeStepMap(nonBackgroundDefSteps.size(), implScenario);
            int visitedSteps = 0;
            for (int i = 0; i<nonBackgroundDefSteps.size();i++) {
                DefaultPlanNode<?> redefinedChild = nonBackgroundDefSteps.get(i).copyAsNode();
                for (int j = visitedSteps; j < visitedSteps + stepMap[i]; j++) {
                    // avoid getting and error if the map expects more implementation steps than actually exist
                    if (nonBackgroundDefSteps.size() >= j) {
                        redefinedChild.addChild(nonBackgroundImplSteps.get(j));
                    }
                }
                visitedSteps += stepMap[i];
                defScenario.addChild(redefinedChild);
            }
            // if there are implementation steps not mapped, add them to the last step
            for (int i = visitedSteps; i < nonBackgroundImplSteps.size(); i++) {
                defScenario.child(defScenario.numChildren()-1).addChild(nonBackgroundImplSteps.get(i));
            }

            // if there are definition steps without implementation, attach them a void step
            defScenario.children()
                .filter(not(PlanNode::hasChildren))
                .forEach(child->child.addChild(factory.voidStep()));
    }







    private Map<String, DefaultPlanNode<?>> collectNodesById(
            String parentNodeType,
            String nodeType,
            Stream<PlanNode> nodes,
            Map<String, DefaultPlanNode<?>> output
   ) {
        for (PlanNode node : nodes.collect(Collectors.toList())) {
            if (node.nodeType().equals(nodeType)) {
                if (node.id() == null) {
                    LOGGER.warn(
                            "Scenario <{}>::'{}' not having a unique id; it will be ignored",
                            node.source(), node.displayName()
                    );
                } else if (output.containsKey(node.id())){
                    PlanNode existing = output.get(node.id());
                    throw new KukumoException("More than one scenario using the id '{}'\n\t<{}>::'{}'\n\t<{}>::'{}'",
                            node.id(), node.source(), node.displayName(), existing.source(), existing.displayName()
                    );
                } else {
                    output.put(node.id(), (DefaultPlanNode<?>) node);
                }
            } else if (node.nodeType().equals(parentNodeType) && node.hasChildren()) {
                collectNodesById(parentNodeType, nodeType, node.children(), output);
            }
        }
        return output;
    }




    protected List<DefaultPlanStep> stepList(PlanNode node, Predicate<DefaultPlanStep> filter) {
        return node.children()
                .filter(DefaultPlanStep.class::isInstance)
                .map(DefaultPlanStep.class::cast)
                .filter(filter)
                .collect(Collectors.toList());
    }





    private static <T> Predicate<T> not (Predicate<T> predicate) {
        return predicate.negate();
    }



    protected int[] computeStepMap(int numDefChildren, PlanNode implNode) {
        int[] stepMap  = new int[numDefChildren];
        String stepMapProperty = implNode.properties().get(KukumoConfiguration.REDEFINITION_STEP_MAP);
        if (stepMapProperty != null) {
            String[] stepMapArray = stepMapProperty.split("-");
            for (int i=0; i<stepMapArray.length; i++) {
                stepMap[i] = Integer.valueOf(stepMapArray[i]);
            }
        } else {
            Arrays.fill(stepMap, 1);
        }
        return stepMap;
    }




    private boolean hasDefinitionTag(PlanNode node) {
        return node.hasTag(this.definitionTag);
    }


    private boolean hasImplementationTag(PlanNode node) {
        return node.hasTag(this.implementationTag);
    }



}
