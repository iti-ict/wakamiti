/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser;


import java.util.List;


/**
 * Provides the Background functionality used by Wakamiti.
 */
public class Background extends ScenarioDefinition {

    /**
     * Creates a background section whose steps provide shared preconditions
     * for scenarios in the enclosing feature.
     *
     * @param location    position of the background keyword
     * @param keyword     localized background keyword
     * @param name        background title
     * @param description free-form description following the title
     * @param steps       shared steps in source order
     * @param comments    comments associated with the background
     */
    public Background(
            Location location,
            String keyword,
            String name,
            String description,
            List<Step> steps,
            List<Comment> comments
    ) {
        super(location, keyword, name, description, steps, comments);
    }

}
