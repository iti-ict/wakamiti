package iti.kukumo.core.plan;

import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeTypes;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;


@SuppressWarnings("unchecked")
public class DefaultPlanNode<S extends DefaultPlanNode<S>> implements PlanNode {

    private final String nodeType;

    private String language;
    private String id;
    private String source;
    private String keyword;
    private String name;
    private boolean testCase;
    private final List<String> description = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private final List<PlanNode> children = new ArrayList<>();
    private final Map<String,String> properties = new HashMap<>();

    private Object gherkinModel;


    public DefaultPlanNode(String nodeType) {
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
    public String nodeType() {
        return nodeType;
    }

    @Override
    public List<String> description() {
        return description;
    }

    @Override
    public Stream<PlanNode> children() {
        return children.stream();
    }

    @Override
    public boolean isTestCase() {
        return testCase;
    }

    @Override
    public boolean isStep() {
        return false;
    }

    @Override
    public List<String> tags() {
        return tags;
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
        StringBuilder displayName = new StringBuilder();
        if (id != null) {
            displayName.append('[').append(id).append("] ");
        }
        if (keyword != null) {
            displayName.append(keyword);
            displayName.append(PlanNodeTypes.STEP.equals(nodeType) ? " " : ": ");
        }
        if (name != null) {
            displayName.append(name);
        }
        return displayName.toString();
    }

    
    @Override
    public PlanNode child(int index) {
        return children.get(index);
    }


    @Override
    public void addChild(PlanNode child) {
        children.add(child);
    }



    @Override
    public void addChildIfSatisfies(PlanNode child, Predicate<PlanNode> filter) {
        if (filter.test(child)) {
            children.add(child);
        }
    }


    @Override
    public void replaceChild(PlanNode oldChild, PlanNode newChild) {
        int index = children.indexOf(oldChild);
        if (index == -1) {
            throw new IllegalArgumentException("Node to replace is not a current child node");
        }
        children.set(index,newChild);
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
    public boolean containsChild(PlanNode child) {
        return children.contains(child);
    }

    @Override
    public Optional<String> getTagThatSatisfies(Predicate<String> filter) {
        return tags.stream().filter(filter).findAny();
    }
    
    
    @Override
    public void removeChildrenIf(Predicate<PlanNode> predicate) {
        children.removeIf(predicate);
    }
    
    @Override
    public void clearChildren() {
        children.clear();
    }
    
    

    public Object getGherkinModel() {
        return gherkinModel;
    }

    public S setId(String id) {
        this.id = id;
        return (S) this;
    }

    public S setKeyword(String keyword) {
        this.keyword = keyword;
        return (S) this;
    }

    public S setLanguage(String language) {
        this.language = language;
        return (S) this;
    }

    public S setName(String name) {
        this.name = name;
        return (S) this;
    }

    public S setTestCase(boolean isTestCase) {
        this.testCase = isTestCase;
        return (S) this;
    }

    public S setSource(String source) {
        this.source = source;
        return (S) this;
    }

    public S addTags(List<String> tags) {
        this.tags.addAll(tags);
        return (S) this;
    }


    public S addDescription(List<String> description) {
        this.description.addAll(description);
        return (S) this;
    }

    public S addProperties(Map<String,String> properties) {
        this.properties.putAll(properties);
        return (S) this;
    }


    public S setGherkinModel(Object gherkinModel) {
        this.gherkinModel = gherkinModel;
        return (S) this;
    }


    public S copy() {
        return copy(new DefaultPlanNode<>(nodeType));
    }


    

    protected S copy(DefaultPlanNode<S> copy) {
        copy.setLanguage(this.language);
        copy.setId(this.id);
        copy.setKeyword(this.keyword);
        copy.setName(this.name);
        copy.addTags(this.tags);
        copy.setSource(this.source());
        copy.addDescription(this.description);
        for (PlanNode child : this.children) {
            copy.addChild(((DefaultPlanNode<?>)child).copy());
        }
        copy.properties.putAll(this.properties);
        return (S) copy;
    }


    @Override
    public String toString() {
        return displayName();
    }







}
