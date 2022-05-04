package iti.kukumo.plugins.cucumber;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.extensions.Reporter;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNodeSnapshot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static iti.kukumo.gherkin.GherkinPlanBuilder.*;

@Extension(name = "cucumber-exporter")
public class CucumberExporter implements Reporter {


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
            Kukumo.LOGGER.error("Error exporting to Cucumber format: {}", e.getMessage(), e);
        }


    }


    private Map<String,Object> mapFeature(PlanNodeSnapshot feature) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("uri",feature.getSource());
        map.put("keyword",keyword(feature));
        map.put("id",feature.getId());
        map.put("name", feature.getName());
        map.put("description", description(feature));
        map.put("tags", mapTags(feature));
        var elements = stream(feature)
            .filter(it -> it.getNodeType() == NodeType.TEST_CASE)
            .map(this::mapScenario)
            .collect(Collectors.toList());
        map.put("elements", elements);
        return map;
    }


    private Map<String,Object> mapScenario(PlanNodeSnapshot scenario) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("keyword",keyword(scenario));
        map.put("id",scenario.getId());
        map.put("name", scenario.getName());
        map.put("description", description(scenario));
        map.put("tags", mapTags(scenario));
        map.put("type","scenario");
        if (scenario.getChildren() != null && !scenario.getChildren().isEmpty()) {
            List<Map<String,Object>> steps = new LinkedList<>();
            for (var child : scenario.getChildren()) {
                if (child.getNodeType().isAnyOf(NodeType.STEP,NodeType.VIRTUAL_STEP)) {
                    steps.add(mapStep(child,child));
                } else if (child.getNodeType() == NodeType.STEP_AGGREGATOR){
                    steps.addAll(mapStepAggregator(child));
                }
            }
            map.put("steps", steps);
        }
        return map;
    }



    private Map<String,Object> mapStep(PlanNodeSnapshot definitionStep, PlanNodeSnapshot resultStep) {
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("keyword",keyword(definitionStep));
        map.put("name", definitionStep.getName());
        Map<String,Object> result = new LinkedHashMap<>();
        String status = "pending";
        switch (definitionStep.getResult()) {
            case PASSED: status = "passed"; break;
            case FAILED:
            case ERROR: status = "failed"; break;
            case SKIPPED: status = "skipped"; break;
            case UNDEFINED: status = "ambiguous"; break;
        }
        result.put("status", status);
        result.put("duration",definitionStep.getDuration() );
        if (resultStep != null) {
            result.put("error_message", resultStep.getErrorMessage());
            result.put("output", resultStep.getErrorTrace());
        }
        map.put("result",result);
        if (definitionStep.getDocument() != null) {
            Map<String,Object> docstring = new LinkedHashMap<>();
            docstring.put("content_type",definitionStep.getDocumentType());
            docstring.put("value", definitionStep.getDocument());
            map.put("doc_string",docstring);
        }
        if (definitionStep.getDataTable() != null) {
            List<Map<String,Object>> rows = new LinkedList<>();
            for (String[] row : definitionStep.getDataTable()) {
                rows.add(Map.of("cells",Arrays.asList(row)));
            }
            map.put("rows",rows);
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
            .map(tag -> Map.of("name","@"+tag))
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
        return node.getProperties() != null && iti.kukumo.gherkin.GherkinPlanBuilder.GHERKIN_TYPE_FEATURE.equals(node.getProperties().get(GHERKIN_PROPERTY));
    }



}
