/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.Resource;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.PlanBuilder;
import es.iti.wakamiti.api.extensions.ResourceType;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.gherkin.parser.*;
import es.iti.wakamiti.api.imconfig.Configurable;
import es.iti.wakamiti.api.imconfig.Configuration;
import org.slf4j.Logger;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;


/**
 * GherkinPlanBuilder is a PlanBuilder extension for processing
 * Gherkin documents and creating test plans.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
@Extension(provider = "es.iti.wakamiti", name = "wakamiti-gherkin",
        extensionPoint = "es.iti.wakamiti.api.extensions.PlanBuilder", version = "2.6")
public class GherkinPlanBuilder implements PlanBuilder, Configurable {

    private static final Logger LOGGER = WakamitiLogger.forClass(GherkinPlanBuilder.class);

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
    private boolean includeFiltered = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptResourceType(ResourceType<?> resourceType) {
        return resourceType.contentType().equals(GherkinDocument.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration configuration) {
        configureFilterFromTagExpression(configuration);
        configureIdTagPattern(configuration);
    }

    /**
     * Configures the scenario filter based on the tag expression.
     *
     * @param configuration The configuration object.
     */
    protected void configureFilterFromTagExpression(Configuration configuration) {
        String tagFilterExpression = configuration.get(WakamitiConfiguration.TAG_FILTER, String.class)
                .orElse("");
        if (!tagFilterExpression.isEmpty()) {
            this.scenarioFilter = node -> Wakamiti.instance().createTagFilter(tagFilterExpression)
                    .filter(node.tags());
        }
        this.includeFiltered = configuration.get(WakamitiConfiguration.INCLUDE_FILTERED_TEST_CASES, Boolean.class)
                .orElse(false);
    }

    /**
     * Configures the ID tag pattern.
     *
     * @param configuration The configuration object.
     */
    protected void configureIdTagPattern(Configuration configuration) {
        this.idTagPattern = configuration.get(WakamitiConfiguration.ID_TAG_PATTERN, String.class)
                .map(this::nullIfEmpty)
                .map(Pattern::compile)
                .orElse(null);
    }


    private String nullIfEmpty(String string) {
        return string.isEmpty() ? null : string;
    }

    /**
     * Creates a test plan based on the provided resources.
     *
     * @param resources The list of resources.
     * @return The root node of the test plan.
     */
    @SuppressWarnings("unchecked")
    @Override
    public PlanNodeBuilder createPlan(List<Resource<?>> resources) {
        PlanNodeBuilder plan = new PlanNodeBuilder(NodeType.AGGREGATOR)
                .setDisplayNamePattern("Test Plan");
        List<Resource<GherkinDocument>> gherkinResources = resources.stream()
                .map(x -> (Resource<GherkinDocument>) x).collect(Collectors.toList());
        for (Resource<GherkinDocument> gherkinResource : gherkinResources) {
            if (isNull(gherkinResource.content().getFeature())) {
                LOGGER.warn("File '{}' is empty. It will be ignored.", gherkinResource.absolutePath());
                continue;
            }
            plan.addChildIf(createFeature(gherkinResource), PlanNodeBuilder::hasChildren);
        }
        return plan;
    }

    /**
     * Creates a feature node for the given Gherkin document and its child scenarios.
     *
     * @param gherkinResource The Gherkin document resource.
     * @return A feature node representing the feature and its scenarios in the test plan.
     */
    protected PlanNodeBuilder createFeature(Resource<GherkinDocument> gherkinResource) {
        Feature feature = gherkinResource.content().getFeature();
        String location = gherkinResource.relativePath().replace(File.separator, "/");
        String language = feature.getLanguage();
        PlanNodeBuilder node = newFeatureNode(feature, language, location);
        for (ScenarioDefinition abstractScenario : feature.getChildren()) {
            if (abstractScenario instanceof Scenario) {
                var child = createScenario(feature, (Scenario) abstractScenario, location, node);
                if (scenarioFilter.test(child) || includeFiltered) {
                    node.addChild(child);
                }
            } else if (abstractScenario instanceof ScenarioOutline) {
                var child = createScenarioOutline(
                        feature,
                        (ScenarioOutline) abstractScenario,
                        location,
                        node
                );
                if (scenarioFilter.test(child) || includeFiltered) {
                    node.addChild(child);
                }
            }
        }
        if (node.name() != null) {
            node.descendants()
                    .filter(child -> child.nodeType() == NodeType.TEST_CASE)
                    .forEach(child -> child.addProperty(GHERKIN_FEATURE_NAME, node.name()));
        }
        return node;
    }

    /**
     * Creates a scenario node for the given Gherkin feature, scenario, and its steps.
     *
     * @param feature    The Gherkin feature.
     * @param scenario   The Gherkin scenario.
     * @param location   The location of the scenario in the source file.
     * @param parentNode The parent node in the test plan hierarchy.
     * @return A scenario node for the test plan.
     */
    protected PlanNodeBuilder createScenario(
            Feature feature,
            Scenario scenario,
            String location,
            PlanNodeBuilder parentNode
    ) {
        PlanNodeBuilder node = newScenarioNode(scenario, location, parentNode);
        node.filtered(!scenarioFilter.test(node));
        if (node.filtered()) {
            return node;
        }
        Optional<PlanNodeBuilder> backgroundSteps = createBackgroundSteps(feature, location, node);
        backgroundSteps.ifPresent(background -> node.addChild(background.copy()));
        for (Step step : scenario.getSteps()) {
            node.addChild(createStep(step, location, parentNode.language(), node));
        }
        return node;
    }

    /**
     * Creates a scenario outline node for the given Gherkin feature, scenario outline, and its examples.
     *
     * @param feature         The Gherkin feature.
     * @param scenarioOutline The Gherkin scenario outline.
     * @param location        The location of the scenario outline in the source file.
     * @param parentNode      The parent node in the test plan hierarchy.
     * @return A scenario outline node for the test plan.
     */
    protected PlanNodeBuilder createScenarioOutline(
            Feature feature,
            ScenarioOutline scenarioOutline,
            String location,
            PlanNodeBuilder parentNode
    ) {
        PlanNodeBuilder node = newScenarioOutlineNode(scenarioOutline, location, parentNode);
        Optional<PlanNodeBuilder> backgroundSteps = createBackgroundSteps(feature, location, node);
        if (scenarioOutline.getExamples().isEmpty()) {
            throw new WakamitiException("Example table is needed at scenario: {}.", node.source());
        }
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

    /**
     * Creates a list of scenario nodes based on the examples of a scenario outline.
     *
     * @param scenarioOutline     The Gherkin scenario outline.
     * @param examples            The examples associated with the scenario outline.
     * @param scenarioOutlineNode The parent node representing the scenario outline in the test plan.
     * @param backgroundSteps     Optional background steps to be included in each scenario.
     * @param language            The language of the scenarios.
     * @param location            The location of the scenario outline in the source file.
     * @return A list of scenario nodes generated from the examples.
     */
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
                    .setKeyword(trim(scenarioOutline.getKeyword()))
                    .setName(replaceOutlineVariables(scenarioOutline.getName(), variables, values.get(row))
                            + " [" + (row + 1) + "]")
                    .setLanguage(language)
                    .setSource(source(location, scenarioOutline.getLocation()))
                    .addDescription(splitAndTrim(replaceOutlineVariables(
                            scenarioOutline.getDescription(), variables, values.get(row))))
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

            exampleScenario.filtered(!scenarioFilter.test(exampleScenario));
            if (!exampleScenario.filtered()) {

                backgroundSteps.ifPresent(background -> exampleScenario.addChild(background.copy()));
                List<PlanNodeBuilder> exampleSteps = replaceOutlineVariables(
                        outlineSteps,
                        variables,
                        values.get(row)
                );
                exampleSteps.forEach(exampleScenario::addChild);

            }

            output.add(exampleScenario);

        }
        return output;
    }

    /**
     * Creates a new feature node for the given Gherkin feature.
     *
     * @param feature  The Gherkin feature.
     * @param language The language of the feature.
     * @param location The location of the feature in the source file.
     * @return A new feature node for the test plan.
     */
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

    /**
     * Creates a new scenario node for the given Gherkin scenario.
     *
     * @param scenario   The Gherkin scenario.
     * @param location   The location of the scenario in the source file.
     * @param parentNode The parent node in the test plan hierarchy.
     * @return A new scenario node for the test plan.
     */
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
                .addDescription(splitAndTrim(scenario.getDescription()))
                .addTags(tags(parentNode.tags(), scenario.getTags()))
                .setSource(source(location, scenario.getLocation()))
                .setUnderlyingModel(scenario)
                .addProperties(propertiesFromComments(scenario, parentNode.properties()))
                .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_SCENARIO)
                ;
    }

    /**
     * Creates a new scenario outline node for the given Gherkin scenario outline.
     *
     * @param scenarioOutline The Gherkin scenario outline.
     * @param location        The location of the scenario outline in the source file.
     * @param parentNode      The parent node in the test plan hierarchy.
     * @return A new scenario outline node for the test plan.
     */
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
                .addDescription(splitAndTrim(scenarioOutline.getDescription()))
                .addTags(tags(parentNode.tags(), scenarioOutline.getTags()))
                .setSource(source(location, scenarioOutline.getLocation()))
                .setUnderlyingModel(scenarioOutline)
                .setData(examplesAsDataTable(scenarioOutline.getExamples()))
                .addProperties(propertiesFromComments(scenarioOutline, parentNode.properties()))
                .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_SCENARIO_OUTLINE);
    }

    /**
     * Creates a new step node for the given Gherkin step.
     *
     * @param step       The Gherkin step.
     * @param location   The location of the step in the source file.
     * @param language   The language of the step.
     * @param parentNode The parent node in the test plan hierarchy.
     * @return A new step node for the test plan.
     */
    protected PlanNodeBuilder newStepNode(
            Step step,
            String location,
            String language,
            PlanNodeBuilder parentNode
    ) {
        return new PlanNodeBuilder(NodeType.STEP)
                .setId(id(List.of(), step.getText(), ""))
                .setKeyword(trim(step.getKeyword()))
                .setName(trim(step.getText()))
                .setDisplayNamePattern("{keyword} {name}")
                .setLanguage(language)
                .setSource(source(location, step.getLocation()))
                .setUnderlyingModel(step)
                .addProperties(propertiesFromComments(step, parentNode.properties()))
                .addProperty(GHERKIN_PROPERTY, GHERKIN_TYPE_STEP);
    }

    /**
     * Retrieves the background from the given Gherkin feature.
     *
     * @param feature The Gherkin feature.
     * @return An optional containing the background if present, otherwise empty.
     */
    protected Optional<Background> getBackground(Feature feature) {
        Background background = null;
        if (!feature.getChildren().isEmpty()
                && feature.getChildren().get(0) instanceof Background) {
            background = (Background) feature.getChildren().get(0);
        }
        return Optional.ofNullable(background);
    }

    /**
     * Creates the plan node builder for the background steps of the given Gherkin feature.
     *
     * @param feature    The Gherkin feature.
     * @param location   The location of the feature.
     * @param parentNode The parent node builder.
     * @return An optional containing the plan node builder for background steps if present,
     * otherwise empty.
     */
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

    /**
     * Creates a step node for the given Gherkin step.
     *
     * @param step       The Gherkin step.
     * @param location   The location of the step in the source file.
     * @param language   The language of the step.
     * @param parentNode The parent node in the test plan hierarchy.
     * @return A step node for the test plan.
     */
    protected PlanNodeBuilder createStep(
            Step step,
            String location,
            String language,
            PlanNodeBuilder parentNode
    ) {
        PlanNodeBuilder node = newStepNode(step, location, language, parentNode);
        if (step.getArgument() != null) {
            if (step.getArgument() instanceof es.iti.wakamiti.core.gherkin.parser.DataTable) {
                node.setData(
                        new DataTable(toArray((es.iti.wakamiti.core.gherkin.parser.DataTable) step.getArgument()))
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

    /**
     * Replaces scenario outline variables (with pattern {@code <name>})
     * in the given outline steps with the provided values.
     *
     * @param outlineSteps The list of outline steps.
     * @param variables    The list of variables to replace.
     * @param values       The list of values to use for replacement.
     * @return A list of plan node builders with replaced variables.
     */
    private List<PlanNodeBuilder> replaceOutlineVariables(
            ArrayList<PlanNodeBuilder> outlineSteps,
            List<String> variables,
            List<String> values
    ) {
        ArrayList<PlanNodeBuilder> exampleSteps = new ArrayList<>();
        for (PlanNodeBuilder outlineStep : outlineSteps) {
            PlanNodeBuilder exampleStep = outlineStep.copy();
            exampleStep
                    .setName(replaceOutlineVariables(exampleStep.name(), variables, values))
                    .setData(exampleStep.data()
                            .map(data -> data.copyReplacingVariables(s -> replaceOutlineVariables(s, variables, values)))
                            .orElse(null));
            exampleSteps.add(exampleStep);
        }
        return exampleSteps;
    }

    /**
     * Replaces scenario outline variables in the given string with the provided values.
     *
     * @param string    The string in which to replace variables.
     * @param variables The list of variables to replace.
     * @param values    The list of values to use for replacement.
     * @return The string with replaced variables.
     */
    private String replaceOutlineVariables(String string, List<String> variables, List<String> values) {
        String result = string;
        for (int i = 0; i < variables.size(); i++) {
            String variableValue = values.get(i);
            String variable = "<" + variables.get(i) + ">";
            result = Optional.ofNullable(trim(result)).map(s -> s.replace(variable, variableValue)).orElse(null);
        }
        return result;
    }

    /**
     * Converts a list of Examples into a DataTable.
     *
     * @param examplesList The list of Examples to convert.
     * @return A DataTable representing the Examples, or null if the list is empty or null.
     */
    private DataTable examplesAsDataTable(List<Examples> examplesList) {
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
        return new DataTable(dataTable);
    }

    /**
     * Converts a Gherkin DataTable to a two-dimensional array.
     *
     * @param table The Gherkin DataTable to convert.
     * @return A two-dimensional array representing the Gherkin DataTable.
     */
    private String[][] toArray(es.iti.wakamiti.core.gherkin.parser.DataTable table) {
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

    /**
     * Splits and trims a string into a list of lines.
     *
     * @param string The string to split and trim.
     * @return A list of lines obtained by splitting and trimming the input string.
     */
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

    /**
     * Null-safe trim string.
     *
     * @param string The string to trim.
     * @return The trimmed string, or null if the input string is null.
     */
    private String trim(String string) {
        return string == null ? null : string.trim();
    }

    /**
     * Extracts and returns a list of tag names from the provided list of tags.
     *
     * @param tags The list of tags.
     * @return A list of tag names.
     */
    private List<String> tags(List<Tag> tags) {
        return tags.stream().map(Tag::getName).map(s -> s.substring(1)).distinct()
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Combines parent tags, provided tags, and excludes ignored tags to create a set of unique tags.
     *
     * @param parentTags  The set of parent tags.
     * @param tags        The collection of tags to be included.
     * @param ignoredTags The tags to be excluded.
     * @return A set of unique tags.
     */
    private Set<String> tags(Set<String> parentTags, Collection<Tag> tags, String... ignoredTags) {
        List<String> ignoredTagList = Arrays.asList(ignoredTags);
        Set<String> tagList = tags.stream()
                .map(Tag::getName)
                .map(s -> s.substring(1))
                .filter(s -> !ignoredTagList.contains(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        tagList.addAll(parentTags);
        return tagList;
    }

    /**
     * Generates a source string based on the file and location information.
     *
     * @param file     The file name or path.
     * @param location The location information.
     * @return A formatted source string.
     */
    protected String source(String file, Location location) {
        return file.endsWith("]") ? file
                : file + "[" + location.getLine() + "," + location.getColumn() + "]";
    }

    /**
     * Generates an ID based on tags, node name, and suffix.
     *
     * @param tags     The list of tags.
     * @param nodeName The name of the node.
     * @param suffix   The suffix to append.
     * @return The generated ID.
     * @throws WakamitiException If more than one ID tag is found in the element.
     */
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

    /**
     * Extracts values from the cells of a table row.
     *
     * @param tableRow The table row.
     * @return A list of cell values.
     */
    private List<String> tableCells(TableRow tableRow) {
        return tableRow.getCells().stream().map(TableCell::getValue).collect(Collectors.toList());
    }

    /**
     * Extracts properties from the comments of a node.
     *
     * @param node                The node containing comments.
     * @param inheritedProperties The properties inherited from the parent.
     * @return A map of properties extracted from comments.
     */
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