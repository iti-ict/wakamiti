/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.plan;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import iti.kukumo.core.model.ExecutableTreeNode;


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

}