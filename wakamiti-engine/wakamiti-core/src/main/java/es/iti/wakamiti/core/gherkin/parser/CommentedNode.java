/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import es.iti.wakamiti.core.gherkin.parser.Comment;
import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.Node;

import java.util.List;


/**
 * Represents a Commented Node node in the execution model.
 */
public abstract class CommentedNode extends Node {

    protected final List<es.iti.wakamiti.core.gherkin.parser.Comment> comments;

    protected CommentedNode(
            Location location,
            List<es.iti.wakamiti.core.gherkin.parser.Comment> comments
    ) {
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
