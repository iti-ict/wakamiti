package iti.kukumo.plugins.cucumber;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.Reporter;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNodeSnapshot;
import iti.kukumo.api.util.KukumoLogger;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Extension(name = "cucumber-exporter")
public class CucumberExporter implements Reporter {

    public static final Logger LOGGER = KukumoLogger.forClass(CucumberExporter.class);

    private static final String DOC_STRING = "doc_string";
    private static final String CELLS = "cells";
    private static final String ROWS = "rows";
    private static final String NAME = "name";
    private static final String CONTENT_TYPE = "content_type";
    private static final String VALUE = "value";
    private static final String RESULT = "result";
    private static final String OUTPUT = "output";
    private static final String ERROR_MESSAGE = "error_message";
    private static final String DURATION = "duration";
    private static final String STATUS = "status";
    private static final String KEYWORD = "keyword";
    private static final String DESCRIPTION = "description";
    private static final String TAGS = "tags";
    private static final String TYPE = "type";
    private static final String STEPS = "steps";
    private static final String ID = "id";
    private static final String ELEMENTS = "elements";
    private static final String URI = "uri";

    public enum Strategy {INNERSTEPS, OUTERSTEPS}

    private final ObjectMapper mapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .enable(SerializationFeature.INDENT_OUTPUT);

    private final Charset charset = StandardCharsets.UTF_8;

    private String outputFile = "cucumber-report.json";
    private Strategy strategy = Strategy.INNERSTEPS;


    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void report(PlanNodeSnapshot rootNode) {

        try (Writer writer = new BufferedWriter(new FileWriter(outputFile,charset))) {
            List<Map<String,Object>> features = stream(rootNode)
                .filter(this::gherkinFeature)
                .map(this::mapFeature)
                .collect(Collectors.toList());
            mapper.writeValue(writer, features);
        } catch (IOException e) {
            LOGGER.error("Error exporting to Cucumber format: {}", e.getMessage(), e);
        }


    }


    private Map<String,Object> mapFeature(PlanNodeSnapshot feature) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put(URI,feature.getSource());
        map.put(KEYWORD,keyword(feature));
        map.put(ID,feature.getId());
        map.put(NAME, feature.getName());
        map.put(DESCRIPTION, description(feature));
        map.put(TAGS, mapTags(feature));
        var elements = stream(feature)
            .filter(it -> it.getNodeType() == NodeType.TEST_CASE)
            .map(this::mapScenario)
            .collect(Collectors.toList());
        map.put(ELEMENTS, elements);
        return map;
    }


    private Map<String,Object> mapScenario(PlanNodeSnapshot scenario) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put(KEYWORD,keyword(scenario));
        map.put(ID,scenario.getId());
        map.put(NAME, scenario.getName());
        map.put(DESCRIPTION, description(scenario));
        map.put(TAGS, mapTags(scenario));
        map.put(TYPE,"scenario");
        if (scenario.getChildren() != null && !scenario.getChildren().isEmpty()) {
            List<Map<String,Object>> steps = new LinkedList<>();
            for (var child : scenario.getChildren()) {
                if (child.getNodeType().isAnyOf(NodeType.STEP,NodeType.VIRTUAL_STEP)) {
                    steps.add(mapStep(child,child));
                } else if (child.getNodeType() == NodeType.STEP_AGGREGATOR){
                    steps.addAll(mapStepAggregator(child));
                }
            }
            map.put(STEPS, steps);
        }
        return map;
    }


    private Map<String, Object> mapStep(PlanNodeSnapshot definitionStep, PlanNodeSnapshot resultStep) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEYWORD, keyword(definitionStep));
        map.put(NAME, definitionStep.getName());
        Map<String, Object> result = new LinkedHashMap<>();
        String status;
        switch (definitionStep.getResult()) {
            case PASSED:
                status = "passed";
                break;
            case FAILED:
            case ERROR:
                status = "failed";
                break;
            case SKIPPED:
                status = "skipped";
                break;
            case UNDEFINED:
                status = "ambiguous";
                break;
            default:
                status = "pending";
        }
        result.put(STATUS, status);
        result.put(DURATION, definitionStep.getDuration());
        if (resultStep != null) {
            result.put(ERROR_MESSAGE, resultStep.getErrorMessage());
            result.put(OUTPUT, resultStep.getErrorTrace());
        }
        map.put(RESULT, result);
        if (definitionStep.getDocument() != null) {
            Map<String, Object> docstring = new LinkedHashMap<>();
            docstring.put(CONTENT_TYPE, definitionStep.getDocumentType());
            docstring.put(VALUE, definitionStep.getDocument());
            map.put(DOC_STRING, docstring);
        }
        if (definitionStep.getDataTable() != null) {
            List<Map<String, Object>> rows = new LinkedList<>();
            for (String[] row : definitionStep.getDataTable()) {
                rows.add(Map.of(CELLS, Arrays.asList(row)));
            }
            map.put(ROWS, rows);
        }
        return map;
    }



    private List<Map<String,Object>> mapStepAggregator(PlanNodeSnapshot step) {
        if (strategy == Strategy.INNERSTEPS) {
            return stream(step)
                .filter(it -> it.getNodeType().isAnyOf(NodeType.STEP,NodeType.VIRTUAL_STEP))
                .map(it -> mapStep(it,it))
                .collect(Collectors.toList());
        } else {
            var firstError = stream(step)
                .filter(it -> it.getNodeType().isAnyOf(NodeType.STEP))
                .filter(it -> it.getErrorMessage() != null)
                .findFirst()
                .orElse(null);
            return List.of(mapStep(step,firstError));
        }
    }


    private List<?> mapTags(PlanNodeSnapshot node) {
        if (node.getTags() == null || node.getTags().isEmpty()) {
            return null;
        }
        return node.getTags().stream()
            .filter(tag -> !tag.equals(node.getId()))
            .map(tag -> Map.of(NAME,"@"+tag))
            .collect(Collectors.toList());
    }


    private static String description (PlanNodeSnapshot node) {
        if (node.getDescription() != null && !node.getDescription().isEmpty()) {
            return String.join("\n", node.getDescription());
        } else {
            return null;
        }
    }


    private static String keyword(PlanNodeSnapshot node) {
        return (node.getKeyword() == null || node.getKeyword().isEmpty() ? " " : node.getKeyword());
    }


    private Stream<PlanNodeSnapshot> stream(PlanNodeSnapshot node) {
        return Stream.concat(
            Stream.of(node),
            node.getChildren() == null ? Stream.empty() : node.getChildren().stream().flatMap(this::stream)
        );
    }


    private boolean gherkinFeature(PlanNodeSnapshot node) {
        return node.getProperties() != null && "feature".equals(node.getProperties().get("gherkinType"));
    }



}
