/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.gherkin;

import java.util.List;

public class Step extends CommentedNode {

    private final String keyword;
    private final String text;
    private final Node argument;

    public Step(
        Location location,
        String keyword,
        String text,
        Node argument,
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