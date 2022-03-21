/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.plan;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;


/** This class is a immutable, non-executable representation of a * {@link PlanNode} in a specific state. * <p> * It is mainly used for serialization/deserialization operations. * </p> */
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
        this.description = (node.description() == null ? null
                        : new ArrayList<>(node.description()));
        this.tags = (node.tags() == null ? null : new ArrayList<>(node.tags()));
        this.properties = (node.properties() == null ? null
                        : new LinkedHashMap<>(node.properties()));
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
        if (node.hasChildren()) {
            this.children = node.children().map(child -> new PlanNodeSnapshot(child,snapshotInstant))
                .collect(Collectors.toList());
            this.testCaseResults = countTestCases(node);
            this.childrenResults = countChildren(node);
        }
    }



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
        copy.tags = new ArrayList<>(this.tags);
        copy.properties = new LinkedHashMap<>(this.properties);
        copy.startInstant = this.startInstant;
        copy.finishInstant = this.finishInstant;
        copy.duration = this.duration;
        copy.result = this.result;
        copy.document = this.document;
        copy.documentType = this.documentType;
        copy.dataTable = this.dataTable;
        copy.errorMessage = this.errorMessage;
        copy.errorTrace = this.errorTrace;
        copy.testCaseResults = new LinkedHashMap<>(this.testCaseResults);
        copy.childrenResults = new LinkedHashMap<>(this.childrenResults);
        return copy;
    }




    private String instantToString(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toString();
    }


    private String errorTrace(Throwable error) {
        StringWriter errorWriter = new StringWriter();
        error.printStackTrace(new PrintWriter(errorWriter));
        return errorWriter.toString();
    }


    /**
     * Creates a new node descriptor as a parent of the specified nodes
     *
     * @param nodes The nodes to be grouped
     * @return A new parent node descriptor
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
            (x, y) -> x.compareTo(y) < 0 ? x : y
        );
        root.finishInstant = childLocalDateTime(
            root,
            PlanNodeSnapshot::getFinishInstant,
            (x, y) -> x.compareTo(y) > 0 ? x : y
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


    private static Map<Result, Long> countChildren(PlanNode node) {
        return node.children()
            .filter(it->it.result().isPresent())
            .collect(groupingBy(it->it.result().orElseThrow(), counting()));
    }


    private static Map<Result, Long> countChildren(PlanNodeSnapshot node) {
        return node.getChildren().stream().collect(groupingBy(it->it.getResult(), counting()));
    }


    private static String childLocalDateTime(
        PlanNodeSnapshot node,
        Function<PlanNodeSnapshot, String> method,
        BinaryOperator<LocalDateTime> reductor
    ) {
        return node.children.stream().map(method).filter(Objects::nonNull).map(LocalDateTime::parse)
            .reduce(reductor)
            .map(LocalDateTime::toString).orElse(null);
    }


    private static <T extends Comparable<T>> T maxChild(
        PlanNodeSnapshot node,
        Function<PlanNodeSnapshot, T> mapper
    ) {
        return node.children.stream().map(mapper).filter(Objects::nonNull)
            .max(Comparator.naturalOrder()).orElse(null);
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


    public Result getResult() {
        return result;
    }


    public Map<Result, Long> getTestCaseResults() {
        return testCaseResults;
    }


    public Map<Result, Long> getChildrenResults() {
        return childrenResults;
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