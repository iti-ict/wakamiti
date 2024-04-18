/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.model;


import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * Abstract class representing a tree node with a generic type.
 *
 * @param <S> The type of the tree node
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public abstract class TreeNode<S extends TreeNode<S>> {

    private final List<S> children;

    protected TreeNode(List<S> children) {
        this.children = Collections.unmodifiableList(children);
    }

    /**
     * Gets a stream of the children of this tree node.
     *
     * @return The stream of children
     */
    public Stream<S> children() {
        return children.stream();
    }

    /**
     * Gets the number of children of this tree node.
     *
     * @return The number of children
     */
    public int numChildren() {
        return children.size();
    }

    /**
     * Checks if this tree node has children.
     *
     * @return {@code true} if this tree node has children,
     * {@code false} otherwise
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Gets a stream of descendants of this tree node.
     *
     * @return The stream of descendants
     */
    public Stream<S> descendants() {
        return Stream.concat(children(), children().flatMap(S::descendants));
    }

    /**
     * Gets the number of descendants satisfying the given predicate.
     *
     * @param predicate The predicate to filter descendants
     * @return The number of descendants satisfying the predicate
     */
    public int numDescendants(Predicate<S> predicate) {
        return (int) descendants().filter(predicate).count();
    }

    /**
     * Checks if this tree node has the given child.
     *
     * @param child The child to check
     * @return {@code true} if this tree node has the given child,
     * {@code false} otherwise
     */
    public boolean hasChild(S child) {
        return children.contains(child);
    }

    /**
     * Checks if this tree node has the given descendant.
     *
     * @param descendant The descendant to check
     * @return {@code true} if this tree node has the given descendant,
     * {@code false} otherwise
     */
    public boolean hasDescendant(S descendant) {
        return hasChild(descendant) ||
                children().anyMatch(child -> child.hasDescendant(descendant));
    }

}