package iti.kukumo.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public abstract class TreeNodeBuilder<S extends TreeNodeBuilder<S>> {

    private final List<S> children = new ArrayList<>();
    private Optional<S> parent = Optional.empty();

    public TreeNodeBuilder() {
        super();
    }

    public TreeNodeBuilder(Collection<S> children) {
        addChildren(children);
    }


    public Stream<S> children() {
        return children.stream();
    }


    public S child(int index) {
        return children.get(index);
    }


    public int numChildren() {
        return children.size();
    }


    public boolean hasChildren() {
        return !children.isEmpty();
    }


    public Optional<S> parent() {
        return parent;
    }


    public S root() {
        return parent.map(TreeNodeBuilder::root).orElse((S)this);
    }


    public Stream<S> ancestors() {
        return parent.isPresent() ?
            Stream.concat(Stream.of(parent.get()),parent.get().ancestors()) :
            Stream.empty();
    }


    public Stream<S> siblings() {
        return parent.isPresent() ?
            parent.get().children().filter(x -> x != this) :
            Stream.empty();
    }


    public Stream<S> descendants() {
        return Stream.concat(children(), children().flatMap(S::descendants));
    }


    public S addChild(S child) {
        children.add(child);
        child.parent().ifPresent(previousParent -> previousParent.removeChild(child));
        ((TreeNodeBuilder<S>)child).parent = Optional.of((S)this);
        return (S) this;
    }

    public S addChild(S child, int index) {
        children.add(index,child);
        child.parent().ifPresent(previousParent -> previousParent.removeChild(child));
        ((TreeNodeBuilder<S>)child).parent = Optional.of((S)this);
        return (S) this;
    }


    public S addFirstChild(S child) {
        return addChild(child,0);
    }

    public S addChildIf(S child, Predicate<S> predicate) {
        if (predicate.test(child)) {
            addChild(child);
        }
        return (S) this;
    }

    public S addChildren(Collection<S> children) {
        this.children.addAll(children);
        return (S) this;
    }


    public S replaceChild(S oldChild, S newChild) {
        int index = children.indexOf(oldChild);
        if (index == -1) {
            throw new IllegalArgumentException("Node to replace is not a current child node");
        }
        children.set(index,newChild);
        ((TreeNodeBuilder<S>)oldChild).parent = Optional.empty();
        newChild.parent().ifPresent(previousParent -> previousParent.removeChild(newChild));
        ((TreeNodeBuilder<S>)newChild).parent = Optional.of((S)this);
        return (S) this;
    }


    public boolean containsChild(S child) {
        return children.contains(child);
    }


    public S removeChild(S child) {
        children.remove(child);
        ((TreeNodeBuilder<S>)child).parent = Optional.empty();
        return (S) this;
    }


    public S removeChildrenIf(Predicate<S> predicate) {
        for (S child : children) {
            if (predicate.test(child)) {
                removeChild(child);
            }
        }
        return (S) this;
    }



    public S clearChildren() {
        children.clear();
        return (S) this;
    }


    public abstract S copy();


    protected S copy(S copy) {
        for (S child : this.children) {
            copy.addChild(child.copy());
        }
        return copy;
    }


}
