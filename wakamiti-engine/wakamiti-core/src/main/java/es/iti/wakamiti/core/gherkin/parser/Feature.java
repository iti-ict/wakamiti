/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.util.Collections;
import java.util.List;


/**
 * Provides the Feature functionality used by Wakamiti.
 */
public class Feature extends CommentedNode implements TaggedNode {

    private final List<Tag> tags;
    private final String language;
    private final String keyword;
    private final String name;
    private final String description;
    private final List<ScenarioDefinition> children;

    /**
     * Creates the root feature declared by a Gherkin document.
     *
     * @param tags        feature-level tags inherited by its scenarios
     * @param location    position of the feature keyword
     * @param language    Gherkin dialect code used by the document
     * @param keyword     localized feature keyword
     * @param name        feature title
     * @param description free-form feature description
     * @param children    backgrounds and scenario definitions in source order
     * @param comments    comments associated with the feature
     */
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
        super(location, comments);
        this.tags = Collections.unmodifiableList(tags);
        this.language = language;
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.children = Collections.unmodifiableList(children);
    }

    /**
     * Returns backgrounds, scenarios, and outlines declared by the feature.
     *
     * @return an unmodifiable list in source order
     */
    public List<ScenarioDefinition> getChildren() {
        return children;
    }

    /**
     * Returns the dialect code used to recognize localized keywords.
     *
     * @return the Gherkin language code
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the localized feature keyword.
     *
     * @return the keyword as written in the source
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns the feature title.
     *
     * @return the source-level feature name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the free-form text between the feature title and first child.
     *
     * @return the feature description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns tags declared directly above the feature.
     *
     * @return an unmodifiable list in source order
     */
    public List<Tag> getTags() {
        return tags;
    }

}
