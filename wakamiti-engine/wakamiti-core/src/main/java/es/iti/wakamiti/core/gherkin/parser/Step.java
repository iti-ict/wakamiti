/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.Comment;
import es.iti.wakamiti.core.gherkin.parser.CommentedNode;
import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.Node;

import java.util.List;

public class Step extends CommentedNode {

    private final String keyword;
    private final String text;
    private final es.iti.wakamiti.core.gherkin.parser.Node argument;

    public Step(
        Location location,
        String keyword,
        String text,
        es.iti.wakamiti.core.gherkin.parser.Node argument,
        List<Comment> comments
    ) {
        super(location,comments);
        this.keyword = keyword;
        this.text = text;
        this.argument = argument;
    }

    public String getText() {
        return text;
    }

    public String getKeyword() {
        return keyword;
    }

    public Node getArgument() {
        return argument;
    }

}