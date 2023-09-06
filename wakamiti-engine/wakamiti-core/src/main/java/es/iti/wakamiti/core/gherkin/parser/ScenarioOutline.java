/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.*;
import es.iti.wakamiti.core.gherkin.parser.Comment;
import es.iti.wakamiti.core.gherkin.parser.Examples;
import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.ScenarioDefinition;

import java.util.Collections;
import java.util.List;

public class ScenarioOutline extends ScenarioDefinition implements TaggedNode {

    private final List<Tag> tags;
    private final List<es.iti.wakamiti.core.gherkin.parser.Examples> examples;

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

    public List<Tag> getTags() {
        return tags;
    }

    public List<Examples> getExamples() {
        return examples;
    }
}