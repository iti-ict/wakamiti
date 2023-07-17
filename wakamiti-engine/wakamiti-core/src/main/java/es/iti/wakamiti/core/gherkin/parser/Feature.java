/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.*;
import es.iti.wakamiti.core.gherkin.parser.Comment;
import es.iti.wakamiti.core.gherkin.parser.CommentedNode;

import java.util.Collections;
import java.util.List;

public class Feature extends CommentedNode implements TaggedNode {

    private final List<Tag> tags;
    private final String language;
    private final String keyword;
    private final String name;
    private final String description;
    private final List<ScenarioDefinition> children;

    public Feature(
        List<Tag> tags,
        Location location,
        String language,
        String keyword,
        String name,
        String description,
        List<ScenarioDefinition> children,
        List<Comment> comments
    ) {
        super(location,comments);
        this.tags = Collections.unmodifiableList(tags);
        this.language = language;
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.children = Collections.unmodifiableList(children);
    }

    public List<ScenarioDefinition> getChildren() {
        return children;
    }

    public String getLanguage() {
        return language;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Tag> getTags() {
        return tags;
    }
}