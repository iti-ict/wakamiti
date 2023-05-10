/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.core.gherkin;

import imconfig.Configurable;
import imconfig.Configuration;
import iti.commons.gherkin.*;
import iti.commons.jext.Extension;
import iti.wakamiti.api.WakamitiConfiguration;
import iti.wakamiti.api.WakamitiException;
import iti.wakamiti.api.Resource;
import iti.wakamiti.api.extensions.PlanBuilder;
import iti.wakamiti.api.extensions.ResourceType;
import iti.wakamiti.api.plan.Document;
import iti.wakamiti.api.plan.NodeType;
import iti.wakamiti.api.plan.PlanNodeBuilder;
import iti.wakamiti.core.Wakamiti;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
@Extension(provider = "iti.wakamiti", name = "wakamiti-gherkin", extensionPoint = "iti.wakamiti.api.extensions.PlanBuilder", version = "1.1")
public class GherkinPlanBuilder implements PlanBuilder, Configurable {

    public static final String GHERKIN_PROPERTY = "gherkinType";
    public static final String GHERKIN_TYPE_FEATURE = "feature";
    public static final String GHERKIN_TYPE_SCENARIO = "scenario";
    public static final String GHERKIN_TYPE_SCENARIO_OUTLINE = "scenarioOutline";
    public static final String GHERKIN_TYPE_BACKGROUND = "background";
    public static final String GHERKIN_TYPE_STEP = "step";
    public static final String GHERKIN_FEATURE_NAME = "featureName";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Predicate<PlanNodeBuilder> scenarioFilter = (x -> true);
    private Pattern idTagPattern = null;


    @Override
    public boolean acceptResourceType(ResourceType<?> resourceType) {
        return resourceType.contentType().equals(GherkinDocument.class);
    }


    @Override
    public void configure(Configuration configuration) {
        configureFilterFromTagExpression(configuration);
        configureIdTagPattern(configuration);
    }


    protected void configureFilterFromTagExpression(Configuration configuration) {
        String tagFilterExpression = configuration.get(WakamitiConfiguration.TAG_FILTER, String.class)
                .orElse("");
        if (tagFilterExpression != null && !tagFilterExpression.isEmpty()) {
            this.scenarioFilter = node -> Wakamiti.instance().createTagFilter(tagFilterExpression)
                    .filter(node.tags());
        }
    }


    protected void configureIdTagPattern(Configuration configuration) {
        this.idTagPattern = configuration.get(WakamitiConfiguration.ID_TAG_PATTERN, String.class)
                .map(this::nullIfEmpty)
                .map(Pattern::compile)
                .orElse(null);
    }


    private String nullIfEmpty(String string) {
        return string.isEmpty() ? null : string;
    }


    @SuppressWarnings("unchecked")
    @Override
    public PlanNodeBuilder createPlan(List<Resource<?>> resources) {
        PlanNodeBuilder plan = new PlanNodeBuilder(NodeType.AGGREGATOR)
                .setDisplayNamePattern("Test Plan");
        List<Resource<GherkinDocument>> gherkinResources = resources.stream()
                .map(x -> (Resource<GherkinDocument>) x).collect(Collectors.toList());
        for (Resource<GherkinDocument> gherkinResource : gherkinResources) {
            plan.addChildIf(createFeature(gherkinResource), PlanNodeBuilder::hasChildren);
        }
        return plan;
    }


    protected PlanNodeBuilder createFeature(Resource<GherkinDocument> gherkinResource) {
        Feature feature = gherkinResource.content().getFeature();
        String location = gherkinResource.relativePath();
        String language = feature.getLanguage();
        PlanNodeBuilder node = newFeatureNode(feature, language, location);
        for (ScenarioDefinition abstractScenario : feature.getChildren()) {
            if (abstractScenario instanceof Scenario) {
                node.addChildIf(
                        createScenario(feature, (Scenario) abstractScenario, location, node),
                        scenarioFilter
                );
            } else if (abstractScenario instanceof ScenarioOutline) {
                node.addChildIf(
                        createScenarioOutline(
                                feature,
                                (ScenarioOutline) abstractScenario,
                                location,
                                node
                        ),
                        scenarioFilter
                );
            }
        }
        if (node.name() != null) {
            node.descendants()
                    .filter(child -> child.nodeType() == NodeType.TEST_CASE)
                    .forEach(child -> child.addProperty(GHERKIN_FEATURE_NAME, node.name()));
        }
        return node;
    }


    protected PlanNodeBuilder createScenario(
            Feature feature,
            Scenario scenario,
            String location,
            PlanNodeBuilder parentNode
    ) {
        PlanNodeBuilder node = newScenarioNode(scenario, location, parentNode);
        Optional<PlanNodeBuilder> backgroundSteps = createBackgroundSteps(feature, location, node);
        backgroundSteps.ifPresent(background -> node.addChild(background.copy()));
        for (Step step : scenario.getSteps()) {
            node.addChild(createStep(step, location, parentNode.language(), node));
        }
        return node;
    }


    protected PlanNodeBuilder createScenarioOutline(
            Feature feature,
            ScenarioOutline scenarioOutline,
            String location,
            PlanNodeBuilder parentNode
    ) {
        PlanNodeBuilder node = newScenarioOutlineNode(scenarioOutline, location, parentNode);
        Optional<PlanNodeBuilder> backgroundSteps = createBackgroundSteps(feature, location, node);
        for (Examples examples : scenarioOutline.getExamples()) {
            List<PlanNodeBuilder> scenarios = createScenariosFromExamples(
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


    protected List<PlanNodeBuilder> createScenariosFromExamples(
            ScenarioOutline scenarioOutline,
            Examples examples,
            PlanNodeBuilder scenarioOutlineNode,
            Optional<PlanNodeBuilder> backgroundSteps,
            String language,
            String location
    ) {
        List<PlanNodeBuilder> output = new ArrayList<>();

        List<String> variables = tableCells(examples.getTableHeader());
        List<List<String>> values = examples.getTableBody().stream().map(this::tableCells)
                .collect(Collectors.toList());

        ArrayList<PlanNodeBuilder> outlineSteps = new ArrayList<>();
        for (Step step : scenarioOutline.getSteps()) {
            outlineSteps.add(createStep(step, location, language, scenarioOutlineNode));
        }

        for (int row = 0; row < values.size(); row++) {

            PlanNodeBuilder exampleScenario = new PlanNodeBuilder(NodeType.TEST_CASE)
                    .setId(id(
                            scenarioOutline.getTags(),
                            scenarioOutline.getName(),
                            ("_" + (row + 1))
                    ))
                    .setName(trim(scenarioOutline.getName()) + " [" + (row + 1) + "]")
                    .setLanguage(language)
                    .setSource(source(location, scenarioOutline.getLocation()))
                    .addTags(
                            tags(
                                    scenarioOutlineNode.tags(),
                                    scenarioOutline.getTags(),
                                    scenarioOutlineNode.id()
                            )
                    )
                    .addProperties(
                            propertiesFromComments(scenarioOutlineNode, scenarioOutlineNode.properties())
                    )
                    .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_SCENARIO);

            backgroundSteps.ifPresent(background -> exampleScenario.addChild(background.copy()));
            List<PlanNodeBuilder> exampleSteps = replaceOutlineVariables(
                    outlineSteps,
                    variables,
                    values.get(row)
            );
            exampleSteps.forEach(exampleScenario::addChild);

            output.add(exampleScenario);

        }
        return output;
    }


    protected PlanNodeBuilder newFeatureNode(
            Feature feature,
            String language,
            String location
    ) {
        return new PlanNodeBuilder(NodeType.AGGREGATOR)
                .setId(id(feature.getTags(), feature.getName(), ""))
                .setName(feature.getName())
                .setDisplayNamePattern("{keyword}: {name}")
                .setLanguage(language)
                .setKeyword(feature.getKeyword())
                .addDescription(splitAndTrim(feature.getDescription()))
                .addTags(tags(feature.getTags()))
                .setSource(source(location, feature.getLocation()))
                .setUnderlyingModel(feature)
                .addProperties(propertiesFromComments(feature, null))
                .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_FEATURE);
    }


    protected PlanNodeBuilder newScenarioNode(
            Scenario scenario,
            String location,
            PlanNodeBuilder parentNode
    ) {
        return new PlanNodeBuilder(NodeType.TEST_CASE)
                .setId(id(scenario.getTags(), scenario.getName(), ""))
                .setName(trim(scenario.getName()))
                .setDisplayNamePattern("[{id}] {keyword}: {name}")
                .setLanguage(parentNode.language())
                .setKeyword(trim(scenario.getKeyword()))
                .addTags(tags(parentNode.tags(), scenario.getTags()))
                .setSource(source(location, scenario.getLocation()))
                .setUnderlyingModel(scenario)
                .addProperties(propertiesFromComments(scenario, parentNode.properties()))
                .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_SCENARIO);
    }


    protected PlanNodeBuilder newScenarioOutlineNode(
            ScenarioOutline scenarioOutline,
            String location,
            PlanNodeBuilder parentNode
    ) {
        return new PlanNodeBuilder(NodeType.AGGREGATOR)
                .setId(id(scenarioOutline.getTags(), scenarioOutline.getName(), ""))
                .setName(trim(scenarioOutline.getName()))
                .setDisplayNamePattern("[{id}] {keyword}: {name}")
                .setLanguage(parentNode.language())
                .setKeyword(trim(scenarioOutline.getKeyword()))
                .addTags(tags(parentNode.tags(), scenarioOutline.getTags()))
                .setSource(source(location, scenarioOutline.getLocation()))
                .setUnderlyingModel(scenarioOutline)
                .setData(examplesAsDataTable(scenarioOutline.getExamples()))
                .addProperties(propertiesFromComments(scenarioOutline, parentNode.properties()))
                .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_SCENARIO_OUTLINE);
    }


    protected PlanNodeBuilder newStepNode(
            Step step,
            String location,
            String language,
            PlanNodeBuilder parentNode
    ) {
        return new PlanNodeBuilder(NodeType.STEP)
                .setKeyword(trim(step.getKeyword()))
                .setName(trim(step.getText()))
                .setDisplayNamePattern("{keyword} {name}")
                .setLanguage(language)
                .setSource(source(location, step.getLocation()))
                .setUnderlyingModel(step)
                .addProperties(propertiesFromComments(step, parentNode.properties()))
                .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_STEP);
    }


    protected Optional<Background> getBackground(Feature feature) {
        Background background = null;
        if (!feature.getChildren().isEmpty()
                && feature.getChildren().get(0) instanceof Background) {
            background = (Background) feature.getChildren().get(0);
        }
        return Optional.ofNullable(background);
    }


    protected Optional<PlanNodeBuilder> createBackgroundSteps(
            Feature feature,
            String location,
            PlanNodeBuilder parentNode
    ) {
        Optional<Background> background = getBackground(feature);
        if (background.isPresent()) {
            PlanNodeBuilder backgroundAggregator = new PlanNodeBuilder(NodeType.STEP_AGGREGATOR)
                    .setKeyword(background.get().getKeyword())
                    .setName(background.get().getName())
                    .setDisplayNamePattern("{keyword}: {name}")
                    .addTags(parentNode.tags())
                    .addProperties(propertiesFromComments(background.get(), parentNode.properties()))
                    .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_BACKGROUND);
            for (Step step : background.get().getSteps()) {
                backgroundAggregator.addChild(
                        createStep(step, location, feature.getLanguage(), backgroundAggregator)
                );
            }
            return Optional.of(backgroundAggregator);
        }
        return Optional.empty();
    }


    protected PlanNodeBuilder createStep(
            Step step,
            String location,
            String language,
            PlanNodeBuilder parentNode
    ) {
        PlanNodeBuilder node = newStepNode(step, location, language, parentNode);
        if (step.getArgument() != null) {
            if (step.getArgument() instanceof DataTable) {
                node.setData(
                        new iti.wakamiti.api.plan.DataTable(toArray((DataTable) step.getArgument()))
                );
            } else if (step.getArgument() instanceof DocString) {
                node.setData(
                        new Document(
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
    private List<PlanNodeBuilder> replaceOutlineVariables(
            ArrayList<PlanNodeBuilder> outlineSteps,
            List<String> variables,
            List<String> values
    ) {
        ArrayList<PlanNodeBuilder> exampleSteps = new ArrayList<>();
        for (PlanNodeBuilder outlineStep : outlineSteps) {
            PlanNodeBuilder exampleStep = outlineStep.copy();
            for (int i = 0; i < variables.size(); i++) {
                String variableValue = values.get(i);
                String variable = "<" + variables.get(i) + ">";
                UnaryOperator<String> replacer = s -> s.replace(variable, variableValue);
                exampleStep
                        .setName(Optional.ofNullable(trim(exampleStep.name())).map(replacer).orElse(null))
                        .setData(exampleStep.data().map(data -> data.copyReplacingVariables(replacer)).orElse(null));
            }
            exampleSteps.add(exampleStep);
        }
        return exampleSteps;
    }


    private iti.wakamiti.api.plan.DataTable examplesAsDataTable(List<Examples> examplesList) {
        if (examplesList == null || examplesList.isEmpty()) {
            return null;
        }
        Examples examples = examplesList.get(0);
        int rows = examples.getTableBody().size() + 1;
        int columns = examples.getTableHeader().getCells().size();
        String[][] dataTable = new String[rows][columns];
        for (int column = 0; column < columns; column++) {
            dataTable[0][column] = examples.getTableHeader().getCells().get(column).getValue();
        }
        for (int row = 1; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                dataTable[row][column] = examples.getTableBody().get(row - 1).getCells().get(column)
                        .getValue();
            }
        }
        return new iti.wakamiti.api.plan.DataTable(dataTable);
    }


    private String[][] toArray(DataTable table) {
        TableRow header = table.getRows().get(0);
        String[][] array = new String[table.getRows().size()][header.getCells().size()];
        for (int row = 0; row < table.getRows().size(); row++) {
            for (int column = 0; column < header.getCells().size(); column++) {
                array[row][column] = trim(
                        table.getRows().get(row).getCells().get(column).getValue()
                );
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
        return tags.stream().map(Tag::getName).map(s -> s.substring(1)).distinct()
                .collect(Collectors.toList());
    }


    private Set<String> tags(Set<String> parentTags, Collection<Tag> tags, String... ignoredTags) {
        List<String> ignoredTagList = Arrays.asList(ignoredTags);
        Set<String> tagList = tags.stream()
                .map(Tag::getName)
                .map(s -> s.substring(1))
                .filter(s -> !ignoredTagList.contains(s))
                .collect(Collectors.toSet());
        tagList.addAll(parentTags);
        return tagList;
    }


    protected String source(String file, Location location) {
        return file.endsWith("]") ? file
                : file + "[" + location.getLine() + "," + location.getColumn() + "]";
    }


    protected String id(List<Tag> tags, String nodeName, String suffix) {
        String idTag = null;
        if (idTagPattern != null) {
            List<String> idTags = tags.stream().map(Tag::getName).map(s -> s.substring(1))
                    .map(idTagPattern::matcher)
                    .filter(Matcher::matches).map(Matcher::group).collect(Collectors.toList());
            if (idTags.size() > 1) {
                throw new WakamitiException("More than one ID tag found in element {}", nodeName);
            }
            if (!idTags.isEmpty()) {
                idTag = idTags.get(0);
            }
        }
        if (idTag == null) {
            idTag = "#" + (char) (RANDOM.nextInt(26) + 'a') + UUID.randomUUID().toString().substring(0, 5);
        }
        return idTag + suffix;
    }


    private List<String> tableCells(TableRow tableRow) {
        return tableRow.getCells().stream().map(TableCell::getValue).collect(Collectors.toList());
    }


    private Map<String, String> propertiesFromComments(
            Object node,
            Map<String, String> inheritedProperties
    ) {
        Map<String, String> properties = new HashMap<>();
        if (inheritedProperties != null) {
            properties.putAll(inheritedProperties);
        }
        if (node instanceof CommentedNode) {
            for (Comment comment : ((CommentedNode) node).getComments()) {
                String text = comment.getText().strip();
                if (text.startsWith("#") && text.contains(":")) {
                    String[] parts = text.split(":", 2);
                    properties.put(parts[0].substring(1).strip(), parts[1].strip());
                }
            }
        }
        return properties;
    }
}