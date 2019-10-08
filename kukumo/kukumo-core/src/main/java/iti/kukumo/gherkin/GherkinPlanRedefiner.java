package iti.kukumo.gherkin;

import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioOutline;
import iti.commons.configurer.Configuration;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.core.plan.PlanNodeBuilder;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class GherkinPlanRedefiner {

    private static final Logger LOGGER = Kukumo.LOGGER;

    private final String definitionTag;
    private final String implementationTag;
    private final GherkinPlanBuilder factory;

    public GherkinPlanRedefiner(Configuration configuration, GherkinPlanBuilder factory) {
        this.factory = factory;
        this.definitionTag = configuration
                .get(KukumoConfiguration.REDEFINITION_DEFINITION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_DEFINITION_TAG);
        this.implementationTag = configuration
                .get(KukumoConfiguration.REDEFINITION_IMPLEMENTATION_TAG,String.class)
                .orElse(KukumoConfiguration.Defaults.DEFAULT_REDEFINITION_IMPLEMENTATION_TAG);
    }



/*
    private PlanNodeBuilderBuilderRule removeScenariosFromScenarioOutlines() {
        return forAnyNode(
          withTag(implementationTag).and(withProperty("gherkin","scenarioOutline"))
        ).perform( PlanNodeBuilder::clearChildren);
    }
*/


    public void arrangeRedefinitions(PlanNodeBuilder plan) {
        removeScenariosFromScenarioOutlines(plan);
        arrangeScenarioRedefinitions(plan);
        arrangeScenarioOutlineRedefinitions(plan);
        plan.removeChildrenIf(this::hasImplementationTag);
    }




    protected void arrangeScenarioRedefinitions(PlanNodeBuilder plan) {

        Map<String,PlanNodeBuilder> definitionScenarios = collectDefinitionScenarios(plan);
        Map<String,PlanNodeBuilder> implementationScenarios = collectImplementationScenarios(plan);

        for (PlanNodeBuilder definitionScenario : definitionScenarios.values()) {
            PlanNodeBuilder implementationScenario = implementationScenarios.get(definitionScenario.id());
            if (implementationScenario == null) {
                throw new KukumoException("Problem in {} '{}'\n\tNo implementation scenario with id '{}'",
                        definitionScenario.source(), definitionScenario.displayName(), definitionScenario.id());
            }
            arrangeScenarioImplIntoDefinition(definitionScenario, implementationScenario);
        }

    }



    protected void arrangeScenarioOutlineRedefinitions(PlanNodeBuilder plan) {

        Map<String,PlanNodeBuilder> defScenarioOutlines = collectDefinitionScenarioOutlines(plan);
        Map<String, PlanNodeBuilder> implScenarioOutlines = collectImplementationScenarioOutlines(plan);

        for (PlanNodeBuilder defScenarioOutline : defScenarioOutlines.values()) {

            PlanNodeBuilder implScenarioOutline = implScenarioOutlines.get(defScenarioOutline.id());
            if (implScenarioOutline == null) {
                throw new KukumoException(
                        "Cannot redefine scenario outline <{}>::'{}'\n\tNo implementation scenario outline with id '{}'",
                        defScenarioOutline.source(), defScenarioOutline.displayName(), defScenarioOutline.id());
            }
            PlanNodeBuilder implFeature = findParentFeature(plan, implScenarioOutline)
                    .orElseThrow(IllegalStateException::new);  // never should throw this exception

            Examples defExamples = ((ScenarioOutline)defScenarioOutline.getUnderlyingModel()).getExamples().get(0);

            Optional<PlanNodeBuilder> implBackgroundSteps = factory.createBackgroundSteps(
                    (Feature) implFeature.getUnderlyingModel(),
                    implFeature.source(),
                    implFeature
            );

            List<PlanNodeBuilder> implScenarios = factory.createScenariosFromExamples(
                    (ScenarioOutline)implScenarioOutline.getUnderlyingModel(),
                    defExamples,
                    implScenarioOutline,
                    implBackgroundSteps,
                    implScenarioOutline.language(),
                    implScenarioOutline.source()
            );

            for (int i=0;i<defScenarioOutline.numChildren();i++) {
                arrangeScenarioImplIntoDefinition(
                    (PlanNodeBuilder) defScenarioOutline.child(i), implScenarios.get(i))
                ;
            }

        }

    }



    private void removeScenariosFromScenarioOutlines(PlanNodeBuilder plan) {
/*
        forAnyNode(
          withTag(implementationTag).and(withProperty("gherkin","scenarioOutline"))
        ).perform( PlanNodeBuilder::clearChildren);

*/
        for (PlanNodeBuilder scenarioOutline : collectNodesById(
                "feature",
                "scenarioOutline",
                plan.children().filter(this::hasImplementationTag),
                new HashMap<>())
                .values()) {
            scenarioOutline.clearChildren();
        }
    }




    private Map<String, PlanNodeBuilder> collectDefinitionScenarios(PlanNodeBuilder plan) {
        return collectNodesById(
            "feature",
            "scenario",
            plan.children().filter(this::hasDefinitionTag),
            new HashMap<>());
    }


    private Map<String, PlanNodeBuilder> collectImplementationScenarios(PlanNodeBuilder plan) {
        return collectNodesById(
            "feature",
            "scenario",
            plan.children().filter(this::hasImplementationTag),
            new HashMap<>());
    }



    private Map<String, PlanNodeBuilder> collectDefinitionScenarioOutlines(PlanNodeBuilder plan) {
        return collectNodesById(
            "feature",
            "scenarioOutline",
            plan.children().filter(this::hasDefinitionTag),
            new HashMap<>());
    }


    private Map<String, PlanNodeBuilder> collectImplementationScenarioOutlines(PlanNodeBuilder plan) {
        return collectNodesById(
            "feature",
            "scenarioOutline",
            plan.children().filter(this::hasImplementationTag),
            new HashMap<>());
    }


    private Optional<PlanNodeBuilder> findParentFeature(PlanNodeBuilder plan, PlanNodeBuilder child) {
        return plan.children()
               .map(PlanNodeBuilder.class::cast)
            .filter(feature -> feature.containsChild(child))
            .findFirst()
            ;
    }



    protected void arrangeScenarioImplIntoDefinition(PlanNodeBuilder defScenario, PlanNodeBuilder implScenario) {

            List<PlanNodeBuilder> nonBackgroundDefSteps =
                    stepList(defScenario, not(this::isBackgroundStep));
            List<PlanNodeBuilder> backgroundDefSteps =
                    stepList(defScenario, this::isBackgroundStep);
            List<PlanNodeBuilder> nonBackgroundImplSteps =
                    stepList(implScenario, not(this::isBackgroundStep));
            List<PlanNodeBuilder> backgroundImplSteps =
                    stepList(implScenario, this::isBackgroundStep);

            defScenario.clearChildren();

            if (!backgroundImplSteps.isEmpty()) {
                PlanNodeBuilder virtualBackgroundNode = new PlanNodeBuilder(NodeType.STEP_AGGREGATOR)
                        .setName("<background>")
                        .setDisplayNamePattern("{name}");
                backgroundImplSteps.forEach(virtualBackgroundNode::addChild);
                defScenario.addChild(virtualBackgroundNode);
            }
            backgroundDefSteps.forEach(defScenario::addChild);

            int[] stepMap = computeStepMap(nonBackgroundDefSteps.size(), implScenario);
            int visitedSteps = 0;
            for (int i = 0; i<nonBackgroundDefSteps.size();i++) {
                PlanNodeBuilder redefinedChild = nonBackgroundDefSteps.get(i).copy();
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
            PlanNodeBuilder lastDefScenarioChild = (PlanNodeBuilder) defScenario.child(defScenario.numChildren()-1);
            for (int i = visitedSteps; i < nonBackgroundImplSteps.size(); i++) {
                lastDefScenarioChild.addChild(nonBackgroundImplSteps.get(i));
            }

            // change the node type of children
            defScenario.children().forEach(child ->{
                ((PlanNodeBuilder)child).setNodeType( child.hasChildren() ? NodeType.STEP_AGGREGATOR : NodeType.VIRTUAL_STEP);
            });

    }







    private Map<String, PlanNodeBuilder> collectNodesById(
            String parentGherkinType,
            String gherkinType,
            Stream<PlanNodeBuilder> nodes,
            Map<String, PlanNodeBuilder> output
   ) {
        for (PlanNodeBuilder node : nodes.collect(Collectors.toList())) {
            if (node.properties().get("gherkinType").equals(gherkinType)) {
                if (node.id() == null) {
                    LOGGER.warn(
                            "Scenario <{}>::'{}' not having a unique id; it will be ignored",
                            node.source(), node.displayName()
                    );
                } else if (output.containsKey(node.id())){
                    PlanNodeBuilder existing = output.get(node.id());
                    throw new KukumoException("More than one scenario using the id '{}'\n\t<{}>::'{}'\n\t<{}>::'{}'",
                            node.id(), node.source(), node.displayName(), existing.source(), existing.displayName()
                    );
                } else {
                    output.put(node.id(),node);
                }
            } else if (node.properties().get("gherkinType").equals(parentGherkinType) && node.hasChildren()) {
                collectNodesById(parentGherkinType, gherkinType, node.children(), output);
            }
        }
        return output;
    }




    protected List<PlanNodeBuilder> stepList(PlanNodeBuilder node, Predicate<PlanNodeBuilder> filter) {
        return node.children()
                .filter(PlanNodeBuilder.class::isInstance)
                .map(PlanNodeBuilder.class::cast)
                .filter(filter)
                .collect(Collectors.toList());
    }





    private static <T> Predicate<T> not (Predicate<T> predicate) {
        return predicate.negate();
    }



    protected int[] computeStepMap(int numDefChildren, PlanNodeBuilder implNode) {
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




    private boolean hasDefinitionTag(PlanNodeBuilder node) {
        return node.tags().contains(this.definitionTag);
    }


    private boolean hasImplementationTag(PlanNodeBuilder node) {
        return node.tags().contains(this.implementationTag);
    }


    private boolean isBackgroundStep(PlanNodeBuilder node) {
        return node.parent().filter(parent -> "background".equals(parent.properties().get("gherkin"))).isPresent();
    }


}
