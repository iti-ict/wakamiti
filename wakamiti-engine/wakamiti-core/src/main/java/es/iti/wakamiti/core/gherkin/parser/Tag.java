/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser;

import es.iti.wakamiti.core.gherkin.parser.Location;
import es.iti.wakamiti.core.gherkin.parser.Node;

public class Tag extends Node {
    private final String name;

    public Tag(Location location, String name) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}