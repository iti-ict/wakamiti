/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.api.model;


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


    public boolean hasChild(S child) {
        return children.contains(child);
    }


    public boolean hasDescendant(S descendant) {
        return hasChild(descendant) ||
                        children().anyMatch(child -> child.hasDescendant(descendant));
    }

}