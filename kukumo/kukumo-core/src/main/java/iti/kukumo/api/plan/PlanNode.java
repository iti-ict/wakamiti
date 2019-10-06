package iti.kukumo.api.plan;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import iti.kukumo.core.model.ExecutableTreeNode;


public class PlanNode extends ExecutableTreeNode<PlanNode,Result> {

    private final List<String> description;
    private final Set<String> tags;
    private final Map<String,String> properties;
    private final NodeType nodeType;
    private final String language;
    private final String id;
    private final String source;
    private final String keyword;
    private final String name;
    private final String displayName;
    private final Optional<PlanNodeData> data;


    public PlanNode (PlanNodeBuilder builder) {
        super(builder.children().map(PlanNode::new).collect(Collectors.toList()));
        description = Collections.unmodifiableList(builder.description());
        tags = Collections.unmodifiableSet(builder.tags());
        properties = Collections.unmodifiableMap(builder.properties());
        nodeType = builder.nodeType();
        language = builder.language();
        id = builder.id();
        source = builder.source();
        keyword = builder.keyword();
        name = builder.name();
        displayName = builder.displayName();
        data = builder.data();
    }


    public PlanNode (NodeType nodeType, List<PlanNode> children) {
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
