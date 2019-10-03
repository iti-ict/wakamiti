package iti.kukumo.gherkin;

import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioOutline;
import iti.commons.configurer.Configuration;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.core.plan.DefaultPlanNode;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GherkinPlanRedefiner {

    private static final Logger LOGGER = Kukumo.LOGGER;

    private final String definitionTag;
    private final String implementationTag;
    private final GherkinPlanner factory;

    public GherkinPlanRedefiner(Configuration configuration, GherkinPlanner factory) {
        this.factory = factory;
        this.definitionTag = configuration
                .get(KukumoConfiguration.REDEFINITION_DEFINITION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_DEFINITION_TAG);
        this.implementationTag = configuration
                .get(KukumoConfiguration.REDEFINITION_IMPLEMENTATION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_IMPLEMENTATION_TAG);
    }



    public void arrangeRedefinitions(DefaultPlanNode plan) {
        removeScenariosFromScenarioOutlines(plan);
        arrangeScenarioRedefinitions(plan);
        arrangeScenarioOutlineRedefinitions(plan);
        plan.removeChildrenIf(this::hasImplementationTag);
    }




    protected void arrangeScenarioRedefinitions(DefaultPlanNode plan) {

        Map<String,DefaultPlanNode> definitionScenarios = collectDefinitionScenarios(plan);
        Map<String,DefaultPlanNode> implementationScenarios = collectImplementationScenarios(plan);

        for (DefaultPlanNode definitionScenario : definitionScenarios.values()) {
            DefaultPlanNode implementationScenario = implementationScenarios.get(definitionScenario.id());
            if (implementationScenario == null) {
                throw new KukumoException("Problem in {} '{}'\n\tNo implementation scenario with id '{}'",
                        definitionScenario.source(), definitionScenario.displayName(), definitionScenario.id());
            }
            arrangeScenarioImplIntoDefinition(definitionScenario, implementationScenario);
        }

    }



    protected void arrangeScenarioOutlineRedefinitions(DefaultPlanNode plan) {

        Map<String,DefaultPlanNode> defScenarioOutlines = collectDefinitionScenarioOutlines(plan);
        Map<String, DefaultPlanNode> implScenarioOutlines = collectImplementationScenarioOutlines(plan);

        for (DefaultPlanNode defScenarioOutline : defScenarioOutlines.values()) {

            DefaultPlanNode implScenarioOutline = implScenarioOutlines.get(defScenarioOutline.id());
            if (implScenarioOutline == null) {
                throw new KukumoException(
                        "Cannot redefine scenario outline <{}>::'{}'\n\tNo implementation scenario outline with id '{}'",
                        defScenarioOutline.source(), defScenarioOutline.displayName(), defScenarioOutline.id());
            }
            DefaultPlanNode implFeature = findParentFeature(plan, implScenarioOutline)
                    .orElseThrow(IllegalStateException::new);  // never should throw this exception

            Examples defExamples = ((ScenarioOutline)defScenarioOutline.getUnderlyingModel()).getExamples().get(0);

            List<DefaultPlanNode> implBackgroundSteps = factory.createBackgroundSteps(
                    (Feature) implFeature.getUnderlyingModel(),
                    implFeature.source()
            );

            List<DefaultPlanNode> implScenarios = factory.createScenariosFromExamples(
                    (ScenarioOutline)implScenarioOutline.getUnderlyingModel(),
                    defExamples,
                    implScenarioOutline,
                    implBackgroundSteps,
                    implScenarioOutline.language(),
                    implScenarioOutline.source()
            );

            for (int i=0;i<defScenarioOutline.numChildren();i++) {
                arrangeScenarioImplIntoDefinition(
                    (DefaultPlanNode) defScenarioOutline.child(i), implScenarios.get(i))
                ;
            }

        }

    }



    private void removeScenariosFromScenarioOutlines(PlanNode plan) {
        for (DefaultPlanNode scenarioOutline : collectNodesById(
                "feature",
                "scenarioOutline",
                plan.children().filter(this::hasImplementationTag),
                new HashMap<>())
                .values()) {
            scenarioOutline.clearChildren();
        }
    }




    private Map<String, DefaultPlanNode> collectDefinitionScenarios(PlanNode plan) {
        return collectNodesById(
            "feature",
            "scenario",
            plan.children().filter(this::hasDefinitionTag),
            new HashMap<>());
    }


    private Map<String, DefaultPlanNode> collectImplementationScenarios(PlanNode plan) {
        return collectNodesById(
            "feature",
            "scenario",
            plan.children().filter(this::hasImplementationTag),
            new HashMap<>());
    }



    private Map<String, DefaultPlanNode> collectDefinitionScenarioOutlines(PlanNode plan) {
        return collectNodesById(
            "feature",
            "scenarioOutline",
            plan.children().filter(this::hasDefinitionTag),
            new HashMap<>());
    }


    private Map<String, DefaultPlanNode> collectImplementationScenarioOutlines(PlanNode plan) {
        return collectNodesById(
            "feature",
            "scenarioOutline",
            plan.children().filter(this::hasImplementationTag),
            new HashMap<>());
    }


    private Optional<DefaultPlanNode> findParentFeature(PlanNode plan, PlanNode child) {
        return plan.children()
               .map(DefaultPlanNode.class::cast)
            .filter(feature -> feature.containsChild(child))
            .findFirst()
            ;
    }



    protected void arrangeScenarioImplIntoDefinition(DefaultPlanNode defScenario, DefaultPlanNode implScenario) {

            List<DefaultPlanNode> nonBackgroundDefSteps =
                    stepList(defScenario, not(DefaultPlanNode::isBackgroundStep));
            List<DefaultPlanNode> backgroundDefSteps =
                    stepList(defScenario, DefaultPlanNode::isBackgroundStep);
            List<DefaultPlanNode> nonBackgroundImplSteps =
                    stepList(implScenario, not(DefaultPlanNode::isBackgroundStep));
            List<DefaultPlanNode> backgroundImplSteps =
                    stepList(implScenario, DefaultPlanNode::isBackgroundStep);

            defScenario.clearChildren();

            if (!backgroundImplSteps.isEmpty()) {
                DefaultPlanNode virtualBackgroundNode = new DefaultPlanNode(NodeType.STEP_AGGREGATOR)
                        .setName("<background>")
                        .setDisplayNamePattern("{name}");
                backgroundImplSteps.forEach(virtualBackgroundNode::addChild);
                defScenario.addChild(virtualBackgroundNode);
            }
            backgroundDefSteps.forEach(defScenario::addChild);

            int[] stepMap = computeStepMap(nonBackgroundDefSteps.size(), implScenario);
            int visitedSteps = 0;
            for (int i = 0; i<nonBackgroundDefSteps.size();i++) {
                DefaultPlanNode redefinedChild = nonBackgroundDefSteps.get(i).copy();
                for (int j = visitedSteps; j < visitedSteps + stepMap[i]; j++) {
                    // avoid getting and error if the map expects more implementation steps than actually exist
                    if (nonBackgroundImplSteps.size() > j) {
                        redefinedChild.addChild(nonBackgroundImplSteps.get(j));
                    }
                }
                visitedSteps += stepMap[i];
                defScenario.addChild(redefinedChild);
            }
            // if there are implementation steps not mapped, add them to the last step
            DefaultPlanNode lastDefScenarioChild = (DefaultPlanNode) defScenario.child(defScenario.numChildren()-1);
            for (int i = visitedSteps; i < nonBackgroundImplSteps.size(); i++) {
                lastDefScenarioChild.addChild(nonBackgroundImplSteps.get(i));
            }

            // change the node type of children
            defScenario.children().forEach(child ->{
                ((DefaultPlanNode)child).setNodeType( child.hasChildren() ? NodeType.STEP_AGGREGATOR : NodeType.VIRTUAL_STEP);
            });

    }







    private Map<String, DefaultPlanNode> collectNodesById(
            String parentGherkinType,
            String gherkinType,
            Stream<PlanNode> nodes,
            Map<String, DefaultPlanNode> output
   ) {
        for (PlanNode node : nodes.collect(Collectors.toList())) {
            if (node.properties().get("gherkinType").equals(gherkinType)) {
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
                    output.put(node.id(), (DefaultPlanNode) node);
                }
            } else if (node.properties().get("gherkinType").equals(parentGherkinType) && node.hasChildren()) {
                collectNodesById(parentGherkinType, gherkinType, node.children(), output);
            }
        }
        return output;
    }




    protected List<DefaultPlanNode> stepList(PlanNode node, Predicate<DefaultPlanNode> filter) {
        return node.children()
                .filter(DefaultPlanNode.class::isInstance)
                .map(DefaultPlanNode.class::cast)
                .filter(filter)
                .collect(Collectors.toList());
    }





    private static <T> Predicate<T> not (Predicate<T> predicate) {
        return predicate.negate();
    }



    protected int[] computeStepMap(int numDefChildren, PlanNode implNode) {
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




    private boolean hasDefinitionTag(PlanNode node) {
        return node.hasTag(this.definitionTag);
    }


    private boolean hasImplementationTag(PlanNode node) {
        return node.hasTag(this.implementationTag);
    }



}
