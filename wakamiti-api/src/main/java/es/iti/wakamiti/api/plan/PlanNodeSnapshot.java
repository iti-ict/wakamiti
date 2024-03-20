/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import es.iti.wakamiti.api.model.ExecutionState;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;


/**
 * This class is an immutable, non-executable representation of a
 * {@link PlanNode} in a specific state. It is mainly used for
 * serialization/deserialization operations.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class PlanNodeSnapshot {

    private String executionID;
    private String snapshotInstant;
    private NodeType nodeType;
    private String id;
    private String name;
    private String keyword;
    private String language;
    private String source;
    private String displayName;
    private List<String> description;
    private List<String> tags;
    private Map<String, String> properties;
    private String startInstant;
    private String finishInstant;
    private Long duration;
    private String document;
    private String documentType;
    private String[][] dataTable;
    private String errorMessage;
    private String errorTrace;
    private String errorClassifier;
    private Map<String, Long> errorClassifiers;
    private Result result;
    private Map<Result, Long> testCaseResults;
    private Map<Result, Long> childrenResults;
    private List<PlanNodeSnapshot> children;

    public PlanNodeSnapshot() {
    }

    public PlanNodeSnapshot(PlanNode node) {
        this(node, LocalDateTime.now().toString());
    }

    public PlanNodeSnapshot(PlanNode node, String snapshotInstant) {
        this.executionID = node.executionID();
        this.snapshotInstant = snapshotInstant;
        this.nodeType = node.nodeType();
        this.id = node.id();
        this.name = node.name();
        this.keyword = node.keyword();
        this.language = node.language();
        this.source = node.source();
        this.displayName = node.displayName();
        this.description = new LinkedList<>(node.description() == null ? List.of() : node.description());
        this.tags = new LinkedList<>(node.tags() == null ? List.of() : node.tags());
        this.properties = new LinkedHashMap<>(node.properties() == null ? Map.of() : node.properties());
        this.startInstant = node.startInstant().map(this::instantToString).orElse(null);
        this.finishInstant = node.finishInstant().map(this::instantToString).orElse(null);
        this.duration = node.duration().map(Duration::toMillis).orElse(null);
        this.result = node.result().orElse(null);
        this.document = node.data().filter(Document.class::isInstance).map(Document.class::cast)
                .map(Document::getContent).orElse(null);
        this.documentType = node.data().filter(Document.class::isInstance).map(Document.class::cast)
                .map(Document::getContentType).orElse(null);
        this.dataTable = node.data().filter(DataTable.class::isInstance).map(DataTable.class::cast)
                .map(DataTable::getValues).orElse(null);
        this.errorMessage = node.errors().findFirst().map(Throwable::getLocalizedMessage)
                .orElse(null);
        this.errorTrace = node.errors().findFirst().map(this::errorTrace).orElse(null);
        if (node.nodeType == NodeType.STEP && node.executionState().flatMap(ExecutionState::errorClassifier).isPresent()) {
            this.errorClassifier = node.executionState().flatMap(ExecutionState::errorClassifier).orElse(null);
        } else if (node.nodeType == NodeType.STEP_AGGREGATOR || node.nodeType == NodeType.TEST_CASE) {
            this.errorClassifier = node.errorClassifiers().findFirst().orElse(null);
        } else if (node.nodeType == NodeType.AGGREGATOR && node.hasChildren()) {
            this.errorClassifiers = countTestClassifiers(node);
        }
        if (node.hasChildren()) {
            this.children = node.children().map(child -> new PlanNodeSnapshot(child, snapshotInstant))
                    .collect(Collectors.toList());
            this.testCaseResults = countTestCases(node);
            this.childrenResults = countChildren(node);
        }
    }

    /**
     * Creates a new node descriptor as a parent of the specified nodes.
     *
     * @param nodes The nodes to be grouped.
     * @return A new parent node descriptor.
     */
    public static PlanNodeSnapshot group(PlanNodeSnapshot... nodes) {
        if (nodes.length == 1) {
            return nodes[0];
        }
        PlanNodeSnapshot root = new PlanNodeSnapshot();
        root.children = Arrays.asList(nodes);
        root.startInstant = childLocalDateTime(
                root,
                PlanNodeSnapshot::getStartInstant,
                (x, y) -> x.isBefore(y) ? x : y
        );
        root.finishInstant = childLocalDateTime(
                root,
                PlanNodeSnapshot::getFinishInstant,
                (x, y) -> x.isAfter(y) ? x : y
        );
        root.duration = maxChild(root, PlanNodeSnapshot::getDuration);
        root.result = maxChild(root, PlanNodeSnapshot::getResult);
        root.testCaseResults = new LinkedHashMap<>();
        root.children.stream().map(PlanNodeSnapshot::getTestCaseResults).filter(Objects::nonNull)
                .flatMap(map -> map.entrySet().stream()).forEach(entry -> {
                    root.testCaseResults.computeIfAbsent(entry.getKey(), x -> 0L);
                    root.testCaseResults.put(
                            entry.getKey(),
                            root.testCaseResults.get(entry.getKey()) + entry.getValue()
                    );
                });
        root.childrenResults = countChildren(root);
        return root;
    }

    private static Map<Result, Long> countTestCases(PlanNode node) {
        LinkedHashMap<Result, Long> results = new LinkedHashMap<>();
        if (node.nodeType() == NodeType.TEST_CASE) {
            node.result().ifPresent(testCaseResult -> results.put(testCaseResult, 1L));
        } else if (node.hasChildren()) {
            node.children().map(PlanNodeSnapshot::countTestCases).filter(Objects::nonNull)
                    .flatMap(map -> map.entrySet().stream())
                    .forEach(entry -> {
                        results.computeIfAbsent(entry.getKey(), x -> 0L);
                        results.put(entry.getKey(), results.get(entry.getKey()) + entry.getValue());
                    });
        }
        return results;
    }


    private static Map<String, Long> countTestClassifiers(PlanNode node) {
        LinkedHashMap<String, LongAdder> results = new LinkedHashMap<>();
        if (node.nodeType() == NodeType.TEST_CASE) {
            node.errorClassifiers().findFirst()
                    .ifPresent(errorClassifier -> results.computeIfAbsent(errorClassifier, x -> new LongAdder()).add(1L));
        } else if (node.nodeType() == NodeType.AGGREGATOR && node.hasChildren()) {
            node.children().map(PlanNodeSnapshot::countTestClassifiers).filter(Objects::nonNull)
                    .flatMap(map -> map.entrySet().stream())
                    .forEach(entry -> results.computeIfAbsent(entry.getKey(), x -> new LongAdder()).add(entry.getValue()));
        }
        return results.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().longValue()));
    }

    private static Map<Result, Long> countChildren(PlanNode node) {
        return node.children()
                .filter(it -> it.result().isPresent())
                .collect(groupingBy(it -> it.result().orElseThrow(), counting()));
    }

    private static Map<Result, Long> countChildren(PlanNodeSnapshot node) {
        return node.getChildren().stream().collect(groupingBy(PlanNodeSnapshot::getResult, counting()));
    }

    private static String childLocalDateTime(
            PlanNodeSnapshot node,
            Function<PlanNodeSnapshot, String> method,
            BinaryOperator<LocalDateTime> reducer
    ) {
        return node.children.stream().map(method).filter(Objects::nonNull).map(LocalDateTime::parse)
                .reduce(reducer)
                .map(LocalDateTime::toString).orElse(null);
    }

    private static <T extends Comparable<T>> T maxChild(
            PlanNodeSnapshot node,
            Function<PlanNodeSnapshot, T> mapper
    ) {
        return node.children.stream().map(mapper).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * Creates a new PlanNodeSnapshot without children.
     *
     * @return A new PlanNodeSnapshot without children.
     */
    public PlanNodeSnapshot withoutChildren() {
        PlanNodeSnapshot copy = new PlanNodeSnapshot();
        copy.executionID = this.executionID;
        copy.snapshotInstant = this.snapshotInstant;
        copy.nodeType = this.nodeType;
        copy.id = this.id;
        copy.name = this.name;
        copy.keyword = this.keyword;
        copy.language = this.language;
        copy.source = this.source;
        copy.displayName = this.displayName;
        copy.description = this.description;
        copy.tags = this.tags;
        copy.properties = this.properties;
        copy.startInstant = this.startInstant;
        copy.finishInstant = this.finishInstant;
        copy.duration = this.duration;
        copy.result = this.result;
        copy.document = this.document;
        copy.documentType = this.documentType;
        copy.dataTable = this.dataTable;
        copy.errorMessage = this.errorMessage;
        copy.errorTrace = this.errorTrace;
        copy.testCaseResults = this.testCaseResults;
        copy.childrenResults = this.childrenResults;
        copy.errorClassifiers = this.errorClassifiers;
        copy.errorClassifier = this.errorClassifier;
        return copy;
    }

    private String instantToString(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toString();
    }

    private String errorTrace(Throwable error) {
        StringWriter errorWriter = new StringWriter();
        error.printStackTrace(new PrintWriter(errorWriter));
        return errorWriter.toString();
    }

    public NodeType getNodeType() {
        return nodeType;
    }


    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getKeyword() {
        return keyword;
    }


    public String getLanguage() {
        return language;
    }


    public String getSource() {
        return source;
    }


    public String getDisplayName() {
        return displayName;
    }


    public List<String> getDescription() {
        return description;
    }


    public List<String> getTags() {
        return tags;
    }


    public Map<String, String> getProperties() {
        return properties;
    }


    public String getStartInstant() {
        return startInstant;
    }


    public String getFinishInstant() {
        return finishInstant;
    }


    public Long getDuration() {
        return duration;
    }


    public String getDocument() {
        return document;
    }


    public String getDocumentType() {
        return documentType;
    }


    public String[][] getDataTable() {
        return dataTable;
    }


    public String getErrorMessage() {
        return errorMessage;
    }


    public String getErrorTrace() {
        return errorTrace;
    }


    public String getErrorClassifier() {
        return errorClassifier;
    }


    public Result getResult() {
        return result;
    }


    public Map<Result, Long> getTestCaseResults() {
        return testCaseResults;
    }


    public Map<Result, Long> getChildrenResults() {
        return childrenResults;
    }


    public Map<String, Long> getErrorClassifiers() {
        return errorClassifiers;
    }

    public List<PlanNodeSnapshot> getChildren() {
        return children;
    }


    public String getExecutionID() {
        return executionID;
    }


    public String getSnapshotInstant() {
        return snapshotInstant;
    }
}