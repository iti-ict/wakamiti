package iti.kukumo.core.model;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class TreeNode<S extends TreeNode<S>> {

    private final List<S> children;


    public TreeNode(List<S> children) {
        this.children = Collections.unmodifiableList(children);
    }


    public Stream<S> children() {
        return children.stream();
    }


    public int numChildren() {
        return children.size();
    }


    public boolean hasChildren() {
        return !children.isEmpty();
    }


    public Stream<S> descendants() {
        return Stream.concat(children(), children().flatMap(S::descendants));
    }


    public int numDescendants(Predicate<S> predicate) {
        return (int) descendants().filter(predicate).count();
    }


}
