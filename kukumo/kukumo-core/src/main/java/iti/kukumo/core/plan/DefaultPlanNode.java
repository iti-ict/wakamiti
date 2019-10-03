package iti.kukumo.core.plan;

import iti.kukumo.api.plan.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class DefaultPlanNode implements PlanNode {

    private final List<String> description = new ArrayList<>();
    private final Set<String> tags = new HashSet<>();
    private final List<PlanNode> children = new ArrayList<>();
    private final Map<String,String> properties = new HashMap<>();

    private NodeType nodeType;
    private String language;
    private String id;
    private String source;
    private String keyword;
    private String name;
    private String displayNamePattern = "[{id}] {keyword} {name}";

    private Optional<PlanNodeData> data = Optional.empty();

    private Object underlyingModel;
    private boolean isBackgroundStep;

    private Optional<PlanNodeExecution> execution = Optional.empty();



    public DefaultPlanNode(NodeType nodeType) {
        this.nodeType = nodeType;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public String keyword() {
        return keyword;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String language() {
        return language;
    }

    @Override
    public NodeType nodeType() {
        return nodeType;
    }

    @Override
    public List<String> description() {
        return Collections.unmodifiableList(description);
    }

    @Override
    public Stream<PlanNode> children() {
        return children.stream();
    }

    @Override
    public Set<String> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public Map<String, String> properties() {
        return this.properties;
    }


    @Override
    public String displayName() {
        String displayName = displayNamePattern;
        displayName = displayName.replace("{id}", id == null ? "" : id);
        displayName = displayName.replace("{keyword}", keyword == null ? "" : keyword);
        displayName = displayName.replace("{name}", name == null ? "" : name);
        return displayName;
    }

    @Override
    public PlanNode child(int index) {
        return children.get(index);
    }


    @Override
    public int numChildren() {
        return children.size();
    }


    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }


    @Override
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }


    @Override
    public PlanNodeDescriptor obtainDescriptor() {
        return new PlanNodeDescriptor(this);
    }


    @Override
    public Optional<PlanNodeData> data() {
        return data;
    }



    @Override
    public PlanNodeExecution prepareExecution() {
        if (execution.isPresent()) {
            return execution.get();
        }
        execution = Optional.of(new DefaultPlanNodeExecution());
        return execution.get();
    }


    @Override
    public Optional<PlanNodeExecution> execution() {
        return execution;
    }








    public void addChild(PlanNode child) {
        children.add(child);
    }


    public void addChildIfSatisfies(PlanNode child, Predicate<PlanNode> filter) {
        if (filter.test(child)) {
            children.add(child);
        }
    }


    public void replaceChild(PlanNode oldChild, PlanNode newChild) {
        int index = children.indexOf(oldChild);
        if (index == -1) {
            throw new IllegalArgumentException("Node to replace is not a current child node");
        }
        children.set(index,newChild);
    }




    public boolean containsChild(PlanNode child) {
        return children.contains(child);
    }


    public Optional<String> getTagThatSatisfies(Predicate<String> filter) {
        return tags.stream().filter(filter).findAny();
    }


    public void removeChildrenIf(Predicate<PlanNode> predicate) {
        children.removeIf(predicate);
    }


    public void clearChildren() {
        children.clear();
    }



    public Object getUnderlyingModel() {
        return underlyingModel;
    }

    public DefaultPlanNode setId(String id) {
        this.id = id;
        return this;
    }

    public DefaultPlanNode setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public DefaultPlanNode setLanguage(String language) {
        this.language = language;
        return this;
    }

    public DefaultPlanNode setName(String name) {
        this.name = name;
        return this;
    }

    public DefaultPlanNode setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }


    public DefaultPlanNode setDisplayNamePattern(String displayNamePattern) {
        this.displayNamePattern = displayNamePattern;
        return this;
    }


    public DefaultPlanNode setSource(String source) {
        this.source = source;
        return this;
    }

    public DefaultPlanNode addTags(Collection<String> tags) {
        this.tags.addAll(tags);
        return this;
    }


    public DefaultPlanNode addDescription(Collection<String> description) {
        this.description.addAll(description);
        return this;
    }

    public DefaultPlanNode addProperties(Map<String,String> properties) {
        this.properties.putAll(properties);
        return this;
    }


    public DefaultPlanNode addProperty(String key, String value) {
        this.properties.put(key,value);
        return this;
    }


    public DefaultPlanNode setGherkinModel(Object gherkinModel) {
        this.underlyingModel = gherkinModel;
        return this;
    }


    public boolean isBackgroundStep() {
        return isBackgroundStep;
    }


    public DefaultPlanNode setData(PlanNodeData data) {
        this.data = Optional.ofNullable(data);
        return this;
    }



    public DefaultPlanNode setBackgroundStep(boolean isBackgroundStep) {
        this.isBackgroundStep = isBackgroundStep;
        return this;
    }


    public DefaultPlanNode copy() {
        return copy(new DefaultPlanNode(nodeType));
    }



    protected DefaultPlanNode copy(DefaultPlanNode copy) {
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
        copy.isBackgroundStep = this.isBackgroundStep;
        for (PlanNode child : this.children) {
            copy.addChild(((DefaultPlanNode)child).copy());
        }
        return copy;
    }


    @Override
    public String toString() {
        return displayName();
    }







}
