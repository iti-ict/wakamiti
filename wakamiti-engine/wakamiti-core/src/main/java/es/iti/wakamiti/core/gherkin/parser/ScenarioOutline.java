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
 * Provides the Scenario Outline functionality used by Wakamiti.
 */
public class ScenarioOutline extends ScenarioDefinition implements TaggedNode {

    private final List<Tag> tags;
    private final List<es.iti.wakamiti.core.gherkin.parser.Examples> examples;

    /**
     * Creates a parameterized scenario template.
     *
     * @param tags        outline-level tags
     * @param location    position of the outline keyword
     * @param keyword     localized outline keyword
     * @param name        outline title
     * @param description free-form outline description
     * @param steps       template steps containing placeholders
     * @param examples    example blocks supplying placeholder values
     * @param comments    comments associated with the outline
     */
    public ScenarioOutline(
            List<Tag> tags,
            Location location,
            String keyword,
            String name,
            String description,
            List<Step> steps,
            List<es.iti.wakamiti.core.gherkin.parser.Examples> examples,
            List<Comment> comments
    ) {
        super(location, keyword, name, description, steps, comments);
        this.tags = Collections.unmodifiableList(tags);
        this.examples = Collections.unmodifiableList(examples);
    }

    /**
     * Returns tags declared directly on the outline.
     *
     * @return an unmodifiable list in source order
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Returns all example blocks used to instantiate this template.
     *
     * @return an unmodifiable list in source order
     */
    public List<Examples> getExamples() {
        return examples;
    }

}
