/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import es.iti.wakamiti.api.model.TreeNodeBuilder;


/**
 * Builder class for creating instances of {@link PlanNode}.
 */
public class PlanNodeBuilder extends TreeNodeBuilder<PlanNodeBuilder> {

    private final List<String> description = new ArrayList<>();
    private final Set<String> tags = new LinkedHashSet<>();
    private final Map<String, String> properties = new HashMap<>();

    private NodeType nodeType;
    private String language;
    private String id;
    private String source;
    private String keyword;
    private String name;
    private String displayNamePattern = "[{id}] {keyword} {name}";
    private PlanNodeData data;
    private Object underlyingModel;
    private boolean filtered;

    /**
     * Starts a builder for a leaf or initially childless node.
     *
     * @param nodeType the semantic role of the node
     */
    public PlanNodeBuilder(
            NodeType nodeType
    ) {
        this.nodeType = nodeType;
    }

    /**
     * Starts a builder with an existing collection of child builders.
     *
     * @param nodeType the semantic role of the node
     * @param children child builders in the order they should appear in the
     *                 resulting plan
     */
    public PlanNodeBuilder(
            NodeType nodeType,
            Collection<PlanNodeBuilder> children
    ) {
        super(children);
        this.nodeType = nodeType;
    }

    /**
     * Returns the source-level name currently assigned to the node.
     *
     * @return the configured name, or {@code null}
     */
    public String name() {
        return name;
    }

    /**
     * Returns the localized Gherkin keyword currently assigned to the node.
     *
     * @return the configured keyword, or {@code null}
     */
    public String keyword() {
        return keyword;
    }

    /**
     * Returns the source or generated identifier currently assigned.
     *
     * @return the configured identifier, or {@code null}
     */
    public String id() {
        return id;
    }

    /**
     * Returns the configured source language.
     *
     * @return the language code, or {@code null}
     */
    public String language() {
        return language;
    }

    /**
     * Returns the semantic role selected for the node.
     *
     * @return the node type
     */
    public NodeType nodeType() {
        return nodeType;
    }

    /**
     * Returns the mutable description accumulated by this builder.
     *
     * @return description lines in insertion order
     */
    public List<String> description() {
        return description;
    }

    /**
     * Returns the mutable, insertion-ordered set of accumulated tags.
     *
     * @return the current tags
     */
    public Set<String> tags() {
        return tags;
    }

    /**
     * Returns the configured source location or logical source name.
     *
     * @return the source identifier, or {@code null}
     */
    public String source() {
        return source;
    }

    /**
     * Returns the mutable property map accumulated by this builder.
     *
     * @return the current node-scoped properties
     */
    public Map<String, String> properties() {
        return properties;
    }

    /**
     * Returns the template used to produce the display name.
     *
     * @return a template supporting the placeholders {@code {id}},
     * {@code {keyword}}, and {@code {name}}
     */
    public String displayNamePattern() {
        return displayNamePattern;
    }

    /**
     * Indicates whether the resulting node will be marked as excluded by a
     * selection filter.
     *
     * @return the current filtered flag
     */
    public boolean filtered() {
        return filtered;
    }

    /**
     * Resolves the current display-name template against the builder state.
     * Unset placeholders resolve to an empty string.
     *
     * @return the display label that {@link #build()} will assign
     */
    public String displayName() {
        String displayName = displayNamePattern;
        displayName = displayName.replace("{id}", id == null ? "" : id);
        displayName = displayName.replace("{keyword}", keyword == null ? "" : keyword);
        displayName = displayName.replace("{name}", name == null ? "" : name);
        return displayName;
    }

    /**
     * Returns structured data currently attached to the node.
     *
     * @return the data table or document, or an empty optional
     */
    public Optional<PlanNodeData> data() {
        return Optional.ofNullable(data);
    }

    /**
     * Returns the parser-specific object from which this builder was derived.
     * Consumers can use it to retain source metadata not represented by
     * {@link PlanNode}.
     *
     * @return the underlying source model, or {@code null}
     */
    public Object getUnderlyingModel() {
        return underlyingModel;
    }

    /**
     * Associates the builder with its parser-specific source object.
     *
     * @param gherkinModel the underlying model, or {@code null}
     * @return this builder
     */
    public PlanNodeBuilder setUnderlyingModel(
            Object gherkinModel
    ) {
        this.underlyingModel = gherkinModel;
        return this;
    }

    /**
     * Assigns the node's source or generated identifier.
     *
     * @param id the identifier, or {@code null}
     * @return this builder
     */
    public PlanNodeBuilder setId(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * Assigns the localized keyword that introduced the source node.
     *
     * @param keyword the keyword, or {@code null} for a synthetic node
     * @return this builder
     */
    public PlanNodeBuilder setKeyword(
            String keyword
    ) {
        this.keyword = keyword;
        return this;
    }

    /**
     * Assigns the language used to parse the source.
     *
     * @param language the language code, or {@code null} when not applicable
     * @return this builder
     */
    public PlanNodeBuilder setLanguage(
            String language
    ) {
        this.language = language;
        return this;
    }

    /**
     * Assigns the source-level node name.
     *
     * @param name the node name, or {@code null}
     * @return this builder
     */
    public PlanNodeBuilder setName(
            String name
    ) {
        this.name = name;
        return this;
    }

    /**
     * Changes the semantic role assigned to the resulting node.
     *
     * @param nodeType the new node type
     * @return this builder
     */
    public PlanNodeBuilder setNodeType(
            NodeType nodeType
    ) {
        this.nodeType = nodeType;
        return this;
    }

    /**
     * Sets the template used by {@link #displayName()}.
     *
     * @param displayNamePattern a template supporting {@code {id}},
     *                           {@code {keyword}}, and {@code {name}}
     * @return this builder
     */
    public PlanNodeBuilder setDisplayNamePattern(
            String displayNamePattern
    ) {
        this.displayNamePattern = displayNamePattern;
        return this;
    }

    /**
     * Records the source location or logical resource name.
     *
     * @param source the source identifier, or {@code null}
     * @return this builder
     */
    public PlanNodeBuilder setSource(
            String source
    ) {
        this.source = source;
        return this;
    }

    /**
     * Adds tags while preserving first-seen order and removing duplicates.
     *
     * @param tags tags to merge into the node
     * @return this builder
     */
    public PlanNodeBuilder addTags(
            Collection<String> tags
    ) {
        this.tags.addAll(tags);
        return this;
    }

    /**
     * Appends source description lines without reordering existing content.
     *
     * @param description the lines to append
     * @return this builder
     */
    public PlanNodeBuilder addDescription(
            Collection<String> description
    ) {
        this.description.addAll(description);
        return this;
    }

    /**
     * Merges node-scoped properties, replacing values for duplicate keys.
     *
     * @param properties properties to merge
     * @return this builder
     */
    public PlanNodeBuilder addProperties(
            Map<String, String> properties
    ) {
        this.properties.putAll(properties);
        return this;
    }

    /**
     * Adds or replaces one node-scoped property.
     *
     * @param key   the property key
     * @param value the property value
     * @return this builder
     */
    public PlanNodeBuilder addProperty(
            String key,
            String value
    ) {
        this.properties.put(key, value);
        return this;
    }

    /**
     * Attaches structured step data to the node.
     *
     * @param data a data table or document, or {@code null} to clear it
     * @return this builder
     */
    public PlanNodeBuilder setData(
            PlanNodeData data
    ) {
        this.data = data;
        return this;
    }

    /**
     * Sets whether selection rules excluded the resulting node from execution.
     *
     * @param filtered {@code true} to mark the node as filtered out
     */
    public void filtered(
            boolean filtered
    ) {
        this.filtered = filtered;
    }

    @Override
    public PlanNodeBuilder copy() {
        return copy(new PlanNodeBuilder(nodeType));
    }

    @Override
    protected PlanNodeBuilder copy(
            PlanNodeBuilder copy
    ) {
        copy.setLanguage(this.language);
        copy.setId(this.id);
        copy.setKeyword(this.keyword);
        copy.setName(this.name);
        copy.setDisplayNamePattern(this.displayNamePattern);
        copy.addTags(this.tags);
        copy.setSource(this.source());
        copy.addDescription(this.description);
        copy.properties.putAll(this.properties);
        copy.setData(this.data().map(PlanNodeData::copy).orElse(null));
        copy.filtered = this.filtered;
        super.copy(copy);
        return copy;
    }

    /**
     * Recursively creates a plan-node tree from this builder and its children.
     * <p>
     * Description, tag, and property collections are exposed through
     * unmodifiable views in the resulting node. Structured data is retained as
     * configured.
     * </p>
     *
     * @return a newly built plan node
     */
    public PlanNode build() {
        PlanNode node = new PlanNode(
                this.nodeType(),
                this.children().map(PlanNodeBuilder::build).collect(Collectors.toList()));
        node.description = Collections.unmodifiableList(this.description());
        node.tags = Collections.unmodifiableSet(this.tags());
        node.properties = Collections.unmodifiableMap(this.properties());
        node.language = this.language();
        node.id = this.id();
        node.source = this.source();
        node.keyword = this.keyword();
        node.name = this.name();
        node.displayName = this.displayName();
        node.data = this.data();
        node.filtered = this.filtered;
        return node;
    }

}
