/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import es.iti.wakamiti.api.model.ExecutableTreeNode;
import es.iti.wakamiti.api.util.Argument;


/**
 * Represents a Plan Node node in the execution model.
 */
public class PlanNode extends ExecutableTreeNode<PlanNode, Result> {

    List<String> description;
    Set<String> tags;
    Map<String, String> properties;
    NodeType nodeType;
    String language;
    String id;
    String source;
    String keyword;
    String name;
    String displayName;
    Optional<PlanNodeData> data;
    List<Argument> arguments;
    boolean filtered;

    /**
     * Creates a plan node with the supplied kind and children.
     * <p>
     * Metadata fields start unset and are normally populated through
     * {@link PlanNodeBuilder}. Execution state is managed by the inherited
     * executable-tree API.
     * </p>
     *
     * @param nodeType the semantic role of the node in the plan
     * @param children the node's direct descendants in execution order
     */
    public PlanNode(
            NodeType nodeType,
            List<PlanNode> children
    ) {
        super(children);
        description = null;
        tags = new LinkedHashSet<>();
        properties = new LinkedHashMap<>();
        this.nodeType = nodeType;
        language = null;
        id = null;
        source = null;
        keyword = null;
        name = null;
        displayName = null;
        data = Optional.empty();
        arguments = new LinkedList<>();
    }

    /**
     * Returns the source-level name of the node.
     *
     * @return the node name, or {@code null} when the source supplied none
     */
    public String name() {
        return name;
    }

    /**
     * Returns the localized Gherkin keyword that introduced the node.
     *
     * @return the keyword, or {@code null} for synthetic nodes
     */
    public String keyword() {
        return keyword;
    }

    /**
     * Returns the source or generated identifier of the node.
     *
     * @return the node identifier, or {@code null} when none was assigned
     */
    public String id() {
        return id;
    }

    /**
     * Returns the language used to parse this node's source document.
     *
     * @return the language code, or {@code null} when not applicable
     */
    public String language() {
        return language;
    }

    /**
     * Returns the node's semantic role in the execution hierarchy.
     *
     * @return the node type
     */
    public NodeType nodeType() {
        return nodeType;
    }

    /**
     * Returns the source description lines in their original order.
     *
     * @return the description, potentially empty; directly constructed nodes
     * may return {@code null} until populated
     */
    public List<String> description() {
        return description;
    }

    /**
     * Returns the effective tags attached to the node.
     *
     * @return the insertion-ordered tag set
     */
    public Set<String> tags() {
        return tags;
    }

    /**
     * Returns the location or logical name of the source that produced the
     * node.
     *
     * @return the source identifier, or {@code null} for synthetic nodes
     */
    public String source() {
        return source;
    }

    /**
     * Returns configuration properties scoped to this node.
     *
     * @return the property map, preserving declaration order
     */
    public Map<String, String> properties() {
        return properties;
    }

    /**
     * Returns structured data attached to the node, such as a
     * {@link DataTable} or {@link Document}.
     *
     * @return the attached data, or an empty optional
     */
    public Optional<PlanNodeData> data() {
        return data;
    }

    /**
     * Returns arguments resolved for the node's executable step.
     *
     * @return the arguments in invocation order
     */
    public List<Argument> arguments() {
        return arguments;
    }

    /**
     * Returns the precomputed label intended for logs, reports, and test
     * runners.
     *
     * @return the display name
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Indicates whether selection rules excluded this node from execution.
     *
     * @return {@code true} when the node is filtered out
     */
    public boolean filtered() {
        return filtered;
    }

    /**
     * Counts descendants having a specific semantic type.
     *
     * @param nodeType the type to count
     * @return the number of matching descendants at any depth
     */
    public int numDescendants(
            NodeType nodeType
    ) {
        return numDescendants(descendant -> descendant.nodeType() == nodeType);
    }

    /**
     * Counts descendants of a given type that completed with a given result.
     * Nodes without execution state are not included.
     *
     * @param nodeType the semantic type to count
     * @param result   the required execution result
     * @return the number of matching executed descendants at any depth
     */
    public int numDescendants(
            NodeType nodeType,
            Result result
    ) {
        return numDescendants(
                descendant -> descendant.nodeType() == nodeType
                        && descendant.executionState().isPresent()
                        && descendant.executionState().get().hasResult(result)
        );
    }

    /**
     * Replaces selected evaluated-property expressions in this node and all
     * descendants.
     * <p>
     * Replacement values are collected from step argument evaluations. Every
     * accepted mapping is applied to the node name and to attached structured
     * data. The operation mutates the current plan tree.
     * </p>
     *
     * @param filter predicate selecting which expression-to-value mappings may
     *               be applied
     */
    public void resolveProperties(
            Predicate<Map.Entry<String, String>> filter
    ) {
        children().forEach(c -> c.resolveProperties(filter));

        arguments.stream().map(Argument::evaluations)
                .reduce((firstMap, secondMap) -> {
                    secondMap.forEach(firstMap::putIfAbsent);
                    return firstMap;
                }).orElse(new HashMap<>())
                .entrySet().stream()
                .filter(filter)
                .forEach(e -> {
                    data = data.map(d -> d.copyReplacingVariables(v -> v.replace(e.getKey(), e.getValue())));
                    name = name.replace(e.getKey(), e.getValue());
                });
    }

}
