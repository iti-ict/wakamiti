/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.api.plan;

import iti.kukumo.api.model.ExecutableTreeNode;
import iti.kukumo.api.util.Argument;

import java.util.*;
import java.util.function.Predicate;

/**
 *
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
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


    public PlanNode(NodeType nodeType, List<PlanNode> children) {
        super(children);
        description = null;
        tags = Collections.emptySet();
        properties = Collections.emptyMap();
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


    public Optional<PlanNodeData> data() {
        return data;
    }

    public List<Argument> arguments() {
        return arguments;
    }

    public String displayName() {
        return displayName;
    }


    public int numDescendants(NodeType nodeType) {
        return numDescendants(descendant -> descendant.nodeType() == nodeType);
    }


    public int numDescendants(NodeType nodeType, Result result) {
        return numDescendants(
                descendant -> descendant.nodeType() == nodeType &&
                        descendant.executionState().isPresent() &&
                        descendant.executionState().get().hasResult(result)
        );
    }

    public void resolveProperties(Predicate<Map.Entry<String, String>> filter) {
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