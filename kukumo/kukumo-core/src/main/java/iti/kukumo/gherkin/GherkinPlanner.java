package iti.kukumo.gherkin;

import gherkin.ast.*;
import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.Resource;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.extensions.Planner;
import iti.kukumo.api.extensions.ResourceType;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeTypes;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.core.plan.DefaultPlanNode;
import iti.kukumo.core.plan.DefaultPlanStep;
import iti.kukumo.gherkin.parser.CommentedNode;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Extension(
    provider = "iti.kukumo",
    name="kukumo-gherkin",
    extensionPoint = "iti.kukumo.api.extensions.Planner"
)
public class GherkinPlanner implements Planner {


    private Predicate<PlanNode> scenarioFilter = (x -> true);
    private Pattern idTagPattern = null;
    private boolean redefinitionEnabled = true;
    private GherkinPlanRedefiner redefinitionHelper;



    public void setRedefinitionEnabled(boolean redefinitionEnabled) {
        this.redefinitionEnabled = redefinitionEnabled;
    }

    public void setRedefinitionHelper(GherkinPlanRedefiner redefinitionHelper) {
        this.redefinitionHelper = redefinitionHelper;
    }

    @Override
    public boolean acceptResourceType(ResourceType<?> resourceType) {
        return resourceType.contentType().equals(GherkinDocument.class);
    }



    protected void configureFilterFromTagExpression(Configuration configuration) {
        String tagFilterExpression = configuration.get(KukumoConfiguration.TAG_FILTER,String.class).orElse("");
        if (tagFilterExpression != null && !tagFilterExpression.isEmpty()) {
            this.scenarioFilter = Kukumo.getTagFilter(tagFilterExpression)::filter;
        }
    }


    protected void configureIdTagPattern(Configuration configuration) {
        this.idTagPattern = configuration.get(KukumoConfiguration.ID_TAG_PATTERN,String.class)
                .map(this::nullIfEmpty)
                .map(Pattern::compile)
                .orElse(null);
    }


    private String nullIfEmpty(String string) {
        return string.isEmpty() ? null : string;
    }





    @SuppressWarnings("unchecked")
    @Override
    public PlanNode createPlan(List<Resource<?>> resources) {
        PlanNode plan = new DefaultPlanNode<>(PlanNodeTypes.PLAN);
        List<Resource<GherkinDocument>> gherkinResources = resources.stream()
                .map(x -> (Resource<GherkinDocument>) x).collect(Collectors.toList());
        for (Resource<GherkinDocument> gherkinResource : gherkinResources) {
            plan.addChildIfSatisfies(createFeature(gherkinResource), PlanNode::hasChildren);
        }
        if (this.redefinitionEnabled) {
            this.redefinitionHelper.arrangeRedefinitions(plan);
        }
        Kukumo.publishEvent(Event.PLAN_CREATED, plan);
        return plan;
    }




    protected DefaultPlanNode<?> createFeature(Resource<GherkinDocument> gherkinResource) {
        Feature feature = gherkinResource.content().getFeature();
        String location = gherkinResource.relativePath();
        String language = feature.getLanguage();
        DefaultPlanNode<?> node = newFeatureNode(feature, language, location);
        for (ScenarioDefinition abstractScenario : feature.getChildren()) {
            if (abstractScenario instanceof Scenario) {
                node.addChildIfSatisfies(
                        createScenario(feature, (Scenario) abstractScenario, location, node),
                        scenarioFilter
                );
            } else if (abstractScenario instanceof ScenarioOutline) {
                node.addChildIfSatisfies(
                        createScenarioOutline(feature,(ScenarioOutline) abstractScenario, location, node),
                        scenarioFilter
                );
            }
        }
        return node;
    }








    protected DefaultPlanNode<?> createScenario(
            Feature feature,
            Scenario scenario,
            String location,
            PlanNode parentNode
    ) {
        DefaultPlanNode<?> node = newScenarioNode(scenario, location, parentNode);
        for (DefaultPlanStep backgroundStep : createBackgroundSteps(feature, location)) {
            node.addChild(backgroundStep);
        }
        for (Step step : scenario.getSteps()) {
            node.addChild(createStep(step, location, parentNode.language(), node));
        }
        return node;
    }




    protected DefaultPlanNode<?> createScenarioOutline(
            Feature feature,
            ScenarioOutline scenarioOutline,
            String location,
            PlanNode parentNode
    ) {

        DefaultPlanNode<?> node = newScenarioOutlineNode(scenarioOutline,location,parentNode);
        List<DefaultPlanStep> backgroundSteps = createBackgroundSteps(feature, location);
        for (Examples examples : scenarioOutline.getExamples()) {
           List<DefaultPlanNode<?>> scenarios = createScenariosFromExamples(
                   scenarioOutline,
                   examples,
                   node,
                   backgroundSteps,
                   parentNode.language(),
                   location
           );
           scenarios.forEach(node::addChild);
        }
        return node;
    }



    protected List<DefaultPlanNode<?>> createScenariosFromExamples(
            ScenarioOutline scenarioOutline,
            Examples examples,
            DefaultPlanNode<?> scenarioOutlineNode,
            List<DefaultPlanStep> backgroundSteps,
            String language,
            String location
    ) {
        List<DefaultPlanNode<?>> output = new ArrayList<>();

        List<String> variables = tableCells(examples.getTableHeader());
        List<List<String>> values = examples.getTableBody().stream().map(this::tableCells)
                .collect(Collectors.toList());

        ArrayList<DefaultPlanStep> outlineSteps = new ArrayList<>();
        for (Step step : scenarioOutline.getSteps()) {
            outlineSteps.add(createStep(step, location, language, scenarioOutlineNode));
        }

        for (int row = 0; row < values.size(); row++) {

            DefaultPlanNode<?> exampleScenario = new DefaultPlanNode<>(PlanNodeTypes.SCENARIO)
                    .setTestCase(true)
                    .setId(id(scenarioOutline.getTags(), scenarioOutline.getName(),("_"+(row+1))))
                    .setName(trim(scenarioOutline.getName()) + " [" + (row + 1) + "]")
                    .setLanguage(language)
                    .setSource(source(location,scenarioOutline.getLocation()))
                    .addTags(tags(scenarioOutlineNode.tags(), scenarioOutline.getTags()))
                    .addProperties(propertiesFromComments(scenarioOutlineNode, scenarioOutlineNode.properties()))
            ;

            for (DefaultPlanStep backgroundStep : backgroundSteps) {
                exampleScenario.addChild(backgroundStep.copy());
            }
            List<DefaultPlanStep> exampleSteps = replaceOutlineVariables(outlineSteps, variables, values.get(row));
            exampleSteps.forEach(exampleScenario::addChild);

            output.add(exampleScenario);

        }
        return output;
    }








    protected DefaultPlanNode<?> newFeatureNode(Feature feature, String language, String location) {
        return new DefaultPlanNode<>(PlanNodeTypes.FEATURE)
            .setId(id(feature.getTags(), feature.getName()))
            .setName(feature.getName())
            .setLanguage(language)
            .setKeyword(feature.getKeyword())
            .addDescription(splitAndTrim(feature.getDescription()))
            .addTags(tags(feature.getTags()))
            .setSource(source(location,feature.getLocation()))
            .setGherkinModel(feature)
            .addProperties(propertiesFromComments(feature,null))
        ;
    }


    protected DefaultPlanNode<?> newScenarioNode(Scenario scenario, String location, PlanNode parentNode) {
        return new DefaultPlanNode<>(PlanNodeTypes.SCENARIO)
            .setTestCase(true)
            .setId(id(scenario.getTags(), scenario.getName()))
            .setName(trim(scenario.getName()))
            .setLanguage(parentNode.language())
            .setKeyword(trim(scenario.getKeyword()))
            .addTags(tags(parentNode.tags(),scenario.getTags()))
            .setSource(source(location,scenario.getLocation()))
            .setGherkinModel(scenario)
            .addProperties(propertiesFromComments(scenario,parentNode.properties()))
        ;
    }


    protected DefaultPlanNode<?> newScenarioOutlineNode(
            ScenarioOutline scenarioOutline, String location, PlanNode parentNode
    ) {
        return new DefaultPlanNode<>(PlanNodeTypes.SCENARIO_OUTLINE)
            .setId(id(scenarioOutline.getTags(), scenarioOutline.getName()))
            .setName(trim(scenarioOutline.getName()))
            .setLanguage(parentNode.language())
            .setKeyword(trim(scenarioOutline.getKeyword()))
            .addTags(tags(parentNode.tags(),scenarioOutline.getTags()))
            .setSource(source(location,scenarioOutline.getLocation()))
            .setGherkinModel(scenarioOutline)
            .addProperties(propertiesFromComments(scenarioOutline, parentNode.properties()))
        ;
    }

    protected DefaultPlanStep newStepNode(Step step, String location, String language, PlanNode parentNode) {
        return new DefaultPlanStep()
            .setKeyword(trim(step.getKeyword()))
            .setName(trim(step.getText()))
            .setLanguage(language)
            .setSource(source(location,step.getLocation()))
            .setGherkinModel(step)
            .addProperties(propertiesFromComments(step, parentNode == null ? null : parentNode.properties()))
        ;
    }



    protected Optional<Background> getBackground(Feature feature) {
        Background background = null;
        if (!feature.getChildren().isEmpty() && feature.getChildren().get(0) instanceof Background) {
            background = (Background) feature.getChildren().get(0);
        }
        return Optional.ofNullable(background);
    }


    protected List<DefaultPlanStep> createBackgroundSteps(Feature feature, String location) {
        Optional<Background> background = getBackground(feature);
        if (background.isPresent()) {
            ArrayList<DefaultPlanStep> steps = new ArrayList<>();
            for (Step step : background.get().getSteps()) {
                steps.add(createStep(step, location, feature.getLanguage(), null).setBackgroundStep(true));
            }
            return steps;
        }
        return Collections.emptyList();
    }





    protected DefaultPlanStep createStep(Step step, String location, String language, PlanNode parentNode) {
        DefaultPlanStep node = newStepNode(step, location, language, parentNode);
        if (step.getArgument() != null) {
            if (step.getArgument() instanceof DataTable) {
                node.setDataTable(new iti.kukumo.api.plan.DataTable(toArray((DataTable) step.getArgument())));
            } else if (step.getArgument() instanceof DocString) {
                node.setDocument(
                        new iti.kukumo.api.plan.Document(
                                ((DocString) step.getArgument()).getContent(),
                                ((DocString) step.getArgument()).getContentType()
                        )
                );
            }
        }
        return node;
    }



    /*
     * Scenario outline variables follow the pattern: <name>
     */
    private List<DefaultPlanStep> replaceOutlineVariables(
            ArrayList<DefaultPlanStep> outlineSteps, List<String> variables, List<String> values
    ) {
        ArrayList<DefaultPlanStep> exampleSteps = new ArrayList<>();
        for (DefaultPlanStep outlineStep : outlineSteps) {
            DefaultPlanStep exampleStep = outlineStep.copy();
            for (int i = 0; i < variables.size(); i++) {
                String variableValue = values.get(i);
                String variable = "<" + variables.get(i) + ">";
                UnaryOperator<String> replacer = s -> s.replace(variable, variableValue);
                exampleStep
                    .setName(Optional.ofNullable(trim(exampleStep.name())).map(replacer).orElse(null))
                    .setDocument(exampleStep.getDocument().map(document -> document.copy(replacer)).orElse(null))
                    .setDataTable(exampleStep.getDataTable().map(table -> table.copy(replacer)).orElse(null))
                ;
            }
            exampleSteps.add(exampleStep);
        }
        return exampleSteps;
    }





    private String[][] toArray(DataTable table) {
        TableRow header = table.getRows().get(0);
        String[][] array = new String[table.getRows().size()][header.getCells().size()];
        for (int row = 0; row < table.getRows().size(); row++) {
            for (int column = 0; column < header.getCells().size(); column++) {
                array[row][column] = trim(table.getRows().get(row).getCells().get(column).getValue());
            }
        }
        return array;
    }



    private List<String> splitAndTrim(String string) {
        if (string == null) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        for (String line : string.split("\\u000D|\\u000A|\\u000D\\u000A")) {
            lines.add(line.trim());
        }
        return lines;
    }



    private String trim(String string) {
        return string == null ? null : string.trim();
    }



    private List<String> tags(List<Tag> tags) {
        return tags.stream().map(Tag::getName).map(s -> s.substring(1)).distinct().collect(Collectors.toList());
    }

    private List<String> tags(List<String> parentTags, List<Tag> tags) {
        Set<String> tagList = tags.stream().map(Tag::getName).map(s -> s.substring(1)).collect(Collectors.toSet());
        tagList.addAll(parentTags);
        return new ArrayList<>(tagList);
    }



    protected String source(String file, Location location) {
        return file.endsWith("]") ? file : file + "["+location.getLine()+","+location.getColumn()+"]";
    }


    protected String id(List<Tag> tags, String nodeName, String suffix) {
        if (idTagPattern == null || tags.isEmpty()) {
            return null;
        }
        List<String> idTags = tags.stream().map(Tag::getName).map(s -> s.substring(1)).map(idTagPattern::matcher)
                .filter(Matcher::matches).map(Matcher::group).collect(Collectors.toList());
        if (idTags.size() > 1) {
            throw new KukumoException("More than one ID tag found in element {}", nodeName) ;
        }
        if (idTags.isEmpty()) {
            return null;
        }
        return suffix == null ? idTags.get(0) : idTags.get(0) + suffix;
    }


    protected String id(List<Tag> tags, String nodeName) {
        return id(tags,nodeName,null);
    }



    private List<String> tableCells(TableRow tableRow) {
        return tableRow.getCells().stream().map(TableCell::getValue).collect(Collectors.toList());
    }




    private Map<String, String> propertiesFromComments(Object node, Map<String,String> inheritedProperties) {
        Map<String,String> properties = new HashMap<>();
        if (inheritedProperties != null) {
            properties.putAll(inheritedProperties);
        }
        if (node instanceof CommentedNode) {
            Pattern pattern = Pattern.compile("\\s*#+\\s*([^\\s]+)\\s*:\\s*([^\\s]+)\\s*");
            for (Comment comment : ((CommentedNode)node).getComments()) {
                Matcher matcher = pattern.matcher(comment.getText());
                if (matcher.matches()) {
                    properties.put(matcher.group(1), matcher.group(2));
                }
            }
        }
        return properties;
    }
}
