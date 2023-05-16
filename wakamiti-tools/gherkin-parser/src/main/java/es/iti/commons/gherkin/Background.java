/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.commons.gherkin;

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