/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.Comment;
import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.ScenarioDefinition;
import es.iti.wakamiti.core.gherkin.parser.Step;

import java.util.List;

public class Background extends ScenarioDefinition {

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