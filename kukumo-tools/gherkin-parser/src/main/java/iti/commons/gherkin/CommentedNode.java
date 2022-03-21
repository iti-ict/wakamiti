/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin;

import java.util.List;

public abstract class CommentedNode extends Node {

    protected final List<Comment> comments;

    protected CommentedNode(Location location, List<Comment> comments) {
        super(location);
        this.comments = comments;
    }

    public Location getLocation() {
        return location;
    }

    public List<Comment> getComments() {
        return comments;
    }

}