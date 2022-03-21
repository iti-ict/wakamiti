/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.plan;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import iti.kukumo.core.model.TreeNodeBuilder;


public class PlanNodeBuilder extends TreeNodeBuilder<PlanNodeBuilder> {

    private final List<String> description = new ArrayList<>();
    private final Set<String> tags = new HashSet<>();
    private final Map<String, String> properties = new HashMap<>();

    private NodeType nodeType;
    private String language;
    private String id;
    private String source;
    private String keyword;
    private String name;
    private String displayNamePattern = "[{id}] {keyword} {name}";
    private Optional<PlanNodeData> data = Optional.empty();
    private Object underlyingModel;


    public PlanNodeBuilder(NodeType nodeType) {
        this.nodeType = nodeType;
    }


    public PlanNodeBuilder(NodeType nodeType, Collection<PlanNodeBuilder> children) {
        super(children);
        this.nodeType = nodeType;
    }



    public String name() {
        return name;
    }


    public String keyword() {
        return keyword;
    }


    public String id() {
        return id;
    }


    public String language() {
        return language;
    }


    public NodeType nodeType() {
        return nodeType;
    }


    public List<String> description() {
        return description;
    }


    public Set<String> tags() {
        return tags;
    }


    public String source() {
        return source;
    }


    public Map<String, String> properties() {
        return properties;
    }


    public String displayNamePattern() {
        return displayNamePattern;
    }


    public String displayName() {
        String displayName = displayNamePattern;
        displayName = displayName.replace("{id}", id == null ? "" : id);
        displayName = displayName.replace("{keyword}", keyword == null ? "" : keyword);
        displayName = displayName.replace("{name}", name == null ? "" : name);
        return displayName;
    }


    public Optional<PlanNodeData> data() {
        return data;
    }


    public Object getUnderlyingModel() {
        return underlyingModel;
    }


    public PlanNodeBuilder setId(String id) {
        this.id = id;
        return this;
    }


    public PlanNodeBuilder setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }


    public PlanNodeBuilder setLanguage(String language) {
        this.language = language;
        return this;
    }


    public PlanNodeBuilder setName(String name) {
        this.name = name;
        return this;
    }


    public PlanNodeBuilder setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }


    public PlanNodeBuilder setDisplayNamePattern(String displayNamePattern) {
        this.displayNamePattern = displayNamePattern;
        return this;
    }


    public PlanNodeBuilder setSource(String source) {
        this.source = source;
        return this;
    }


    public PlanNodeBuilder addTags(Collection<String> tags) {
        this.tags.addAll(tags);
        return this;
    }


    public PlanNodeBuilder addDescription(Collection<String> description) {
        this.description.addAll(description);
        return this;
    }


    public PlanNodeBuilder addProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
        return this;
    }


    public PlanNodeBuilder addProperty(String key, String value) {
        this.properties.put(key, value);
        return this;
    }


    public PlanNodeBuilder setUnderlyingModel(Object gherkinModel) {
        this.underlyingModel = gherkinModel;
        return this;
    }


    public PlanNodeBuilder setData(PlanNodeData data) {
        this.data = Optional.ofNullable(data);
        return this;
    }


    @Override
    public PlanNodeBuilder copy() {
        return copy(new PlanNodeBuilder(nodeType));
    }


    @Override
    protected PlanNodeBuilder copy(PlanNodeBuilder copy) {
        copy.setLanguage(this.language);
        copy.setId(this.id);
        copy.setKeyword(this.keyword);
        copy.setName(this.name);
        copy.setDisplayNamePattern(this.displayNamePattern);
        copy.addTags(this.tags);
        copy.setSource(this.source());
        copy.addDescription(this.description);
        copy.properties.putAll(this.properties);
        copy.data = this.data.map(PlanNodeData::copy);
        super.copy(copy);
        return copy;
    }



    public PlanNode build() {
        PlanNode node = new PlanNode(
            this.nodeType(),
            this.children().map(PlanNodeBuilder::build).collect(Collectors.toList()))
        ;
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
        return node;
    }
}