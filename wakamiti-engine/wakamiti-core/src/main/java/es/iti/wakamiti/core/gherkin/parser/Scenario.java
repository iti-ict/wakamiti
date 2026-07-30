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
 * Provides the Scenario functionality used by Wakamiti.
 */
public class Scenario extends ScenarioDefinition implements TaggedNode {

    private final List<Tag> tags;

    /**
     * Creates a concrete scenario whose steps execute once.
     *
     * @param tags        scenario-level tags
     * @param location    position of the scenario keyword
     * @param keyword     localized scenario keyword
     * @param name        scenario title
     * @param description free-form scenario description
     * @param steps       executable steps in source order
     * @param comments    comments associated with the scenario
     */
    public Scenario(
            List<Tag> tags,
            Location location,
            String keyword,
            String name,
            String description,
            List<Step> steps,
            List<Comment> comments
    ) {
        super(location, keyword, name, description, steps, comments);
        this.tags = Collections.unmodifiableList(tags);
    }

    /**
     * Returns tags declared directly on the scenario.
     *
     * @return an unmodifiable list in source order
     */
    public List<Tag> getTags() {
        return tags;
    }

}
