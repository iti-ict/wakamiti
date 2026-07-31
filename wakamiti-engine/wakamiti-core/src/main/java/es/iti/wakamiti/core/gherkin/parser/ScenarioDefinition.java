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
 * Provides the Scenario Definition functionality used by Wakamiti.
 */
public abstract class ScenarioDefinition extends CommentedNode {

    private final String keyword;
    private final String name;
    private final String description;
    private final List<Step> steps;

    /**
     * Initializes the common structure shared by backgrounds, scenarios, and
     * scenario outlines.
     *
     * @param location    position of the definition keyword
     * @param keyword     localized source keyword
     * @param name        definition title
     * @param description free-form descriptive text
     * @param steps       steps in source order
     * @param comments    comments associated with the definition
     */
    public ScenarioDefinition(
            Location location,
            String keyword,
            String name,
            String description,
            List<Step> steps,
            List<Comment> comments
    ) {
        super(location, comments);
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.steps = Collections.unmodifiableList(steps);
    }

    /**
     * Returns the definition title.
     *
     * @return the source-level name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the localized keyword introducing the definition.
     *
     * @return the source keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns free-form descriptive text preceding the first step.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns executable steps declared by this definition.
     *
     * @return an unmodifiable list in source order
     */
    public List<Step> getSteps() {
        return steps;
    }

}
