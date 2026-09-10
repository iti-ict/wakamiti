/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import es.iti.wakamiti.api.model.ExecutionState;


/**
 * This class is an immutable, non-executable representation of a
 * {@link PlanNode} in a specific state. It is mainly used for
 * serialization/deserialization operations.
 */
public class PlanNodeSnapshot {

    private static final int INSTANT_PRECISION = 6;
    private static final DateTimeFormatter UTC_INSTANT_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendInstant(INSTANT_PRECISION)
                    .toFormatter();

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
    private String response;
    private Map<String, Long> errorClassifiers;
    private Result result;
    private Map<Result, Long> testCaseResults;
    private Map<Result, Long> childrenResults;
    private List<PlanNodeSnapshot> children;

    /**
     * Creates an empty snapshot for serialization frameworks and synthetic
     * aggregation roots.
     * <p>
     * Application code should normally use {@link #PlanNodeSnapshot(PlanNode)}
     * so that all derived execution fields are populated consistently.
     * </p>
     */
    public PlanNodeSnapshot() {
    }

    /**
     * Captures the current state of a plan node and all its descendants using
     * the current UTC instant as the snapshot marker.
     *
     * @param node the executable node whose current state will be captured
     */
    public PlanNodeSnapshot(
            PlanNode node
    ) {
        this(node, Instant.now().toString());
    }

    /**
     * Captures the current state of a plan node tree with a caller-provided
     * timestamp shared by every descendant snapshot.
     * <p>
     * Execution instants are serialized as UTC instants.
     * Throwable information is reduced to the first error's message and stack
     * trace, while result summaries are computed for child and test-case nodes.
     * </p>
     *
     * @param node            the executable node whose state will be captured
     * @param snapshotInstant the textual timestamp assigned to this snapshot
     *                        and its descendants
     */
    public PlanNodeSnapshot(
            PlanNode node,
            String snapshotInstant
    ) {
        this.executionID = node.executionID();
        this.snapshotInstant = toUtcInstantString(snapshotInstant);
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
        this.startInstant = node.startInstant().map(PlanNodeSnapshot::instantToString).orElse(null);
        this.finishInstant = node.finishInstant().map(PlanNodeSnapshot::instantToString).orElse(null);
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
        this.response = node.executionState().flatMap(ExecutionState::response).orElse(null);
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
    public static PlanNodeSnapshot group(
            PlanNodeSnapshot... nodes
    ) {
        if (nodes.length == 1) {
            return nodes[0];
        }
        PlanNodeSnapshot root = new PlanNodeSnapshot();
        root.children = Arrays.asList(nodes);
        root.startInstant = childInstant(
                root,
                PlanNodeSnapshot::getStartInstant,
                (x, y) -> x.isBefore(y) ? x : y
        );
        root.finishInstant = childInstant(
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

    private static Map<Result, Long> countTestCases(
            PlanNode node
    ) {
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

    private static Map<String, Long> countTestClassifiers(
            PlanNode node
    ) {
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

    private static Map<Result, Long> countChildren(
            PlanNode node
    ) {
        return node.children()
                .filter(child -> child.nodeType() != NodeType.LIFECYCLE_HOOK)
                .filter(it -> it.result().isPresent())
                .collect(groupingBy(it -> it.result().orElseThrow(), counting()));
    }

    private static Map<Result, Long> countChildren(
            PlanNodeSnapshot node
    ) {
        return node.getChildren().stream().collect(groupingBy(PlanNodeSnapshot::getResult, counting()));
    }

    private static String childInstant(
            PlanNodeSnapshot node,
            Function<PlanNodeSnapshot, String> method,
            BinaryOperator<Instant> reducer
    ) {
        return node.children.stream().map(method).filter(Objects::nonNull).map(PlanNodeSnapshot::toInstant)
                .reduce(reducer)
                .map(Instant::toString).orElse(null);
    }

    private static <T extends Comparable<T>> T maxChild(
            PlanNodeSnapshot node,
            Function<PlanNodeSnapshot, T> mapper
    ) {
        return node.children.stream().map(mapper).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * Traverses this snapshot tree in pre-order and returns nodes accepted by a
     * predicate.
     * <p>
     * A rejected parent does not prune its descendants; each node is evaluated
     * independently.
     * </p>
     *
     * @param filter predicate selecting snapshots for the resulting stream
     * @return a lazy stream containing matching nodes, starting with this node
     * when accepted
     */
    public Stream<PlanNodeSnapshot> flatten(
            Predicate<PlanNodeSnapshot> filter
    ) {
        return Stream.concat(
                Optional.of(this).filter(filter).stream(),
                Optional.ofNullable(this.children).stream().flatMap(Collection::stream)
                        .flatMap(p -> p.flatten(filter))
        );
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
        copy.response = this.response;
        copy.testCaseResults = this.testCaseResults;
        copy.childrenResults = this.childrenResults;
        copy.errorClassifiers = this.errorClassifiers;
        copy.errorClassifier = this.errorClassifier;
        return copy;
    }

    private static String instantToString(
            Instant instant
    ) {
        return UTC_INSTANT_FORMATTER.format(instant);
    }

    private static Instant toInstant(
            String instant
    ) {
        try {
            return Instant.parse(instant);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(instant).atZone(ZoneId.systemDefault()).toInstant();
        }
    }

    private static String toUtcInstantString(
            String instant
    ) {
        if (instant == null) {
            return null;
        }
        try {
            return instantToString(toInstant(instant));
        } catch (DateTimeParseException ignored) {
            return instant;
        }
    }

    private String errorTrace(
            Throwable error
    ) {
        StringWriter errorWriter = new StringWriter();
        error.printStackTrace(new PrintWriter(errorWriter));
        return errorWriter.toString();
    }

    /**
     * Returns the semantic role of the captured node.
     *
     * @return the node type, or {@code null} for a synthetic grouping root
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * Returns the captured source or generated identifier.
     *
     * @return the node identifier, or {@code null}
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the captured source-level node name.
     *
     * @return the node name, or {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the localized source keyword.
     *
     * @return the keyword, or {@code null} for synthetic nodes
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns the language used to parse the source.
     *
     * @return the language code, or {@code null}
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the source location or logical resource name.
     *
     * @return the source identifier, or {@code null}
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the label prepared for reports and test-runner output.
     *
     * @return the captured display name, or {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the captured source description.
     *
     * @return description lines in source order; regular node snapshots use an
     * empty list when no description exists
     */
    public List<String> getDescription() {
        return description;
    }

    /**
     * Returns the captured effective tags.
     *
     * @return tags in their original iteration order
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Returns the configuration properties scoped to the captured node.
     *
     * @return the captured properties in declaration order
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns when node execution started, expressed in UTC.
     *
     * @return an ISO-8601 UTC instant string, or {@code null} if execution did
     * not start
     */
    public String getStartInstant() {
        return startInstant;
    }

    /**
     * Returns when node execution finished, expressed in UTC.
     *
     * @return an ISO-8601 UTC instant string, or {@code null} if execution did
     * not finish
     */
    public String getFinishInstant() {
        return finishInstant;
    }

    /**
     * Returns the captured execution duration.
     *
     * @return elapsed milliseconds, or {@code null} if no duration was
     * available
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Returns attached document content when the node carries a
     * {@link Document}.
     *
     * @return the document body, or {@code null}
     */
    public String getDocument() {
        return document;
    }

    /**
     * Returns the media type associated with {@link #getDocument()}.
     *
     * @return the document type, or {@code null} when absent or unspecified
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * Returns the cells attached when the node carries a {@link DataTable}.
     *
     * @return the captured table array, or {@code null}
     */
    public String[][] getDataTable() {
        return dataTable;
    }

    /**
     * Returns the localized message of the first captured execution error.
     *
     * @return the error message, or {@code null} when no error was recorded
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the complete stack trace of the first captured execution error.
     *
     * @return the rendered stack trace, or {@code null} when no error was
     * recorded
     */
    public String getErrorTrace() {
        return errorTrace;
    }

    /**
     * Returns the classifier associated with this node's failure.
     *
     * @return the direct or derived classifier, or {@code null} when the node
     * has no classified error
     */
    public String getErrorClassifier() {
        return errorClassifier;
    }

    /**
     * Returns the textual response captured from the node execution state.
     *
     * @return the response, or {@code null} when none was produced
     */
    public String getResponse() {
        return response;
    }

    /**
     * Returns the node's result at snapshot time.
     *
     * @return the execution result, or {@code null} before a result exists
     */
    public Result getResult() {
        return result;
    }

    /**
     * Returns aggregate result counts for all descendant test cases.
     *
     * @return result-to-count mappings, or {@code null} for nodes without
     * children
     */
    public Map<Result, Long> getTestCaseResults() {
        return testCaseResults;
    }

    /**
     * Returns result counts for direct child snapshots.
     *
     * @return direct-child result counts, or {@code null} for leaf snapshots
     */
    public Map<Result, Long> getChildrenResults() {
        return childrenResults;
    }

    /**
     * Returns aggregate failure-classifier counts for descendant test cases.
     *
     * @return classifier-to-count mappings for aggregator snapshots, or
     * {@code null} when no aggregate was computed
     */
    public Map<String, Long> getErrorClassifiers() {
        return errorClassifiers;
    }

    /**
     * Returns snapshots of direct descendants.
     *
     * @return children in plan order, or {@code null} for a leaf snapshot
     */
    public List<PlanNodeSnapshot> getChildren() {
        return children;
    }

    /**
     * Returns the identifier shared by nodes belonging to the same execution.
     *
     * @return the execution identifier, or {@code null} when none was assigned
     */
    public String getExecutionID() {
        return executionID;
    }

    /**
     * Returns the UTC timestamp at which this state was captured.
     *
     * @return an ISO-8601 UTC instant string, or the original textual value
     * when it could not be parsed as a date-time
     */
    public String getSnapshotInstant() {
        return snapshotInstant;
    }

}
