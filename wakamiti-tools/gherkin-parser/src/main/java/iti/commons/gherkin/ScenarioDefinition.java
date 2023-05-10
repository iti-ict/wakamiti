/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin;

import java.util.Collections;
import java.util.List;

public abstract class ScenarioDefinition extends CommentedNode {

    private final String keyword;
    private final String name;
    private final String description;
    private final List<Step> steps;

    public ScenarioDefinition(
            Location location,
            String keyword,
            String name,
            String description,
            List<Step> steps,
            List<Comment> comments
    ) {
        super(location,comments);
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.steps = Collections.unmodifiableList(steps);
    }

    public String getName() {
        return name;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDescription() {
        return description;
    }

    public List<Step> getSteps() {
        return steps;
    }
}