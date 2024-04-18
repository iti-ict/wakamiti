/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * Abstract class representing a builder for a tree node with
 * a generic type.
 *
 * @param <S> The type of the tree node builder
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
@SuppressWarnings("unchecked")
public abstract class TreeNodeBuilder<S extends TreeNodeBuilder<S>> {

    private final List<S> children = new ArrayList<>();
    private S parent;

    protected TreeNodeBuilder() {
        super();
    }

    protected TreeNodeBuilder(Collection<S> children) {
        addChildren(children);
    }

    /**
     * Gets a stream of the children of this tree node builder.
     *
     * @return The stream of children
     */
    public Stream<S> children() {
        return children.stream();
    }

    /**
     * Gets a stream of the children of this tree node builder
     * filtered by the given predicate.
     *
     * @param filter The predicate to filter children
     * @return The stream of filtered children
     */
    public Stream<S> children(Predicate<S> filter) {
        return children.stream().filter(filter);
    }

    /**
     * Gets the child at the specified index.
     *
     * @param index The index of the child
     * @return The child at the specified index
     */
    public S child(int index) {
        return children.get(index);
    }

    /**
     * Gets the number of children of this tree node builder.
     *
     * @return The number of children
     */
    public int numChildren() {
        return children.size();
    }

    /**
     * Checks if this tree node builder has children.
     *
     * @return {@code true} if this tree node builder has children,
     * {@code false} otherwise
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Gets the optional parent of this tree node builder.
     *
     * @return The optional parent
     */
    public Optional<S> parent() {
        return Optional.ofNullable(parent);
    }

    /**
     * Gets the position of the specified child in the list of
     * children.
     *
     * @param child The child to find
     * @return The position of the child, or -1 if not found
     */
    public int positionOfChild(S child) {
        return children.indexOf(child);
    }

    /**
     * Gets the position of this tree node builder in its parent's
     * list of children.
     *
     * @return The position in the parent, or {@code -1} if there is
     * no parent
     */
    public int positionInParent() {
        return parent().map(p -> p.positionOfChild((S) this)).orElse(-1);
    }

    /**
     * Gets the root of the tree containing this tree node builder.
     *
     * @return The root of the tree
     */
    public S root() {
        return Optional.ofNullable(parent).map(TreeNodeBuilder::root).orElse((S) this);
    }

    /**
     * Gets a stream of ancestors of this tree node builder.
     *
     * @return The stream of ancestors
     */
    public Stream<S> ancestors() {
        return Optional.ofNullable(parent).map(s -> Stream.concat(Stream.of(s), s.ancestors())).orElseGet(Stream::empty);
    }

    /**
     * Gets a stream of siblings of this tree node builder.
     *
     * @return The stream of siblings
     */
    public Stream<S> siblings() {
        return Optional.ofNullable(parent).stream().flatMap(S::children).filter(x -> x != this);
    }

    /**
     * Gets a stream of descendants of this tree node builder.
     *
     * @return The stream of descendants
     */
    public Stream<S> descendants() {
        return Stream.concat(children(), children().flatMap(S::descendants));
    }

    /**
     * Adds the specified child to the list of children.
     *
     * @param child The child to add
     * @return This tree node builder
     */
    public S addChild(S child) {
        children.add(child);
        child.parent().ifPresent(previousParent -> previousParent.removeChild(child));
        ((TreeNodeBuilder<S>) child).parent = (S) this;
        return (S) this;
    }

    /**
     * Adds the specified child to the list of children at
     * the specified index.
     *
     * @param child The child to add
     * @param index The index at which to add the child
     * @return This tree node builder
     */
    public S addChild(S child, int index) {
        children.add(index, child);
        child.parent().ifPresent(previousParent -> previousParent.removeChild(child));
        ((TreeNodeBuilder<S>) child).parent = (S) this;
        return (S) this;
    }

    /**
     * Adds the specified child to the list of children as
     * the first child.
     *
     * @param child The child to add
     * @return This tree node builder
     */
    public S addFirstChild(S child) {
        return addChild(child, 0);
    }

    /**
     * Adds the specified child to the list of children if
     * the provided predicate is true.
     *
     * @param child     The child to add
     * @param predicate The predicate to check before adding
     *                  the child
     * @return This tree node builder
     */
    public S addChildIf(S child, Predicate<S> predicate) {
        if (predicate.test(child)) {
            addChild(child);
        }
        return (S) this;
    }

    /**
     * Adds a collection of children to the list of children.
     *
     * @param children The children to add
     * @return This tree node builder
     */
    public S addChildren(Collection<S> children) {
        for (S child : children) {
            addChild(child);
        }
        return (S) this;
    }

    /**
     * Replaces the old child with the new child in the list
     * of children.
     *
     * @param oldChild The child to be replaced
     * @param newChild The new child to replace the old child
     * @return This tree node builder
     * @throws IllegalArgumentException If the old child is not
     *                                  a current child node
     */
    public S replaceChild(S oldChild, S newChild) {
        int index = children.indexOf(oldChild);
        if (index == -1) {
            throw new IllegalArgumentException("Node to replace is not a current child node");
        }
        children.set(index, newChild);
        ((TreeNodeBuilder<S>) oldChild).parent = null;
        newChild.parent().ifPresent(previousParent -> previousParent.removeChild(newChild));
        ((TreeNodeBuilder<S>) newChild).parent = (S) this;
        return (S) this;
    }

    /**
     * Checks if the specified child is in the list of children.
     *
     * @param child The child to check
     * @return {@code true} if the child is present,
     * {@code false} otherwise
     */
    public boolean containsChild(S child) {
        return children.contains(child);
    }

    /**
     * Removes the specified child from the list of children.
     *
     * @param child The child to remove
     * @return This tree node builder
     */
    public S removeChild(S child) {
        children.remove(child);
        ((TreeNodeBuilder<S>) child).parent = null;
        return (S) this;
    }

    /**
     * Removes children from the list of children based on
     * the provided predicate.
     *
     * @param predicate The predicate to filter children for
     *                  removal
     * @return This tree node builder
     */
    public S removeChildrenIf(Predicate<S> predicate) {
        for (S child : children) {
            if (predicate.test(child)) {
                removeChild(child);
            }
        }
        return (S) this;
    }

    /**
     * Clears the list of children.
     *
     * @return This tree node builder
     */
    public S clearChildren() {
        children.clear();
        return (S) this;
    }

    /**
     * Creates a copy of this tree node builder.
     *
     * @return The copied tree node builder
     */
    public abstract S copy();

    /**
     * Copies the specified tree node builder and adds its
     * children to this tree node builder.
     *
     * @param copy The tree node builder to copy
     * @return This tree node builder
     */
    protected S copy(S copy) {
        for (S child : this.children) {
            copy.addChild(child.copy());
        }
        return copy;
    }

}